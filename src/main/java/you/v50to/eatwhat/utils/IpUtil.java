package you.v50to.eatwhat.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;

public final class IpUtil {

    private IpUtil() {}

    /**
     * 获取客户端真实IP（优先从代理头获取）
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = getHeader(request, "X-Forwarded-For");
        if (ip != null) {
            // 格式：client, proxy1, proxy2
            return extractFirstIp(ip);
        }

        ip = getHeader(request, "X-Real-IP");
        if (ip != null) {
            return normalize(ip);
        }

        ip = getHeader(request, "CF-Connecting-IP"); // Cloudflare
        if (ip != null) {
            return normalize(ip);
        }

        ip = getHeader(request, "True-Client-IP");   // Akamai
        if (ip != null) {
            return normalize(ip);
        }

        return normalize(request.getRemoteAddr());
    }

    /**
     * 获取完整代理链
     */
    public static String getProxyChain(HttpServletRequest request) {
        String xff = getHeader(request, "X-Forwarded-For");
        if (xff != null) {
            return xff;
        }
        return normalize(request.getRemoteAddr());
    }

    private static String getHeader(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        if (v == null || v.isBlank() || "unknown".equalsIgnoreCase(v)) {
            return null;
        }
        return v.trim();
    }

    private static String extractFirstIp(String xff) {
        // 取第一个非 unknown 的 IP
        String[] parts = xff.split(",");
        for (String p : parts) {
            String ip = normalize(p);
            if (ip != null) {
                return ip;
            }
        }
        return null;
    }

    private static String normalize(String ip) {
        if (ip == null) return null;

        ip = ip.trim();
        if (ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return null;
        }

        // 处理 IPv6 本地回环
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }

        // 如果是 IPv6 + 端口形式 [::1]:12345
        if (ip.startsWith("[") && ip.contains("]")) {
            ip = ip.substring(1, ip.indexOf(']'));
        }

        // 如果是 IPv4:port
        int idx = ip.indexOf(':');
        if (idx > 0 && ip.indexOf('.') > -1) {
            ip = ip.substring(0, idx);
        }

        // 基本校验
        if (!isValidIp(ip)) {
            return null;
        }

        return ip;
    }

    private static boolean isValidIp(String ip) {
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
