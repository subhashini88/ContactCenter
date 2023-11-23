package com.opentext.apps.cc.importcontent;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	/**
	 * This method creates zip file by taking the content from ZipContentFolder
	 * 
	 * @param ZipContentFolder       actual content of the zip file
	 * @param deleteZipContentFolder whether to delete the zip content folder or not
	 */
	public static void createZipFile(Path ZipContentFolder, boolean deleteZipContentFolder) {

		Path zipFilePath = Paths.get(ZipContentFolder.toString() + ".zip");
		try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipFilePath));) {
			ZipFileVisitor fileVisitor = new ZipFileVisitor(ZipContentFolder, out, deleteZipContentFolder);
			Files.walkFileTree(ZipContentFolder, fileVisitor);
		} catch (IOException e) {
			throw new ContentManagementRuntimeException(e);
		}

	}

	/**
	 * Extracts a zip file and returns the path for extracted folder
	 * 
	 * @param zipFileName path of the zip file
	 * @param filePath
	 * @return Path object representing the extracted folder
	 */

	public static Path extractZipFile(Path zipFileName, String jobID) {
		Path zipContentFolder;
		try {
			int zipIndex = zipFileName.toString().indexOf(".zip");
			zipContentFolder = Paths.get(zipFileName.toString().substring(0, zipIndex));
			unzip(zipFileName.toFile(), zipContentFolder.toFile(), jobID);
		}

		catch (IOException e) {
			throw new RuntimeException(e);
		}

		return zipContentFolder;
	}

	public static final void unzip(File zip, File extractTo, String jobID) throws IOException {
		try (ZipFile archive = new ZipFile(zip);) {
			Enumeration<? extends ZipEntry> e = archive.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = e.nextElement();
				Path basePath = null;
				String installationPath = FileUtil.getDownloadReadPath();
				basePath = Paths.get(installationPath + jobID);
				Path extractToPath = Paths.get(extractTo + File.separator + entry.getName());
				if (extractToPath.startsWith(basePath)) {
					File file = new File(extractTo, entry.getName());
					if (entry.isDirectory() && !file.exists()) {
						file.mkdirs();
					} else {
						if (!file.getParentFile().exists()) {
							file.getParentFile().mkdirs();
						}
						if (!file.isDirectory()) {
							try (InputStream in = archive.getInputStream(entry);
									BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));) {
								byte[] buffer = new byte[8192];
								int read;
								while (-1 != (read = in.read(buffer))) {
									out.write(buffer, 0, read);
								}
							}
						}
					}
				}
			}
		}
	}
}
