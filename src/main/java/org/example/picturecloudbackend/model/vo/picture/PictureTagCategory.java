package org.example.picturecloudbackend.model.vo.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureTagCategory implements Serializable {
    private static final long serialVersionUID = -2453831922184060272L;
    /**
     * 标签列表
     */
    private List<String> tagList;
    /**
     * 分类列表
     */
    private List<String> categoryList;
}
