package org.example.picturecloudbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.picturecloudbackend.api.aliyun.AlibabaCloudApi;
import org.example.picturecloudbackend.api.aliyun.model.CreateImageOutPaintingTaskRequest;
import org.example.picturecloudbackend.api.aliyun.model.CreateImageOutPaintingTaskResponse;
import org.example.picturecloudbackend.common.DeleteRequest;
import org.example.picturecloudbackend.constant.UploadConstant;
import org.example.picturecloudbackend.model.enums.PictureReviewStatusEnum;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.manager.CosManager;
import org.example.picturecloudbackend.manager.upload.FilePictureUpload;
import org.example.picturecloudbackend.manager.upload.PictureUploadTemplate;
import org.example.picturecloudbackend.manager.upload.UrlPictureUpload;
import org.example.picturecloudbackend.mapper.PictureMapper;
import org.example.picturecloudbackend.model.dto.file.UploadPictureResult;
import org.example.picturecloudbackend.model.dto.picture.*;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.model.entity.Space;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.picture.PictureVO;
import org.example.picturecloudbackend.model.vo.user.UserVO;
import org.example.picturecloudbackend.service.PictureService;
import org.example.picturecloudbackend.service.SpaceService;
import org.example.picturecloudbackend.service.UserService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LinZeyuan
 * @description 针对表【picture(图片表)】的数据库操作Service实现
 * @createDate 2026-01-17 22:51:06
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private CosManager cosManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private AlibabaCloudApi alibabaCloudApi;

    /**
     * 图片校验(图片修改校验)
     *
     * @param picture
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片信息不可以为空");
        Long id = picture.getId();
        String url = picture.getPicUrl();
        String intro = picture.getPicIntro();
        // 修改数据时, id不可以为空, 有参数才校验
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "图片id不可以为空");
        if (StrUtil.isNotBlank(url)) ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "图片url过长");
        if (StrUtil.isNotBlank(intro))
            ThrowUtils.throwIf(intro.length() > 1024, ErrorCode.PARAMS_ERROR, "图片简介过长");
    }

    /**
     * 校验空间图片权限
     *
     * @param loginUser
     * @param picture
     */
    @Deprecated
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库
            if (!Objects.equals(loginUser.getId(), picture.getUserId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "用户无权限操作图片");
            }
        } else {
            // 私有图库
            if (!Objects.equals(loginUser.getId(), picture.getUserId())) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "用户无权限操作图片");
            }
        }
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断图片（图片是否被多条记录使用）
        String picUrl = oldPicture.getPicUrl();
        long count = this.lambdaQuery().eq(Picture::getPicUrl, picUrl).count();
        if (count > 1) {
            return;
        }
        cosManager.deleteObject(picUrl);
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    /**
     * 审核参数填充
     *
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        picture.setReviewerId(loginUser.getId());
        picture.setReviewTime(new Date());
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动过审");
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * 获取查询图片
     *
     * @param pictureQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        Long id = pictureQueryRequest.getId();
        String picName = pictureQueryRequest.getPicName();
        String picIntro = pictureQueryRequest.getPicIntro();
        String picCategory = pictureQueryRequest.getPicCategory();
        List<String> picTags = pictureQueryRequest.getPicTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        Long userId = pictureQueryRequest.getUserId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        String searchText = pictureQueryRequest.getSearchText();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("pic_name", searchText).or().like("pic_intro", searchText));
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(picCategory), "pic_category", picCategory);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "pic_size", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "pic_width", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "pic_height", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "pic_scale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "space_id", spaceId);
        queryWrapper.isNull(ObjUtil.isEmpty(spaceId), "space_id");
        queryWrapper.like(StrUtil.isNotBlank(picName), "pic_name", picName);
        queryWrapper.like(StrUtil.isNotBlank(picIntro), "pic_intro", picIntro);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "pic_format", picFormat);
        queryWrapper.like(ObjUtil.isNotEmpty(reviewStatus), "review_status", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "review_message", reviewMessage);
        queryWrapper.like(ObjUtil.isNotEmpty(reviewerId), "reviewer_id", reviewerId);
        if (CollUtil.isNotEmpty(picTags)) {
            for (String tag : picTags) {
                queryWrapper.like("pic_tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "edit_time", startEditTime);
        queryWrapper.le(ObjUtil.isNotEmpty(endEditTime), "edit_time", endEditTime);
        Optional<String> optionalField = Optional.ofNullable(sortField).map(StringUtils::camelToUnderline);
        optionalField.ifPresent(s -> queryWrapper.orderBy(StrUtil.isNotBlank(s), sortOrder.equals("ascend"), s));
        return queryWrapper;
    }
    /*=========================公共模块=============================*/

    /**
     * 上传图片(仅管理员)
     *
     * @param inputStream
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    @Override
    public <T> PictureVO uploadPicture(PictureUploadRequest pictureUploadRequest, T inputStream, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(inputStream == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 校验权限
            ThrowUtils.throwIf(!spaceService.checkSpaceAuth(space, loginUser), ErrorCode.NOT_AUTH_ERROR, "用户无权限上传图片");
            // 校验额度
            ThrowUtils.throwIf(space.getTotalCount() >= space.getMaxCount(), ErrorCode.OPERATION_ERROR, "空间图片数量额度不足");
            ThrowUtils.throwIf(space.getTotalSize() >= space.getMaxSize(), ErrorCode.OPERATION_ERROR, "空间图片容量额度不足");
        }
        // 判断更新或删除
        Long pictureId = Optional.ofNullable(pictureUploadRequest).map(PictureUploadRequest::getId).orElse(null);
        Picture pictureFromDb = null;
        if (pictureId != null) {
            pictureFromDb = this.getById(pictureId);
            ThrowUtils.throwIf(pictureFromDb == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 校验空间是否一致
            Long spaceIdFromDb = pictureFromDb.getSpaceId();
            if (spaceId == null) {
                if (spaceIdFromDb != null) {
                    spaceId = spaceIdFromDb;
                }
            } else {
                ThrowUtils.throwIf(Objects.equals(spaceId, spaceIdFromDb), ErrorCode.PARAMS_ERROR, "用户更新图片上传空间不一致");
            }
        }
        // 上传图片,得到图片信息
        String uploadPrefix = String.format("%s/%s", UploadConstant.PUBLIC, loginUser.getId());
        if (spaceId != null) {
            uploadPrefix = String.format("%s/%s", UploadConstant.PRIVATE, spaceId);
        }
        PictureUploadTemplate pictureUploadTemplate = null;
        if (inputStream instanceof MultipartFile) {
            pictureUploadTemplate = filePictureUpload;
        } else if (inputStream instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        } else {
            throw new UnsupportedOperationException("文件上传方式未实现");
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputStream, uploadPrefix);
        Picture picture = new Picture();
        BeanUtil.copyProperties(uploadPictureResult, picture);
        picture.setSpaceId(spaceId); // 指定空间id
        String picName = pictureUploadRequest.getPicName();
        if (StrUtil.isNotBlank(picName)) {
            picture.setPicName(picName);
        }
        picture.setUserId(loginUser.getId());
        // 操作数据库
        // 新增/更新图片记录
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        fillReviewParams(picture, loginUser);
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            // 新增/更新图片记录
            boolean pictureRes = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!pictureRes, ErrorCode.OPERATION_ERROR, "数据库保存图片失败");
            // 更新空间额度记录
            if (finalSpaceId != null) {
                boolean spaceRes = spaceService.lambdaUpdate().eq(Space::getId, finalSpaceId).setSql("total_size = total_size + " + picture.getPicSize()).setSql("total_count = total_count + 1").update();
                ThrowUtils.throwIf(!spaceRes, ErrorCode.OPERATION_ERROR, "数据库更新空间失败");
            }
            return true;
        });
        if (pictureFromDb != null) {
            clearPictureFile(pictureFromDb);
        }
        return PictureVO.objToVo(picture);
    }

    /**
     * 批量上传图片(返回批量上传成功的图片数量)
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        // 1.校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        String prefixName = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(prefixName)) {
            prefixName = searchText;
        }
        ThrowUtils.throwIf(count > 5, ErrorCode.PARAMS_ERROR, "请求不得超过5条数据");
        // 2.提取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&first=%d&mmasync=1", searchText, RandomUtil.randomInt(1, 15));
        Document document = null;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面操作失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面操作失败");
        }
        // 3.解析内容(上传图片)
        Element dgControl = document.getElementsByClass("dgControl").first();
        ThrowUtils.throwIf(dgControl == null, ErrorCode.OPERATION_ERROR, "获取页面元素失败");
        Elements imgElementList = dgControl.select("img.mimg");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.error("图片链接为空, 已跳过:{}", fileUrl);
                continue;
            }
            //处理图片地址, 防止转义或者对象存储冲突问题
            int queryIndex = fileUrl.indexOf("?");
            if (queryIndex > -1) {
                fileUrl = fileUrl.substring(0, queryIndex);
            }
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setPicName(String.format("%s_%d", prefixName, uploadCount + 1));
            try {
                uploadPicture(pictureUploadRequest, fileUrl, loginUser);
                uploadCount++;
            } catch (Exception e) {
                log.info("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param req
     * @return
     */
    @Override
    public Boolean deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "删除图片id为空");
        Picture pictureFromDb = getById(id);
        ThrowUtils.throwIf(pictureFromDb == null, ErrorCode.NOT_FOUND_ERROR, "删除图片不存在");
        // 校验权限
