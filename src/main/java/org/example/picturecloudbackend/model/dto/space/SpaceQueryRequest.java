package org.example.picturecloudbackend.model.dto.space;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import org.example.picturecloudbackend.common.PageRequest;

import java.io.Serializable;
import java.util.Date;

/**
 * @author kyle
 * @description 空间查询请求
 * @createDate 2026-03-09 20:18
 */
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = -2913805036849625091L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间类型(0-私有 1-团队)
     */
    private Integer spaceType;

    /**
     * 创建用户 id
     */
    private Long userId;
}
