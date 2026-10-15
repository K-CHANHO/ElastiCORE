package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.model.*;
import io.elasticore.base.util.CodeTemplate;

/**
 * Handles the publishing of source files for entities.
 * This class is responsible for generating Java source code
 * based on entity definitions and their annotations.
 */
public class EtcSrcFilePublisher extends SrcFilePublisher {

    private CodePublisher publisher;


    /**
     * Initializes a new instance of the EntitySrcFilePublisher class.
     * Sets up the template path and packages based on the provided publisher.
     *
     * @param publisher The JPACodePublisher instance used for publishing.
     */
    public EtcSrcFilePublisher(CodePublisher publisher) {
        super(publisher);

        this.publisher = publisher;
    }

    /**
     * Generates and publishes Java source code for the specified entity.
     * This method orchestrates the creation of entity classes based on
     * entity definition and annotations.
     *
     * @param domain The model domain to which the entity belongs.
     */
    public void publish(ModelDomain domain) {
        ECoreModel model = publisher.getECoreModelContext().getDomain().getModel();

        if(model.getNamespace(ConstantParam.KEYNAME_ENTITY) == null)
            return;

        String packageNm = model.getNamespace(ConstantParam.KEYNAME_DTO);

        // Only generate PageableObject if custom pageable interface is not configured
        String customPageableInterface = model.getConfig("interface.pageable");
        if (customPageableInterface == null) {
            publishSrcFile(packageNm, "PageableObject", "template.etc.PageableObject", "java_PageableObject.tmpl");
        }

        // Only generate SortableObject if custom sortable interface is not configured
        String customSortableInterface = model.getConfig("interface.sortable");
        if (customSortableInterface == null) {
            publishSrcFile(packageNm, "SortableObject", "template.etc.SortableObject", "java_SortableObject.tmpl");
        }
    }

    private void publishSrcFile(String packageName, String className, String configPath, String defaultFileNm) {
        String templatePath = this.publisher.getECoreModelContext().getDomain().getModel().getConfig(configPath);
        if (templatePath == null)
            templatePath = "elasticore-template/"+defaultFileNm;
        else
            templatePath = "resource://" + templatePath;

        CodeTemplate javaClassTmpl = CodeTemplate.newInstance(templatePath);
        CodeTemplate.Parameters p = CodeTemplate.newParameters();
        p.set("packageName", packageName);

        String qualifiedClassName = packageName + "."+className;
        String code = javaClassTmpl.toString(p);

        this.writeSrcCode(this.publisher, null, qualifiedClassName, code);
    }


}
