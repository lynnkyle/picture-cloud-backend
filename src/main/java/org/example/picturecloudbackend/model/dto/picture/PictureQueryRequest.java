package org.example.picturecloudbackend.model.dto.picture;

import lombok.Data;
import org.example.picturecloudbackend.common.PageRequest;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = -6859236916420254859L;

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

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 搜索词(同时搜索名称，简介等)
     */
    private String searchText;

    /**
     * 审核状态:0-待审核, 1-通过, 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人id
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date ReviewTime;

}
