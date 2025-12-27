package org.example.picturecloudbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.picturecloudbackend.constant.UserConstant;
import org.example.picturecloudbackend.enums.UserRoleEnum;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.mapper.UserMapper;
import org.example.picturecloudbackend.model.dto.user.UserQueryRequest;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.LoginUserVO;
import org.example.picturecloudbackend.model.vo.UserVO;
import org.example.picturecloudbackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author LinZeyuan
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-12-23 17:39:51
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "请求参数为空");
        // 账号长度
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        // 密码长度
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        // 账号格式
        String validPattern = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        ThrowUtils.throwIf(!matcher.matches(), ErrorCode.PARAMS_ERROR, "用户账号格式不正确");
        // 校验用户密码
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        // 2.检查用户账号是否和数据库中的账号一致
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        long count = this.count(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在");
        // 3.密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4.插入数据
        User user = new User();
        user.setUserName(UserConstant.DEFAULT_USER_NAME);
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "注册失败,数据库保存用户失败");
        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest req) {
        // 1.校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "请求参数为空");
        // 账号长度
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        // 密码长度
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        // 账号格式
        String validPattern = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        ThrowUtils.throwIf(!matcher.matches(), ErrorCode.PARAMS_ERROR, "用户账号格式不正确");
        // 2.加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        // 3.查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User userFromDb = this.getOne(queryWrapper);
        ThrowUtils.throwIf(userFromDb == null || !encryptPassword.equals(userFromDb.getUserPassword()), ErrorCode.PARAMS_ERROR, "用户账户或密码错误");
        // 4.保存用户登录态
        req.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, userFromDb);
        return getLoginUserVO(userFromDb);
    }

    /**
     * 获取当前登录用户
     * @param req
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest req) {
        // 1.判断是否已经登录
        User currentUser = (User) req.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2.从数据库中查询 (追求性能可以注释, 返回上述结果)
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        return currentUser;
    }

    @Override
    public Boolean userLogout(HttpServletRequest req) {
        // 1.判断是否已经登录
        User currentUser = (User) req.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(currentUser == null, ErrorCode.OPERATION_ERROR, "用户未登录");
        // 2.移除登录态
        req.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 密码加密
     * @param userPassword
     * @return
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        return DigestUtils.md5DigestAsHex((UserConstant.MD5_SALT + userPassword).getBytes());
    }

    /**
     * 获取登录用户脱敏信息
     * @param userFromDb
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User userFromDb) {
        if (userFromDb != null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(userFromDb, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取用户脱敏信息
     * @param userFromDb
     * @return
     */
    @Override
    public UserVO getUserVO(User userFromDb) {
        if (userFromDb == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(userFromDb, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userFromDbList) {
        if(CollectionUtil.isEmpty(userFromDbList)){
            return new ArrayList<>();
        }
        return userFromDbList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR,"请求参数为空");
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "user_role", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userName), "user_name", userName);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "user_account", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "user_profile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
}




