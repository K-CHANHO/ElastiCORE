package io.elasticore.springboot3.bean;

import io.elasticore.base.Version;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Prints the ElastiCORE version banner at Spring Boot startup.
 * Uses System.out (like Spring Boot's own banner) for guaranteed visibility
 * regardless of logging configuration.
 * Activated via @EnableElastiCore annotation.
 */
public class ElastiCoreBannerPrinter implements ApplicationListener<ContextRefreshedEvent> {

    private static boolean printed = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!printed) {
            printed = true;
            System.out.println(Version.getBanner());
        }
    }
}
