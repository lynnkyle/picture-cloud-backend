package org.example.picturecloudbackend.api.aliyun;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.picturecloudbackend.api.aliyun.model.CreateImageOutPaintingTaskRequest;
import org.example.picturecloudbackend.api.aliyun.model.CreateImageOutPaintingTaskResponse;
import org.example.picturecloudbackend.api.aliyun.model.GetImageOutPaintingTaskResponse;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "alibaba.cloud.ai")
public class AlibabaCloudApi {
    // 【安全修复】移除硬编码 Key，请从 application.yml 读取
    private String apiKey;

    private static final String CREATE_OUT_PAINTING_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    private static final String GET_OUT_PAINTING_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    public CreateImageOutPaintingTaskResponse createImageOutPaintingTask(CreateImageOutPaintingTaskRequest req) {
        ThrowUtils.throwIf(req == null, ErrorCode.PARAMS_ERROR, "图像画面扩展异常");
        // API请求构造
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_URL)
                .header("X-DashScope-Async", "enable")
                .header("Authorization", String.format("Bearer %s", apiKey))
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(req));

        try (HttpResponse httpResponse = httpRequest.execute()) {
            // 【修复】即使 HTTP 状态码错误，也要打印响应体以便排查
            String respBody = httpResponse.body();
            log.info("创建任务响应：{}", respBody);

            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "图像画面扩展请求失败：" + respBody);
            }

            CreateImageOutPaintingTaskResponse resp = JSONUtil.toBean(respBody, CreateImageOutPaintingTaskResponse.class);
            // 【修复】修复日志占位符，并改为 error 级别
            if (resp.getOutput().getCode() != null) {
                log.error("图像画面扩展业务失败：{}", resp.getOutput().getMessage());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "图像画面扩展请求失败：" + resp.getOutput().getMessage());
            }
            return resp;
        } catch (Exception e) {
            // 【修复】不要吞掉异常，记录日志并抛出
            log.error("创建任务发生异常", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建任务异常：" + e.getMessage());
        }
    }

    public GetImageOutPaintingTaskResponse getImageOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "图像画面扩展异常");
        // API请求构造
        HttpRequest httpRequest = HttpRequest.get(String.format(GET_OUT_PAINTING_URL, taskId))
                .header("Authorization", String.format("Bearer %s", apiKey));

        try (HttpResponse httpResponse = httpRequest.execute()) {
            String respBody = httpResponse.body();
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "查询任务失败：" + respBody);
            }
            GetImageOutPaintingTaskResponse resp = JSONUtil.toBean(respBody, GetImageOutPaintingTaskResponse.class);
            if (resp.getOutput().getCode() != null) {
                log.error("查询任务业务失败：{}", resp.getOutput().getMessage());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "查询任务失败：" + resp.getOutput().getMessage());
            }
            return resp;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询任务发生异常", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "查询任务异常：" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // 注意：在 Spring 环境中应通过 @Autowired 注入，而不是 new
        // 这里为了测试方便，手动设置 Key (生产环境请移除)
        AlibabaCloudApi api = new AlibabaCloudApi();
        api.setApiKey("sk-f3af8b52fa6e47f7a975b806361697e9"); // 【测试用】请替换为新密钥，或从配置读取

        try {
            CreateImageOutPaintingTaskRequest req = new CreateImageOutPaintingTaskRequest();
            CreateImageOutPaintingTaskRequest.Input input = new CreateImageOutPaintingTaskRequest.Input();
            input.setImageUrl("https://help-static-aliyun-doc.aliyuncs.com/assets/img/zh-CN/7181881571/p826951.png");
            req.setInput(input);
            CreateImageOutPaintingTaskRequest.Parameters parameters = new CreateImageOutPaintingTaskRequest.Parameters();
            req.setParameters(parameters);

            // 1. 创建任务
            CreateImageOutPaintingTaskResponse taskResp = api.createImageOutPaintingTask(req);
            String taskId = taskResp.getOutput().getTaskId();
            System.out.println("任务创建成功，任务 ID: " + taskId);

            // 2. 轮询查询任务状态 (异步任务需要等待)
            GetImageOutPaintingTaskResponse result = null;
            int retry = 0;
            while (retry < 10) {
                Thread.sleep(2000); // 等待 2 秒
                result = api.getImageOutPaintingTask(taskResp.getRequestId());
                String status = result.getOutput().getTaskStatus();
                System.out.println("当前状态：" + status);

                if ("SUCCEEDED".equals(status)) {
                    break;
                } else if ("FAILED".equals(status)) {
                    throw new RuntimeException("任务执行失败：" + result.getOutput().getMessage());
                }
                retry++;
            }

            if (result != null && result.getOutput().getOutputImageUrl() != null) {
                System.out.println("最终图片 URL: " + result.getOutput().getOutputImageUrl());
            } else {
                System.out.println("未获取到图片 URL");
            }

        } catch (Exception e) {
            System.err.println("处理失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
