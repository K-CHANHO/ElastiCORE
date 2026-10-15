package io.elasticore.blueprint;

import io.elasticore.springboot3.EnableElastiCore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import io.elasticore.annotation.*;


@ElastiCore
@EnableElastiCore
@EnableCaching
@SpringBootApplication
public class ElCoreBlueprintApp {

    public static void main(String[] args) {
        SpringApplication.run(ElCoreBlueprintApp.class, args);
    }


}
