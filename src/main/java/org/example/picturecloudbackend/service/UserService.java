package org.example.picturecloudbackend.service;

import org.example.picturecloudbackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author LinZeyuan
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2025-12-23 17:39:51
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return 用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 密码加密
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);
}
