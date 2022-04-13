package com.tanhua.manage.service;

import com.tanhua.manage.domain.Log;
import com.tanhua.manage.mapper.LogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    @Autowired
    private LogMapper logMapper;

    /**
     * 添加log日志
     * @param log
     */
    public void add(Log log) {
        logMapper.insert(log);
    }
}
