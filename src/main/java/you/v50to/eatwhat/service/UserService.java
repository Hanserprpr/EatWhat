package you.v50to.eatwhat.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import you.v50to.eatwhat.data.dto.BindMobileReq;
import you.v50to.eatwhat.data.dto.SendCodeReq;
import you.v50to.eatwhat.data.enums.BizCode;
import you.v50to.eatwhat.data.enums.Scene;
import you.v50to.eatwhat.data.po.Contact;
import you.v50to.eatwhat.data.vo.Result;
import you.v50to.eatwhat.data.dto.UserInfoDTO;
import you.v50to.eatwhat.mapper.ContactMapper;
import you.v50to.eatwhat.mapper.UserMapper;

@Service
@Slf4j
public class UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private SmsService smsService;
    @Resource
    private ContactMapper contactMapper;

    public Result<UserInfoDTO> getInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserInfoDTO info = userMapper.selectUserInfoById(userId);
        if (info == null) {
            return Result.fail(BizCode.USER_NOT_FOUND);
        }
        return Result.ok(info);
    }

    public Result<Void> getCode(SendCodeReq sendCodeReq, String ip) {
        Scene scene = sendCodeReq.getScene();
        String mobile = sendCodeReq.getMobile();
        if (!StpUtil.hasRole("verified")){
            return Result.fail(BizCode.STATE_NOT_ALLOWED, "未通过认证，无法发送验证码");
        }
        if (scene.equals(Scene.bind) && contactMapper.exists(new LambdaQueryWrapper<Contact>()
                .eq(Contact::getAccountId, StpUtil.getLoginIdAsLong()))) {
            return Result.fail(BizCode.OP_FAILED, "已绑定手机号，无法重复绑定");
        }
        smsService.sendCode(scene, mobile, ip);
        return Result.ok();
    }

    public Result<Void> bindMobile(Scene scene, BindMobileReq bindMobileReq) {
        String mobile = bindMobileReq.getMobile();
        String code = bindMobileReq.getCode();
        Long userId = StpUtil.getLoginIdAsLong();

        if (contactMapper.exists(new LambdaQueryWrapper<Contact>()
                .eq(Contact::getAccountId, userId))) {
            return Result.fail(BizCode.OP_FAILED, "已绑定手机号，无法重复绑定");
        }
        if (contactMapper.exists(new LambdaQueryWrapper<Contact>()
                .eq(Contact::getPhone, mobile))) {
            return Result.fail(BizCode.OP_FAILED, "该手机号已被绑定");
        }
        if (smsService.verifyCode(scene, mobile, code)){
            Contact contact = new Contact();
            contact.setAccountId(userId);
            contact.setPhone(mobile);
            contactMapper.insert(contact);
            return Result.ok();
        } else {
            return Result.fail(BizCode.VERIFY_CODE_ERROR);
        }
    }
}
