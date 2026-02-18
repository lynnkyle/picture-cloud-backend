package org.example.picturecloudbackend.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.picturecloudbackend.annotation.AuthCheck;
import org.example.picturecloudbackend.enums.UserRoleEnum;
import org.example.picturecloudbackend.exception.BusinessException;
import org.example.picturecloudbackend.exception.ErrorCode;
import org.example.picturecloudbackend.model.entity.User;
import org.example.picturecloudbackend.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    /**
     * 拦截所有被AuthCheck注解的方法
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        // 无需权限,放行
        if (mustRole == null) {
            return joinPoint.proceed();
        }
        UserRoleEnum mustRoleEnum = UserRoleEnum.getUserRoleEnumByValue(mustRole);
        // 获取当前登录用户
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        // 必须权限,放行
        UserRoleEnum userRoleEnum = UserRoleEnum.getUserRoleEnumByValue(loginUser.getUserRole());
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无用户权限");
        }
        // 管理员权限
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无管理员权限");
        }
        return joinPoint.proceed();
    }
}
