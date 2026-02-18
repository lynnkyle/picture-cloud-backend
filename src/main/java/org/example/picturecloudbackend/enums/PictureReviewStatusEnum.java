package org.example.picturecloudbackend.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Objects;

/**
 * @author kyle
 * @description 图片审核状态枚举类
 * @createDate 2026-02-13 16:54
 */

@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final Integer value;

    PictureReviewStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     * @param value
     * @return
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtil.isAllEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            if (Objects.equals(pictureReviewStatusEnum.value, value)) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}
