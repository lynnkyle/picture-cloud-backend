package org.example.picturecloudbackend.api.aliyun.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Input input;
    private Parameters parameters;

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
        private Integer angle;
        @Alias("x_scale")
        @JsonProperty("xScale")
        private Double xScale;
        @Alias("y_scale")
        @JsonProperty("yScale")
        private Double yScale;
    }
}
