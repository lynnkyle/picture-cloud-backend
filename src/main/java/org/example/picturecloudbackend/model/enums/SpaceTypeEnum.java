package org.example.picturecloudbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum SpaceTypeEnum {
    PRIVATE("私有空间", 0),
    TEAM("团队空间", 1);
    private final String text;
    private final int value;

    SpaceTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     * @param value
     * @return
     */
    public static SpaceTypeEnum getSpaceTypeEnumByValue(Integer value) {
        if (ObjUtil.isAllEmpty(value)) {
            return null;
        }
        for (SpaceTypeEnum e : SpaceTypeEnum.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }
}
