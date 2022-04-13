package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.vo.UserLocationVo;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Service
public class UserLocationApiImpl implements UserLocationApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 上报地理位置
     *
     * @param userId
     * @param latitude
     * @param longitude
     * @param addrStr
     */
    @Override
    public void add(Long userId, Double latitude, Double longitude, String addrStr) {
        // 判断是否存在
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        long timeMillis = System.currentTimeMillis();
        // 地理位置
        // x=经度， y=纬度
        // 横坐标【（X轴），纬线方向】上标注的数值是地理经度；纵坐标【（Y轴），经线方向】上的数值是纬度。
        GeoJsonPoint jsonPoint = new GeoJsonPoint(longitude, latitude);
        if (mongoTemplate.exists(query, UserLocation.class)) {
            // 存在则更新
            Update update = new Update();
            update.set("updated",timeMillis);
            update.set("lastUpdated",timeMillis);

            update.set("location", jsonPoint);
            update.set("address",addrStr);
            mongoTemplate.updateFirst(query,update,UserLocation.class);
        }else {
            // 不存在，则添加
            UserLocation userLocation = new UserLocation();
            userLocation.setLocation(jsonPoint);
            userLocation.setUserId(userId);
            userLocation.setAddress(addrStr);
            userLocation.setCreated(timeMillis);
            userLocation.setUpdated(timeMillis);
            userLocation.setLastUpdated(timeMillis);
            mongoTemplate.insert(userLocation);
        }
    }

    /**
     * 搜附近
     * @param distance 单为米
     * @param loginUserId
     * @return
     */
    @Override
    public List<UserLocationVo> searchNearBy(String distance, Long loginUserId) {
        // 构建条件
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));
        // 登陆用户的坐标
        UserLocation loginUserLocation = mongoTemplate.findOne(query, UserLocation.class);

        Query locationQuery = new Query();
        // 半径 单位是米
        Distance radius = new Distance(Long.valueOf(distance)/1000, Metrics.KILOMETERS);

        // 创建圆形
        Circle circle = new Circle(loginUserLocation.getLocation(),radius);
        // 查询圆形范围内的数据
        locationQuery.addCriteria(Criteria.where("location").withinSphere(circle));
        // 在圆形内的用户
        List<UserLocation> userLocations = mongoTemplate.find(locationQuery, UserLocation.class);
        // 查询
        // 转成locationVo
        List<UserLocationVo> userLocationVos = UserLocationVo.formatToList(userLocations);
        return userLocationVos;
    }
}
