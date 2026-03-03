package org.example.picturecloudbackend.service.cache;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.picturecloudbackend.constant.CacheConstant;
import org.example.picturecloudbackend.manager.cache.AbstractRedisCache;
import org.example.picturecloudbackend.model.dto.picture.PictureQueryRequest;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.model.vo.picture.PictureVO;
import org.example.picturecloudbackend.service.PictureService;
import org.example.picturecloudbackend.utils.RedisUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PicturePageCache extends AbstractRedisCache<PictureQueryRequest, Set<Long>> {
    @Resource
    private PictureCache pictureCache;
    @Resource
    private PictureService pictureService;

    public PicturePageCache() {
        super((Class<Set<Long>>) (Class<?>) Set.class);
    }

    @Override
    public String getKey(PictureQueryRequest pictureQueryRequest) {
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String queryHashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        return RedisUtils.getKey(CacheConstant.PICTURE_ZSET, queryHashKey);
    }

    @Override
    public Long getExpireSeconds() {
        return CacheConstant.TTL_5_MINUTES;
    }

    @Override
    public Map<PictureQueryRequest, Set<Long>> loadFromDb(List<PictureQueryRequest> req) {
        return req.stream().collect(Collectors.toMap(Function.identity(), e -> {
            QueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(e);
            Page<Picture> page = new Page<>(e.getCurrent(), e.getPageSize());
            return pictureService.page(page, queryWrapper).getRecords().stream().map(Picture::getId).collect(Collectors.toSet());
        }));
    }

    public IPage<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest) {
        Map<PictureQueryRequest, Set<Long>> map = getBatch(Collections.singletonList(pictureQueryRequest));
        // TODO 优化多次请求
        Set<Long> idsSet = map.get(pictureQueryRequest);
        Map<Long, Picture> pictureMap = pictureCache.getBatch(new ArrayList<>(idsSet));
        List<Picture> pictureList = pictureMap.keySet().stream().map(pictureMap::get).collect(Collectors.toList());
        Page<Picture> page = new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize());
        page.setRecords(pictureList);
        return pictureService.getPictureVOPage(page);
    }
}
