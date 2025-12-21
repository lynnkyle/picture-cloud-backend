package org.example.picturecloudbackend.controller;

import org.example.picturecloudbackend.common.BaseResponse;
import org.example.picturecloudbackend.common.ResultUtils;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainController {
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success(ErrorCode.SUCCESS);
    }
}
