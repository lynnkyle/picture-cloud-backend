package org.example.picturecloudbackend.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.picturecloudbackend.model.dto.picture.PictureQueryRequest;
import org.example.picturecloudbackend.model.dto.picture.PictureReviewerRequest;
import org.example.picturecloudbackend.model.dto.picture.PictureUploadRequest;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.picture.PictureVO;

/**
 * @author LinZeyuan
 * @description 针对表【picture(图片表)】的数据库操作Service
 * @createDate 2026-01-17 22:51:06
 */
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     * @param pictureUploadRequest
     * @param inputStream
     * @param loginUser
     * @return
     */
    <T> PictureVO uploadPicture(PictureUploadRequest pictureUploadRequest, T inputStream, User loginUser);

    /**
     * 图片校验(图片修改校验)
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 获取用户脱敏信息列表
     * @param picture
     * @return
     */
    PictureVO getPictureVO(Picture picture);

    /**
     * c列表
     * @param picturePage
     * @return
     */
    IPage<PictureVO> getPictureVOPage(IPage<Picture> picturePage);

    /**
     * 获取查询图片
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 图片审核
     * @param pictureReviewerRequest
     */
    boolean doPictureReview(PictureReviewerRequest pictureReviewerRequest, User loginUser);

    /**
     * 图片写权限判断
     * @param picture
     * @param loginUser
     * @return
     */
    boolean hasWritePermission(Picture picture, User loginUser);

    /**
     * 审核参数填充
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);
}
