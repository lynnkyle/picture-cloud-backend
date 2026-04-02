package org.example.picturecloudbackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间成员权限
 */
@Data
public class SpaceUserPermission implements Serializable {

    private static final long serialVersionUID = 6275139372180517689L;

    private String key;

    private String name;

    private String description;
}
