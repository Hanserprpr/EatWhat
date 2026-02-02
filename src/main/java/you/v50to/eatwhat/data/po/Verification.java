package you.v50to.eatwhat.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@TableName("verifications")
public class Verification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long accountId;
    private String method;
    private Boolean verified;
    private String studentId;
    private String realName;
    private String verifiedEmail;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
