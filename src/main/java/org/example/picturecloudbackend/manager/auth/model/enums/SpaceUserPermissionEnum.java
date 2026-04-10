package org.example.picturecloudbackend.manager.auth.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;
import org.example.picturecloudbackend.manager.auth.model.SpaceUserPermission;
import org.example.picturecloudbackend.manager.auth.model.constant.SpaceUserPermissionConstant;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author kyle
 * @description 空间用户权限枚举类
 * @createDate 2026-02-13 16:54
 */

@Getter
public enum SpaceUserPermissionEnum {
    SPACE_USER_MANAGE("用户管理", SpaceUserPermissionConstant.SPACE_USER_MANAGE),
    PICTURE_VIEW("图片浏览", SpaceUserPermissionConstant.PICTURE_VIEW),
    PICTURE_UPLOAD("图片上传", SpaceUserPermissionConstant.PICTURE_UPLOAD),
    PICTURE_EDIT("图片编辑", SpaceUserPermissionConstant.PICTURE_EDIT),
    PICTURE_DELETE("图片删除", SpaceUserPermissionConstant.PICTURE_DELETE);

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
    public static SpaceUserPermissionEnum getEnumByValue(String value) {
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
