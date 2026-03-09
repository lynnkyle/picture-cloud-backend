package org.example.picturecloudbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.picturecloudbackend.model.dto.space.SpaceQueryRequest;
import org.example.picturecloudbackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.picturecloudbackend.model.vo.space.SpaceVO;

/**
 * @author kyle
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2026-03-09 20:10:10
 */
public interface SpaceService extends IService<Space> {
    /**
     * 空间校验(空间修改校验)
     * @param space
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取用户脱敏信息列表
     * @param space
     * @return
     */
    SpaceVO getSpaceVO(Space space);

    /**
     * 分页获取用户脱敏信息列表
     * @param spacePage
     * @return
     */
    IPage<SpaceVO> getSpaceVOPage(IPage<Space> spacePage);

    /**
     * 获取查询空间
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 填充空间
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);
}