//        User loginUser = userService.getLoginUser(req);
//        checkPictureAuth(loginUser, pictureFromDb);
        transactionTemplate.execute(status -> {
            // 删除图片记录
            boolean pictureRes = removeById(id);
            ThrowUtils.throwIf(!pictureRes, ErrorCode.OPERATION_ERROR, "数据库保存图片失败");
            // 更新空间额度记录
            boolean spaceRes = spaceService.lambdaUpdate().eq(Space::getId, pictureFromDb.getSpaceId()).setSql("total_size = total_size - " + pictureFromDb.getPicSize()).setSql("total_count = total_count - 1").update();
            ThrowUtils.throwIf(!spaceRes, ErrorCode.OPERATION_ERROR, "数据库更新空间失败");
            return true;
        });
        // 清理图片资源
        clearPictureFile(pictureFromDb);
        return true;
    }

    /**
     * 编辑图片
     *
     * @param pictureEditRequest
     * @param req
     * @return
     */
    @Override
    public Long editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest req) {
        ThrowUtils.throwIf(pictureEditRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        picture.setPicTags(JSONUtil.toJsonStr(pictureEditRequest.getPicTags()));
        picture.setEditTime(new Date());
        validPicture(picture);
        Long id = pictureEditRequest.getId();
        Picture pictureFromDb = getById(id);
        ThrowUtils.throwIf(pictureFromDb == null, ErrorCode.NOT_FOUND_ERROR, "编辑图片不存在");
        // 权限校验
        User loginUser = userService.getLoginUser(req);
//        checkPictureAuth(loginUser, pictureFromDb);
        fillReviewParams(picture, loginUser);
        boolean res = updateById(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库编辑图片失败");
        return picture.getId();
    }

    /**
     * 图片审核
     *
     * @param pictureReviewerRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean doPictureReview(PictureReviewerRequest pictureReviewerRequest, User loginUser) {
        ThrowUtils.throwIf(pictureReviewerRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = pictureReviewerRequest.getId();
        Integer reviewStatus = pictureReviewerRequest.getReviewStatus();
        PictureReviewStatusEnum pictureReviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewerRequest.getReviewMessage();
        if (ObjUtil.hasEmpty(id, pictureReviewStatusEnum) || Objects.equals(PictureReviewStatusEnum.REVIEWING, pictureReviewerRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数错误");
        }
        Picture pictureFromDb = this.getById(id);
        ThrowUtils.throwIf(pictureFromDb == null, ErrorCode.NOT_FOUND_ERROR, "审核图片不存在");
        ThrowUtils.throwIf(Objects.equals(pictureFromDb.getReviewStatus(), reviewStatus), ErrorCode.PARAMS_ERROR, "状态未发生改变");
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureFromDb, picture);
        picture.setReviewerId(loginUser.getId());
        picture.setReviewStatus(reviewStatus);
        picture.setReviewTime(new Date());
        picture.setReviewMessage(reviewMessage);
        boolean res = this.updateById(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库审核用户失败");
        return res;
    }

    /**
     * 获取用户脱敏信息
     *
     * @param picture
     * @return
     */
    @Override
    public PictureVO getPictureVO(Picture picture) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 获取用户脱敏信息列表
     *
     * @param picturePage
     * @return
     */
    @Override
    public IPage<PictureVO> getPictureVOPage(IPage<Picture> picturePage) {
        IPage<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        List<Picture> pictureList = picturePage.getRecords();
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1.查询关联
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdToUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2.填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = userIdToUserListMap.get(userId).get(0);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * AI扩图任务
     *
     * @param req
     * @param loginUser
     */
    @Override
    public CreateImageOutPaintingTaskResponse createPictureOutPaintingTask(PictureCreateOutPaintingTaskRequest req, User loginUser) {
        ThrowUtils.throwIf(req == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        // 获取图片信息
        Long pictureId = req.getPictureId();
        ThrowUtils.throwIf(pictureId == null, ErrorCode.PARAMS_ERROR, "图片id为空");
        Picture picture = this.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
        // 权限校验
//        checkPictureAuth(loginUser, picture);
        // 图像画面扩展任务
        CreateImageOutPaintingTaskRequest createImageOutPaintingTaskRequest = new CreateImageOutPaintingTaskRequest();
        CreateImageOutPaintingTaskRequest.Input input = new CreateImageOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getPicUrl());
        createImageOutPaintingTaskRequest.setInput(input);
        createImageOutPaintingTaskRequest.setParameters(req.getParameters());
        CreateImageOutPaintingTaskResponse resp = alibabaCloudApi.createImageOutPaintingTask(createImageOutPaintingTaskRequest);
        return resp;
    }
}




