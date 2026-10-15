package io.elasticore.base.model;

public interface ConstantParam {


    String KEYNAME_BASE = "base";

    String KEYNAME_ENTITY = "entity";

    String KEYNAME_DTO = "dto";

    String KEYNAME_ENUM = "enum";

    String KEYNAME_ENUMERATION = "enumeration";

    String KEYNAME_REPOSITORY = "repository";

    String KEYNAME_MAPPER = "mapper";

    String KEYNAME_SERVICE = "service";

    /** A domain-level section: operations belonging to no single aggregate. */
    String KEYNAME_DOMAIN_SERVICE = "domainService";

    /** A domain-level section: the business's measures (revenue, order count ...), one line each. */
    String KEYNAME_METRICS = "metrics";

    String KEYNAME_CONTROL = "control";

    String KEYNAME_PORT = "port";

    String KEYNAME_METHOD = "method";

    String KEYNAME_PROTO = "proto";




    String PROPERTY_INFO = "info";

    String PROPERTY_DESC = "desc";

    String PROPERTY_FIELDS = "fields";

    String PROPERTY_META = "meta";

    String PROPERTY_ENUM = "enum";

    String PROPERTY_METHODS = "methods";

    String POSTFIX_SERVICE = "CoreService";

    String POSTFIX_EXT_SERVICE = "Service";

    String POSTFIX_CONTROL = "Controller";


    String PROPERTY_ELCORE_HOME = "src/main/resources/blueprint";

    String PROPERTY_JAVA_SRC_HOME = "src/main/java";

    String PROPERTY_PROTO_SRC_HOME = "src/main/proto";
    
}
