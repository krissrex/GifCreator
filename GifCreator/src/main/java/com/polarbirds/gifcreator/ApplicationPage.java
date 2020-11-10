package com.polarbirds.gifcreator;

import java.util.Collections;
import java.util.List;

public enum ApplicationPage {

    SELECT_IMAGES("/res/image_selection.fxml", "locale.TextBundle", Collections.singletonList("/res/application.css")),

    CONFIGURE_GIF("/res/gif_settings.fxml", "locale.TextBundle", Collections.emptyList());

    public final String fxmlPath;
    public final String resourcesBundlePath;
    public final List<String> stylesheetPaths;

    ApplicationPage(String fxmlPath, String resourcesBundlePath, List<String> stylesheetPaths) {
        this.fxmlPath = fxmlPath;
        this.resourcesBundlePath = resourcesBundlePath;
        this.stylesheetPaths = stylesheetPaths;
    }
}
