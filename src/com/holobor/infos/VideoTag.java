package com.holobor.infos;

import com.holobor.helper.DatabaseHelper;

import java.sql.SQLException;

public class VideoTag {

    public final int videoId;
    public final int tagId;

    public VideoTag(int videoId, int tagId) {
        this.videoId = videoId;
        this.tagId = tagId;
    }

    public boolean insertIfNeeded() {
        try {
            return DatabaseHelper.executeSql(String.format("insert into video_tag (`video_id`, `tag_id`) select %d, %d from dual where not exists (select id from video_tag where video_id = %d and tag_id = %d)", videoId, tagId, videoId, tagId));
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
