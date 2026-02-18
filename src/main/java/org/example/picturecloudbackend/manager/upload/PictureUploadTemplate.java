package org.example.picturecloudbackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import org.example.picturecloudbackend.config.CosClientConfig;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.manager.CosManager;
import org.example.picturecloudbackend.model.dto.file.UploadPictureResult;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;

/**
 * 图片上传模板
 */
public abstract class PictureUploadTemplate<T> {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     * @param inputSource   文件
     * @param uploadPrefix  路径前缀(对应用户id)
     * @return
     */
    public UploadPictureResult uploadPicture(T inputSource, String uploadPrefix) {
        // 1.校验图片
        validPicture(inputSource);
        // 2.图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(inputSource);
        // 拼接文件上传路径, 而不是使用原始文件名称, 可以增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPrefix, uploadFileName);
        // 3.解析结果返回
        File file = null;
        try {
            // 3.1 创建临时文件(获取文件到服务器)
            file = File.createTempFile(uploadPath, null);
            // 3.2 上传对象到对象存储
            processTempFile(inputSource, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 3.3 获取图片信息, 封装返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            return buildResult(uploadPath, originalFilename, file, imageInfo);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败");
        } finally {
            // 3.4 临时文件清理
            deleteTempFile(file);
        }
    }

    /**
     * 封装返回结果
     * @param uploadPath
     * @param originalFilename
     * @param file
     * @param imageInfo
     * @return
     */
    private UploadPictureResult buildResult(String uploadPath, String originalFilename, File file, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setPicUrl(String.format("%s/%s", cosClientConfig.getHost(), uploadPath));
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        return uploadPictureResult;
    }

    /**
     * 文件校验
     * @param inputSource
     */
    protected abstract void validPicture(T inputSource);

    /**
     * 获取文件名
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(T inputSource);

    /**
     * 临时文件存储
     * @param inputSource
     * @param file
     */
    protected abstract void processTempFile(T inputSource, File file);

    /**
     * 临时文件清理
     * @param file
     */
    private void deleteTempFile(File file) {
        if (file == null) return;
        boolean isDelete = file.delete();
        ThrowUtils.throwIf(!isDelete, ErrorCode.SYSTEM_ERROR, "临时文件删除失败");
    }
}
