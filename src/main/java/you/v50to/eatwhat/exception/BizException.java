package you.v50to.eatwhat.exception;

import lombok.Getter;
import you.v50to.eatwhat.data.enums.BizCode;

/**
 * 业务异常类
 * 用于封装业务逻辑中的异常情况
 */
@Getter
public class BizException extends RuntimeException {
    
    private final BizCode bizCode;

    public BizException(BizCode bizCode) {
        super(bizCode.getMsg());
        this.bizCode = bizCode;
    }

    public BizException(BizCode bizCode, String extraMsg) {
        super(bizCode.getMsg() + "：" + extraMsg);
        this.bizCode = bizCode;
    }

    public BizException(BizCode bizCode, Throwable cause) {
        super(bizCode.getMsg(), cause);
        this.bizCode = bizCode;
    }
}

