package com.tanhua.manage.jobs;

import cn.hutool.core.date.DateUtil;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.mapper.AnalysisByDayMapper;
import com.tanhua.manage.mapper.LogMapper;
import com.tanhua.manage.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 统计log日志，统计数据存入analysisByDate表
 */
@Component
@Slf4j
public class AnalysisJob {

    @Autowired
    private AnalysisService analysisService;

    @Scheduled(cron = "0/30 * * * * ?")
    public void doJob(){
        log.info("开始统计数据: " + DateUtil.now());
        analysisService.analysisData();
        log.info("统计数据完成: " + DateUtil.now());
    }
}
