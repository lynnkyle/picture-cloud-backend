package org.example.picturecloudbackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.manager.auth.model.SpaceUserAuthConfig;
import org.example.picturecloudbackend.manager.auth.model.SpaceUserRole;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpaceUserAuthManage {
    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        String jsonString = ResourceUtil.readUtf8Str("business/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(jsonString, SpaceUserAuthConfig.class);
    }

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
}
