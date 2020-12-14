package com.holobor.infos;

import java.util.Arrays;
import java.util.List;

public class VideoInformation {
    public final Video video;
    public final List<Tag> tag;

    public VideoInformation(Video video, Tag... tags) {
        this.video = video;
        this.tag = Arrays.asList(tags);
    }
}
