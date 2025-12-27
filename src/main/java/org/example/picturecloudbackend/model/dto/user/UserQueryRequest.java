package org.example.picturecloudbackend.model.dto.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.picturecloudbackend.common.PageRequest;

import java.io.Serializable;

/**
 * @author LinZeyuan
 * @description 用户查询请求
 * @createDate 2025-12-27 20:25
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 3165627386755267874L;
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户账户
     */
    private String userAccount;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色(0-普通用户,1-管理员)
     */
    private String userRole;

}
