package com.yfckevin.bingBao.config;

import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.Interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final ConfigProperties configProperties;
    private final LoginInterceptor loginInterceptor;
    public WebConfig(ConfigProperties configProperties, LoginInterceptor loginInterceptor) {
        this.configProperties = configProperties;
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/bingBao_files/**").addResourceLocations("file:"+ configProperties.getFileSavePath());
        registry.addResourceHandler("/bingBao_images/**").addResourceLocations("file:"+ configProperties.getPicSavePath());
        registry.addResourceHandler("/ai_images/**").addResourceLocations("file:"+ configProperties.getAiPicSavePath());
        registry.addResourceHandler("/bingBao/**")
                .addResourceLocations("classpath:/static/");
//        super.addResourceHandlers(registry);
    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(loginInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/login.html")
//                .excludePathPatterns("/loginCheck")
//                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/webfonts/**");
//    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        AntPathMatcher matcher = new AntPathMatcher();
        matcher.setCaseSensitive(false);
        configurer.setPathMatcher(matcher);
        configurer.setUseTrailingSlashMatch(true);
    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**") // 匹配所有路徑
//                .allowedOrigins("*") // 允許所有來源（生產環境建議限制為特定域名）
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允許的請求方法
//                .allowedHeaders("*") // 允許所有請求頭
//                .allowCredentials(false) // 允許發送憑證（如Cookie）
//                .maxAge(3600); // 預檢請求的緩存時間（單位：秒）
//    }
//
}
