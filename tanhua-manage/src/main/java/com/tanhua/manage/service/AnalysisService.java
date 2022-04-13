package com.tanhua.manage.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.exception.BusinessException;
import com.tanhua.manage.mapper.AnalysisByDayMapper;
import com.tanhua.manage.mapper.LogMapper;
import com.tanhua.manage.utils.ComputeUtil;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import com.tanhua.manage.vo.AnalysisUsersVo;
import com.tanhua.manage.vo.DataPointVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 统计分析业务类
 */
@Service
public class AnalysisService {

    @Autowired
    private LogMapper logMapper;

    @Autowired
    private AnalysisByDayMapper analysisByDayMapper;

    /**
     * 概要统计信息
     * @return
     */
    public AnalysisSummaryVo getSummary() {
        // 过去30天, 把当前时间减去30天。java.util.Calendar 日历操作
        String last30Day = DateUtil.offset(new Date(), DateField.DAY_OF_MONTH, -30).toDateStr();
        String last7Day = DateUtil.offset(new Date(), DateField.DAY_OF_MONTH, -7).toDateStr();
        String today = DateUtil.today();
        String yesterday = DateUtil.yesterday().toDateStr();

        //累计用户数 select sum(num_registered) from tb_analysis_by_day;
        Integer cumulativeUsers= analysisByDayMapper.totalUserCount();
        //过去30天活跃用户数
        // select count(distinct user_id) from tb_log where log_time>?;
        Integer activePassMonth=logMapper.countActiveUserAfterDate(last30Day);
        //过去7天活跃用户
        Integer activePassWeek=logMapper.countActiveUserAfterDate(last7Day);
        //今日新增用户数量
        // 今日新增、登录、活跃
        //select * From tb_analysis_by_day where record_date = '2021-03-20';
        //QueryWrapper<AnalysisByDay> queryWrapper = new QueryWrapper<>();
        //queryWrapper.eq("record_date",today);
        //AnalysisByDay analysisByDay = analysisByDayMapper.selectOne(queryWrapper);
        // 今天的数据
        AnalysisByDay todayData = analysisByDayMapper.findByDate(today);
        // 昨天的数据
        AnalysisByDay yesterdayData = analysisByDayMapper.findByDate(yesterday);
        Integer newUsersToday=todayData.getNumRegistered();
        //今日新增用户涨跌率，单位百分数，正数为涨，负数为跌
        // (今天-昨天)/昨天
        BigDecimal newUsersTodayRate= ComputeUtil.computeRate(newUsersToday.longValue(),yesterdayData.getNumRegistered().longValue());
        //今日登录次数
        Integer loginTimesToday=todayData.getNumLogin();
        //今日登录次数涨跌率，单位百分数，正数为涨，负数为跌
        BigDecimal loginTimesTodayRate=ComputeUtil.computeRate(loginTimesToday.longValue(),yesterdayData.getNumLogin().longValue());;
        //今日活跃用户数量
        Integer activeUsersToday=todayData.getNumActive();
        //今日活跃用户涨跌率，单位百分数，正数为涨，负数为跌
        BigDecimal activeUsersTodayRate=ComputeUtil.computeRate(activeUsersToday.longValue(),yesterdayData.getNumActive().longValue());

        AnalysisSummaryVo vo = new AnalysisSummaryVo();
        vo.setActiveUsersTodayRate(activeUsersTodayRate);
        vo.setLoginTimesTodayRate(loginTimesTodayRate);
        vo.setLoginTimesToday(loginTimesToday);
        vo.setNewUsersTodayRate(newUsersTodayRate);
        vo.setNewUsersToday(newUsersToday);
        vo.setActiveUsersToday(activeUsersToday);
        vo.setActivePassWeek(activePassWeek);
        vo.setActivePassMonth(activePassMonth);
        vo.setCumulativeUsers(cumulativeUsers);
        return vo;
    }

    /**
     * 新增、活跃用户、次日留存率
     * @param sd
     * @param ed
     * @param type
     * @return
     */
    public AnalysisUsersVo getUsersCount(Long sd, Long ed, Integer type) {
        String column = ""; // 要查询的列名
        // 根据类型来决定要查询的列
        switch (type){
            case 101: column="num_registered";break;
            case 102: column="num_active";break;
            case 103: column="num_retention1d";break;
            default:throw new BusinessException("参数格式不正确");
        }
        // 今年的是开始日期
        String thisYearStartDate = DateUtil.date(sd).toDateStr();
        // 今年的是结束日期
        String thisYearEndDate = DateUtil.date(ed).toDateStr();
        // 查询今年的数据
        List<DataPointVo> thisYearData = analysisByDayMapper.findBetweenDate(thisYearStartDate, thisYearEndDate,column);

        // 去年的是开始日期
        String lastYearStartDate = DateUtil.date(sd).offset(DateField.YEAR,-1).toDateStr();
        // 去年的是结束日期
        String lastYearEndDate = DateUtil.date(ed).offset(DateField.YEAR,-1).toDateStr();
        // 查询今年的数据
        List<DataPointVo> lastsYearData = analysisByDayMapper.findBetweenDate(lastYearStartDate, lastYearEndDate,column);
        // 构建返回的vo对象
        AnalysisUsersVo vo = new AnalysisUsersVo();
        vo.setThisYear(thisYearData);
        vo.setLastYear(lastsYearData);
        return vo;
    }

    /**
     * 统计数据任务
     */
    public void analysisData(){
        // 统计数据
        String today = DateUtil.today();
        String yesterday = DateUtil.yesterday().toDateStr();
        // 今天新注册用户数量 type=0102
        Integer numRegistered = logMapper.countRegisteredByDate(today);
        Integer numActive = logMapper.countActiveByDate(today);
        Integer numLogin = logMapper.countLoginByDate(today);
        Integer numRetention1d = logMapper.countNumRetention1d(today, yesterday);
        // 判断今天的数据是否已添加
        AnalysisByDay todayData = analysisByDayMapper.findByDate(today);
        boolean add = false; // 标记记录是否存在
        if(null == todayData) {
            // 没添加过，则插入数据
            todayData = new AnalysisByDay();
            todayData.setCreated(new Date());
            todayData.setRecordDate(new Date());
            add = true;
        }
        todayData.setNumActive(numActive);
        todayData.setNumLogin(numLogin);
        todayData.setNumRegistered(numRegistered);
        todayData.setNumRetention1d(numRetention1d);
        todayData.setUpdated(new Date());
        // 添加过了则更新数据
        if(add) {
            analysisByDayMapper.insert(todayData);
        }else{
            analysisByDayMapper.updateById(todayData);
        }
    }
}
