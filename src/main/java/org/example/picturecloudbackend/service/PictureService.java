package org.example.picturecloudbackend.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.picturecloudbackend.api.aliyun.model.CreateImageOutPaintingTaskResponse;
import org.example.picturecloudbackend.common.DeleteRequest;
import org.example.picturecloudbackend.model.dto.picture.*;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.picture.PictureVO;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author LinZeyuan
 * @description 针对表【picture(图片表)】的数据库操作Service
 * @createDate 2026-01-17 22:51:06
 */
public interface PictureService extends IService<Picture> {

    /**
     * 图片校验(图片修改校验)
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 校验空间图片权限
     *
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 清理图片文件
     *
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 审核参数填充
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 获取查询图片
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);
    /*=========================公共模块=============================*/

    /**
     * 上传图片
     *
     * @param pictureUploadRequest
     * @param inputStream
     * @param loginUser
     * @return
     */
    <T> PictureVO uploadPicture(PictureUploadRequest pictureUploadRequest, T inputStream, User loginUser);

    /**
     * 批量上传图片(返回批量上传成功的图片数量)
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param req
     * @return
     */
    Boolean deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest req);

    /**
     * 编辑图片
     *
     * @param pictureEditRequest
     * @param req
     * @return
     */
    Long editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest req);

    /**
     * 图片审核
     *
     * @param pictureReviewerRequest
     */
    boolean doPictureReview(PictureReviewerRequest pictureReviewerRequest, User loginUser);

    /**
     * 获取图片脱敏信息列表
     *
     * @param picture
     * @return
     */
    PictureVO getPictureVO(Picture picture);

    /**
     * 分页获取图片脱敏信息列表
     *
     * @param picturePage
     * @return
     */
    IPage<PictureVO> getPictureVOPage(IPage<Picture> picturePage);
    /*=========================通用模块(增删改查)=============================*/

    /**
     * AI扩图任务
     * @param req
     * @param loginUser
     */
    CreateImageOutPaintingTaskResponse createPictureOutPaintingTask(PictureCreateOutPaintingTaskRequest req, User loginUser);
    /*=========================AI模块(增删改查)=============================*/
}

