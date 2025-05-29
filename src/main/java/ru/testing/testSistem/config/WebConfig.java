package ru.testing.testSistem.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {        // настройка загрузки и отдачи файлов
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {     // Переопределяет стандартную конфигурацию обработки ресурсов.
        String uploadPath = Paths.get("").toAbsolutePath() + "/src/main/resources/static/" + uploadDir;     // формирование пути
        registry.addResourceHandler("/uploads/**")     //Настраивает обработку HTTP-запросов по пути /upload
                .addResourceLocations("file:" + uploadPath + "/")       //file: - указывает, что ресурсы находятся в файловой системе
                .setCacheControl(CacheControl.noCache());
    }
}