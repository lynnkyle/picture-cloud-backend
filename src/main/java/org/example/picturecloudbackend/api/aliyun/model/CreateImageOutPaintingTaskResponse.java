package org.example.picturecloudbackend.api.aliyun.model;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateImageOutPaintingTaskResponse {

    @Alias("request_id")
    private String requestId;

    private Output output = new Output();

    // 错误码
    private String code;
    // 错误信息
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Output {

        @Alias("task_status")
        private String taskStatus;

        @Alias("task_id")
        private String taskId;
    }
}