package org.example.picturecloudbackend.controller.picture;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.picturecloudbackend.annotation.AuthCheck;
import org.example.picturecloudbackend.api.aliyun.AlibabaCloudApi;
import org.example.picturecloudbackend.api.aliyun.model.CreateImageOutPaintingTaskResponse;
import org.example.picturecloudbackend.api.aliyun.model.GetImageOutPaintingTaskResponse;
import org.example.picturecloudbackend.common.BaseResponse;
import org.example.picturecloudbackend.common.DeleteRequest;
import org.example.picturecloudbackend.common.ResultUtils;
import org.example.picturecloudbackend.constant.CacheConstant;
import org.example.picturecloudbackend.constant.UserConstant;
import org.example.picturecloudbackend.enums.PictureReviewStatusEnum;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.model.dto.picture.*;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.model.entity.Space;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.picture.PictureTagCategory;
import org.example.picturecloudbackend.model.vo.picture.PictureVO;
import org.example.picturecloudbackend.service.PictureService;
import org.example.picturecloudbackend.service.SpaceService;
import org.example.picturecloudbackend.service.UserService;
import org.example.picturecloudbackend.service.cache.PictureCache;
import org.example.picturecloudbackend.service.cache.PicturePageCache;
import org.example.picturecloudbackend.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RequestMapping("/picture")
@RestController
public class PictureController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private PicturePageCache picturePageCache;
    @Autowired
    private PictureCache pictureCache;
    @Autowired
    private AlibabaCloudApi alibabaCloudApi;

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

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User loginUser = userService.getLoginUser(req);
        Integer successCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(successCount, String.format("图片批量上传成功，批量大小为%d", successCount));
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Boolean res = pictureService.deletePicture(deleteRequest, req);
        return ResultUtils.success(res, "成功删除图片");
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest req) {
        // 1.检验参数
        ThrowUtils.throwIf(pictureUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = pictureUpdateRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "更新图片id为空");
        Long spaceId = pictureUpdateRequest.getSpaceId();
        // 2.转换实体类为dto
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        picture.setPicTags(JSONUtil.toJsonStr(pictureUpdateRequest.getPicTags()));
        // 数据校验
        pictureService.validPicture(picture);
        Picture pictureFromDb = pictureService.getById(id);
        ThrowUtils.throwIf(pictureFromDb == null, ErrorCode.NOT_FOUND_ERROR, "更新图片不存在");
        Long spaceIdFromDb = pictureFromDb.getSpaceId();
        ThrowUtils.throwIf(spaceId != null && spaceId != spaceIdFromDb, ErrorCode.PARAMS_ERROR, "更新图片不存在");
        User loginUser = userService.getLoginUser(req);
        pictureService.fillReviewParams(picture, loginUser);
        // 3.操作数据库
        boolean res = pictureService.updateById(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库更新图片失败");
        return ResultUtils.success(picture.getId(), "成功更新图片");
    }

    @PostMapping("/edit")
    public BaseResponse<Long> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(pictureEditRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long pictureId = pictureService.editPicture(pictureEditRequest, req);
        return ResultUtils.success(pictureId, "成功编辑图片");
    }

    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewerRequest pictureReviewerRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(pictureReviewerRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User loginUser = userService.getLoginUser(req);
        boolean res = pictureService.doPictureReview(pictureReviewerRequest, loginUser);
        return ResultUtils.success(res, "图片审核完成");
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "获取图片不存在");
        return ResultUtils.success(picture, "成功获取图片");
    }

    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(Long id, HttpServletRequest req) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "获取图片不存在");
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            User loginUser = userService.getLoginUser(req);
            pictureService.checkPictureAuth(loginUser, picture);
        }
        PictureVO pictureVO = pictureService.getPictureVO(picture);
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

    @Deprecated
    @PostMapping("/list/page/vo/withoutCache")
    public BaseResponse<IPage<PictureVO>> listPictureVOByPageWithoutCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "获取图片页面大小过大");
        // 判断空间权限
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId == null) {
            // 公共图库
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        } else {
            // 私有图库
            User loginUser = userService.getLoginUser(req);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            ThrowUtils.throwIf(spaceService.checkSpaceAuth(space, loginUser), ErrorCode.NOT_AUTH_ERROR, "用户无权限编辑空间");
            pictureQueryRequest.setSpaceId(spaceId);
        }
        QueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(pictureQueryRequest);
        IPage<Picture> picturePage = pictureService.page(new Page<>(current, size), queryWrapper);
        IPage<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage);
        return ResultUtils.success(pictureVOPage, "成功获取图片列表");
    }

    @Deprecated
    @PostMapping("/list/page/vo/deprecated")
    public BaseResponse<IPage<PictureVO>> listPictureVOByPageDeprecated(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "获取图片页面大小过大");
        // 普通用户只能看到审核通过数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询缓存
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String queryHashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = RedisUtils.getKey(CacheConstant.PICTURE_ZSET, queryHashKey);
        Page<PictureVO> pictureVOPageByCache = RedisUtils.get(cacheKey, Page.class);
//        String cacheValue = localCache.getIfPresent(cacheKey);
//        IPage<PictureVO> pictureVOPageByCache = JSONUtil.toBean(cacheValue, Page.class);
        if (pictureVOPageByCache != null) {
            return ResultUtils.success(pictureVOPageByCache, "成功获取图片列表");
        }
        // 查询数据库
        QueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(pictureQueryRequest);
        IPage<Picture> picturePage = pictureService.page(new Page<>(current, size), queryWrapper);
        IPage<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage);
        // 存入缓存
        RedisUtils.set(cacheKey, pictureVOPage, CacheConstant.TTL_5_MINUTES);
//        cacheValue = JSONUtil.toJsonStr(pictureVOPage);
//        localCache.put(cacheKey, cacheValue);
        return ResultUtils.success(pictureVOPage, "成功获取图片列表");
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<IPage<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "获取图片页面大小过大");
        // 普通用户只能看到审核通过数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询缓存
        IPage<PictureVO> pictureVOPage = picturePageCache.listPictureVOByPage(pictureQueryRequest);
        return ResultUtils.success(pictureVOPage, "成功获取图片列表");
    }

    @PostMapping("/out_painting/create_task")
    public BaseResponse<CreateImageOutPaintingTaskResponse> createPictureOutPaintingTask(PictureCreateOutPaintingTaskRequest pictureCreateOutPaintingTaskRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureCreateOutPaintingTaskRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User loginUser = userService.getLoginUser(request);
        CreateImageOutPaintingTaskResponse resp = pictureService.createPictureOutPaintingTask(pictureCreateOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(resp, "成功创建括图任务");
    }

    @PostMapping("/out_painting/get_task")
    public BaseResponse<GetImageOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId, HttpServletRequest request) {
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        GetImageOutPaintingTaskResponse resp = alibabaCloudApi.getImageOutPaintingTask(taskId);
        return ResultUtils.success(resp, "成功获取扩图图片");
    }

}
