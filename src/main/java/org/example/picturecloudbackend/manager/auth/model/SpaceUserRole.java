package org.example.picturecloudbackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 空间成员角色
 */
@Data
public class SpaceUserRole implements Serializable {

    private static final long serialVersionUID = -8029645777964049445L;

    private String key;

    private String name;

    private List<String> permissions;

    private String description;
}
