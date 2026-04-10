package org.example.picturecloudbackend.manager.auth;

import lombok.Data;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.model.entity.Space;
import org.example.picturecloudbackend.model.entity.SpaceUser;


@Data
public class SpaceUserAuthContext {

    /**
     * 临时参数，不同请求对应的 id 可能不同
     */
    private Long id;

    /**
     * 图片 ID
     */
    private Long pictureId;

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 空间用户 ID
     */
    private Long spaceUserId;

    /**
     * 图片信息
     */
    private Picture picture;

    /**
     * 空间信息
     */
    private Space space;

    /**
     * 空间用户信息
     */
    private SpaceUser spaceUser;
}