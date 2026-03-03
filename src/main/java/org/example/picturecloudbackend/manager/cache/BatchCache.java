package org.example.picturecloudbackend.manager.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BatchCache<IN, OUT> {
    /**
     * 获取单个对象
     * @param req
     * @return
     */
    OUT get(IN req);

    /**
     * 获取批量对象
     * @param req
     * @return
     */
    Map<IN, OUT> getBatch(List<IN> req);

    /**
     * 删除单个
     * @param req
     */
    void delete(IN req);

    /**
     * 删除批量
     * @param req
     */
    void deleteBatch(List<IN> req);
}
