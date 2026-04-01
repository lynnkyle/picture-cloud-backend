package org.example.picturecloudbackend.model.dto.spaceuser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author kyle
 * @description 空间成员创建请求
 * @createDate 2026-03-09 20:18
 */
@Data
public class SpaceUserAddRequest implements Serializable {

    private static final long serialVersionUID = 6701906286526767949L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

}
