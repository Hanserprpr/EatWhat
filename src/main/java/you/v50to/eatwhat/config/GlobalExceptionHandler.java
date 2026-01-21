package you.v50to.eatwhat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import you.v50to.eatwhat.data.enums.BizCode;
import you.v50to.eatwhat.data.vo.Result;
import you.v50to.eatwhat.service.SmsService;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SmsService.BizException.class)
    public Result<Void> handleBiz(SmsService.BizException e) {
        return new Result<>(
                e.getBizCode().getCode(),
                null,
                e.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleAll(Exception e) {
        log.error(e.getMessage(), e);
        return Result.fail(BizCode.UNKNOWN_ERROR);
    }
}
