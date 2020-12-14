package com.holobor.infos;

import com.holobor.helper.DatabaseHelper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Video {
    public final String md5;
    public final long sizeByte;
    public final long durationMs;
    public final int width;
    public final int height;
    public final double fps;
    public final int videoBitrate;
    public final int audioBitrate;
    public final String format;
    public final String title;
    public final long modifyTime;
    public final String extension;

    public Video(String md5, long sizeByte, long durationMs, int width, int height, double fps, int videoBitrate, int audioBitrate, String format, String title, long modifyTime, String extension) {
        this.md5 = md5;
        this.sizeByte = sizeByte;
        this.durationMs = durationMs;
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.videoBitrate = videoBitrate;
        this.audioBitrate = audioBitrate;
        this.format = format;
        this.title = title;
        this.modifyTime = modifyTime;
        this.extension = extension;
    }

    public boolean insertIfNeeded() {
        try {
            String sql = String.format("insert into video (" +
                            "`md5`, " +
                            "`size_byte`, " +
                            "`duration_ms`, " +
                            "`width`, " +
                            "`height`, " +
                            "`fps`, " +
                            "`video_bitrate`, " +
                            "`audio_bitrate`, " +
                            "`format`, " +
                            "`title`, " +
                            "`modify_time`, " +
                            "`extension`) " +
                            "select '%s', %d, %d, %d, %d, %d, %d, %d, '%s', '%s', %d, '%s' " +
                            "from dual where not exists (select id from video where md5 = '%s' and size_byte = %d)",
                            md5,
                            sizeByte,
                            durationMs,
                            width,
                            height,
                            Math.round(fps),
                            videoBitrate,
                            audioBitrate,
                            format,
                            title,
                            modifyTime,
                            extension,
                            md5,
                            sizeByte);
            return DatabaseHelper.executeSql(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getId() {
        ResultSet resultSet = null;
        try {
            resultSet = DatabaseHelper.query(String.format("select id from video where md5 = '%s'", md5));
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("id");
            }

            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
