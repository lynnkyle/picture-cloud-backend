package org.example.picturecloudbackend.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.common.ImageProcessRequest;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.example.picturecloudbackend.config.CosClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key   文件名称(唯一键)
     * @param file  文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 文件名称(唯一键)
     * @return
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key 文件名称(唯一键)
     * @return
     */
    public void deleteObject(String key) {
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(cosClientConfig.getBucket(), key);
        cosClient.deleteObject(deleteObjectRequest);
    }

    /**
     * 上传并解析对象(附带文件信息)
     *
     * @param key   文件名称(唯一键)
     * @param file  文件
     * @return
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 数据万象
        PicOperations picOperations = new PicOperations();
        // 1. 原图信息
        picOperations.setIsPicInfo(1);
        // 2. 图片处理
        List<PicOperations.Rule> rules = new ArrayList<>();
        // 图片压缩
        String webpKey = String.format("%s.%s", FileUtil.mainName(key), "webp");
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setFileId(webpKey);
        compressRule.setRule("imageMogr2/format/webp");
        rules.add(compressRule);
        // 缩略图处理
        final long ONE_KB = 1024;
        if (file.length() > 2 * ONE_KB) {
            String thumbnailKey = String.format("%s_thumbnail.%s", FileUtil.mainName(key), FileUtil.getSuffix(key));
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256));
            rules.add(thumbnailRule);
        }
        picOperations.setRules(rules);
        //
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }
}

