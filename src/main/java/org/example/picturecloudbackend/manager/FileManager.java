package org.example.picturecloudbackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import org.example.picturecloudbackend.config.CosClientConfig;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.model.dto.file.UploadPictureResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author kyle
 * @description 文件服务类(模板方法模式)
 * @createDate 2026-02-17 22:42
 */

@Deprecated
@Service
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     * @param multipartFile 文件
     * @param uploadPrefix  路径前缀(对应用户id)
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPrefix) {
        // 1.校验图片
        validPicture(multipartFile);
        // 2.图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        // 拼接文件上传路径, 而不是使用原始文件名称, 可以增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPrefix, uploadFileName);
        // 3.解析结果返回
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
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
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);
        }
    }

    /**
     * 上传图片(通过URL上传图片)
     * @param fileUrl   URL文件资源
     * @param uploadPrefix  路径前缀(对应用户id)
     */
    private UploadPictureResult uploadPictureByURL(String fileUrl, String uploadPrefix) {
        // 1.校验图片
        validPicture(fileUrl);
        // 2.图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = FileUtil.mainName(fileUrl);
        // 拼接文件上传路径, 而不是使用原始文件名称, 可以增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPrefix, uploadFileName);
        // 3.解析结果返回
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            HttpUtil.downloadFile(fileUrl, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
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
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);
        }
    }

    /**
     * 校验文件
     * @param multipartFile
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        //1. 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 2 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过2MB");
        //2. 校验文件后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件格式错误");
    }

    /**
     * 校验文件(通过Url校验文件)
     * @param fileUrl
     */
    private void validPicture(String fileUrl) {
        // 校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址为空");
        // 校验Url格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        // 校验Url协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "仅支持HTTP或者HTTPS协议的文件地址");
        // HEAD请求验证文件是否存在
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 文件大小, 类型校验
            String contentType = httpResponse.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                final List<String> ALLOW_CONTENT_TYPE = Arrays.asList("image/jpg", "image/jpeg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPE.contains(contentType), ErrorCode.PARAMS_ERROR, "文件格式错误");
            }
            String contentLength = httpResponse.header("Content-Length");
            try {
                long fileSize = Long.parseLong(contentLength);
                if (StrUtil.isNotBlank(contentLength)) {
                    final long ONE_MB = 1024 * 1024;
                    ThrowUtils.throwIf(fileSize > 2 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过2MB");
                }
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式异常");
            }
        } finally {
            // important
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }

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
