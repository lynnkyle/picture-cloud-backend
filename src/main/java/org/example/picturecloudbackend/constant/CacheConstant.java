package org.example.picturecloudbackend.constant;

import java.util.concurrent.TimeUnit;

public interface CacheConstant {

    // ========================缓存键========================

    String BASE_KEY = "picture-cloud:";

    // 图片列表缓存
    String PICTURE_ZSET = "listPictureVOByPage:%s";

    // 图片信息缓存
    String PICTURE_INFO_STRING = "pictureInfo:pictureId_%d";
    String THUMBNAIL_PICTURE_INFO_STRING = "thumbnailPictureInfo:pictureId_%d";
    // ========================缓存值========================
    String CACHE_NULL = "NULL";

    // ==================缓存过期时间(默认:秒)==================
    long TTL_5_MINUTES = 5 * 60;
}
