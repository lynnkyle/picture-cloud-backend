package org.example.picturecloudbackend.model.vo.spaceuser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.example.picturecloudbackend.model.vo.space.SpaceVO;
import org.example.picturecloudbackend.model.vo.user.UserVO;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间成员相应类
 */
@Data
public class SpaceUserVO implements Serializable {

    private static final long serialVersionUID = 7453072284566158587L;

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
     * 空间信息
     */
    private SpaceVO spaceVO;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户信息
     */
    private UserVO userVO;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
