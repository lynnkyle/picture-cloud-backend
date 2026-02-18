package org.example.picturecloudbackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author kyle
 * @description 图片编辑请求
 * @createDate 2026-02-06 16:16
 */

@Data
public class PictureEditRequest implements Serializable {

    private static final long serialVersionUID = -7692532705751252243L;
    /**
     * 图片id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片简介
     */
    private String picIntro;

    /**
     * 图片分类
     */
    private String picCategory;

    /**
     * 图片标签（JSON 数组）
     */
    private List<String> picTags;

}
