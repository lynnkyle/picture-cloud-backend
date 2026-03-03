package org.example.picturecloudbackend.manager.cache;

import org.example.picturecloudbackend.constant.CacheConstant;
import org.example.picturecloudbackend.utils.RedisUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractRedisCache<IN, OUT> implements BatchCache<IN, OUT> {

    private Class<OUT> outClass;

    public abstract String getKey(IN req);

    public abstract Long getExpireSeconds();

    public abstract Map<IN, OUT> loadFromDb(List<IN> req);

    public AbstractRedisCache() {

    }

    public AbstractRedisCache(Class<OUT> outClass) {
        this.outClass = outClass;
    }

    @Override
    public OUT get(IN req) {
        return getBatch(Collections.singletonList(req)).get(req);
    }

    @Override
    public Map<IN, OUT> getBatch(List<IN> req) {
        if (CollectionUtils.isEmpty(req)) {
            return new HashMap<>();
        }
        // 去重
        req = req.stream().distinct().collect(Collectors.toList());
        // 组装key
        List<String> keys = req.stream().map(this::getKey).collect(Collectors.toList());
        // 批量get
        List<OUT> values = RedisUtils.mget(keys, outClass);
        // 缓存穿透
        // 1. 差集计算
        List<IN> loadReq = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            if (!RedisUtils.hasKey(keys.get(i))) {
                loadReq.add(req.get(i));
            }
        }
        // 2. 不足,重新加载到Redis
        Map<IN, OUT> loadFromDb;
        if (!CollectionUtils.isEmpty(loadReq)) {
            // 批量load
            loadFromDb = loadFromDb(loadReq);
            Map<String, OUT> loadMap = loadReq.stream()
                    .collect(HashMap::new, (map, e) -> map.put(getKey(e), loadFromDb.get(e)), Map::putAll);
            RedisUtils.mset(loadMap, getExpireSeconds());
        } else {
            loadFromDb = new HashMap<>();
        }
        // 3. 组装最后结果
        Map<IN, OUT> loadRes = new HashMap<>();
        for (int i = 0; i < req.size(); i++) {
            IN in = req.get(i);
            OUT out = Optional.ofNullable(values.get(i)).orElse(loadFromDb.get(in));
            loadRes.put(in, out);
        }
        return loadRes;
    }

    @Override
    public void delete(IN req) {
        deleteBatch(Collections.singletonList(req));
    }

    @Override
    public void deleteBatch(List<IN> req) {
        List<String> keys = req.stream().map(this::getKey).collect(Collectors.toList());
        RedisUtils.del(keys);
    }
}
