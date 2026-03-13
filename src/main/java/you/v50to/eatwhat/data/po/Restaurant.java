package you.v50to.eatwhat.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class Restaurant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String address;
    private Double gcjLng;
    private Double gcjLat;
    private Long mallId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
