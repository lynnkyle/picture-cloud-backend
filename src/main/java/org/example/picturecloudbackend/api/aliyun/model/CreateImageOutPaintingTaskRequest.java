package org.example.picturecloudbackend.api.aliyun.model;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateImageOutPaintingTaskRequest {

    private String model = "image-out-painting";
    private Input input = new Input();
    private Parameters parameters = new Parameters();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Input {
        @Alias("image_url")
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameters {
        private Integer angle = 90;
        @Alias("x_scale")
        private Double xScale = 1.5;
        @Alias("y_scale")
        private Double yScale = 1.5;
    }
}
