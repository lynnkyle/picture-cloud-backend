package org.example.picturecloudbackend.manager.auth.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Objects;

/**
 * @author kyle
 * @description 图片审核状态枚举类
 * @createDate 2026-02-13 16:54
 */

@Getter
public enum SpaceUserPermissionEnum {
    SPACE_USER_MANAGE("用户管理", "spaceUser:manage"),
    PICTURE_VIEW("图片浏览", "picture:view"),
    PICTURE_UPLOAD("图片上传", "picture:upload"),
    PICTURE_EDIT("图片编辑", "picture:edit"),
    PICTURE_DELETE("图片删除", "picture:delete");

    private final String text;
    private final String value;

    SpaceUserPermissionEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     * @param value
     * @return
     */
    public static SpaceUserPermissionEnum getEnumByValue(Integer value) {
        if (ObjectUtil.isAllEmpty(value)) {
            return null;
        }
        for (SpaceUserPermissionEnum pictureReviewStatusEnum : SpaceUserPermissionEnum.values()) {
            if (Objects.equals(pictureReviewStatusEnum.value, value)) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}
