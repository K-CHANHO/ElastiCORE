package io.elasticore.base.model.loader.javasrc;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.elasticore.base.model.ConstantParam;
import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.core.Items;
import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.FileSource;
import io.elasticore.base.model.loader.ModelLoader;
import io.elasticore.base.model.loader.ModelLoaderContext;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JavaSrcEntityModelLoader extends AbstractModelLoader implements ConstantParam, ModelLoader<Entity> {


    @SneakyThrows
    public boolean loadModel(ModelLoaderContext ctx, FileSource source) {

        TypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        String code = new String(Files.readAllBytes(Paths.get(source.getFilepath())));
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);

        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDeclaration -> {
            loadModel(ctx, classDeclaration);
        });

        return false;
    }

    public boolean loadModel(ModelLoaderContext ctx, ClassOrInterfaceDeclaration classDeclaration) {
        System.out.println("class : " + classDeclaration.getName());


        String entityNm = classDeclaration.getNameAsString();

        Items<Field> items = Items.create(Field.class);

        classDeclaration.getFields().forEach(fieldDeclaration -> {

            Map<String, Annotation> annotationMap = new HashMap<>();


            // Check annotations defined on field
            if (fieldDeclaration.getAnnotations().isNonEmpty()) {

                for (AnnotationExpr annotation : fieldDeclaration.getAnnotations()) {

                    String annotationName = annotation.getName().asString().toLowerCase();
                    String annotationFullTxt = annotationName;

                    //System.out.println("Field annotation: " + annotationName);

                    Annotation annotation1 = Annotation.create(annotationName, annotationFullTxt, null);
                    annotationMap.put(annotationName, annotation1);
                }
            }

            // Check field comments
            fieldDeclaration.getComment().ifPresent(comment -> {
                Annotation annotation1 = Annotation.create("label", comment.getContent(), null);
                annotationMap.put("label", annotation1);
            });


            // Output field name and type
            fieldDeclaration.getVariables().forEach(variable -> {
                String fieldNm = variable.getName().asString();
                String type = variable.getType().asString();

                //System.out.println("  - " + fieldNm +": "+type);

                Field field = Field.builder().name(fieldNm)
                        .type(type)
                        .isPrimaryKey(annotationMap.containsKey("id"))
                        .unique(annotationMap.containsKey("unique"))
                        .annotationMap(annotationMap).build();

                items.addItem(field);
            });

        });

        Entity entity = Entity.create(ctx.getDomainId(), entityNm, null, items);
        ;

        ctx.getEntityItems().addItem(entity);

        return false;
    }


}
