package you.v50to.eatwhat.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 省份实体
 */
@Data
@TableName("provinces")
public class Province {
    
    /**
     * 省份ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 省份名称
     */
    private String name;
    
    /**
     * 创建时间
     */
    private OffsetDateTime createdAt;
}

