package com.tanhua.server.config;

import com.tanhua.server.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>
 * springMVC配置文件
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/7
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    /**
     * 配置拦截器
     * <mvc:interceptors>
     *     <mvc:interceptor>
     *         <mvc:xxxxx ../>
     *         <bean class="...."></bean>
     *     </mvc:interceptor>
     * </mvc:interceptors>
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
            .addPathPatterns("/**") // 拦截所有
            //excludePathPatterns 不拦截的路径
            .excludePathPatterns("/user/login","/user/loginVerification");
    }
}
