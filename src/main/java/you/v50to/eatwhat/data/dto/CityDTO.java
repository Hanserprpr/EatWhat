package you.v50to.eatwhat.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 城市 DTO（不包含 createdAt）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityDTO {
    
    /**
     * 城市ID
     */
    private Integer id;
    
    /**
     * 省份ID
     */
    private Integer provinceId;
    
    /**
     * 城市名称
     */
    private String name;
}

