package org.example.picturecloudbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum SpaceRoleEnum {
    VIEWER("浏览者", "viewer"), EDITOR("编辑者", "editor"), ADMIN("管理员", "admin");
    private final String text;
    private final String value;

    SpaceRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     *
     * @param value
     * @return
     */
    public static SpaceRoleEnum getSpaceLevelEnumByValue(String value) {
        if (ObjUtil.isAllEmpty(value)) {
            return null;
        }
        for (SpaceRoleEnum e : SpaceRoleEnum.values()) {
            if (Objects.equals(e.value, value)) {
                return e;
            }
        }
        return null;
    }
}
