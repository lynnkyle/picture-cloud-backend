package org.example.picturecloudbackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.picturecloudbackend.annotation.AuthCheck;
import org.example.picturecloudbackend.common.BaseResponse;
import org.example.picturecloudbackend.common.ResultUtils;
import org.example.picturecloudbackend.constant.UserConstant;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.manager.CosManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * @author LinZeyuan
 * @description 文件控制器
 * @createDate 2026/1/16 17:00
 */
@Slf4j
@RequestMapping("/file")
@RestController
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 测试文件上传
     *
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", fileName);
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath, file);
            return ResultUtils.success(filePath);
        } catch (Exception e) {
            log.error("file upload error, filepath={}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                boolean isDelete = file.delete();
                if (!isDelete) {
                    log.error("file delete error, filepath={}", filePath);
                }
            }
        }
    }
}
