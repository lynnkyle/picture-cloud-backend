package org.example.picturecloudbackend.common;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author kyle
 * @description 游标分页请求
 * @createDate 2026-03-01 12:13
 */

@Data
public class CursorPageRequest {
    // 普通分页位置
    private Integer current = 1;
    // 分页大小
    @Min(0)
    @Max(100)
    private Integer pageSize = 10;
    // 游标分页位置(当前游标翻页位置)
    private String cursor;

    public Page plusPage() {
        return new Page(this.current, this.pageSize, false);
    }

    public Boolean isFirstPage() {
        return StringUtils.isEmpty(cursor);
    }
}
