package org.example.picturecloudbackend.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import org.example.picturecloudbackend.constant.UserConstant;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.exception.ThrowUtils;
import org.example.picturecloudbackend.manager.auth.model.SpaceUserPermission;
import org.example.picturecloudbackend.manager.auth.model.enums.SpaceUserPermissionEnum;
import org.example.picturecloudbackend.model.entity.Picture;
import org.example.picturecloudbackend.model.entity.Space;
import org.example.picturecloudbackend.model.entity.SpaceUser;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.model.enums.SpaceLevelEnum;
import org.example.picturecloudbackend.model.enums.SpaceRoleEnum;
import org.example.picturecloudbackend.model.enums.SpaceTypeEnum;
import org.example.picturecloudbackend.service.PictureService;
import org.example.picturecloudbackend.service.SpaceService;
import org.example.picturecloudbackend.service.SpaceUserService;
import org.example.picturecloudbackend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component
public class StpInterfaceImpl implements StpInterface {
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private SpaceUserAuthManage spaceUserAuthManage;

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Override
    public List<String> getPermissionList(Object o, String loginType) {
        if (!Objects.equals(StpKit.SPACE_TYPE, loginType)) return new ArrayList<>();
        List<String> permissionByAdmin = spaceUserAuthManage.getPermissionByRole(SpaceRoleEnum.ADMIN.getValue());
        SpaceUserAuthContext authContext = getAuthContextByRequest();
        if (isAllFieldsNull(authContext)) return permissionByAdmin;
        // 1.获取用户ID
        User loginUser = (User) StpKit.SPACE.getSession().get(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_AUTH_ERROR, "用户未登录");
        // 2.从上下文获取SpaceUser对象
        Long userId = loginUser.getId();
        // 2.1 存在spaceUser
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (spaceUser != null) {
            return spaceUserAuthManage.getPermissionByRole(spaceUser.getSpaceRole());
        }
        // 2.2 存在spaceUserId
        Long spaceUserId = authContext.getSpaceUserId();
        if (spaceUserId != null) {
            spaceUser = spaceUserService.getById(spaceUserId);
            ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR, "空间用户信息不存在");
            SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                    .eq(SpaceUser::getUserId, userId).one();
            if (loginSpaceUser == null) return new ArrayList<>();
            return spaceUserAuthManage.getPermissionByRole(loginSpaceUser.getSpaceRole());
        }
        // 2.从上下文获取Space对象
        Long spaceId = authContext.getSpaceId();
        if (spaceId == null) {
            Long pictureId = authContext.getPictureId();
            if (pictureId == null) {
                return permissionByAdmin;
            }
            Picture picture = pictureService.lambdaQuery().eq(Picture::getId, pictureId).select(Picture::getId, Picture::getSpaceId, Picture::getUserId).one();
            ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片信息不存在");
            spaceId = picture.getSpaceId();
            // 公共图库
            if (spaceId == null) {
                if (Objects.equals(picture.getUserId(), userId) || userService.isAdmin(loginUser)) {
                    return permissionByAdmin;
                } else {
                    return spaceUserAuthManage.getPermissionByRole(SpaceRoleEnum.VIEWER.getValue());
                }
            }
        }
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间信息不存在");
        if (Objects.equals(SpaceTypeEnum.PRIVATE, space.getSpaceType())) {
            if (Objects.equals(space.getUserId(), userId) || userService.isAdmin(loginUser)) {
                return permissionByAdmin;
            } else {
                return new ArrayList<>();
            }
        } else {
            spaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, space.getId())
                    .eq(SpaceUser::getUserId, userId)
                    .one();
            if (spaceUser == null) {
                return new ArrayList<>();
            }
            return spaceUserAuthManage.getPermissionByRole(spaceUser.getSpaceRole());
        }
    }

    @Override
    public List<String> getRoleList(Object o, String loginType) {
        return new ArrayList<>();
    }

    private SpaceUserAuthContext getAuthContextByRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext authContext;
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            authContext = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authContext = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        Long id = authContext.getId();
        if (ObjUtil.isNotNull(id)) {
            // 获取请求路径业务前缀
            String requestURI = request.getRequestURI();
            String pathURI = requestURI.replace(String.format("%s/", contextPath), "");
            String moduleName = StrUtil.subBefore(pathURI, "/", false);
            switch (moduleName) {
                case "picture":
                    authContext.setPictureId(id);
                    break;
                case "space":
                    authContext.setSpaceId(id);
                    break;
                case "space_user":
                    authContext.setSpaceUserId(id);
                    break;
            }
        }
        return authContext;
    }

    /**
     * 判断所有字段是否为空
     * @param object
     * @return
     */
    private boolean isAllFieldsNull(Object object) {
        if (object == null) return false;
        return Arrays.stream(ReflectUtil.getFields(object.getClass())).map(field -> ReflectUtil.getFieldValue(object, field)).allMatch(ObjUtil::isEmpty);
    }
}
