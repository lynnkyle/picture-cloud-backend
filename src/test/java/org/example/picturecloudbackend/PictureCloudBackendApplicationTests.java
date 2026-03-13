package org.example.picturecloudbackend;


import org.example.picturecloudbackend.mapper.PictureMapper;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.service.cache.PictureCache;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

@SpringBootTest
class PictureCloudBackendApplicationTests {

    @Resource
    private PictureCache pictureCache;

    @Resource
    private PictureMapper pictureMapper;

    @Test
    void contextLoads() {
        Picture picture = pictureMapper.selectById(999L);
        System.out.println(picture);
        Map<Long, Picture> map = pictureCache.getBatch(Arrays.asList(999L, 2020877402732498945L, 2020877585331523586L));
        System.out.println(map);
    }

}
