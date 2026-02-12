package you.v50to.eatwhat.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PresignBatchUploadRespDTO {
    private List<PresignUploadRespDTO> files;
}
