package you.v50to.eatwhat.data.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import you.v50to.eatwhat.data.enums.BizCode;

@Data
@NoArgsConstructor
public class Result<T> {

    private Integer code; // 业务状态码
    private T data;       // 数据
    private String msg;   // 提示信息
    private long timestamp;

    public Result(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> ok() {
        return new Result<>(BizCode.SUCCESS.getCode(), null, BizCode.SUCCESS.getMsg());
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(BizCode.SUCCESS.getCode(), data, BizCode.SUCCESS.getMsg());
    }

    public static <T> Result<T> fail(BizCode bizCode) {
        return new Result<>(bizCode.getCode(), null, bizCode.getMsg());
    }

    public static <T> Result<T> fail(BizCode bizCode, String extraMsg) {
        // 追加信息：适合携带更具体的原因（例如字段名、上下文）
        return new Result<>(bizCode.getCode(), null, bizCode.getMsg() + "：" + extraMsg);
    }

}
