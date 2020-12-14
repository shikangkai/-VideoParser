package com.holobor.utils;


import com.holobor.constants.Config;

import java.awt.*;

public class ScaleUtils {

    public static class Size {
        public final int width;
        public final int height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public static Size scaleSize(Size size) {

        int width;
        int height;

        if (1f * size .width / size.height <
                1f * Config.BROWSER_THUMBNAIL_WIDTH / Config.BROWSER_THUMBNAIL_HEIGHT) {
            width = Config.BROWSER_THUMBNAIL_HEIGHT * size.width / size.height;
            height = Config.BROWSER_THUMBNAIL_HEIGHT;
        } else {
            width = Config.BROWSER_THUMBNAIL_WIDTH;
            height = Config.BROWSER_THUMBNAIL_WIDTH * size.height / size.width;
        }

        return new Size(width, height);
    }
}
