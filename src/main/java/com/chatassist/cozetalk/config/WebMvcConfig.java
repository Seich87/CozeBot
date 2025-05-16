package com.chatassist.cozetalk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация Spring MVC для настройки статических ресурсов,
 * маршрутов и обработки представлений.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Настройка обработчиков статических ресурсов для админ-панели.
     * Этот метод добавляет пути к статическим ресурсам, таким как
     * CSS, JavaScript и изображения.
     *
     * @param registry Реестр обработчиков ресурсов
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Настройка обработки статических ресурсов
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // Добавляем обработчик для Webjars (Bootstrap, jQuery, и т.д.)
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * Настройка контроллеров представлений для страниц, которые не требуют
     * дополнительной логики, таких как страница входа.
     *
     * @param registry Реестр контроллеров представлений
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Перенаправление корневого пути на страницу входа для админ-панели
        registry.addViewController("/").setViewName("redirect:/admin/dashboard");

        // Настройка страницы входа
        registry.addViewController("/login").setViewName("login");

        // Страница ошибки
        registry.addViewController("/error").setViewName("error");

        // Страница успешной оплаты
        registry.addViewController("/payment/success").setViewName("payment-success");
    }
}