package org.example.picturecloudbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("org.example.picturecloudbackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class PictureCloudBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureCloudBackendApplication.class, args);
    }

}
