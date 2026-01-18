package org.example.picturecloudbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 图片表
 * @TableName picture
 */
@TableName(value = "picture")
@Data
public class Picture implements Serializable {
    /**
     * 图片id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片url
     */
    private String picUrl;

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
    private String picTags;

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
     * 逻辑删除(1-删除)
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}