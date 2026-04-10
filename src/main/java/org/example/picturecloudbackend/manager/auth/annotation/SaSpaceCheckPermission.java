package org.example.picturecloudbackend.manager.auth.annotation;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.annotation.AliasFor;
import org.example.picturecloudbackend.manager.auth.StpKit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SaCheckPermission(type = StpKit.SPACE_TYPE)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SaSpaceCheckPermission {

    @AliasFor(annotation = SaCheckPermission.class)
    String[] value() default {};

    @AliasFor(annotation = SaCheckPermission.class)
    SaMode mode() default SaMode.AND;

    @AliasFor(annotation = SaCheckPermission.class)
    String[] orRole() default {};
}
