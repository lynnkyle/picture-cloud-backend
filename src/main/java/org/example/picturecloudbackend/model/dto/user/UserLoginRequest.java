package org.example.picturecloudbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author LinZeyuan
 * @description 用户登录请求
 * @createDate 2025-12-25 20:05
 */

@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 1137419670736374623L;
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户密码
     */
    private String userPassword;
}
