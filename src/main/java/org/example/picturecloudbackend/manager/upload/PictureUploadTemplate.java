package org.example.picturecloudbackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.Img;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import org.example.picturecloudbackend.config.CosClientConfig;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.manager.CosManager;
import org.example.picturecloudbackend.model.dto.file.UploadPictureResult;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * 图片上传模板
 */
public abstract class PictureUploadTemplate<T> {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private CosManager cosManager;

    /**
     * 等比例缩放图片
     *
     * @param file
     */
    private void scaleFile(File file, String suffix) {
        final int MIN_SIZE = 512;
        final int MAX_SIZE = 4096;
        try {
            BufferedImage src = ImageIO.read(file);
            ThrowUtils.throwIf(src == null, ErrorCode.OPERATION_ERROR, "图片读取失败");

            int width = src.getWidth();
            int height = src.getHeight();

            double ratio = (double) Math.max(width, height) / Math.min(width, height);
            ThrowUtils.throwIf(ratio > 8, ErrorCode.PARAMS_ERROR, "图片宽高比过于极端，请裁剪后重试");

            if (width >= MIN_SIZE && height >= MIN_SIZE && width <= MAX_SIZE && height <= MAX_SIZE) {
                return;
            }

            double scale = 1.0;
            if (width < MIN_SIZE || height < MIN_SIZE) {
                scale = Math.max(scale, (double) MIN_SIZE / Math.min(width, height));
            }
            if (width > MAX_SIZE || height > MAX_SIZE) {
                scale = Math.min(scale, (double) MAX_SIZE / Math.max(width, height));
            }

            int targetWidth = (int) Math.round(width * scale);
            int targetHeight = (int) Math.round(height * scale);

            ThrowUtils.throwIf(
                    targetWidth < MIN_SIZE || targetHeight < MIN_SIZE
                            || targetWidth > MAX_SIZE || targetHeight > MAX_SIZE,
                    ErrorCode.PARAMS_ERROR,
                    "图片尺寸调整后仍不符合要求，请更换图片重试"
            );

            BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.drawImage(src, 0, 0, targetWidth, targetHeight, null);
            } finally {
                g.dispose();
            }

            boolean success = ImageIO.write(scaled, suffix, file);
            ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "不支持的图片格式：" + suffix);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片缩放失败：" + e.getMessage());
        }
    }

    /**
     * 上传图片
     *
     * @param inputSource  文件
     * @param uploadPrefix 路径前缀(对应用户id)
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
            String suffix = FileUtil.getSuffix(originalFilename);
            if (StrUtil.isBlank(suffix)) {
                suffix = "png";
            } else {
                suffix = suffix.toLowerCase();
            }
            if (!CollUtil.newHashSet("jpg", "jpeg", "png", "bmp").contains(suffix)) {
                suffix = "jpg";
            }
            String prefix = "upload_";
            file = File.createTempFile(prefix, "." + suffix);
            // 3.2 上传对象到对象存储
            processTempFile(inputSource, file);
            scaleFile(file, suffix);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 3.3 获取图片信息, 封装返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 3.4 获取图片压缩处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                CIObject compressedCiObject = objectList.get(0);
                // 缩略图默认为压缩图
                CIObject thumbnailCiObject = compressedCiObject;
                if (objectList.size() > 1) {
                    thumbnailCiObject = objectList.get(1);
                }
                return buildResult(originalFilename, compressedCiObject, thumbnailCiObject);
            }
            return buildResult(originalFilename, uploadPath, file, imageInfo);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败");
        } finally {
            // 3.4 临时文件清理
            deleteTempFile(file);
        }
    }

    /**
     * 封装返回结果(原始图片)
     *
     * @param uploadPath
     * @param originalFilename
     * @param file
     * @param imageInfo
     * @return
     */
    private UploadPictureResult buildResult(String originalFilename, String uploadPath, File file, ImageInfo imageInfo) {
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
     * 封装返回结果(原始图片)
     *
     * @param originalFilename
     * @param compressedCiObject
     * @param thumbnailCiObject
     * @return
     */
    private UploadPictureResult buildResult(String originalFilename, CIObject compressedCiObject, CIObject thumbnailCiObject) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setPicUrl(String.format("%s/%s", cosClientConfig.getHost(), compressedCiObject.getKey()));
        uploadPictureResult.setThumbnailUrl(String.format("%s/%s", cosClientConfig.getHost(), thumbnailCiObject.getKey()));
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(Long.valueOf(compressedCiObject.getSize()));
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        return uploadPictureResult;
    }

    /**
     * 文件校验
     *
     * @param inputSource
     */
    protected abstract void validPicture(T inputSource);

    /**
     * 获取文件名
     *
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(T inputSource);

    /**
     * 临时文件存储
     *
     * @param inputSource
     * @param file
     */
    protected abstract void processTempFile(T inputSource, File file);

    /**
     * 临时文件清理
     *
     * @param file
     */
    private void deleteTempFile(File file) {
        if (file == null) return;
        boolean isDelete = file.delete();
        ThrowUtils.throwIf(!isDelete, ErrorCode.SYSTEM_ERROR, "临时文件删除失败");
    }
}
