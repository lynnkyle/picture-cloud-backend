package org.example.picturecloudbackend.common;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author kyle
 * @description 游标分页响应
 * @createDate 2026-03-01 12:14
 */

@Data
public class CursorPageResponse<T> {
    // 游标分页位置(下次游标翻页位置)
    private String cursor;

    // 是否最后一页
    private Boolean isLast = Boolean.FALSE;

    // 游标分页数据列表
    private List<T> list;

    public Boolean isEmpty() {
        return CollectionUtils.isEmpty(list);
    }

    public static <T> CursorPageResponse<T> empty() {
        CursorPageResponse<T> resp = new CursorPageResponse<>();
        resp.setIsLast(true);
        resp.setList(new ArrayList<>());
        return resp;
    }

    public static <T> CursorPageResponse<T> init(CursorPageResponse cursorPageResponse, List<T> list) {
        CursorPageResponse<T> resp = new CursorPageResponse<>();
        resp.setCursor(cursorPageResponse.getCursor());
        resp.setIsLast(cursorPageResponse.getIsLast());
        resp.setList(list);
        return resp;
    }
}
