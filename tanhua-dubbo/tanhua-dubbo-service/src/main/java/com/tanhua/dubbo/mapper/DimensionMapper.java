package com.tanhua.dubbo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.domain.db.Dimension;

import java.util.List;
import java.util.Map;

public interface DimensionMapper extends BaseMapper<Dimension> {
    /**
     * 通过报告id查询对应的维度值
     * @param reportId
     * @return
     */
    List<Map<String, String>> findByReportId(String reportId);

    /**
     * 计算并添加报告的维度值
     * @param reportId
     */
    void addReportDimension(String reportId);

    /**
     * 删除旧的维度值
     * @param reportIdIDb
     */
    void deleteReportDimension(String reportIdIDb);
}
