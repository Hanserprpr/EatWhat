package you.v50to.eatwhat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.SaTokenInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.*;
import you.v50to.eatwhat.data.dto.LoginDTO;
import you.v50to.eatwhat.data.dto.RegisterDTO;
import you.v50to.eatwhat.data.enums.BizCode;
import you.v50to.eatwhat.data.vo.Result;
import you.v50to.eatwhat.service.AuthService;
import jakarta.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     *
     * @param username
     * @return 获取用户名是否可用（前端自己校验格式）
     */
    @PostMapping("/checkUsername")
    public Result<Void> checkUsername(@RequestParam String username) {
        return authService.checkUsername(username);
    }

    /**
     *
     * @param registerDTO 注册请求体
     * @return token
     * <p>
     * 用户名只能包含字母数字下划线，且不能是邮箱，长度3-10
     * 密码长度6-64
     */
    @PostMapping("/register")
    public Result<SaTokenInfo> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return authService.register(registerDTO);
    }

    /**
     *
     * @param loginDTO 手机号/邮箱/用户名和密码
     * @return token
     */
    @PostMapping("/login")
    public Result<SaTokenInfo> login(@Valid @RequestBody LoginDTO loginDTO) {
        return authService.login(loginDTO);
    }

    @SaCheckLogin
    @PostMapping("/logout")
    public Result<Void> logout() {
        return authService.logout();
    }

    @SaCheckLogin
    @RequestMapping("/callback")
    public Result<Void> callback(@RequestParam String token, HttpServletResponse response) {
        return authService.callBack(token);
    }
}

