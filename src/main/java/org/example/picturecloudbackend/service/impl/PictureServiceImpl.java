package org.example.picturecloudbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.service.PictureService;
import org.example.picturecloudbackend.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
 * @author LinZeyuan
 * @description 针对表【picture(图片表)】的数据库操作Service实现
 * @createDate 2026-01-17 22:51:06
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

}




