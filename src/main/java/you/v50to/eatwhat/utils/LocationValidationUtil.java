package you.v50to.eatwhat.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import you.v50to.eatwhat.data.enums.BizCode;
import you.v50to.eatwhat.data.po.City;
import you.v50to.eatwhat.data.po.Province;
import you.v50to.eatwhat.data.vo.Result;
import you.v50to.eatwhat.service.LocationService;

/**
 * 地理位置校验工具类
 * 提供省份和城市的有效性及关联性校验
 * 复用 LocationService 的缓存机制
 */
@Slf4j
@Component
public class LocationValidationUtil {

    @Resource
    private LocationService locationService;

    /**
     * 校验省份是否存在
     * 
     * @param provinceId 省份 ID
     * @return 校验结果，成功返回 null，失败返回错误信息
     */
    public Result<Void> validateProvince(Integer provinceId) {
        if (provinceId == null) {
            return null;
        }

        Province province = locationService.getProvinceById(provinceId);
        if (province == null) {
            log.warn("省份不存在: provinceId={}", provinceId);
            return Result.fail(BizCode.PROVINCE_NOT_FOUND);
        }

        return null;
    }

    /**
     * 校验城市是否存在
     * 
     * @param cityId 城市 ID
     * @return 校验结果，成功返回 null，失败返回错误信息
     */
    public Result<Void> validateCity(Integer cityId) {
        if (cityId == null) {
            return null;
        }

        City city = locationService.getCityById(cityId);
        if (city == null) {
            log.warn("城市不存在: cityId={}", cityId);
            return Result.fail(BizCode.CITY_NOT_FOUND);
        }

        return null;
    }

    /**
     * 校验城市是否属于指定省份
     * 
     * @param provinceId 省份 ID
     * @param cityId     城市 ID
     * @return 校验结果，成功返回 null，失败返回错误信息
     */
    public Result<Void> validateCityBelongsToProvince(Integer provinceId, Integer cityId) {
        if (provinceId == null || cityId == null) {
            return null;
        }

        City city = locationService.getCityById(cityId);
        if (city == null) {
            log.warn("城市不存在: cityId={}", cityId);
            return Result.fail(BizCode.CITY_NOT_FOUND);
        }

        if (!city.getProvinceId().equals(provinceId)) {
            log.warn("城市与省份不匹配: provinceId={}, cityId={}, actualProvinceId={}",
                    provinceId, cityId, city.getProvinceId());
            return Result.fail(BizCode.CITY_PROVINCE_MISMATCH);
        }

        return null;
    }

    /**
     * 综合校验省份和城市的有效性及关联关系
     * 
     * @param provinceId 省份 ID（可为空）
     * @param cityId     城市 ID（可为空）
     * @return 校验结果，成功返回 null，失败返回错误信息
     */
    public Result<Void> validateProvinceAndCity(Integer provinceId, Integer cityId) {
        if (provinceId == null && cityId == null) {
            return null;
        }

        // 校验省份
        if (provinceId != null && cityId == null) {
            return validateProvince(provinceId);
        }

        // 校验城市
        if (provinceId == null && cityId != null) {
            return validateCity(cityId);
        }

        // 校验两者的关联关系
        Result<Void> provinceResult = validateProvince(provinceId);
        if (provinceResult != null) {
            return provinceResult;
        }

        return validateCityBelongsToProvince(provinceId, cityId);
    }
}
