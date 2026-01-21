package you.v50to.eatwhat.data.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private Integer code; // 业务状态码
    private Object data;  // 数据
    private String msg;   // 提示信息

    public static Result ok() {
        return new Result(200, null, "ok");
    }

    public static Result ok(Object data) {
        return new Result(200, data, "ok");
    }

    public static Result fail(String msg) {
        return new Result(400, null, msg);
    }

    public static Result fail(int code, String msg) {
        return new Result(code, null, msg);
    }

}
