package you.v50to.eatwhat.data.dto;

import lombok.Data;

@Data
public class PrivacyDTO {
    /**
     * 关注列表是否可见
     */
    private Boolean following;
    /**
     * 粉丝列表是否可见
     */
    private Boolean follower;
}
