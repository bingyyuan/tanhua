package com.tanhua.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.vo.DataPointVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnalysisByDayMapper extends BaseMapper<AnalysisByDay> {

    /**
     * 总用户数
     * @return
     */
    @Select("select sum(num_registered) from tb_analysis_by_day")
    Integer totalUserCount();

    /**
     * 通过日期查询统计数据
     * @param today
     * @return
     */
    @Select("select * From tb_analysis_by_day where record_date = #{today}")
    AnalysisByDay findByDate(String today);

    /**
     * 通过日期范围查询统计数据
     * #{} ${} 两者的区别
     *  ? 点位符，prepared预编译，sql发给数据库
     *  ${} sql拼接 防止 sql注入
     * @param startDate
     * @param endDate
     * @param column
     * @return
     */
    @Select("select date_format(record_date,'%Y-%m-%d') title, ${column} amount From tb_analysis_by_day where record_date \n" +
            "between #{startDate} and #{endDate}")
    List<DataPointVo> findBetweenDate(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("column") String column);
}