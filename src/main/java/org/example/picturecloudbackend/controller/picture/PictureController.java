package org.example.picturecloudbackend.controller.picture;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.picturecloudbackend.annotation.AuthCheck;
import org.example.picturecloudbackend.common.BaseResponse;
import org.example.picturecloudbackend.common.DeleteRequest;
import org.example.picturecloudbackend.common.ResultUtils;
import org.example.picturecloudbackend.constant.UserConstant;
import org.example.picturecloudbackend.enums.PictureReviewStatusEnum;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.model.dto.picture.*;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.picture.PictureTagCategory;
import org.example.picturecloudbackend.model.vo.picture.PictureVO;
import org.example.picturecloudbackend.service.PictureService;
import org.example.picturecloudbackend.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequestMapping("/picture")
@RestController
public class PictureController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;


    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureCategoryTag() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "建立", "创意");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory, "成功获取分类标签");
    }

    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(PictureUploadRequest pictureUploadRequest, @RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(pictureUploadRequest, multipartFile, loginUser);
        return ResultUtils.success(pictureVO, "成功上传图片");
    }

    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        String fileUrl = pictureUploadRequest.getFileUrl();
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(pictureUploadRequest, fileUrl, loginUser);
        return ResultUtils.success(pictureVO, "成功上传图片");
    }


    @PostMapping("/update")
    public BaseResponse<Long> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest req) {
        // 1.检验参数
        ThrowUtils.throwIf(pictureUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = pictureUpdateRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "更新图片id为空");
        // 2.转换实体类为dto
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        picture.setPicTags(JSONUtil.toJsonStr(pictureUpdateRequest.getPicTags()));
        Picture pictureFromDb = pictureService.getById(id);
        ThrowUtils.throwIf(pictureFromDb == null, ErrorCode.NOT_FOUND_ERROR, "更新图片不存在");
        User loginUser = userService.getLoginUser(req);
        if (!pictureService.hasWritePermission(pictureFromDb, loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "用户无权限更新图片");
        }
        pictureService.fillReviewParams(picture, loginUser);
        // 3.操作数据库
        boolean res = pictureService.updateById(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库更新图片失败");
        return ResultUtils.success(picture.getId(), "成功更新图片");
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "获取图片id为空");
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "获取图片不存在");
        return ResultUtils.success(picture, "成功获取图片");
    }

    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "获取图片id为空");
        Picture picture = pictureService.getById(id);
        PictureVO pictureVO = pictureService.getPictureVO(picture);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "获取图片不存在");
        return ResultUtils.success(pictureVO, "成功获取图片");
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<IPage<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        QueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(pictureQueryRequest);
        IPage<Picture> page = pictureService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(page, "成功获取图片列表");
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<IPage<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size >= 20, ErrorCode.PARAMS_ERROR, "获取图片页面大小过大");
        // 普通用户只能看到审核通过数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        QueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(pictureQueryRequest);
        IPage<Picture> picturePage = pictureService.page(new Page<>(current, size), queryWrapper);
        IPage<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage);
        return ResultUtils.success(pictureVOPage, "成功获取图片列表");
    }

    @PostMapping("/edit")
    public BaseResponse<Long> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(pictureEditRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        picture.setPicTags(JSONUtil.toJsonStr(pictureEditRequest.getPicTags()));
        picture.setEditTime(new Date());
        pictureService.validPicture(picture);
        Long id = pictureEditRequest.getId();
        Picture pictureFromDb = pictureService.getById(id);
        ThrowUtils.throwIf(pictureFromDb == null, ErrorCode.NOT_FOUND_ERROR, "编辑图片不存在");
        User loginUser = userService.getLoginUser(req);
        if (!pictureService.hasWritePermission(pictureFromDb, loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "用户无权限编辑图片");
        }
        pictureService.fillReviewParams(picture, loginUser);
        boolean res = pictureService.updateById(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库编辑图片失败");
        return ResultUtils.success(picture.getId(), "成功编辑图片");
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "删除图片id为空");
        Picture pictureFromDb = pictureService.getById(id);
        ThrowUtils.throwIf(pictureFromDb == null, ErrorCode.NOT_FOUND_ERROR, "删除图片不存在");
        User loginUser = userService.getLoginUser(req);
        if (!pictureService.hasWritePermission(pictureFromDb, loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "用户无权限删除图片");
        }
        boolean res = pictureService.removeById(id);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库删除图片失败");
        return ResultUtils.success(res, "成功删除用户");
    }

    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewerRequest pictureReviewerRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(pictureReviewerRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User loginUser = userService.getLoginUser(req);
        boolean res = pictureService.doPictureReview(pictureReviewerRequest, loginUser);
        return ResultUtils.success(res, "图片审核完成");
    }
}
