package org.example.picturecloudbackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.model.dto.file.UploadPictureResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author kyle
 * @description 文件图片上传
 * @createDate 2026-02-17 22:35
 */

@Service
public class FilePictureUpload extends PictureUploadTemplate<MultipartFile> {

    @Override
    protected void validPicture(MultipartFile multipartFile) {
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

    @Override
    protected String getOriginalFilename(MultipartFile multipartFile) {
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processTempFile(MultipartFile multipartFile, File file) {
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "临时文件存储失败");
        }
    }
}
