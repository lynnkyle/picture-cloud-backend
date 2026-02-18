package org.example.picturecloudbackend.model.dto.picture;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class PictureReviewerRequest implements Serializable {
    private static final long serialVersionUID = -4400663590298585962L;
    /**
     * 图片id
     */
    private Long id;

    /**
     * 审核状态:0-待审核, 1-通过, 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;
}
