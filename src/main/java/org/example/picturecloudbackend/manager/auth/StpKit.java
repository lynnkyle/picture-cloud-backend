package org.example.picturecloudbackend.manager.auth;

import cn.dev33.satoken.stp.StpLogic;
import org.springframework.stereotype.Component;

@Component
public class StpKit {
    /**
     * 团队空间会话对象
     */
    public static final String SPACE_TYPE = "space";

    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);
}
