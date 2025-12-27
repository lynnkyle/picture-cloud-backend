package org.example.picturecloudbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author LinZeyuan
 * @description 用户视图
 * @createDate 2025-12-27 20:42
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 6795999944597312956L;
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色(0-普通用户,1-管理员)
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;
}
