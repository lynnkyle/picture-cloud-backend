package org.example.picturecloudbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.picturecloudbackend.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author LinZeyuan
 * @description 针对表【user(用户表)】的数据库操作Mapper
 * @createDate 2025-12-23 17:39:51
 * @Entity org.example.picturecloudbackend.model.entity.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




