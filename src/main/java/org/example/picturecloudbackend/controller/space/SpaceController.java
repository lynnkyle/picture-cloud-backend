package org.example.picturecloudbackend.controller.space;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.picturecloudbackend.annotation.AuthCheck;
import org.example.picturecloudbackend.common.BaseResponse;
import org.example.picturecloudbackend.common.DeleteRequest;
import org.example.picturecloudbackend.common.ResultUtils;
import org.example.picturecloudbackend.constant.UserConstant;
import org.example.picturecloudbackend.enums.SpaceLevelEnum;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.model.dto.space.SpaceAddRequest;
import org.example.picturecloudbackend.model.dto.space.SpaceEditRequest;
import org.example.picturecloudbackend.model.dto.space.SpaceQueryRequest;
import org.example.picturecloudbackend.model.dto.space.SpaceUpdateRequest;
import org.example.picturecloudbackend.model.entity.Space;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.space.SpaceVO;
import org.example.picturecloudbackend.service.SpaceService;
import org.example.picturecloudbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequestMapping("/space")
@RestController
public class SpaceController {

    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;

    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User loginUser = userService.getLoginUser(req);
        long spaceId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(spaceId, "成功创建空间");
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "删除空间id为空");
        Space spaceFromDb = spaceService.getById(id);
        ThrowUtils.throwIf(spaceFromDb == null, ErrorCode.NOT_FOUND_ERROR, "删除空间不存在");
        User loginUser = userService.getLoginUser(req);
        if (!spaceService.hasWritePermission(spaceFromDb, loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "用户无权限删除空间");
        }
        boolean res = spaceService.removeById(id);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库删除空间失败");
        return ResultUtils.success(res, "成功删除空间");
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest req) {
        // 1.检验参数
        ThrowUtils.throwIf(spaceUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = spaceUpdateRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "更新空间id为空");
        // 2.转换实体类为dto
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest, space);
        spaceService.fillSpaceBySpaceLevel(space);
        spaceService.validSpace(space, false);

        Space spaceFromDb = spaceService.getById(id);
        ThrowUtils.throwIf(spaceFromDb == null, ErrorCode.NOT_FOUND_ERROR, "更新空间不存在");
        // 3.操作数据库
        boolean res = spaceService.updateById(space);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库更新空间失败");
        return ResultUtils.success(space.getId(), "成功更新空间");
    }

    @PostMapping("/edit")
    public BaseResponse<Long> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(spaceEditRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Space space = new Space();
        BeanUtil.copyProperties(spaceEditRequest, space);
        space.setEditTime(new Date());
        spaceService.fillSpaceBySpaceLevel(space);
        spaceService.validSpace(space, false);
        Long id = spaceEditRequest.getId();
        Space spaceFromDb = spaceService.getById(id);
        ThrowUtils.throwIf(spaceFromDb == null, ErrorCode.NOT_FOUND_ERROR, "编辑空间不存在");
        User loginUser = userService.getLoginUser(req);
        if (!spaceService.hasWritePermission(spaceFromDb, loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "用户无权限编辑空间");
        }
        boolean res = spaceService.updateById(space);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库编辑空间失败");
        return ResultUtils.success(space.getId(), "成功编辑空间");
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "获取空间不存在");
        return ResultUtils.success(space, "成功获取空间");
    }

    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Space space = spaceService.getById(id);
        SpaceVO spaceVO = spaceService.getSpaceVO(space);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "获取空间不存在");
        return ResultUtils.success(spaceVO, "成功获取空间");
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<IPage<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int current = spaceQueryRequest.getCurrent();
        int size = spaceQueryRequest.getPageSize();
        QueryWrapper<Space> queryWrapper = spaceService.getQueryWrapper(spaceQueryRequest);
        IPage<Space> page = spaceService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(page, "成功获取空间列表");
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<IPage<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int current = spaceQueryRequest.getCurrent();
        int size = spaceQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "获取空间页面大小过大");
        // 查询数据库
        QueryWrapper<Space> queryWrapper = spaceService.getQueryWrapper(spaceQueryRequest);
        IPage<Space> page = spaceService.page(new Page<>(current, size), queryWrapper);
        IPage<SpaceVO> spaceVOPage = spaceService.getSpaceVOPage(page);
        return ResultUtils.success(spaceVOPage, "成功获取空间列表");
    }
}
