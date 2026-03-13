package you.v50to.eatwhat.data.po;

import org.locationtech.jts.geom.MultiPolygon;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class Hub {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String center;
    private Double gcjLng;
    private Double gcjLat;
    private MultiPolygon boundary;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
