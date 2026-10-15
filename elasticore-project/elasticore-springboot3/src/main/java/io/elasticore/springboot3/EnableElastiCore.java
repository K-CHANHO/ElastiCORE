package io.elasticore.springboot3;


import io.elasticore.springboot3.bean.ApplicationContextProvider;
import io.elasticore.springboot3.bean.DbTransactionGateway;
import io.elasticore.springboot3.bean.PortServiceRegistrar;
import io.elasticore.springboot3.bean.ElastiCoreBannerPrinter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;
import io.elasticore.springboot3.bean.*;

/**
 * Annotation to enable ElastiCore framework integration in Spring Boot applications.
 * 
 * <p>This annotation automatically imports and configures the necessary ElastiCore
 * components for the application, including:</p>
 * <ul>
 *   <li>{@link PortServiceRegistrar} - Registers port services for dependency injection</li>
 *   <li>{@link DbTransactionGateway} - Provides database transaction and query execution capabilities</li>
 *   <li>{@link ApplicationContextProvider} - Enables static access to the Spring ApplicationContext</li>
 * </ul>
 * 
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * @SpringBootApplication
 * @EnableElastiCore
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }
 * </pre>
 * 
 * @author ElastiCore Framework
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({PortServiceRegistrar.class, DbTransactionGateway.class, ApplicationContextProvider.class, ElastiCoreBannerPrinter.class})
public @interface EnableElastiCore {
}
