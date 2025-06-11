package com.dita.config;

import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ServletContext servletContext;

    /**
     * 외부 업로드 디렉토리 (application.properties에 app.upload.dir=/opt/eatometer/uploads 와 같이 설정)
     */
    @Value("${app.upload.dir}")
    private String uploadDir;

    public WebMvcConfig(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1) 톰캣 웹앱 내부 /uploads 실제 물리 경로 (예: webapps/ROOT/uploads/)
        String realPath = servletContext.getRealPath("/uploads/");
        // 2) 외부 uploads 디렉토리 (예: /opt/eatometer/uploads/)
        String externalPath = "file:" + uploadDir + "/";

        if (realPath != null) {
            // realPath 끝에 슬래시가 없을 수 있으니 보장
            if (!realPath.endsWith("/")) realPath += "/";
            registry
              .addResourceHandler("/uploads/**")
              // 순서대로 내부 → 외부 디렉토리를 탐색
              .addResourceLocations("file:" + realPath, externalPath);
        } else {
            // 톰캣 내부 경로를 못 찾았을 때도 외부 디렉토리 서빙
            registry
              .addResourceHandler("/uploads/**")
              .addResourceLocations(externalPath);
        }

        // 3) 클래스패스(static/images) 내부의 정적 이미지
        registry
          .addResourceHandler("/images/**")
          .addResourceLocations("classpath:/static/images/");
    }
}