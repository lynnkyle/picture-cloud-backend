package org.example.picturecloudbackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.example.picturecloudbackend.manager.auth.model.SpaceUserAuthConfig;
import org.example.picturecloudbackend.manager.auth.model.SpaceUserRole;
import org.example.picturecloudbackend.manager.auth.model.constant.SpaceUserPermissionConstant;
import org.example.picturecloudbackend.model.entity.Space;
import org.example.picturecloudbackend.model.entity.SpaceUser;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.enums.SpaceRoleEnum;
import org.example.picturecloudbackend.model.enums.SpaceTypeEnum;
import org.example.picturecloudbackend.service.SpaceUserService;
import org.example.picturecloudbackend.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class SpaceUserAuthManage {
    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        String jsonString = ResourceUtil.readUtf8Str("business/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(jsonString, SpaceUserAuthConfig.class);
    }

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    public List<String> getPermissionByRole(String spaceUserRole) {
        if (StrUtil.isEmpty(spaceUserRole)) {
            return new ArrayList<>();
        }
        List<SpaceUserRole> roles = SPACE_USER_AUTH_CONFIG.getRoles();
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream().filter(
                r -> r.getKey().equals(spaceUserRole)
        ).findFirst().orElse(null);
        if (role == null) return new ArrayList<>();
        return role.getPermissions();
    }

    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) return new ArrayList<>();
        // 管理员权限
        List<String> permissionByAdmin = getPermissionByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return permissionByAdmin;
            }
            return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getSpaceTypeEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) return new ArrayList<>();
        switch (spaceTypeEnum) {
            case PRIVATE:
                if (Objects.equals(space.getUserId(), loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return permissionByAdmin;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser != null) {
                    return getPermissionByRole(spaceUser.getSpaceRole());
                } else {
                    return new ArrayList<>();
                }
        }
        return new ArrayList<>();
    }
}
