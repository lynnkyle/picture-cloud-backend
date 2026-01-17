package org.example.picturecloudbackend.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.picturecloudbackend.annotation.AuthCheck;
import org.example.picturecloudbackend.common.BaseResponse;
import org.example.picturecloudbackend.common.DeleteRequest;
import org.example.picturecloudbackend.common.ResultUtils;
import org.example.picturecloudbackend.constant.UserConstant;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.model.dto.user.*;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.LoginUserVO;
import org.example.picturecloudbackend.model.vo.UserVO;
import org.example.picturecloudbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author LinZeyuan
 * @description 用户控制器
 * @createDate 2026/1/13 12:00
 */
@RequestMapping("/user")
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long userId = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(userId, "用户注册成功");
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, req);
        return ResultUtils.success(loginUserVO, "用户登录成功");
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest req) {
        User loginUser = userService.getLoginUser(req);
        return ResultUtils.success(userService.getLoginUserVO(loginUser), "成功获取当前用户");
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest req) {
        boolean logout = userService.userLogout(req);
        return ResultUtils.success(logout, "用户退出成功");
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        user.setUserPassword(userService.getEncryptPassword(UserConstant.DEFAULT_USER_PASSWORD));
        boolean res = userService.save(user);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库插入用户失败");
        return ResultUtils.success(user.getId(), "成功添加用户");
    }

    /**
     * 根据id获取用户(仅管理员)
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ResultUtils.success(user, "成功获取用户");
    }

    /**
     * 根据id获取用户包装类
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<UserVO> getUserVOById(@RequestParam Long id) {
        BaseResponse<User> response = getUserById(id);
        User userFromDb = response.getData();
        UserVO userVO = userService.getUserVO(userFromDb);
        ThrowUtils.throwIf(userVO == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ResultUtils.success(userVO, "成功获取用户");
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        boolean res = userService.removeById(id);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库删除用户失败");
        return ResultUtils.success(res, "成功获取用户");
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean res = userService.updateById(user);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库更新用户失败");
        return ResultUtils.success(user.getId(), "成功更新用户");
    }

    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<IPage<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        QueryWrapper<User> queryWrapper = userService.getQueryWrapper(userQueryRequest);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        IPage<User> userPage = userService.page(new Page<>(current, pageSize), queryWrapper);
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        IPage<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage, "成功获取用户列表");
    }
}
