package org.example.picturecloudbackend.service;

import org.example.picturecloudbackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.picturecloudbackend.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

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
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @return
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest req);

    /**
     * 获取登录用户
     * @param req
     * @return
     */
    User getLoginUser(HttpServletRequest req);


    /**
     * 用户注销
     * @param req
     * @return
     */
    Boolean userLogout(HttpServletRequest req);

    /**
     * 密码加密
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取用户脱敏信息
     * @param userFromDb
     * @return
     */
    LoginUserVO getLoginUserVO(User userFromDb);
}
