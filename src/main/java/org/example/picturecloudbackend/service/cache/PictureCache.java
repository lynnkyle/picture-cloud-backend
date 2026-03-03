package org.example.picturecloudbackend.service.cache;

import org.example.picturecloudbackend.constant.CacheConstant;
import org.example.picturecloudbackend.manager.cache.AbstractRedisCache;
import org.example.picturecloudbackend.mapper.PictureMapper;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.utils.RedisUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PictureCache extends AbstractRedisCache<Long, Picture> {
    @Resource
    private PictureMapper pictureMapper;

    public PictureCache() {
        super(Picture.class);
    }

    @Override
    public String getKey(Long req) {
        return RedisUtils.getKey(CacheConstant.PICTURE_INFO_STRING, req);
    }

    @Override
    public Long getExpireSeconds() {
        return CacheConstant.TTL_5_MINUTES;
    }

    @Override
    public Map<Long, Picture> loadFromDb(List<Long> req) {
        List<Picture> pictureList = pictureMapper.selectByIds(req);
        return pictureList.stream().collect(Collectors.toMap(Picture::getId, Function.identity()));
    }
}
