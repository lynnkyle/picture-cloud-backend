package org.example.picturecloudbackend.controller.spaceuser;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import org.example.picturecloudbackend.annotation.AuthCheck;
import org.example.picturecloudbackend.common.BaseResponse;
import org.example.picturecloudbackend.common.DeleteRequest;
import org.example.picturecloudbackend.common.ResultUtils;
import org.example.picturecloudbackend.constant.UserConstant;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.manager.auth.annotation.SaSpaceCheckPermission;
import org.example.picturecloudbackend.manager.auth.model.constant.SpaceUserPermissionConstant;
import org.example.picturecloudbackend.model.dto.spaceuser.SpaceUserAddRequest;
import org.example.picturecloudbackend.model.dto.spaceuser.SpaceUserEditRequest;
import org.example.picturecloudbackend.model.dto.spaceuser.SpaceUserQueryRequest;
import org.example.picturecloudbackend.model.entity.SpaceUser;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.spaceuser.SpaceUserVO;
import org.example.picturecloudbackend.service.SpaceService;
import org.example.picturecloudbackend.service.SpaceUserService;
import org.example.picturecloudbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequestMapping("/space_user")
@RestController
public class SpaceUserController {

    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private SpaceUserService spaceUserService;


    @PostMapping("/add")
    @SaSpaceCheckPermission(value = {SpaceUserPermissionConstant.SPACE_USER_MANAGE})
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        long spaceId = spaceUserService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(spaceId, "成功创建空间成员");
    }

    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = {SpaceUserPermissionConstant.SPACE_USER_MANAGE})
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "删除空间成员id为空");
        SpaceUser spaceFromDb = spaceUserService.getById(id);
        ThrowUtils.throwIf(spaceFromDb == null, ErrorCode.NOT_FOUND_ERROR, "删除空间成员不存在");
        boolean res = spaceService.removeById(id);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库删除空间成员失败");
        return ResultUtils.success(res, "成功删除空间成员");
    }

    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = {SpaceUserPermissionConstant.SPACE_USER_MANAGE})
    public BaseResponse<Long> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest) {
        // 数据校验
        ThrowUtils.throwIf(spaceUserEditRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserEditRequest, spaceUser);
        spaceUserService.validSpaceUser(spaceUser, false);
        // 操作数据库
        boolean res = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库编辑空间成员失败");
        return ResultUtils.success(spaceUser.getId(), "成功编辑空间成员");
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<SpaceUser> getSpaceUserById(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        ThrowUtils.throwIf(ObjUtil.hasEmpty(spaceId, userId), ErrorCode.NOT_FOUND_ERROR, "获取空间不存在");
        // 查询数据库
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser, "成功获取空间成员");
    }

    @GetMapping("/get/vo")
    @SaSpaceCheckPermission(value = {SpaceUserPermissionConstant.SPACE_USER_MANAGE})
    public BaseResponse<SpaceUserVO> getSpaceUserVOById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        SpaceUser space = spaceUserService.getById(id);
        SpaceUserVO spaceVO = spaceUserService.getSpaceUserVO(space);
        ThrowUtils.throwIf(spaceVO == null, ErrorCode.NOT_FOUND_ERROR, "获取空间成员不存在");
        return ResultUtils.success(spaceVO, "成功获取空间成员");
    }

    /**
     * 查询空间成员列表
     *
     * @param spaceUserQueryRequest
     * @return
     */
    @PostMapping("/list/vo")
    @SaSpaceCheckPermission(value = {SpaceUserPermissionConstant.SPACE_USER_MANAGE})
    public BaseResponse<List<SpaceUserVO>> listSpaceUserVO(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        List<SpaceUserVO> spaceUserVOList = spaceUserService.getSpaceUserVOList(spaceUserList);
        return ResultUtils.success(spaceUserVOList, "成功获取空间成员列表");
    }

    /**
     * 查询加入的团队空间列表
     *
     * @param req
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<List<SpaceUserVO>> listTeamSpace(HttpServletRequest req) {
        User loginUser = userService.getLoginUser(req);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }
}
