package org.example.picturecloudbackend.model.dto.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.io.Serializable;
import java.util.Date;

/**
 * @author LinZeyuan
 * @description 用户创建请求
 * @createDate 2025/12/26 15:09
 */
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = 6392684026581168685L;
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
}
