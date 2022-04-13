package com.tanhua.server.controller;

import com.tanhua.domain.vo.NearUserVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.TodayBestVo;
import com.tanhua.server.service.IMService;
import com.tanhua.server.service.LocationService;
import com.tanhua.server.service.TodayBestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 好友推荐controller
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/9
 */
@RestController
@RequestMapping("/tanhua")
public class
TodayBestController {

    @Autowired
    private TodayBestService todayBestService;

    @Autowired
    private IMService imService;

    @Autowired
    private LocationService locationService;

    /**
     * 今日佳人
     */
    @GetMapping("/todayBest")
    public ResponseEntity todayBest(){
        // 查询佳人，缘分值最高的
        TodayBestVo vo = todayBestService.queryTodayBest();
        return ResponseEntity.ok(vo);
    }

    /**
     * 首页推荐用户
     */
    @GetMapping("/recommendation")
    public ResponseEntity recommendList(RecommendUserQueryParam param){
        PageResult<TodayBestVo> pageResult = todayBestService.recommendList(param);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 佳人信息
     */
    @GetMapping("/{id}/personalInfo")
    public ResponseEntity personalInfo(@PathVariable Long id){
        TodayBestVo todayBestVo = todayBestService.personalInfo(id);
        return ResponseEntity.ok(todayBestVo);
    }

    /**
     * 查看陌生人问题
     */
    @GetMapping("/strangerQuestions")
    public ResponseEntity queryStrangerQuestion(Long userId){
        String question = todayBestService.queryStrangerQuestion(userId);
        return ResponseEntity.ok(question);
    }

    /**
     * 回复陌生人问题
     */
    @PostMapping("/strangerQuestions")
    public ResponseEntity replyStrangerQuestion(@RequestBody Map<String,Object> param){
        imService.replyStrangerQuestion(param);
        return ResponseEntity.ok(null);
    }

    /**
     * 搜附近
     * @param gender
     * @param distance
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity searchNearBy(@RequestParam(required=false) String gender,
                                       @RequestParam(defaultValue = "2000") String distance){
        List<NearUserVo> list = locationService.searchNearBy(gender, distance);
        return ResponseEntity.ok(list);
    }


    @GetMapping("/cards")
    public ResponseEntity list(){
        List<TodayBestVo> list = todayBestService.list();
        return ResponseEntity.ok(list);
    }

    /**
     * 交友：喜欢
     * @param userId
     * @return
     */
    @GetMapping("/{id}/love")
    public ResponseEntity<TodayBestVo> loveUser(@PathVariable("id") Long userId){
        todayBestService.loveUser(userId);
        return ResponseEntity.ok(null);
    }

    /**
     * 交友：不喜欢
     * @param userId
     * @return
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity<TodayBestVo> unloveUser(@PathVariable("id") Long userId){
        todayBestService.unloveUser(userId);
        return ResponseEntity.ok(null);
    }
}
