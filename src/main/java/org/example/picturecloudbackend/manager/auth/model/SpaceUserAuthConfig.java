package org.example.picturecloudbackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 空间成员权限配置
 */
@Data
public class SpaceUserAuthConfig implements Serializable {

    private static final long serialVersionUID = 4542007290430810514L;

    /**
     * 权限列表
     */
    private List<SpaceUserPermission> permissions;
    /**
     * 角色列表
     */
    private List<SpaceUserRole> roles;

}
