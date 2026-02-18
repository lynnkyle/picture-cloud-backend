package org.example.picturecloudbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.picturecloudbackend.constant.UploadConstant;
import org.example.picturecloudbackend.enums.PictureReviewStatusEnum;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.manager.FileManager;
import org.example.picturecloudbackend.manager.upload.FilePictureUpload;
import org.example.picturecloudbackend.manager.upload.PictureUploadTemplate;
import org.example.picturecloudbackend.manager.upload.UrlPictureUpload;
import org.example.picturecloudbackend.mapper.PictureMapper;
import org.example.picturecloudbackend.model.dto.file.UploadPictureResult;
import org.example.picturecloudbackend.model.dto.picture.PictureQueryRequest;
import org.example.picturecloudbackend.model.dto.picture.PictureReviewerRequest;
import org.example.picturecloudbackend.model.dto.picture.PictureUploadRequest;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.picture.PictureVO;
import org.example.picturecloudbackend.model.vo.user.UserVO;
import org.example.picturecloudbackend.service.PictureService;
import org.example.picturecloudbackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LinZeyuan
 * @description 针对表【picture(图片表)】的数据库操作Service实现
 * @createDate 2026-01-17 22:51:06
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private UserService userService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    /**
     * 上传图片
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
        // 判断更新或删除
        Long pictureId = Optional.ofNullable(pictureUploadRequest).map(PictureUploadRequest::getId).orElse(null);
        if (pictureId != null) {
            Picture pictureFromDb = this.getById(pictureId);
            ThrowUtils.throwIf(pictureFromDb == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人和管理员可编辑图片
            if (!hasWritePermission(pictureFromDb, loginUser)) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "用户无权限更新图片");
            }
        }
        // 上传图片,得到图片信息
        String uploadPrefix = String.format("%s/%s", UploadConstant.PUBLIC, loginUser.getId());
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
        picture.setUserId(loginUser.getId());
        // 操作数据库
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        fillReviewParams(picture, loginUser);
        boolean res = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库保存图片失败");
        return PictureVO.objToVo(picture);
    }

    /**
     * 图片校验(图片修改校验)
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
        String searchText = pictureQueryRequest.getSearchText();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
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
        Optional<String> optionalField = Optional.ofNullable(sortField).map(StringUtils::camelToUnderline);
        queryWrapper.orderBy(StrUtil.isNotBlank(optionalField.get()), sortOrder.equals("ascend"), optionalField.get());
        return queryWrapper;
    }

    /**
     * 图片审核
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
     * 图片写权限判断
     * @param picture
     * @param loginUser
     * @return
     */
    @Override
    public boolean hasWritePermission(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            return true;
        }
        return Objects.equals(picture.getUserId(), loginUser.getId());
    }

    /**
     * 审核参数填充
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

}




