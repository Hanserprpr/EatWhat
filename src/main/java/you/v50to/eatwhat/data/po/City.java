package you.v50to.eatwhat.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 城市实体
 */
@Data
@TableName("cities")
public class City {
    
    /**
     * 城市ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 省份ID
     */
    private Integer provinceId;
    
    /**
     * 城市名称
     */
    private String name;
    
    /**
     * 创建时间
     */
    private OffsetDateTime createdAt;
}

