package you.v50to.eatwhat.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import you.v50to.eatwhat.data.dto.LoginDTO;
import you.v50to.eatwhat.data.dto.RegisterDTO;
import you.v50to.eatwhat.data.enums.BizCode;
import you.v50to.eatwhat.data.po.Contact;
import you.v50to.eatwhat.data.po.User;
import you.v50to.eatwhat.data.po.Verification;
import you.v50to.eatwhat.data.vo.Result;
import you.v50to.eatwhat.mapper.ContactMapper;
import you.v50to.eatwhat.mapper.UserMapper;
import you.v50to.eatwhat.mapper.VerificationMapper;
import you.v50to.eatwhat.utils.JwtUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class AuthService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private VerificationMapper verificationMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private ContactMapper contactMapper;

    private boolean usernameExists(String username) {
        return userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getUserName, username));
    }

    public Result<Void> checkUsername(String username) {
        if (usernameExists(username)) {
            return Result.fail(BizCode.USERNAME_EXISTS);
        }
        return Result.ok();
    }

    public Result<SaTokenInfo> register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();
        String password = registerDTO.getPassword();
        String device = registerDTO.getDevice().name();
        if (usernameExists(username)) {
            return Result.fail(BizCode.USERNAME_EXISTS);
        }
        User user = new User();
        user.setUserName(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        userMapper.insert(user);

        StpUtil.login(user.getId(), device);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        return Result.ok(tokenInfo);
    }

    private boolean looksLikeEmail(String s) {
        return s.contains("@");
    }

    private boolean looksLikePhone(String s) {
        return s.matches("^\\d{11}$");
    }

    public Result<SaTokenInfo> login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        String device = loginDTO.getDevice().name();

        if (!usernameExists(username)) {
            return Result.fail(BizCode.USER_NOT_FOUND);
        }

        User user;

        if (looksLikeEmail(username)) {
            Contact c = contactMapper.selectOne(
                    new LambdaQueryWrapper<Contact>()
                            .eq(Contact::getEmail, username)
            );
            user = userMapper.selectById(c.getAccountId());
        } else if (looksLikePhone(username)) {
            Contact c = contactMapper.selectOne(
                    new LambdaQueryWrapper<Contact>()
                            .eq(Contact::getPhone, username)
            );
            user = userMapper.selectById(c.getAccountId());
        } else {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getUserName, username)
            );
        }
        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            StpUtil.login(user.getId(), device);
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            return Result.ok(tokenInfo);
        } else {
            return Result.fail(BizCode.PASSWORD_ERROR);
        }
    }

    public Result<Void> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    public Result<Void> callBack(String token) {
        String key = ""; // TODO: 添加key
        Long userId = (Long) StpUtil.getLoginId();
        Optional<JwtUtil.User> userOpt = JwtUtil.getClaim(token, key);

        if (userOpt.isEmpty()) {
            return Result.fail(BizCode.THIRD_PARTY_BAD_RESPONSE); // TODO: 错误处理
        } else {
            JwtUtil.User user = userOpt.get();
            String casID = URLEncoder.encode(user.casId(), StandardCharsets.UTF_8);
            String name = URLEncoder.encode(user.name(), StandardCharsets.UTF_8);
            Verification v = new Verification();
            v.setAccountId(userId);
            v.setMethod("sso");
            v.setVerified(true);
            v.setRealName(name);
            v.setStudentId(casID);
            verificationMapper.insert(v);
            //response.sendRedirect(CasPageLogin.DEFAULT_FORWARD + "?casId=" + casID + "&name=" + name);
            return null;
        }
    }
}
