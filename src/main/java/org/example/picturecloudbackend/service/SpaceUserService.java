package org.example.picturecloudbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.picturecloudbackend.model.dto.space.SpaceAddRequest;
import org.example.picturecloudbackend.model.dto.space.SpaceQueryRequest;
import org.example.picturecloudbackend.model.dto.spaceuser.SpaceUserAddRequest;
import org.example.picturecloudbackend.model.dto.spaceuser.SpaceUserQueryRequest;
import org.example.picturecloudbackend.model.entity.Space;
import org.example.picturecloudbackend.model.entity.SpaceUser;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.space.SpaceVO;
import org.example.picturecloudbackend.model.vo.spaceuser.SpaceUserVO;

import java.util.List;

/**
 * @author kyle
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2026-04-01 20:40:37
 */
public interface SpaceUserService extends IService<SpaceUser> {
    /**
     * 空间成员校验(空间修改校验)
     *
     * @param spaceUser
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取查询空间
     *
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);
    /*=========================公共模块=============================*/

    /**
     * 创建空间成员
     *
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 获取空间成员包装类
     *
     * @param spaceUser
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser);

    /**
     * 分页获取空间成员包装类
     *
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
