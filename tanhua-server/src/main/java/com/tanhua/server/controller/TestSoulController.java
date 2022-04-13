package com.tanhua.server.controller;

import com.tanhua.domain.db.Questionnaire;
import com.tanhua.domain.db.UserAnswer;
import com.tanhua.domain.vo.UserReportVo;
import com.tanhua.server.service.TestSoulService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/testSoul")
public class TestSoulController {

    @Autowired
    private TestSoulService testSoulService;

    /**
     * 获取问卷信息
     * @return
     */
    @GetMapping
    public ResponseEntity getTests(){
        List<Questionnaire> list = testSoulService.findAll();
        return ResponseEntity.ok(list);
    }

    /**
     * 提交问卷答案
     * @param answers
     * @return
     */
    @PostMapping
    public ResponseEntity submit(@RequestBody Map<String,List<UserAnswer>> answers){
        String reportId = testSoulService.submit(answers.get("answers"));
        return ResponseEntity.ok(reportId);
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity getReport(@PathVariable("reportId") String reportId){
        UserReportVo vo = testSoulService.findReportById(reportId);
        return ResponseEntity.ok(vo);
    }


}
