package com.tanhua.dubbo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.domain.db.Conclusion;

public interface ConclusionMapper extends BaseMapper<Conclusion> {
    Conclusion findByReportId(String reportId);
}
