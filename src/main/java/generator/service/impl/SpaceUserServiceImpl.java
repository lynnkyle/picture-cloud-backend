package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.picturecloudbackend.model.entity.SpaceUser;
import generator.service.SpaceUserService;
import org.example.picturecloudbackend.mapper.SpaceUserMapper;
import org.springframework.stereotype.Service;

/**
* @author kyle
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2026-04-01 20:51:36
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

}




