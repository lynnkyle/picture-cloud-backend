package org.example.picturecloudbackend.utils;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.picturecloudbackend.constant.CacheConstant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class RedisUtils {

    private static StringRedisTemplate stringRedisTemplate;

    static {
        RedisUtils.stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);
    }

    /**
     * 缓存键key
     * @param key
     * @param objects
     * @return
     */
    public static String getKey(String key, Object... objects) {
        return String.format(CacheConstant.BASE_KEY + key, objects);
    }
    // ========================key操作========================

    /**
     * 设置过期时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return true=成功 false=失败
     */
    public static boolean expire(String key, long time) {
        try {
            if (time > 0) {
                stringRedisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("设置过期时间失败 key={}, time={}", key, time, e);
            return false;
        }
    }

    /**
     * 设置过期时间
     *
     * @param key  键
     * @param time 时间
     * @param unit 时间单位
     * @return true=成功 false=失败
     */
    public static boolean expire(String key, long time, TimeUnit unit) {
        try {
            if (time > 0) {
                stringRedisTemplate.expire(key, time, unit);
            }
            return true;
        } catch (Exception e) {
            log.error("设置过期时间失败 key={}, time={}, unit={}", key, time, unit, e);
            return false;
        }
    }

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 时间(秒) 返回-1=永不过期 返回-2=key不存在
     */
    public static long getExpire(String key) {
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : -1L;
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true=存在 false=不存在
     */
    public static boolean hasKey(String key) {
        try {
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            return hasKey != null && hasKey;
        } catch (Exception e) {
            log.error("判断key是否存在失败 key={}", key, e);
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 键（可以传一个或多个）
     * @return 成功删除的个数
     */
    public static long del(String... key) {
        if (key == null || key.length == 0) {
            return 0;
        }
        try {
            if (key.length == 1) {
                Boolean delete = stringRedisTemplate.delete(key[0]);
                return delete != null && delete ? 1 : 0;
            } else {
                Long count = stringRedisTemplate.delete(Arrays.asList(key));
                return count != null ? count : 0;
            }
        } catch (Exception e) {
            log.error("删除缓存失败 keys={}", Arrays.toString(key), e);
            return 0;
        }
    }

    /**
     * 删除缓存（批量）
     *
     * @param keys 键集合
     * @return 成功删除的个数
     */
    public static long del(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return 0;
        }
        try {
            Long count = stringRedisTemplate.delete(keys);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("批量删除缓存失败 keys={}", keys, e);
            return 0;
        }
    }

    // ========================普通缓存操作========================

    /**
     *
     * @param o
     * @return
     */
    public static String objToJsonStr(Object o) {
        if (o == null) return CacheConstant.CACHE_NULL;
        return JsonUtils.toStr(o);
    }

    /**
     *
     * @param jsonStr
     * @param tClass
     * @return
     * @param <T>
     */
    public static <T> T jsonStrToObj(String jsonStr, Class<T> tClass) {
        if (jsonStr == null || Objects.equals(jsonStr, CacheConstant.CACHE_NULL)) return null;
        try {
            return JsonUtils.toObj(jsonStr, tClass);
        } catch (Exception e) {
            log.error("缓存存在问题 key={}", jsonStr);
            return null;
        }
    }

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public static String get(String key) {
        return key == null ? null : stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存获取并转换为对象
     * @param key
     * @param tClass
     * @return
     * @param <T>
     */
    public static <T> T get(String key, Class<T> tClass) {
        String jsonStr = get(key);
        return key == null || jsonStr == null ? null : jsonStrToObj(jsonStr, tClass);
    }

    /**
     * 普通缓存批量获取
     * @param keys
     * @return
     * @param <T>
     */
    public static <T> List<T> mget(Collection<String> keys, Class<T> tClass) {
        List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);
        if (Objects.isNull(values)) {
            return new ArrayList<>();
        }
        return values.stream().map(val -> jsonStrToObj(val, tClass)).collect(Collectors.toList());
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public static Boolean set(String key, Object value) {
        try {
            stringRedisTemplate.opsForValue().set(key, objToJsonStr(value));
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public static Boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                stringRedisTemplate.opsForValue().set(key, objToJsonStr(value), time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key      键
     * @param value    值
     * @param time     时间
     * @param timeUnit 类型
     * @return true成功 false 失败
     */
    public static Boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                stringRedisTemplate.opsForValue().set(key, objToJsonStr(value), time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 普通缓存批量放入并设置时间
     * @param map
     * @param time
     * @param <T>
     */
    public static <T> void mset(Map<String, T> map, long time) {
        Map<String, String> collect = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> objToJsonStr(e.getValue())));
        stringRedisTemplate.opsForValue().multiSet(collect);
        map.forEach((key, value) -> {
            expire(key, time);
        });
    }

    // ========================Set缓存操作========================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public static Set<String> sGet(String key) {
        try {
            return stringRedisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public static Long sSet(String key, Object... values) {
        try {
            String[] s = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                s[i] = objToJsonStr(values[i]);
            }
            return stringRedisTemplate.opsForSet().add(key, s);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 0L;
        }
    }
    // ========================ZSet缓存操作========================

}
