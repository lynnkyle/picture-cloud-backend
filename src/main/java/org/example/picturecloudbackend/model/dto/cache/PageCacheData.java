package org.example.picturecloudbackend.model.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageCacheData {
    private List<Long> ids;
    private long total;
}
