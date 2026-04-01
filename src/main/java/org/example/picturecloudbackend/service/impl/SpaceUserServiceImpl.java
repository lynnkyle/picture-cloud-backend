package org.example.picturecloudbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.mapper.SpaceUserMapper;
import org.example.picturecloudbackend.model.dto.spaceuser.SpaceUserAddRequest;
import org.example.picturecloudbackend.model.dto.spaceuser.SpaceUserQueryRequest;
import org.example.picturecloudbackend.model.entity.SpaceUser;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.space.SpaceVO;
import org.example.picturecloudbackend.service.SpaceService;
import org.example.picturecloudbackend.service.SpaceUserService;
import org.example.picturecloudbackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author kyle
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2026-04-01 20:40:37
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserService {

    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;

    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        Long userId = spaceUser.getUserId();
        Long spaceId = spaceUser.getSpaceId();
        if (add) {
            ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户ID为空");
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR, "空间ID为空");
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "");
        }
    }

    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        return null;
    }

    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        validSpaceUser(spaceUser, true);
        // 插入数据库
        boolean res = this.save(spaceUser);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "数据库操作异常");
        return spaceUser.getSpaceId();
    }

    @Override
    public SpaceVO getSpaceUserVO(SpaceUser spaceUser) {
        return null;
    }

    @Override
    public List<SpaceVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        return Collections.emptyList();
    }
}




