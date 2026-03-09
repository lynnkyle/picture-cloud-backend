package org.example.picturecloudbackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kyle
 * @description 空间编辑请求
 * @createDate 2026-03-09 20:18
 */
@Data
public class SpaceEditRequest implements Serializable {

    private static final long serialVersionUID = 7517855208294546653L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;
}
