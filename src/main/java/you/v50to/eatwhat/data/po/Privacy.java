package you.v50to.eatwhat.data.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@TableName("privacy")
public class Privacy {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long accountId;
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Boolean following;
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Boolean follower;
    private OffsetDateTime createdAt;
}
