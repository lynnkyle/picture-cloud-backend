package org.example.picturecloudbackend.model.vo.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 空间级别
 */
@Data
@AllArgsConstructor
public class SpaceLevel {
    /**
     * 枚举值
     */
    private int value;

    /**
     * 枚举文本
     */
    private String text;

    /**
     * 最大数量
     */
    private long maxCount;

    /**
     * 最大容量
     */
    private long maxSize;

}
