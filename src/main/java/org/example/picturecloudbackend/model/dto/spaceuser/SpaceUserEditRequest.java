package org.example.picturecloudbackend.model.dto.spaceuser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @author kyle
 * @description 空间成员编辑请求
 * @createDate 2026-03-09 20:18
 */
@Data
public class SpaceUserEditRequest implements Serializable {

    private static final long serialVersionUID = 1821514597462754008L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;
}
