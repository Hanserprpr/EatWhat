package you.v50to.eatwhat.data.dto;

import you.v50to.eatwhat.data.po.City;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 省份及其城市列表 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceWithCitiesDTO {

    private Integer id;

    private String name;

    private List<City> cities;
}

