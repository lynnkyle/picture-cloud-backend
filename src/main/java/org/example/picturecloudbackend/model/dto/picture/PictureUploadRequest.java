package org.example.picturecloudbackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
    private static final long serialVersionUID = 7304870997437631125L;

    /**
     * 图片id
     */
    private Long id;

    /**
     * 图片url
     */
    private String fileUrl;

}
