package org.example.picturecloudbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -4128769888189675445L;

    private int current = 1;

    private int pageSize = 10;

    private String sortField;

    private String sortOrder = "descend";
}
