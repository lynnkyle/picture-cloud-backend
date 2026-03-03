package org.example.picturecloudbackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 批量导入图片请求
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {

    private static final long serialVersionUID = 7304870997437631125L;

    /**
     * 图片搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 5;

    /**
     * 图片名称前缀
     */
    private String namePrefix;
}