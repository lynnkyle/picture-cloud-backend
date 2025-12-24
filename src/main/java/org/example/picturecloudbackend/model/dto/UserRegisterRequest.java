package org.example.picturecloudbackend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author LinZeyuan
 * @description 用户注册请求
 * @createDate 2025-12-24 21:20
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 8333048093599332943L;
    // 账号
    private String userAccount;
    // 密码
    private String userPassword;
    // 校验密码
    private String checkPassword;
}

