package org.example.picturecloudbackend.api.aliyun;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.example.picturecloudbackend.api.aliyun.model.CreateImageOutPaintingTaskRequest;
import org.example.picturecloudbackend.api.aliyun.model.CreateImageOutPaintingTaskResponse;
import org.example.picturecloudbackend.api.aliyun.model.GetImageOutPaintingTaskResponse;
import org.example.picturecloudbackend.common.ResultUtils;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "alibaba.cloud.ai")
public class AliYunApi {
    private String apiKey;
    private static final String CREATE_OUT_PAINTING_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    private static final String GET_OUT_PAINTING_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    // 创建任务
    public CreateImageOutPaintingTaskResponse createImageOutPaintingTask(CreateImageOutPaintingTaskRequest req) {
        ThrowUtils.throwIf(req == null, ErrorCode.PARAMS_ERROR, "图像画面扩展异常");
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_URL)
                .header("X-DashScope-Async", "enable")
                .header("Authorization", String.format("Bearer %s", apiKey))
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(req));
        try (HttpResponse httpResponse = httpRequest.execute()) {
            ThrowUtils.throwIf(httpResponse.isOk(), ErrorCode.OPERATION_ERROR, "图像画面扩展请求失败");
            CreateImageOutPaintingTaskResponse resp = JSONUtil.toBean(httpResponse.body(), CreateImageOutPaintingTaskResponse.class);
            if (resp.getCode() != null) {
                log.info("图像画面扩展请求失败", resp.getMessage());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "图像画面扩展请求失败");
            }
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 查询创建任务结果
    public GetImageOutPaintingTaskResponse getImageOutPaintingTask(CreateImageOutPaintingTaskResponse req) {
        ThrowUtils.throwIf(req == null, ErrorCode.PARAMS_ERROR, "图像画面扩展异常");
        HttpRequest httpRequest = HttpRequest.get(GET_OUT_PAINTING_URL)
                .header("Authorization", String.format("Bearer %s", apiKey))
                .body(JSONUtil.toJsonStr(req));
        try (HttpResponse httpResponse = httpRequest.execute()) {
            ThrowUtils.throwIf(!httpResponse.isOk(), ErrorCode.OPERATION_ERROR, "图像画面扩展请求失败");
            GetImageOutPaintingTaskResponse resp = JSONUtil.toBean(httpResponse.body(), GetImageOutPaintingTaskResponse.class);
            if (resp.getCode() != null) {
                log.info("图像画面扩展请求失败", resp.getMessage());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "图像画面扩展请求失败");
            }
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        CreateImageOutPaintingTaskRequest req = new CreateImageOutPaintingTaskRequest();
        req.getInput().setImageUrl("https://huarong123.oss-cn-hangzhou.aliyuncs.com/image/%E5%9B%BE%E5%83%8F%E7%94%BB%E9%9D%A2%E6%89%A9%E5%B1%95.png");
        AliYunApi api = new AliYunApi();
        CreateImageOutPaintingTaskResponse task = api.createImageOutPaintingTask(req);
        String taskId = task.getOutput().getTaskId();
        GetImageOutPaintingTaskResponse resp = api.getImageOutPaintingTask(task);
        String outputImageUrl = resp.getOutput().getOutputImageUrl();
        System.out.println(outputImageUrl);
    }
}
