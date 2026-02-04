package you.v50to.eatwhat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import you.v50to.eatwhat.data.po.City;
import org.apache.ibatis.annotations.Mapper;

/**
 * 城市 Mapper
 */
@Mapper
public interface CityMapper extends BaseMapper<City> {
}

