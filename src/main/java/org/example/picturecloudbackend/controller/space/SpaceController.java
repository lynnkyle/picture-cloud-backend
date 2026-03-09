package org.example.spacecloudbackend.controller.space;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.picturecloudbackend.annotation.AuthCheck;
import org.example.picturecloudbackend.common.BaseResponse;
import org.example.picturecloudbackend.common.DeleteRequest;
import org.example.picturecloudbackend.common.ResultUtils;
import org.example.picturecloudbackend.constant.CacheConstant;
import org.example.picturecloudbackend.constant.UserConstant;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.model.dto.space.*;
import org.example.picturecloudbackend.model.entity.Space;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.space.SpaceVO;
import org.example.picturecloudbackend.service.SpaceService;
import org.example.picturecloudbackend.service.UserService;
import org.example.picturecloudbackend.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/tag_category")

    public BaseResponse<SpaceTagCategory> listSpaceCategoryTag() {
        SpaceTagCategory spaceTagCategory = new SpaceTagCategory();
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "建立", "创意");
        spaceTagCategory.setTagList(tagList);
        spaceTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(spaceTagCategory, "成功获取分类标签");
    }


    @PostMapping("/update")
    public BaseResponse<Long> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest req) {
        // 1.检验参数
        ThrowUtils.throwIf(spaceUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = spaceUpdateRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "更新图片id为空");
        // 2.转换实体类为dto
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest, space);
        space.setPicTags(JSONUtil.toJsonStr(spaceUpdateRequest.getPicTags()));
        Space spaceFromDb = spaceService.getById(id);
        ThrowUtils.throwIf(spaceFromDb == null, ErrorCode.NOT_FOUND_ERROR, "更新图片不存在");
        User loginUser = userService.getLoginUser(req);
        if (!spaceService.hasWritePermission(spaceFromDb, loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "用户无权限更新图片");
        }
        spaceService.fillReviewParams(space, loginUser);
        // 3.操作数据库
        boolean res = spaceService.updateById(space);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库更新图片失败");
        return ResultUtils.success(space.getId(), "成功更新图片");
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "获取图片id为空");
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "获取图片不存在");
        return ResultUtils.success(space, "成功获取图片");
    }

    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "获取图片id为空");
        Space space = spaceService.getById(id);
        SpaceVO spaceVO = spaceService.getSpaceVO(space);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "获取图片不存在");
        return ResultUtils.success(spaceVO, "成功获取图片");
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<IPage<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int current = spaceQueryRequest.getCurrent();
        int size = spaceQueryRequest.getPageSize();
        QueryWrapper<Space> queryWrapper = spaceService.getQueryWrapper(spaceQueryRequest);
        IPage<Space> page = spaceService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(page, "成功获取图片列表");
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<IPage<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int current = spaceQueryRequest.getCurrent();
        int size = spaceQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "获取图片页面大小过大");
        // 普通用户只能看到审核通过数据
        spaceQueryRequest.setReviewStatus(SpaceReviewStatusEnum.PASS.getValue());
        // 查询缓存
        IPage<SpaceVO> spaceVOPage = spacePageCache.listSpaceVOByPage(spaceQueryRequest);
        return ResultUtils.success(spaceVOPage, "成功获取图片列表");
    }

    @PostMapping("/edit")
    public BaseResponse<Long> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(spaceEditRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Space space = new Space();
        BeanUtil.copyProperties(spaceEditRequest, space);
        space.setPicTags(JSONUtil.toJsonStr(spaceEditRequest.getPicTags()));
        space.setEditTime(new Date());
        spaceService.validSpace(space);
        Long id = spaceEditRequest.getId();
        Space spaceFromDb = spaceService.getById(id);
        ThrowUtils.throwIf(spaceFromDb == null, ErrorCode.NOT_FOUND_ERROR, "编辑图片不存在");
        User loginUser = userService.getLoginUser(req);
        if (!spaceService.hasWritePermission(spaceFromDb, loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "用户无权限编辑图片");
        }
        spaceService.fillReviewParams(space, loginUser);
        boolean res = spaceService.updateById(space);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库编辑图片失败");
        return ResultUtils.success(space.getId(), "成功编辑图片");
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "删除图片id为空");
        Space spaceFromDb = spaceService.getById(id);
        ThrowUtils.throwIf(spaceFromDb == null, ErrorCode.NOT_FOUND_ERROR, "删除图片不存在");
        User loginUser = userService.getLoginUser(req);
        if (!spaceService.hasWritePermission(spaceFromDb, loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "用户无权限删除图片");
        }
        boolean res = spaceService.removeById(id);
        // 清理图片资源
        spaceService.clearSpaceFile(spaceFromDb);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库删除图片失败");
        return ResultUtils.success(res, "成功删除用户");
    }

    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doSpaceReview(@RequestBody SpaceReviewerRequest spaceReviewerRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(spaceReviewerRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User loginUser = userService.getLoginUser(req);
        boolean res = spaceService.doSpaceReview(spaceReviewerRequest, loginUser);
        return ResultUtils.success(res, "图片审核完成");
    }

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadSpaceByBatch(@RequestBody SpaceUploadByBatchRequest spaceUploadByBatchRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(spaceUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User loginUser = userService.getLoginUser(req);
        Integer successCount = spaceService.uploadSpaceByBatch(spaceUploadByBatchRequest, loginUser);
        return ResultUtils.success(successCount, String.format("图片批量上传成功，批量大小为%d", successCount));
    }
}
