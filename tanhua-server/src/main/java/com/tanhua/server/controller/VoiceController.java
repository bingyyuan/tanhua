package com.tanhua.server.controller;

import com.tanhua.domain.vo.VoiceVo;
import com.tanhua.server.service.VoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/peachblossom")
public class VoiceController {

    @Autowired
    private VoiceService voiceService;

    @GetMapping
    public ResponseEntity getVoiceMessage(){
      VoiceVo vo = voiceService.findOne();
      return ResponseEntity.ok(vo);
    }

    /**
     * 桃花传音：发送语音
     * @param soundFile
     * @return
     */
    @PostMapping
    public ResponseEntity sendVoiceMessage(MultipartFile soundFile){
        voiceService.save(soundFile);
        return ResponseEntity.ok(null);
    }
}
