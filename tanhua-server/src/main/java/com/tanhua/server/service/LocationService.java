package com.tanhua.server.service;


import com.aliyuncs.utils.StringUtils;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.vo.NearUserVo;
import com.tanhua.domain.vo.UserLocationVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.UserLocationApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 地理位置的服务
 */
@Service
public class LocationService {

    @Reference
    private UserLocationApi userLocationApi;

    @Reference
    private UserInfoApi userInfoApi;

    /**
     * 上报地理位置
     *
     * @param paramMap
     */
    public void reportLocation(Map<String, Object> paramMap) {
        Double latitude = (Double) paramMap.get("latitude");// 纬度
        Double longitude = (Double) paramMap.get("longitude");// 经度
        String addrStr = (String) paramMap.get("addrStr");
        userLocationApi.add(UserHolder.getUserId(), latitude, longitude, addrStr);
    }


    /**
     * 搜附近
     *
     * @param gender
     * @param distance
     * @return
     */
    public List<NearUserVo> searchNearBy(String gender, String distance) {
        // 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        // 调用api搜索附近
        List<UserLocationVo> locationList = userLocationApi.searchNearBy(distance, loginUserId);
        List<NearUserVo> voList = new ArrayList<>();
        // 遍历
        if (CollectionUtils.isNotEmpty(locationList)) {
            // 补全用户信息，转成NearUserVo
            for (UserLocationVo userLocationVo : locationList) {
                // 周围人信息
                Long userId = userLocationVo.getUserId();

                if (userId.longValue() != loginUserId.longValue()) {
                    // 不是登陆用户
                    UserInfo userInfo = userInfoApi.findById(userId);
                    // 条件过滤
                    if (StringUtils.isNotEmpty(gender)) {
                        if (!userInfo.getGender().equals(gender)) {
                            continue;
                        }
                    }
                    // 转成vo
                    NearUserVo vo = new NearUserVo();
                    BeanUtils.copyProperties(userInfo, vo);
                    vo.setUserId(userInfo.getId());
                    voList.add(vo);
                }

            }
        }
        return voList;
    }
}
