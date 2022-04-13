package com.tanhua.dubbo.api;

import com.tanhua.domain.vo.UserLocationVo;

import java.util.List;

public interface UserLocationApi {

    /**
     * 上报地理位置
     * @param userId
     * @param latitude
     * @param longitude
     * @param addrStr
     */
    void add(Long userId, Double latitude, Double longitude, String addrStr);

    /**
     * 搜附近
     * @param distance
     * @param loginUserId
     * @return
     */
    List<UserLocationVo> searchNearBy(String distance, Long loginUserId);
}
