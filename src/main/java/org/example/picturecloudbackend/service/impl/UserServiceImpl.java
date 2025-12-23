package org.example.picturecloudbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.service.UserService;
import org.example.picturecloudbackend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author LinZeyuan
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-12-23 17:39:51
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




