package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.model.ConstantParam;
import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.dto.DataTransfer;
import io.elasticore.base.model.dto.DataTransferAnnotation;
import io.elasticore.base.util.CodeTemplate;

/**
 * Generates Views.java class for Jackson @JsonView support.
 * Only generates when at least one DTO in the domain uses @jsonview annotation
 * or when env.yml jsonview config is set to "true" or "auto".
 */
public class ViewsSrcFilePublisher extends SrcFilePublisher {

    private CodeTemplate viewsTemplate;
    private CodePublisher publisher;
    private ECoreModel eCoreModel;
    private String packageName;

    public ViewsSrcFilePublisher(CodePublisher publisher) {
        super(publisher);
        this.publisher = publisher;
        this.eCoreModel = publisher.getECoreModelContext().getDomain().getModel();
        this.viewsTemplate = CodeTemplate.newInstance("elasticore-template/views.tmpl");
        this.packageName = eCoreModel.getNamespace(ConstantParam.KEYNAME_DTO);
    }

    /**
     * Check if any DTO in the domain uses @jsonview annotation,
     * or if env.yml has jsonview config enabled.
     */
    public boolean hasJsonViewDTO(ModelDomain domain) {
        // Check env.yml config first
        String envConfig = eCoreModel.getConfig("jsonview", "false");
        if ("auto".equalsIgnoreCase(envConfig) || "true".equalsIgnoreCase(envConfig)) {
            return true;
        }

        // Check individual DTOs for @jsonview annotation
        ModelComponentItems<DataTransfer> dtos = domain.getModel()
                .getDataTransferModels().getItems();
        while (dtos.hasNext()) {
            DataTransfer dto = dtos.next();
            if (dto.getMetaInfo().hasMetaAnnotation(DataTransferAnnotation.META_JSONVIEW)) {
                String val = dto.getMetaInfo().getMetaAnnotationValue(DataTransferAnnotation.META_JSONVIEW);
                if (!"false".equalsIgnoreCase(val)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate Views.java in DTO package if any DTO uses @jsonview.
     */
    public void publish(ModelDomain domain) {
        if (packageName == null) return;
        if (!hasJsonViewDTO(domain)) return;

        CodeTemplate.Parameters params = CodeTemplate.newParameters();
        params.set("packageName", packageName);

        String code = viewsTemplate.toString(params);
        String qualifiedClassName = packageName + ".Views";

        this.writeSrcCode(this.publisher, null, qualifiedClassName, code);
    }
}
