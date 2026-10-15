package io.elasticore.base.model.port;

/**
 * This interface defines various annotation names used in elastiCore modeling.
 */
public interface PortAnnotation {



    String[] META_HTTP_ENDPOINT =  new String[]{"httpendpoint" };


    String[] META_GET_MAPPING =  new String[]{"getmapping","get" };

    String[] META_POST_MAPPING =  new String[]{"postmapping","post" };

    String[] META_PUT_MAPPING =  new String[]{"putmapping","put" };

    String[] META_DELETE_MAPPING =  new String[]{"deletemapping","delete" };


    String[] META_AUTHORIZATION =  new String[]{"authorization" ,"auth"};

    String[] META_CONTENT_TYPE =  new String[]{"contenttype" };


    String META_URL = "url";

}