package org.example.picturecloudbackend.service.cache;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.picturecloudbackend.constant.CacheConstant;
import org.example.picturecloudbackend.manager.cache.AbstractRedisCache;
import org.example.picturecloudbackend.model.dto.cache.PageCacheData;
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
public class PicturePageCache extends AbstractRedisCache<PictureQueryRequest, PageCacheData> {
    @Resource
    private PictureCache pictureCache;
    @Resource
    private PictureService pictureService;

    public PicturePageCache() {
        super(PageCacheData.class);
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
    public Map<PictureQueryRequest, PageCacheData> loadFromDb(List<PictureQueryRequest> reqList) {
        return reqList.stream().collect(Collectors.toMap(Function.identity(), e -> {
            QueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(e);
            Page<Picture> page = new Page<>(e.getCurrent(), e.getPageSize());
            Page<Picture> dbPage = pictureService.page(page, queryWrapper);
            List<Long> ids = dbPage.getRecords().stream().map(Picture::getId).collect(Collectors.toList());
            long total = dbPage.getTotal();
            return new PageCacheData(ids, total);
        }));
    }

    public IPage<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest) {
        Map<PictureQueryRequest, PageCacheData> map = getBatch(Collections.singletonList(pictureQueryRequest));
        // TODO 优化多次请求
        PageCacheData pageCacheData = map.get(pictureQueryRequest);
        Map<Long, Picture> pictureMap = pictureCache.getBatch(pageCacheData.getIds());
        List<Picture> pictureList = new ArrayList<>(pictureMap.values());
        Page<Picture> page = new Page<>(pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize());
        page.setRecords(pictureList);
        page.setTotal(pageCacheData.getTotal());
        return pictureService.getPictureVOPage(page);
    }
}
