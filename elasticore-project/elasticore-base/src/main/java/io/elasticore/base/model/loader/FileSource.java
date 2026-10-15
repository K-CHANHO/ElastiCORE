package io.elasticore.base.model.loader;


import lombok.Builder;
import lombok.Getter;
import org.w3c.dom.Element;

import java.util.Map;

@Builder
@Getter
public class FileSource {

    private String filepath;

    private Map infoMap;

    private Element element;

    private Throwable error;
}
