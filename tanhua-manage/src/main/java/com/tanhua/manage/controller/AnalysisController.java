package com.tanhua.manage.controller;

import com.tanhua.manage.service.AnalysisService;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import com.tanhua.manage.vo.AnalysisUsersVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页数据统计controller
 */
@RestController
@RequestMapping("/dashboard")
public class AnalysisController {
    
    @Autowired
    private AnalysisService analysisService;

    /**
     * 概要统计信息
     * @return
     */
    @GetMapping("/summary")
    public ResponseEntity getSummary(){
        // 调用业务查询
        AnalysisSummaryVo vo = analysisService.getSummary();
        return ResponseEntity.ok(vo);
    }

    /**
     * 新增、活跃用户、次日留存率
     */
    @GetMapping("/users")
    public ResponseEntity getUsersCount(@RequestParam(name = "sd") Long sd
            , @RequestParam("ed") Long ed
            , @RequestParam("type") Integer type){
        AnalysisUsersVo vo = analysisService.getUsersCount(sd,ed,type);
        return ResponseEntity.ok(vo);
    }
}
