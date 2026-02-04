package you.v50to.eatwhat.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;

import org.springframework.web.client.ResourceAccessException;
import work.foofish.smsverify.SmsTemplate;
import work.foofish.smsverify.config.AliyunDefaultRequest;
import work.foofish.smsverify.core.SmsRequest;
import work.foofish.smsverify.core.SmsResponse;
import you.v50to.eatwhat.data.enums.BizCode;
import you.v50to.eatwhat.data.enums.Scene;
import you.v50to.eatwhat.exception.BizException;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SmsService {

    private final StringRedisTemplate redis;

    private final SmsTemplate smsTemplate;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Duration CODE_TTL = Duration.ofMinutes(5);         // 验证码有效期
    private static final Duration SEND_LIMIT_TTL = Duration.ofSeconds(60);  // 同手机号发送间隔
    private static final int MAX_VERIFY_FAILS = 5;                          // 最大错误次数
    private static final int CODE_LEN = 6;

    private static final boolean ENABLE_IP_LIMIT = true;
    private static final Duration IP_LIMIT_TTL = Duration.ofSeconds(60);
    private static final int IP_LIMIT_MAX = 20; // 60秒内同IP最多发送20次（按需调）

    private static final int GLOBAL_MAX_PER_MIN = 300; // 每分钟最高发送量（可选，全局限流）
    private static final long GLOBAL_WINDOW_SECONDS = 60;

    private static final boolean STORE_HASHED_CODE = false;
    private static final String HASH_SALT = "change-me-to-a-long-random-secret";

    public SmsService(StringRedisTemplate redis, SmsTemplate smsTemplate) {
        this.redis = redis;
        this.smsTemplate = smsTemplate;
    }

    /**
     * 发送验证码
     *
     * @param scene    业务场景
     * @param mobile   手机号
     * @param clientIp 客户端IP
     */
    public void sendCode(Scene scene, String mobile, String clientIp) {
        validate(scene, mobile);
        applyGlobalLimit(scene);

        if (ENABLE_IP_LIMIT && clientIp != null && !clientIp.isBlank()) {
            applyIpRateLimit(scene, clientIp);
        }

        String limitKey = keyLimit(scene, mobile);

        // 同手机号发送频率限制：60秒一次
        Boolean ok = redis.opsForValue().setIfAbsent(limitKey, "1", SEND_LIMIT_TTL);
        if (ok == null || !ok) {
            throw new BizException(BizCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试");
        }

        String code = gen6Digits();

        try {
            SmsRequest request = switch (scene) {
                case auth -> AliyunDefaultRequest.auth(mobile, code, String.valueOf(CODE_TTL.toMinutes()));
                case change -> AliyunDefaultRequest.change(mobile, code, String.valueOf(CODE_TTL.toMinutes()));
                case forget -> AliyunDefaultRequest.forget(mobile, code, String.valueOf(CODE_TTL.toMinutes()));
                case bind -> AliyunDefaultRequest.bind(mobile, code, String.valueOf(CODE_TTL.toMinutes()));
                case verifybind ->
                        AliyunDefaultRequest.verifyBind(mobile, code, String.valueOf(CODE_TTL.toMinutes()));
                };
            SmsResponse resp = null;

            try {
                resp = smsTemplate.send(request);
            } catch (ResourceAccessException e) {
                if (e.getCause() instanceof SocketTimeoutException) {
                    throw new BizException(
                            BizCode.THIRD_PARTY_TIMEOUT,
                            "短信服务请求超时"
                    );
                }
                log.error("短信服务访问异常", e);
                throw new BizException(
                        BizCode.THIRD_PARTY_UNAVAILABLE,
                        "短信服务不可用"
                );
            }

            if (resp == null || !resp.isSuccess()) {
                log.warn(
                        "短信发送失败，resp={}",
                        resp == null ? "null" : resp
                );
                throw new BizException(BizCode.THIRD_PARTY_BAD_RESPONSE, "短信发送失败：" + (resp == null ? "null response" : resp.getErrorMessage()));
            }
        } catch (RuntimeException e) {
            redis.delete(limitKey);
            throw e;
        } catch (Exception e) {
            redis.delete(limitKey);
            log.error("短信发送异常：{}", e.getMessage(), e);
            throw new BizException(BizCode.UNKNOWN_ERROR, "短信发送异常：" + e.getMessage());
        }

        String codeKey = keyCode(scene, mobile);
        String cntKey = keyFailCount(scene, mobile);

        String toStore = STORE_HASHED_CODE ? hashCode(scene, mobile, code) : code;
        redis.opsForValue().set(codeKey, toStore, CODE_TTL);
        redis.opsForValue().set(cntKey, "0", CODE_TTL);
    }

    public boolean verifyCode(Scene scene, String mobile, String inputCode) {
        validate(scene, mobile);

        if (inputCode == null || inputCode.isBlank()) {
            throw new BizException(BizCode.PARAM_MISSING, "验证码不能为空");
        }

        String codeKey = keyCode(scene, mobile);
        String cntKey = keyFailCount(scene, mobile);

        String stored = redis.opsForValue().get(codeKey);
        if (stored == null) {
            throw new BizException(BizCode.PARAM_INVALID, "验证码已过期或不存在");
        }

        int fails = parseInt(redis.opsForValue().get(cntKey)).orElse(0);
        if (fails >= MAX_VERIFY_FAILS) {
            throw new BizException(BizCode.TOO_MANY_REQUESTS, "验证码错误次数过多，请稍后再试");
        }

        boolean match;
        if (STORE_HASHED_CODE) {
            match = Objects.equals(stored, hashCode(scene, mobile, inputCode));
        } else {
            match = Objects.equals(stored, inputCode);
        }

        if (!match) {
            long newFails = redis.opsForValue().increment(cntKey);

            redis.expire(cntKey, CODE_TTL.toSeconds(), TimeUnit.SECONDS);

            if (newFails >= MAX_VERIFY_FAILS) {
                throw new BizException(BizCode.TOO_MANY_REQUESTS, "验证码错误次数过多，请稍后再试");
            }
            throw new BizException(BizCode.VERIFY_CODE_ERROR);
        }

        redis.delete(codeKey);
        redis.delete(cntKey);

        return true;
    }

    private void applyIpRateLimit(Scene scene, String clientIp) {
        String ipKey = keyIpLimit(scene, clientIp);
        Long cnt = redis.opsForValue().increment(ipKey);
        if (cnt != null && cnt == 1L) {
            redis.expire(ipKey, IP_LIMIT_TTL.toSeconds(), TimeUnit.SECONDS);
        }
        if (cnt != null && cnt > IP_LIMIT_MAX) {
            throw new BizException(BizCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试");
        }
    }

    private static String gen6Digits() {
        int n = RANDOM.nextInt(1_000_000);
        return String.format("%0" + CODE_LEN + "d", n);
    }

    private static void validate(Scene scene, String mobile) {
        if (scene == null) {
            throw new BizException(BizCode.PARAM_MISSING, "scene不能为空");
        }
        if (mobile == null || mobile.isBlank()) {
            throw new BizException(BizCode.PARAM_MISSING, "手机号不能为空");
        }
        if (mobile.length() < 6 || mobile.length() > 20) {
            throw new BizException(BizCode.PARAM_INVALID, "手机号格式不正确");
        }
    }

    private static Optional<Integer> parseInt(String s) {
        try {
            if (s == null) return Optional.empty();
            return Optional.of(Integer.parseInt(s));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Redis keys
    private static String keyCode(Scene scene, String mobile) {
        return "sms:code:" + scene + ":" + mobile;
    }

    private static String keyFailCount(Scene scene, String mobile) {
        return "sms:cnt:" + scene + ":" + mobile;
    }

    private static String keyLimit(Scene scene, String mobile) {
        return "sms:limit:" + scene + ":" + mobile;
    }

    private static String keyIpLimit(Scene scene, String ip) {
        return "sms:ip:" + scene + ":" + ip;
    }

    private static String hashCode(Scene scene, String mobile, String code) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String raw = scene + "|" + mobile + "|" + code + "|" + HASH_SALT;
            byte[] dig = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(dig);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException(BizCode.SYSTEM_ERROR);
        }
    }
    private void applyGlobalLimit(Scene scene) {
        String minute = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String key = "sms:global:" + scene + ":m:" + minute;

        Long cnt = redis.opsForValue().increment(key);
        if (cnt != null && cnt == 1L) {
            redis.expire(key, GLOBAL_WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        if (cnt != null && cnt > GLOBAL_MAX_PER_MIN) {
            log.warn(
                    "达到全局速率限制，scene={}, cnt={}, max={}",
                    scene, cnt, GLOBAL_MAX_PER_MIN
            );

            throw new BizException(BizCode.TOO_MANY_REQUESTS, "短信发送过于频繁");
        }
    }
}
