package com.android.xreader.utils;

import android.os.Environment;


import com.android.xreader.module.Files;
import com.android.xreader.module.Module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FileManager {
	public static final String DATABASE_NAME = "wow.db";
	private static final String SYS_DATABASE_PATH = "/data/data/"
			+ FusionField.baseActivity.getPackageName() + "/databases/";
	private static FileManager manager = null;
	public static final String IMAGE_SDCARD_PATH = Environment
			.getExternalStorageDirectory() + "/focustech/typ/image/";
	public static final String FILE_SDCARD_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/reader/novels/";

	private FileManager() {

	}

	public static FileManager getInstance() {
		if (manager == null) {
			manager = new FileManager();
		}
		return manager;
	}

	public void moveToSystemDatabaseDir(CopyFileListener copy) {
		if (isFileExists(SYS_DATABASE_PATH + DATABASE_NAME)) {
			copy.onCopyFinish();
			return;
		}
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			int byteRead = 0;
			is = FusionField.baseActivity.getAssets().open(DATABASE_NAME);
			File file = new File(SYS_DATABASE_PATH);
			if (!file.exists()) {
				file.mkdirs();
			}
			File file1 = new File(SYS_DATABASE_PATH + DATABASE_NAME);
			if (!file1.exists()) {
				file1.createNewFile();
			}
			byte[] b = new byte[1024];
			fos = new FileOutputStream(SYS_DATABASE_PATH + DATABASE_NAME);
			while ((byteRead = is.read(b)) != -1) {
				fos.write(b, 0, byteRead);
			}
			copy.onCopyFinish();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void moveToSDDir(File book) {
		if (book == null) {
			Tools.log("book not exist!!");
			return;
		}
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			int byteRead = 0;
			is = new FileInputStream(book);
			File file = new File(FILE_SDCARD_PATH);
			if (!file.exists()) {
				file.mkdirs();
			}
			File file1 = new File(FILE_SDCARD_PATH + book.getName());
			if (!file1.exists()) {
				file1.createNewFile();
			}else{
				Tools.log("book already exist!! name:" + file1.getPath());
				return;
			}
			byte[] b = new byte[1024];
			fos = new FileOutputStream(FILE_SDCARD_PATH + book.getName());
			while ((byteRead = is.read(b)) != -1) {
				fos.write(b, 0, byteRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean copyFilesToSDCard(ArrayList<File> filesList, CopyFileListener copy) {
		boolean isCopyFinish = false;
		try {
			for (int i = 0; i < filesList.size(); i++) {
				moveToSDDir(filesList.get(i));
			}
			isCopyFinish = true;
			copy.onCopyFinish();
		}
		catch (Exception e){
			Tools.log("copy exception");
		}
		return isCopyFinish;
	}

	/**
	 *
	 * @param filesList
	 * @param copy
	 * @return
	 */
	public boolean copyAllFilesToSDCard(ArrayList<Module> filesList, CopyFileListener copy) {
		boolean isCopyFinish = false;
		Files file;
		for (int i = 0; i < filesList.size(); i++) {
			file = (Files) filesList.get(i);
			copyAllFilesToSDCard(file.key);
			if (file.key.equals(((Files) filesList.get(filesList.size() - 1)).key)) {
				copy.onCopyFinish();
			}
		}
		return isCopyFinish;
	}

	/**
	 *
	 * @param key
	 */
	public void copyAllFilesToSDCard(String key) {
		// FileOutputStream fos = null;
		// try
		// {
		// File file = new File(IMAGE_SDCARD_PATH);
		// if (!file.exists())
		// {
		// file.mkdirs();
		// }
		// File file1 = new File(IMAGE_SDCARD_PATH + key);
		// if (!file1.exists() && file1.getTotalSpace() == 0)
		// {
		// file1.createNewFile();
		// }
		// else
		// {
		// return;
		// }
		// byte[] b = DataManager.getInstance().getImageByteByKey(key);
		// fos = new FileOutputStream(IMAGE_SDCARD_PATH + key);
		// fos.write(b);
		// }
		// catch (IOException e)
		// {
		// e.printStackTrace();
		// }
		// finally
		// {
		// try
		// {
		// if (fos != null)
		// {
		// fos.close();
		// }
		// }
		// catch (IOException e)
		// {
		// e.printStackTrace();
		// }
		// }
	}

	/**
	 *
	 * @param filePath
	 * @return
	 */
	public boolean isFileExists(String filePath) {
		File file = new File(filePath);
        return file.exists();
    }

}

