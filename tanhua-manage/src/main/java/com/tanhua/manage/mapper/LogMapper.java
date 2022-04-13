package com.tanhua.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.manage.domain.Log;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LogMapper extends BaseMapper<Log> {

    /**
     * 过去?天活跃用户数
     * @param date
     * @return
     */
    @Select("select count(distinct user_id) from tb_log where log_time>#{date}")
    Integer countActiveUserAfterDate(String date);

    /**
     * 通过类型与日期统计用户数
     * @param dateStr
     * @return
     */
    @Select("select count(1) from tb_log where type='0102' and log_time = #{dateStr}")
    Integer countRegisteredByDate(@Param("dateStr") String dateStr);

    /**
     * 统计今日活跃数
     * @param today
     * @return
     */
    @Select("select count(distinct user_id) from tb_log where log_time = #{today}")
    Integer countActiveByDate(String today);

    /**
     * 统计今日登陆数
     * @param today
     * @return
     */
    @Select("select count(distinct user_id) from tb_log where type='0101' and log_time = #{today}")
    Integer countLoginByDate(String today);

    /**
     * 统计次日留存数
     * @param today
     * @param yesterday
     * @return
     */
    @Select("select count(1) From ( " +
            " select distinct user_id from tb_log where log_time=#{today} " +
            ") y,( " +
            " select distinct user_id from tb_log where log_time=#{yesterday} " +
            ") t where y.user_id=t.user_id ")
    Integer countNumRetention1d(@Param("today") String today, @Param("yesterday") String yesterday);
}
