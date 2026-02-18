package org.example.picturecloudbackend.model.dto.picture;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author kyle
 * @description 图片更新请求
 * @createDate 2026-02-06 16:10
 */

@Data
public class PictureUpdateRequest implements Serializable {
    
    private static final long serialVersionUID = -7205978866443687452L;
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
