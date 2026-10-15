package io.elasticore.blueprint.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.stereotype.Component;
import org.reflections.Reflections;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.Set;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "elasticore.schema.export", havingValue = "true", matchIfMissing = false)
public class SchemaExportRunner {

    @PostConstruct
    public void generateDDL() {
        StandardServiceRegistry registry = null;
        try {
            // 1. Create registry
            registry = new StandardServiceRegistryBuilder()
                    .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                    .applySetting("hibernate.hbm2ddl.auto", "none")
                    .build();

            // 2. Create metadata
            MetadataSources metadataSources = new MetadataSources(registry);

            // Scan and add Entity classes
            Reflections reflections = new Reflections("io.elasticore.blueprint");
            Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

            if (entities.isEmpty()) {
                System.out.println("WARNING: No Entity classes found. Please check the package path.");
                return;
            }

            for (Class<?> entity : entities) {
                metadataSources.addAnnotatedClass(entity);
                System.out.println("Entity added: " + entity.getSimpleName());
            }

            Metadata metadata = metadataSources.buildMetadata();

            // 3. Generate DDL and save to file
            String[] ddlStatements = metadata.getDatabase().getDefaultNamespace()
                    .getTables()
                    .stream()
                    .map(table -> table.getExportIdentifier())
                    .toArray(String[]::new);

            // Simple DDL export
            StringBuilder ddlBuilder = new StringBuilder();
            ddlBuilder.append("-- Generated DDL for Entities\n\n");

            // For full CREATE TABLE statements, more complex logic is needed
            // Here we only output basic table information
            metadata.getDatabase().getDefaultNamespace().getTables().forEach(table -> {
                ddlBuilder.append("-- Table: ").append(table.getName()).append("\n");
                table.getColumns().forEach(column -> {
                    ddlBuilder.append("-- Column: ").append(column.getName())
                            .append(" Type: ").append(column.getSqlType()).append("\n");
                });
                ddlBuilder.append("\n");
            });

            // Save to file
            try (Writer writer = new OutputStreamWriter(
                    new FileOutputStream("schema.sql"), java.nio.charset.StandardCharsets.UTF_8)) {
                String ddl = ddlBuilder.toString();
                System.out.println(ddl);
                writer.write(ddl);
            }

            System.out.println("schema.sql file generated successfully");
            System.out.println("Processed entity count: " + entities.size());

        } catch (IOException e) {
            System.err.println("File write error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("DDL generation error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (registry != null) {
                StandardServiceRegistryBuilder.destroy(registry);
            }
        }
    }
}