package org.example.picturecloudbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author LinZeyuan
 * @description 用户角色枚举
 * @createDate 2025/12/23 17:53
 */
@Getter
public enum SpaceLevelEnum {

    COMMON("普通版", 0, 100, 100L * 1024 * 1024),
    PROFESSIONAL("专业版", 1, 1000, 1000L * 1024 * 1024),
    FLAGSHIP("旗舰版", 2, 10000, 10000L * 1024 * 1024);

    private final String text;
    private final int value;
    private final long maxCount;
    private final long maxSize;

    SpaceLevelEnum(String text, int value, long maxCount, long maxSize) {
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    /**
     * 根据value获取枚举
     * @param value
     * @return
     */
    public static SpaceLevelEnum getSpaceLevelEnumByValue(Integer value) {
        if (ObjUtil.isAllEmpty(value)) {
            return null;
        }
        for (SpaceLevelEnum e : SpaceLevelEnum.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }
}
