package com.tanhua.server.utils;

import org.joda.time.DateTime;
import org.joda.time.Years;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 根据出生日期获取年龄
 */
public class GetAgeUtil {
    /**
     * 通过出生年月日获取年龄
     * @param yearMonthDay
     * @return
     */
    public static int getAge(String yearMonthDay){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date birthDay = sdf.parse(yearMonthDay);
            Years years = Years.yearsBetween(new DateTime(birthDay), DateTime.now());
            // 获取相关的年数
            return years.getYears();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}