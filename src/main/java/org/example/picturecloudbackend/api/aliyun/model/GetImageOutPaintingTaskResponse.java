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
public class GetImageOutPaintingTaskResponse {

    @Alias("request_id")
    private String requestId;

    private Output output;

    private Usage usage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Output {

        @Alias("task_id")
        private String taskId;

        @Alias("task_status")
        private String taskStatus;

        @Alias("submit_time")
        private String submitTime;

        @Alias("scheduled_time")
        private String scheduledTime;

        @Alias("end_time")
        private String endTime;

        @Alias("output_image_url")
        private String outputImageUrl;

        // 错误码
        private String code;
        // 错误信息
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {

        @Alias("image_count")
        private Integer imageCount;
    }
}