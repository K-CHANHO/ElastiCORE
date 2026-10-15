package io.elasticore.springboot3.bean;

import io.elasticore.runtime.port.DbmsService;
import io.elasticore.runtime.port.ExternalService;
import io.elasticore.springboot3.dbms.DbmsServiceProxyFactory;
import io.elasticore.springboot3.http.HttpServiceProxyFactory;
import org.reflections.Reflections;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

/**
 * Bean definition registrar that automatically discovers and registers port service interfaces.
 * This registrar scans for interfaces annotated with @ExternalService and @DbmsService
 * and creates proxy beans for dependency injection.
 * 
 * <p>The registrar creates dynamic proxies that handle method invocations by:</p>
 * <ul>
 *   <li>Converting @ExternalService interfaces to HTTP client proxies</li>
 *   <li>Converting @DbmsService interfaces to database query proxies</li>
 * </ul>
 * 
 * @author ElastiCore Framework
 * @since 1.0
 */
public class PortServiceRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware {

    private Environment environment;

    private static BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        PortServiceRegistrar.beanFactory = beanFactory;
    }



    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    private String getParentPackage(String packageName, int levelsUp) {
        String[] parts = packageName.split("\\.");
        if (parts.length <= levelsUp) {
            throw new IllegalArgumentException(
                    "Cannot navigate " + levelsUp + " levels up from package: " + packageName);
        }
        return String.join(".", java.util.Arrays.copyOf(parts, parts.length - levelsUp));
    }


    private String detectMainPackage() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            try {
                Class<?> cls = Class.forName(element.getClassName());
                if (cls.isAnnotationPresent(SpringBootApplication.class)) {
                    return cls.getPackage().getName();
                }
            } catch (ClassNotFoundException e) {

            }
        }

        Package pkg = Package.getPackage("com.gesa");
        if(pkg!=null) {
            return "com.gesa";
        }

        return "com.autosure";


        //throw new IllegalStateException("Could not detect main package");
    }



    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        //String currentPackage = this.getClass().getPackage().getName();
        //String basePackage = getParentPackage(currentPackage, 2);


        String basePackage = detectMainPackage();
        Reflections reflections = new Reflections(basePackage);

        Set<Class<?>> extPortServices = reflections.getTypesAnnotatedWith(ExternalService.class);
        // port
        for (Class<?> serviceClass : extPortServices) {
            GenericBeanDefinition definition = new GenericBeanDefinition();
            definition.setBeanClass(serviceClass);
            definition.setInstanceSupplier(() -> createExternalPortProxy(serviceClass));
            // definition.setInstanceSupplier(() ->
            // ServiceProxyFactory.createService(serviceClass));
            registry.registerBeanDefinition(serviceClass.getSimpleName(), definition);
        }

        // dbms
        Set<Class<?>> dbmsServices = reflections.getTypesAnnotatedWith(DbmsService.class);
        for (Class<?> serviceClass : dbmsServices) {
            GenericBeanDefinition definition = new GenericBeanDefinition();
            definition.setBeanClass(serviceClass);
            definition.setInstanceSupplier(() -> createDbmsPortProxy(serviceClass));
            // definition.setInstanceSupplier(() ->
            // ServiceProxyFactory.createService(serviceClass));
            registry.registerBeanDefinition(serviceClass.getSimpleName(), definition);
        }

    }

    private Object createExternalPortProxy(Class<?> serviceClass) {
        ExternalService externalService = serviceClass.getAnnotation(ExternalService.class);
        return HttpServiceProxyFactory.createService(serviceClass, externalService, beanFactory, environment);
    }

    private Object createDbmsPortProxy(Class<?> serviceClass) {
        DbmsService externalService = serviceClass.getAnnotation(DbmsService.class);
        return DbmsServiceProxyFactory.createService(serviceClass, externalService, beanFactory, environment);
    }

}
