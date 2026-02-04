package you.v50to.eatwhat.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 省份 DTO（不包含 createdAt）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceDTO {
    
    /**
     * 省份ID
     */
    private Integer id;
    
    /**
     * 省份名称
     */
    private String name;
}

