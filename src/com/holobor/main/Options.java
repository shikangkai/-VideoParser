package com.holobor.main;

import com.holobor.constants.Config;
import com.holobor.helper.DatabaseHelper;
import com.holobor.infos.VideoInformation;
import com.holobor.utils.Filer;
import com.holobor.utils.VideoProcessor;
import com.sun.xml.internal.bind.v2.model.core.EnumLeafInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Options {

    public static void parseAndStoreVideos() {

        File dir = new File(Config.SRC_WORKSPACE_VIDEO_SRC_DIR);
        _extract(dir);
        for (File file : dir.listFiles()) {
            _clearEmptyFolder(file);
        }
    }


    private static void _extract(File root) {
//        if (needStop) { return; }
        if (root == null) { return; }
        if (root.isFile()) {
            if (root.length() < 32 * 1024) { return; }
            try {
                String videoPath = root.getAbsolutePath();
                if (!Filer.isMp4File(videoPath)) { return; }
                VideoInformation videoInformation = VideoProcessor.processVideo(videoPath);
                if (videoInformation == null) {
                    return;
                }
                File dstFile = new File(Config.SRC_WORKSPACE_VIDEO_DST_DIR, videoInformation.video.md5 + ".mp4");
                if (dstFile.exists()) {
                    root.delete();
                } else {
                    root.renameTo(dstFile);
                }
//                needStop = true;
            } catch (FileAlreadyExistsException e) {
//                root.delete();
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return;
            }
        }

        File[] subFiles = root.listFiles();
        if (subFiles == null) { return; }
        for (File file : subFiles) {
            _extract(file);
        }


    }

    private static void _clearEmptyFolder(File root) {
        if (root != null && root.isFile() && root.getName().startsWith(".")) {
            root.delete();
        }
        if (root == null || !root.isDirectory()) { return; }
        File[] subFiles = root.listFiles();
        if (subFiles != null && subFiles.length != 0) {
            for (File file : subFiles) {
                _clearEmptyFolder(file);
            }
        }
        subFiles = root.listFiles();
        if (subFiles == null || subFiles.length == 0) {
            root.delete();
        }
    }

    public static void listUnStoredVideos() {
		File f = new File(Config.SRC_WORKSPACE_VIDEO_DST_DIR);
		for (File file : f.listFiles()) {
		    String md5 = file.getName().split("\\.")[0];
            ResultSet resultSet = null;
            try {
                resultSet = DatabaseHelper.query("select md5 from video where md5 = '" + md5 + "'");
                if (resultSet.next()) {
                    // pass
                } else {
                    System.out.println("未索引的视频：" + md5);
                    file.renameTo(new File(Config.SRC_WORKSPACE_VIDEO_SRC_DIR, file.getName()));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        }
    }

    public static void removeDeletedVideos() {
        try {
            ResultSet resultSet = DatabaseHelper.query("select md5, title from video where deleted = 1");
            while (resultSet.next()) {
                String md5 = resultSet.getString(1);
                File file = new File(Config.SRC_WORKSPACE_VIDEO_DST_DIR + md5 + ".mp4");
                file.renameTo(new File(Config.SRC_WORKSPACE_VIDEO_ABANDON_DIR + md5 + ".mp4"));
                System.out.println("删除视频：" + md5 + " | " + resultSet.getString(2));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public static void parseAndStorePictures() {
        File albumDir = new File(Config.SRC_WORKSPACE_IMAGE_SRC_DIR);
        for (File album : albumDir.listFiles()) {
            if (!album.isDirectory()) {
                continue;
            }

            if (album.getName().startsWith(".")) {
                continue;
            }

            int index = 1;
            File[] imageFiles = album.listFiles();
            for (File imgFile : imageFiles) {
                if (imgFile.getName().startsWith(".")) {
                    continue;
                }

                try {
                    Image image = ImageIO.read(imgFile);
                    int w = image.getWidth(null);
                    int h = image.getHeight(null);

                    int targetW = 320;

                    float scale = 1f * w / targetW;
                    w = (int) (w / scale);
                    h = (int) (h / scale);


                    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    Graphics g = bi.getGraphics();
                    g.drawImage(image, 0, 0, w, h, Color.LIGHT_GRAY, null);
                    g.dispose();
                    String suffix = "jpg";//imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1);
                    File thumbnailDir = new File(Config.SRC_WORKSPACE_IMAGE_THUMBNAIL_DIR + "/" + album.getName());

                    thumbnailDir.mkdirs();
                    String fileName = index + "." + suffix;
                    ImageIO.write(bi, suffix, new File(thumbnailDir, fileName));
                    imgFile.renameTo(new File(imgFile.getParent(), fileName));
                    index++;
                    System.out.println("正在处理：" + imgFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void move7ZFileOut(File srcDir) {

        if (!srcDir.isDirectory()) {
            System.out.println(srcDir.getAbsolutePath() + " 不是路径，停止");
            return;
        }

        for (File subDir : srcDir.listFiles()) {
            if (!subDir.isDirectory()) {
                System.out.println(subDir.getAbsolutePath() + " 不是路径，跳过");
                continue;
            }

            File[] files = subDir.listFiles();
            if (files == null) {
                System.out.println(subDir.getAbsolutePath() + " 子文件获取出错，跳过");
                continue;
            }

            if (files.length != 1) {
                System.out.println(subDir.getAbsolutePath() + " 子文件超过 1 个，跳过");
                continue;
            }

            files[0].renameTo(new File(files[0].getParentFile().getParentFile(), subDir.getName() + ".7z"));
            files = subDir.listFiles();
            if (files == null || files.length == 0) {
                subDir.delete();
            }
        }
    }

    public static void batchRemoveEmojiChar(File srcDir) {
        if (srcDir.isFile()) {
            String fileName = srcDir.getName();
            char[] chars = fileName.toCharArray();
            boolean needRename = false;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] < 128) {
                    continue;
                }

                if (chars[i] >= 0x4e00 && chars[i] <= 0x9ea5) {
                    continue;
                }

                needRename = true;
                chars[i] = '-';
            }

            Pattern emoji = Pattern.compile("[\u4e00-\u9ea5]",
                    Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
            Matcher emojiMatcher = emoji.matcher(srcDir.getName());
            if (needRename) {
                srcDir.renameTo(new File(srcDir.getParentFile(), new String(chars)));
            }
            return;
        }

        File[] files = srcDir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            batchRemoveEmojiChar(file);
        }
    }



    public static void batchRename(File srcDir) {
        if (!srcDir.isDirectory()) {
            // skip for file
            return;
        }

        File[] files = srcDir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            batchRename(file, file.getName());
        }
    }

    private static void batchRename(File file, String prefix) {

        System.out.println("重命名：" + file.getAbsolutePath());

        if (!file.isDirectory()) {
            // skip for file
            return;
        }

        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        int index = 0;
        for (File subFile : files) {
            String fileName = subFile.getName();
            String renameFileName;
            if (subFile.isFile()) {
                int extensionIndex = fileName.lastIndexOf('.');
                if (extensionIndex == -1) {
                    renameFileName = index == 0 ? fileName : fileName + "-" + index;
                } else {
                    renameFileName = index == 0
                            ? (prefix + fileName.substring(extensionIndex))
                            : (prefix + "-" + index + fileName.substring(extensionIndex)) ;
                }

                subFile.renameTo(new File(Config.SRC_WORKSPACE_VIDEO_SRC_DIR, renameFileName));
                index++;
            }

            if (subFile.isDirectory()) {
                renameFileName = index == 0 ? prefix : prefix + "-" + index;
                File renamedFile = new File(subFile.getParentFile(), renameFileName);
                subFile.renameTo(renamedFile);
                batchRename(renamedFile, prefix + "_" + subFile.getName());
                index++;
            }
        }
    }

    public static void listTagsAndMakeFolder() {
        try {
            System.out.println("\n\n当前数据库中的视频标签列表：\nTAG ID | TAG NAME\n---------------------------------");
            ResultSet resultSet = DatabaseHelper.query("select id, name from tag order by name asc");
            Map<Integer, String> tags = new HashMap<>();
            while (resultSet.next()) {
                tags.put(resultSet.getInt(1), resultSet.getString(2));
                System.out.println(String.format("%6d | %s", resultSet.getInt(1), resultSet.getString(2)));
            }

            System.out.print("请输入需要导出视频的标签ID：");
            Scanner scanner = new Scanner(System.in);
            int tagId = scanner.nextInt();

            System.out.print("导出的视频列表为：");
            resultSet = DatabaseHelper.query("select B.md5, B.title from video_tag A left join video B on A.video_id = B.id where B.deleted = 0 AND A.tag_id = " + tagId);

            String tagName = tags.get(tagId);
            if (tagName == null) {
                return;
            }

            // make dir
            File dstDir = new File(Config.SRC_WORKSPACE_VIDEO_CATEGORY_DIR, tagName);
            Filer.deleteDir(dstDir);
            if (!dstDir.mkdirs()) {
                System.out.println(dstDir + " 目录创建失败");
                return;
            }

            while (resultSet.next()) {
                String md5 = resultSet.getString(1);
                String name = resultSet.getString(2);
                System.out.println(String.format("[%s] %s", md5, name));
                File src = new File(Config.SRC_WORKSPACE_VIDEO_DST_DIR, md5 + ".mp4");
                File dst = new File(dstDir, name + "_" + md5 + ".mp4");

                try {
                    Files.copy(src.toPath(), dst.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void tagDuplicatedVideos() {
        try {
            DatabaseHelper.executeSql("delete from video_tag where tag_id = 197");
            ResultSet resultSet = DatabaseHelper.query("select id, duration_ms, width, height from video where deleted = 0 order by duration_ms desc");
            HashMap<Long, LinkedList> durationIds = new HashMap<>();

            int count = 0;
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                long durationMs = resultSet.getLong(2);

                LinkedList<Integer> ids = durationIds.get(durationMs);
                if (ids == null) {
                    ids = new LinkedList<>();
                    durationIds.put(durationMs, ids);
                }
                ids.add(id);

                count++;
            }
            resultSet.close();

            System.out.println(String.format("共有视频%d个，时间不重复的共有%d个", count, durationIds.size()));
            for (LinkedList<Integer> ids : durationIds.values()) {
                if (ids.size() <= 1) {
                    continue;
                }

                for (Integer id : ids) {
                    DatabaseHelper.executeSql(String.format("insert into video_tag (`video_id`, `tag_id`) select %s, %s from dual where not exists (select id from video_tag where video_id = %s and tag_id = %s)", id, 197, id, 197)); // tagId=197
                }
            }
        } catch (Throwable throwables) {
            throwables.printStackTrace();
        }
    }

    public static void tagVideosAutomatic() {
        try {
            DatabaseHelper.executeSql("delete from video_tag where tag_id = 169"); // 小于30秒
            DatabaseHelper.executeSql("delete from video_tag where tag_id = 142"); // 大于1小时
            DatabaseHelper.executeSql("delete from video_tag where tag_id = 153"); // 低清视频

            ResultSet resultSet = DatabaseHelper.query("select id, duration_ms, width, height from video where deleted = 0");
            Set<Integer> less30SecIds = new HashSet<>();
            Set<Integer> more3600SecIds = new HashSet<>();
            Set<Integer> lowDpiIds = new HashSet<>();
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                long durationMs = resultSet.getLong(2);
                int width = resultSet.getInt(3);
                int height = resultSet.getInt(4);

                if (durationMs < 30_000L) less30SecIds.add(id);
                if (durationMs > 3600_000L) more3600SecIds.add(id);
                if (Math.min(width, height) <= 320) lowDpiIds.add(id);
            }
            resultSet.close();

            for (Integer id : less30SecIds) DatabaseHelper.executeSql(String.format("insert into video_tag (`video_id`, `tag_id`) select %s, %s from dual where not exists (select id from video_tag where video_id = %s and tag_id = %s)", id, 169, id, 169)); // tagId=169
            for (Integer id : more3600SecIds) DatabaseHelper.executeSql(String.format("insert into video_tag (`video_id`, `tag_id`) select %s, %s from dual where not exists (select id from video_tag where video_id = %s and tag_id = %s)", id, 142, id, 142)); // tagId=142
            for (Integer id : lowDpiIds) DatabaseHelper.executeSql(String.format("insert into video_tag (`video_id`, `tag_id`) select %s, %s from dual where not exists (select id from video_tag where video_id = %s and tag_id = %s)", id, 153, id, 153)); // tagId=153

    } catch (Throwable throwables) {
            throwables.printStackTrace();
        }
    }
}
