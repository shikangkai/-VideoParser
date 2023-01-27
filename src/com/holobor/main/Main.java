package com.holobor.main;

import com.holobor.constants.Config;

import javax.swing.text.html.Option;
import java.io.File;
import java.util.Scanner;

public class Main {

    private static boolean needStop = false;
	public static void main(String[] args) {

	    System.out.print(
	            "操作列表：\n"
                        + "\t1. 解析提取视频\n"
                        + "\t2. 实际删除视频\n"
						+ "\t3. 检索未落库视频\n"
						+ "\t4. 解析提取图片\n"
						+ "\t11. 【资源获取】已全部首次解压缩，批量取出文件夹内的压缩包文件\n"
						+ "\t12. 【资源获取】已全部二次解压缩，批量文件重命名\n"
						+ "请输入数字选项（按回车确认）：");

		Scanner scanner = new Scanner(System.in);
		switch (scanner.nextInt()) {
			case 1:
				Options.parseAndStoreVideos();
				break;

			case 2:
				Options.removeDeletedVideos();
				break;

			case 3:
				Options.listUnStoredVideos();
				break;

			case 4:
				Options.parseAndStorePictures();
				break;

			case 11:
				Options.move7ZFileOut(new File(Config.SRC_WORKSPACE_BASE_DIR));
				break;

			case 12:
				Options.batchRename(new File(Config.SRC_WORKSPACE_BASE_DIR));
				break;

			default:
				System.out.println("输入错误，退出");
		}

    }

}
