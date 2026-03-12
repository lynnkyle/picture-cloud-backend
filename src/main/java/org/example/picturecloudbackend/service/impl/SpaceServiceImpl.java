package org.example.picturecloudbackend.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.picturecloudbackend.enums.SpaceLevelEnum;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.mapper.SpaceMapper;
import org.example.picturecloudbackend.model.dto.space.SpaceAddRequest;
import org.example.picturecloudbackend.model.dto.space.SpaceQueryRequest;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.model.entity.Space;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.vo.picture.PictureVO;
import org.example.picturecloudbackend.model.vo.space.SpaceVO;
import org.example.picturecloudbackend.model.vo.user.UserVO;
import org.example.picturecloudbackend.service.SpaceService;
import org.example.picturecloudbackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

/**
 * @author kyle
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-03-09 20:10:10
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {
    @Resource
    private UserService userService;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 1.填充参数默认值
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        if (StrUtil.isEmpty(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        fillSpaceBySpaceLevel(space);
        // 2.校验参数
        validSpace(space, true);
        // 3.检验权限，非管理员只能创建普通级别的空间
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权限创建指定级别空间");
        }
        // 4.控制同一用户只能创建一个私有空间（采用加锁+事务方式实现）
        // TODO: 本地锁优化 / 分布式锁
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            Long spaceId = transactionTemplate.execute(status -> {
                // 判断是否已有空间
                boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
                // 已有空间不能创建
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "一个用户只能创建一个空间");
                // 创建空间
                boolean res = this.save(space);
                ThrowUtils.throwIf(res, ErrorCode.OPERATION_ERROR, "数据库插入空间失败");
                return space.getId();
            });
            return spaceId;
        }
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        String spaceName = space.getSpaceName();
        SpaceLevelEnum spaceLevelEnumByValue = SpaceLevelEnum.getSpaceLevelEnumByValue(space.getSpaceLevel());
        // 1.创建时校验
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不可以为空");
        }
        // 2.修改数据校验
        ThrowUtils.throwIf(StrUtil.isNotBlank(spaceName) && spaceName.length() > 20, ErrorCode.PARAMS_ERROR, "空间名称过长");
        ThrowUtils.throwIf(spaceLevelEnumByValue == null, ErrorCode.PARAMS_ERROR, "空间级别不能为空");

    }

    @Override
    public SpaceVO getSpaceVO(Space space) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        Long userId = spaceVO.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public IPage<SpaceVO> getSpaceVOPage(IPage<Space> spacePage) {
        IPage<SpaceVO> pictureVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        List<Space> pictureList = spacePage.getRecords();
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        List<SpaceVO> pictureVOList = pictureList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1.查询关联
        Set<Long> userIdSet = pictureList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdToUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2.填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = userIdToUserListMap.get(userId).get(0);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        Long id = spaceQueryRequest.getId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Long userId = spaceQueryRequest.getUserId();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "pic_name", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "space_level", spaceLevel);
        Optional<String> optionalField = Optional.ofNullable(sortField).map(StringUtils::camelToUnderline);
        optionalField.ifPresent(s -> queryWrapper.orderBy(StrUtil.isNotBlank(s), sortOrder.equals("ascend"), s));
        return queryWrapper;
    }

    @Override
    public boolean hasWritePermission(Space space, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            return true;
        }
        return Objects.equals(space.getUserId(), loginUser.getId());
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnumByValue = SpaceLevelEnum.getSpaceLevelEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnumByValue != null) {
            long maxSize = Optional.ofNullable(space.getMaxSize()).orElse(spaceLevelEnumByValue.getMaxSize());
            space.setMaxSize(maxSize);
            long maxCount = Optional.ofNullable(space.getMaxCount()).orElse(spaceLevelEnumByValue.getMaxCount());
            space.setMaxCount(maxCount);
        }
    }
}




