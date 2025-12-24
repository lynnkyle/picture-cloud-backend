package org.example.picturecloudbackend.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author LinZeyuan
 * @description 用户角色枚举
 * @createDate 2025/12/23 17:53
 */
@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     * @param value
     * @return
     */
    public static UserRoleEnum getUserRoleEnumByValue(String value) {
        if (ObjUtil.isAllEmpty(value)) {
            return null;
        }
        for (UserRoleEnum e : UserRoleEnum.values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }

}
