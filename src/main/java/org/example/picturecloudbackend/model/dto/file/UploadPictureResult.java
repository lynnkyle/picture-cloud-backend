package org.example.picturecloudbackend.model.dto.file;

import lombok.Data;

/**
 * @author kyle
 * @description 上传图片的结果
 * @createDate 2026-02-05 23:39
 */
@Data
public class UploadPictureResult {

    /**
     * 图片地址
     */
    private String picUrl;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

}
