package com.tanhua.server.interceptor;

import com.tanhua.domain.db.User;
import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 统一处理token，转成User对象存入ThreadLocal
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/7
 */
@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    /**
     * 前置拦截
     * @param request
     * @param response
     * @param handler
     * @return true是放行，false阻止不能调用controller
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("method={},url={}",request.getMethod(),request.getRequestURI());
        //log.info("进入了统一处理token的拦截器==============={}",request.getRequestURI());
        // 获取token
        String token = request.getHeader("Authorization");
        //log.info("===========token=========== {}",token);
        if(StringUtils.isNotEmpty(token)) {
            // 如果有值
            User user = userService.getUserByToken(token);
            log.info("===loginUser=== {},{}",request.getRequestURI(),user);
            if(null != user) {
                //  存入ThreadLocal
                UserHolder.setUser(user);
                return true;
            }
        }
        // 如果没值, 返回 401 状态码
        response.setStatus(401);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }
}
