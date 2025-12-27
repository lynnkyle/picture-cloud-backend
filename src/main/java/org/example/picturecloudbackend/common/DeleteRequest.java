package org.example.picturecloudbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = 2086017223709154874L;
    private Long id;
}
