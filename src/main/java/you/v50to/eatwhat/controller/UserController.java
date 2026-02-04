package you.v50to.eatwhat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import you.v50to.eatwhat.data.dto.BindMobileReq;
import you.v50to.eatwhat.data.dto.SendCodeReq;
import you.v50to.eatwhat.data.enums.Scene;
import you.v50to.eatwhat.data.vo.Result;
import you.v50to.eatwhat.data.dto.UserInfoDTO;
import you.v50to.eatwhat.service.UserService;
import you.v50to.eatwhat.utils.IpUtil;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private HttpServletRequest request;

    @SaCheckLogin
    @GetMapping("/info")
    public Result<UserInfoDTO> info() {
        return userService.getInfo();
    }

    /**
     * Sends a verification code based on the provided request parameters and client IP address.
     *
     * @param sendCodeReq the request containing information needed to send the verification code
     * @return a {@link Result} indicating whether the verification code was sent successfully
     */
    @SaCheckLogin
    @GetMapping("/getCode")
    public Result<Void> getCode(@Valid @RequestBody SendCodeReq sendCodeReq) {
        String ip = IpUtil.getClientIp(request);
        return userService.getCode(sendCodeReq, ip);
    }

    @SaCheckLogin
    @PostMapping("/bindMobile")
    public Result<Void> bindMobile(@Valid @RequestBody BindMobileReq bindMobileReq) {
        return userService.bindMobile(Scene.bind, bindMobileReq);
    }


}
