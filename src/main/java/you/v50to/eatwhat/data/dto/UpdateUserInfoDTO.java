package you.v50to.eatwhat.data.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新用户个人信息请求 DTO
 */
@Data
public class UpdateUserInfoDTO {

    /**
     * 性别：male, female, other
     */
    @Pattern(regexp = "^(male|female|other)$", message = "性别只能是 male、female 或 other")
    private String gender;

    /**
     * 生日
     */
    @Past(message = "生日必须是过去的日期")
    private LocalDate birthday;

    /**
     * 个性签名
     */
    @Size(max = 255, message = "个性签名不能超过 255 个字符")
    private String signature;

    /**
     * 家乡省份 ID
     */
    @Min(value = 1, message = "省份 ID 必须大于 0")
    private Integer hometownProvinceId;

    /**
     * 家乡城市 ID
     */
    @Min(value = 1, message = "城市 ID 必须大于 0")
    private Integer hometownCityId;
}
