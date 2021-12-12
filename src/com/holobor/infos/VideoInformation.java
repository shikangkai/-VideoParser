package com.holobor.infos;

import java.util.List;

public class VideoInformation {
    public final Video video;
    public final List<Tag> tag;

    public VideoInformation(Video video, List<Tag> tags) {
        this.video = video;
        this.tag = tags;
    }
}
