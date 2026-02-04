package you.v50to.eatwhat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import you.v50to.eatwhat.data.dto.CityDTO;
import you.v50to.eatwhat.data.dto.ProvinceDTO;
import you.v50to.eatwhat.data.po.City;
import you.v50to.eatwhat.data.po.Province;
import you.v50to.eatwhat.mapper.CityMapper;
import you.v50to.eatwhat.mapper.ProvinceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 地理位置服务
 */
@Service
@RequiredArgsConstructor
public class LocationService {

    private final ProvinceMapper provinceMapper;
    private final CityMapper cityMapper;

    /**
     * 获取所有省份
     * 使用缓存，省份数据基本不变
     */
    @Cacheable(value = "provinces", unless = "#result == null || #result.isEmpty()")
    public List<ProvinceDTO> getAllProvinces() {
        List<Province> provinces = provinceMapper.selectList(null);
        return provinces.stream()
            .map(this::convertToProvinceDTO)
            .collect(Collectors.toList());
    }

    /**
     * 根据省份ID获取城市列表
     * 使用缓存
     */
    @Cacheable(value = "cities", key = "#provinceId", unless = "#result == null || #result.isEmpty()")
    public List<CityDTO> getCitiesByProvinceId(Integer provinceId) {
        List<City> cities = cityMapper.selectList(
            new LambdaQueryWrapper<City>()
                .eq(City::getProvinceId, provinceId)
                .orderByAsc(City::getId)
        );
        return cities.stream()
            .map(this::convertToCityDTO)
            .collect(Collectors.toList());
    }

    /**
     * 根据省份名称获取省份
     */
    @Cacheable(value = "province", key = "#name")
    public Province getProvinceByName(String name) {
        return provinceMapper.selectOne(
            new LambdaQueryWrapper<Province>()
                .eq(Province::getName, name)
        );
    }

    /**
     * 根据省份ID和城市名称获取城市
     */
    @Cacheable(value = "city", key = "#provinceId + '_' + #cityName")
    public City getCityByProvinceAndName(Integer provinceId, String cityName) {
        return cityMapper.selectOne(
            new LambdaQueryWrapper<City>()
                .eq(City::getProvinceId, provinceId)
                .eq(City::getName, cityName)
        );
    }

    /**
     * 将 Province 实体转换为 ProvinceDTO
     */
    private ProvinceDTO convertToProvinceDTO(Province province) {
        return new ProvinceDTO(
            province.getId(),
            province.getName()
        );
    }

    /**
     * 将 City 实体转换为 CityDTO
     */
    private CityDTO convertToCityDTO(City city) {
        return new CityDTO(
            city.getId(),
            city.getProvinceId(),
            city.getName()
        );
    }
}

