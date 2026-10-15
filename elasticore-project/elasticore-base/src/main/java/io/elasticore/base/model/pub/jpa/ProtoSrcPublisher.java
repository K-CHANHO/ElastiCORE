package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.CodePublisher;
import io.elasticore.base.ModelDomain;
import io.elasticore.base.model.ConstantParam;
import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.ModelComponentItems;
import io.elasticore.base.model.core.Items;
import io.elasticore.base.model.dto.DataTransfer;
import io.elasticore.base.model.dto.DataTransferAnnotation;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.entity.TypeInfo;
import io.elasticore.base.model.enums.EnumConstant;
import io.elasticore.base.model.enums.EnumModel;
import io.elasticore.base.model.port.Port;
import io.elasticore.base.model.repo.Method;
import io.elasticore.base.util.CodeTemplate;
import io.elasticore.base.util.JavaToProtobufTypeConverter;
import io.elasticore.base.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProtoSrcPublisher extends SrcFilePublisher {

    private CodeTemplate protoCodeTmpl;

    private CodeTemplate gRpcProviderCodeTmpl;

    private CodeTemplate gRpcClientCodeTmpl;


    private CodePublisher publisher;

    private String packageName;

    protected ECoreModel eCoreModel;

    private boolean isEnable = false;


    public ProtoSrcPublisher(CodePublisher publisher) {
        super(publisher);

        this.publisher = publisher;

        this.eCoreModel = this.publisher.getECoreModelContext().getDomain().getModel();

        String templatePath = eCoreModel.getConfig("template.proto");
        if (templatePath == null)
            templatePath = "elasticore-template/proto.tmpl";
        else
            templatePath = "resource://" + templatePath;

        this.protoCodeTmpl = CodeTemplate.newInstance(templatePath);


        String templatePath2 = eCoreModel.getConfig("template.gprc-svc-provider");
        if (templatePath2 == null)
            templatePath2 = "elasticore-template/grpc_svc_provider.tmpl";
        else
            templatePath2 = "resource://" + templatePath2;

        this.gRpcProviderCodeTmpl = CodeTemplate.newInstance(templatePath2);


        String templatePath3 = eCoreModel.getConfig("template.gprc-svc-client");
        if (templatePath3 == null)
            templatePath3 = "elasticore-template/grpc_svc_client.tmpl";
        else
            templatePath3 = "resource://" + templatePath3;

        this.gRpcClientCodeTmpl = CodeTemplate.newInstance(templatePath3);

        ECoreModel model = publisher.getECoreModelContext().getDomain().getModel();
        this.packageName = model.getNamespace(ConstantParam.KEYNAME_PROTO);

    }

    public void publish(ModelDomain domain) {
        ModelComponentItems<Port> portServices = this.eCoreModel.getPortModels().getItems();
        while (portServices.hasNext()) {
            Port port = portServices.next();
            publish(domain, port);
        }
    }


    public void publish(ModelDomain domain, Port portService) {

        if (this.packageName == null) return;

        String type = portService.getMeta().getMetaAnnotationValue("type");
        if (!"proto".equals(type))
            return;


        CodeTemplate.Parameters params = CodeTemplate.newParameters();

        String classNm = portService.getIdentity().getName();

        String desc = portService.getMeta().getLabel();

        params
                .set("protoPackageName", packageName)
                .set("protoServiceName", classNm);


        CodeTemplate.Paragraph fieldP = CodeTemplate.newParagraph();
        CodeTemplate.Paragraph methodP = CodeTemplate.newParagraph();
        Set<String> typeSet = loadRepositoryInfo(portService, fieldP, methodP);

        if (fieldP.size() == 0 && methodP.size() == 0)
            return;

        Set<EnumModel> enumModelSet = new HashSet<>();
        CodeTemplate.Paragraph msgP = CodeTemplate.newParagraph();
        makeMessage(typeSet, enumModelSet, msgP, "");


        CodeTemplate.Paragraph enumP = CodeTemplate.newParagraph();
        makeEnumeration(enumModelSet, enumP);

        params.set("protoFuncList", methodP);
        params.set("protoMsgList", msgP);
        params.set("protoEnumList", enumP);

        String code = protoCodeTmpl.toString(params);
        String qualifiedClassName = packageName + "." + classNm;

        this.writeSrcCode(this.publisher, null, "proto://"+qualifiedClassName, code);

        publishGrpcSvcProviderCode(domain, portService,packageName,classNm);
        publishGrpcSvcClientCode(domain, portService,packageName,classNm);
    }





    private String getProtoType(Field f) {
        try {
            TypeInfo typeInfo = f.getTypeInfo();
            if (typeInfo.isBaseType()) {
                return typeInfo.getBaseFieldType().getProtoType();
            } else {
                return typeInfo.getInitTypeInfo();
            }

        } catch (RuntimeException re) {
            return null;
        }
    }

    protected void makeEnumeration(Set<EnumModel> enumModels, CodeTemplate.Paragraph msgP) {
        if (enumModels == null || enumModels.size() == 0)
            return;


        EnumModel[] enumes = enumModels.toArray(new EnumModel[0]);

        for (EnumModel enumModel : enumes) {

            msgP.add("");
            msgP.add("enum %s {", enumModel.getIdentity().getName());

            Items<EnumConstant> enumConstantItems = enumModel.getEnumConstantItems();

            List<EnumConstant> itemList = enumConstantItems.getItemList();
            int seq = 0;
            for (EnumConstant item : itemList) {
                msgP.add("  %s = %s;", item.getIdentity().getName(), seq);
                seq++;
            }
            msgP.add("}");
        }
    }


    protected void makeMessage(Set<String> typeSet, Set<EnumModel> enumModels, CodeTemplate.Paragraph msgP, String spaceText) {
        if (typeSet == null || typeSet.size() == 0)
            return;

        String[] types = typeSet.toArray(new String[0]);

        if (spaceText == null)
            spaceText = "";

        for (String type : types) {

            msgP.add("");
            msgP.add(spaceText + "message %s {", type);



            DataTransfer dto = this.publisher.getECoreModelContext().findModelComponent(type, DataTransfer.class);
            if(dto==null) {

                if("stringList".equalsIgnoreCase(type)) {
                    msgP.add(spaceText + "  repeated string value = 1;");
                }
                else if("IntList ".equalsIgnoreCase(type)) {
                    msgP.add(spaceText + "  repeated int32 value = 1;");
                }
                else if("LongList ".equalsIgnoreCase(type)) {
                    msgP.add(spaceText + "  repeated int64 value = 1;");
                }
                else if("DoubleList ".equalsIgnoreCase(type)) {
                    msgP.add(spaceText + "  repeated double value = 1;");
                }
                else if("FloatList  ".equalsIgnoreCase(type)) {
                    msgP.add(spaceText + "  repeated float value = 1;");
                }
                else {
                    int p = type.lastIndexOf("List");
                    if(p>0) {
                        String genericTypeNm = type.substring(0,p);
                        msgP.add(spaceText + "  repeated %s value = 1;",genericTypeNm);
                    }

                }


                msgP.add(spaceText + "}");
                continue;
            }

            List<Field> list = dto.getAllFieldListMap().getList();



            Set<String> embededTypeSet = new HashSet<>();
            int seq = 1;
            for (Field f : list) {
                String typeName = null;
                TypeInfo typeInfo = f.getTypeInfo();
                if (typeInfo.isBaseType()) {
                    typeName = typeInfo.getBaseFieldType().getProtoType();
                }
                else if(f.getTypeInfo().isList()) {
                    typeName = f.getTypeInfo().getCoreItemType();

                    DataTransfer dto2 = this.publisher.getECoreModelContext().findModelComponent(typeName, DataTransfer.class);
                    if (dto2 == null) {
                        EnumModel enumModel = this.publisher.getECoreModelContext().findModelComponent(typeName, EnumModel.class);
                        if (enumModel != null) {
                            enumModels.add(enumModel);
                        }
                    } else {
                        embededTypeSet.add(typeName);
                    }

                    typeName = "repeated "+typeName;
                }
                else {
                    String initType = typeInfo.getInitTypeInfo();

                    JavaToProtobufTypeConverter.ProtobufTypeInfo protobufTypeInfo =
                            JavaToProtobufTypeConverter.convertJavaTypeToProtobuf(initType);;

                    typeName = protobufTypeInfo.getType();

                    if(protobufTypeInfo.getTypeList()!=null) {
                        for(String msgType:protobufTypeInfo.getTypeList()) {
                            embededTypeSet.add(msgType);
                        }
                    }



                    DataTransfer dto2 = this.publisher.getECoreModelContext().findModelComponent(typeName, DataTransfer.class);
                    if (dto2 == null) {
                        EnumModel enumModel = this.publisher.getECoreModelContext().findModelComponent(typeName, EnumModel.class);
                        if (enumModel != null) {
                            enumModels.add(enumModel);
                        }
                    } else {
                        embededTypeSet.add(typeName);
                    }
                }

                msgP.add(spaceText + "  %s %s = %s;", typeName, f.getName(), seq);
                seq++;
            }

            if (embededTypeSet.size() > 0) {
                makeMessage(embededTypeSet, enumModels, msgP, spaceText + "  ");
            }

            msgP.add(spaceText + "}");

        }

    }

    protected String geParamInfosForGRpc(Items<Field> fieldItems) {

        String result = geParamInfos(fieldItems);
        if(result==null)
            return "_Empty";
        return result;
    }

    protected String geParamInfos(Items<Field> fieldItems) {
        if(fieldItems!=null) {
            for (Field f : fieldItems.getItemList()) {
                return f.getTypeInfo()
                        .getDefaultTypeName();
            }
        }

        return null;
    }


    private Set<String> loadRepositoryInfo(Port portService, CodeTemplate.Paragraph fieldP, CodeTemplate.Paragraph methodP) {
        //private final ContactInfoRepository contactInfo;
        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
        ModelComponentItems<Method> methods = portService.getItems();

        Set<String> typeSet = new HashSet<>();

        while (methods.hasNext()) {
            Method method = methods.next();

            String returnType = method.getReturnType();
            Items<Field> fieldItems = method.getParams();

            String inputType = geParamInfosForGRpc(fieldItems);

            String methodName = method.getIdentity().getName();

            MetaInfo metaInfo = method.getMetaInfo();

            String desc = method.getDescription();
            if (!StringUtils.hasValue(desc)) {
                desc = methodName;
            }
            methodP.add("/*");
            methodP.add(StringUtils.addPrefixToEachLine(desc, " * "));
            methodP.add("*/");

            methodP.add("rpc %s (%s) returns (%s);", methodName, inputType, returnType);
            methodP.add("");

            typeSet.add(inputType);
            typeSet.add(returnType);
        }

        return typeSet;
    }

    public void publishGrpcSvcProviderCode(ModelDomain domain, Port portService, String packageName, String svcName) {

        if(!portService.getMeta().hasMetaAnnotation("server"))
            return;

        CodeTemplate.Parameters params = CodeTemplate.newParameters();

        params
                .set("packageName", packageName)
                .set("gRpcServiceName", svcName);

        CodeTemplate.Paragraph msgP = CodeTemplate.newParagraph();
        makeGrpcMethodForProvider(portService, msgP);
        params.set("gRpcMethodList", msgP);

        String code = gRpcProviderCodeTmpl.toString(params);
        String qualifiedClassName = packageName + "." + svcName+"Provider";
        this.writeSrcCode(this.publisher, null, "java://"+qualifiedClassName, code);
    }


    public void publishGrpcSvcClientCode(ModelDomain domain, Port portService, String packageName, String svcName) {
        if(!portService.getMeta().hasMetaAnnotation("client"))
            return;

        String providerName =  portService.getMeta().getMetaAnnotationValue("client");
        if(!StringUtils.hasValue(providerName)) {
            providerName=  StringUtils.uncapitalize(svcName); //"service";
        }

        CodeTemplate.Parameters params = CodeTemplate.newParameters();

        String configPropertyName = "grpc."+providerName;

        params
                .set("packageName", packageName)
                .set("gRpcServiceName", svcName)
                .set("gRpcServiceIp", "${"+configPropertyName+".ip:localhost}")
                .set("gRpcServicePort", "${"+configPropertyName+".port:9090}");

        CodeTemplate.Paragraph msgP = CodeTemplate.newParagraph();
        makeGrpcMethodForClient(portService, msgP);
        params.set("gRpcMethodList", msgP);

        String code = gRpcClientCodeTmpl.toString(params);
        String qualifiedClassName = packageName + "." + svcName+"Client";
        this.writeSrcCode(this.publisher, null, "java://"+qualifiedClassName, code);

    }



    private void makeGrpcMethodForProvider(Port portService, CodeTemplate.Paragraph methodP) {
        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
        ModelComponentItems<Method> methods = portService.getItems();

        while (methods.hasNext()) {
            Method method = methods.next();

            Items<Field> fieldItems = method.getParams();
            String inputType = geParamInfosForGRpc(fieldItems);
            String returnType = method.getReturnType();

            String rpcInputType = toRpcName(inputType);
            String rpcReturnType = toRpcName(returnType);

            String methodName = method.getIdentity().getName();
            MetaInfo metaInfo = method.getMetaInfo();
            String desc = method.getDescription();
            if (!StringUtils.hasValue(desc)) {
                desc = methodName;
            }
            methodP.add("/*");
            methodP.add(StringUtils.addPrefixToEachLine(desc, " * "));
            methodP.add("*/");

            methodP.add("public void  %s(%s request, StreamObserver<%s> responseObserver) {", methodName, inputType, returnType);
            methodP.add("    // TODO: implement for gRPC service");
            methodP.add("    %s.Builder builder = %s.newBuilder();", rpcReturnType,rpcReturnType);
            methodP.add("    //Mapper.of(output, builder).execute();");
            methodP.add("    %s response = builder.build();",rpcReturnType);
            methodP.add("    responseObserver.onNext(response);");
            methodP.add("    responseObserver.onCompleted();");
            methodP.add("}");
        }
    }

    private void makeGrpcMethodForClient(Port portService, CodeTemplate.Paragraph methodP) {
        CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
        ModelComponentItems<Method> methods = portService.getItems();

        while (methods.hasNext()) {
            Method method = methods.next();

            Items<Field> fieldItems = method.getParams();

            String inputType = geParamInfos(fieldItems);
            String returnType = method.getReturnType();

            boolean isProtoInput = isProtoObject(inputType);
            boolean isProtoOutput = isProtoObject(returnType);

            String inputDtoType = inputType;
            if(!isProtoInput)
                inputDtoType = toFullName(inputType);

            String returnDtoType = returnType;
            if(!isProtoOutput)
                returnDtoType = toFullName(returnType);


            String inputParamInfo = "";
            if(inputType!=null) {
                if(isProtoInput) {
                    inputParamInfo = inputType+" request";
                }else {
                    inputParamInfo = inputDtoType+" input";
                }
            }








            String rpcInputType = geParamInfosForGRpc(fieldItems);
            String rpcReturnType = toRpcName(returnType);


            String methodName = method.getIdentity().getName();
            MetaInfo metaInfo = method.getMetaInfo();
            String desc = method.getDescription();
            if (!StringUtils.hasValue(desc)) {
                desc = methodName;
            }
            methodP.add("/*");
            methodP.add(StringUtils.addPrefixToEachLine(desc, " * "));
            methodP.add("*/");

            methodP.add("public %s %s(%s) {", returnDtoType, methodName, inputParamInfo);

            /*
            if(inputParamInfo.isEmpty()) {
                methodP.add("    %s response = blockingStub.%s();", rpcReturnType, methodName);
            } else
            */

            if(inputParamInfo.isEmpty() || !isProtoInput) {
                methodP.add("    %s.Builder builder = %s.newBuilder();", rpcInputType, rpcInputType);
            }

            if(!inputParamInfo.isEmpty() && !isProtoInput) {
                methodP.add("    Mapper.of(input, builder).execute();");
            }


            if(inputParamInfo.isEmpty() || !isProtoInput) {
                methodP.add("    %s request = builder.build();", rpcInputType);
            }

            methodP.add("    %s response = blockingStub.%s(request);", rpcReturnType, methodName);

            if(isProtoOutput) {
                // for proto message
                methodP.add("    return response;");
            }
            else {
                methodP.add("    %s output = new %s();",returnDtoType, returnDtoType);
                methodP.add("    Mapper.of(response, output).execute();");
                methodP.add("    return output;");
            }






            /*
            methodP.add("    %s.%sBuilder respBuilder = ",returnDtoType, returnType);
            methodP.add("        %s.builder();",returnDtoType);
            methodP.add("    Mapper.of(response, respBuilder).execute();");
            methodP.add("    return respBuilder.build();");
             */
            methodP.add("}");
        }
    }


    private boolean isProtoObject(String typeName) {
        if(typeName==null || typeName.isEmpty())
            return false;

        DataTransfer dto = this.findDTO(typeName);
        if(dto!=null) {
            if( dto.getMeta().hasMetaAnnotation(DataTransferAnnotation.META_PROTO) )
                return true;
        }
        return false;
    }


    private String toFullName(String typeName) {
        if(typeName==null)
            return null;
        DataTransfer dto = this.publisher.getECoreModelContext().findModelComponent(typeName, DataTransfer.class);
        if(dto==null)
            return null;
        return dto.getFullName();
    }

    private String toRpcName(String typeName) {
        return typeName;
    }
}
