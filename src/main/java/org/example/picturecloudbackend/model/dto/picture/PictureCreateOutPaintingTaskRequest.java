package org.example.picturecloudbackend.model.dto.picture;

import lombok.Data;
import org.example.picturecloudbackend.api.aliyun.model.CreateImageOutPaintingTaskRequest;

import java.io.Serializable;

@Data
public class PictureCreateOutPaintingTaskRequest implements Serializable {
    private static final long serialVersionUID = -6949515542229925257L;
    // 图片id
    private Long pictureId;
    // 扩图参数
    private CreateImageOutPaintingTaskRequest.Parameters parameters;
}
