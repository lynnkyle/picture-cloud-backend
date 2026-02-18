package org.example.picturecloudbackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author kyle
 * @description URL图片上传
 * @createDate 2026-02-17 22:35
 */

@Service
public class UrlPictureUpload extends PictureUploadTemplate<String> {
    @Override
    protected void validPicture(String fileUrl) {
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

    @Override
    protected String getOriginalFilename(String fileUrl) {
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void processTempFile(String fileUrl, File file) {
        HttpUtil.downloadFile(fileUrl, file);
    }
}
