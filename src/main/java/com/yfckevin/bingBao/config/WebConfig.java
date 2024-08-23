package com.yfckevin.bingBao.config;

import com.yfckevin.bingBao.ConfigProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final ConfigProperties configProperties;
    public WebConfig(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/bingBao_files/**").addResourceLocations("file:"+ configProperties.getFileSavePath());
        registry.addResourceHandler("/bingBao_images/**").addResourceLocations("file:"+ configProperties.getPicSavePath());
//        super.addResourceHandlers(registry);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        AntPathMatcher matcher = new AntPathMatcher();
        matcher.setCaseSensitive(false);
        configurer.setPathMatcher(matcher);
        configurer.setUseTrailingSlashMatch(true);
    }

}
