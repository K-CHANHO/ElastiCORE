package io.elasticore.base.model.loader;

import io.elasticore.base.model.ECoreModel;

public interface MainModelLoader {

    ECoreModel load(ModelLoaderContext context);
}
