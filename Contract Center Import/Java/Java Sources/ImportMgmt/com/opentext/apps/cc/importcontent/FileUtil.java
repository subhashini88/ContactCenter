package com.opentext.apps.cc.importcontent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Workbook;

import com.eibus.util.Base64;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.EIBProperties;
import com.opentext.apps.cc.custom.Utilities;

public class FileUtil {
	private static final CordysLogger LOGGER = CordysLogger.getCordysLogger(FileUtil.class);

	public static void deleteDirectory(Path path) {

		try {
			DeleteFileVisitor fileVisistor = new DeleteFileVisitor();
			Files.walkFileTree(path, fileVisistor);
		} catch (IOException e) {
			throw new ContentManagementRuntimeException(e);
		}
	}

	public static List<File> getAllFilesFromNestedFolder(String rootFolder, String nestedFolder) {
		File dir = new File(rootFolder);
		List<File> filesList = new ArrayList<>();
		List<File> nestedFolders = new ArrayList<>();

		getDirectories(dir, nestedFolder, nestedFolders);

		for (File file : nestedFolders) {
			getFiles(file, "", filesList);
		}
		return filesList;
	}

	public static void writeAllZipBytes(Path path, byte[] input) {

		deleteFileAndCreateParent(path);
		try (FileOutputStream out = new FileOutputStream(path.toFile())) {
			out.write(input);
		}

		catch (IOException e) {
			ContentManagementRuntimeException exception = new ContentManagementRuntimeException(e,
					Messages.EXCEPTION_CREATING_FILE, path.toString());
			LOGGER.error(exception, Messages.EXCEPTION_CREATING_FILE, path.toString());
			throw exception;
		}

	}

	public static void getFiles(File directory, String filter, List<File> filesList) {
		if (directory != null && directory.isDirectory()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					getFiles(file, filter, filesList);
				} else {
					if (Utilities.isStringEmpty(filter) || file.getName().indexOf(filter) > 0) {
						filesList.add(file);
					}
				}

			}
		}
	}

	public static void getDirectories(File directory, String filter, List<File> filesList) {
		if (directory != null && directory.isDirectory()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					if (file.getName().indexOf(filter) >= 0) {
						filesList.add(file);
					} else {
						getDirectories(file, filter, filesList);
					}
				}

			}
		}
	}

	/*
	 * public static byte[] getBytes(String content) { try { return
	 * content.getBytes("UTF-8"); } catch(IOException e) {
	 * ContentManagementRuntimeException exception = new
	 * ContentManagementRuntimeException(e,AssureAlertMessages.ENCODING_FAILED);
	 * //LOGGER.error(exception,AssureAlertMessages.ENCODING_FAILED); throw
	 * exception; } }
	 */

	public static byte[] readAllBytes(Path path) {

		try {
			return Files.readAllBytes(path);
		} catch (IOException e) {
			ContentManagementRuntimeException exception = new ContentManagementRuntimeException(e,
					Messages.EXCEPTION_READING_FILE_CONTENT, path.toString());
			LOGGER.error(exception, Messages.EXCEPTION_READING_FILE_CONTENT, path.toString());
			throw exception;
		}

	}

	public static String getDocumentContent(Path path, boolean isBase64Encoded) {

		String content;
		byte[] fileBytes = readAllBytes(path);
		if (isBase64Encoded) {
			content = Base64.encodeToStr(fileBytes);
		} else {
			content = new String(fileBytes, StandardCharsets.UTF_8);
		}

		return content;
	}

	public static void writeAllBytes(Path path, byte[] input) {

		/*
		 * deleteFileAndCreateParent(path); ByteBuffer bb = ByteBuffer.wrap(input); try
		 * (SeekableByteChannel sbc = Files.newByteChannel(path,
		 * EnumSet.of(CREATE_NEW,WRITE))) { sbc.write(bb); }
		 * 
		 * catch (IOException e) { ContentManagementRuntimeException exception = new
		 * ContentManagementRuntimeException(e,Messages.EXCEPTION_CREATING_FILE,path.
		 * toString());
		 * LOGGER.error(exception,Messages.EXCEPTION_CREATING_FILE,path.toString());
		 * throw exception; }
		 */

		writeAllZipBytes(path, input);

	}

	public static void writeStringContent(Path path, String input) {
		deleteFileAndCreateParent(path);
		try (BufferedWriter sbc = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			sbc.write(input);
		}

		catch (IOException e) {
			ContentManagementRuntimeException exception = new ContentManagementRuntimeException(e,
					Messages.EXCEPTION_CREATING_FILE, path.toString());
			LOGGER.error(exception, Messages.EXCEPTION_CREATING_FILE, path.toString());
			throw exception;
		}

	}

	public static void deleteFileAndCreateParent(Path path) {
		File file = path.toFile();
		if (file.exists()) {
			file.delete();
		}

		file.getParentFile().mkdirs();
	}

	public static void writeWorkBookToFile(Path path, Workbook workbook) {

		deleteFileAndCreateParent(path);
		try (OutputStream out = Files.newOutputStream(path);) {
			workbook.write(out);
		} catch (IOException e) {
			ContentManagementRuntimeException exception = new ContentManagementRuntimeException(e,
					Messages.EXCEPTION_CREATING_FILE, path.toString());
			LOGGER.error(exception, Messages.EXCEPTION_CREATING_FILE, path.toString());
			throw exception;
		}
	}

	/**
	 * Read the value from cws.properties file and given as string.
	 * 
	 * @return path that ends with file separator.
	 */
	public static String getDownloadReadPath() {
		String downloadReadPath = EIBProperties.getProperty(ImportConstants.DOWNLOAD_READ_PATH);
		if (Objects.isNull(downloadReadPath) || downloadReadPath.isBlank()) {
			downloadReadPath = EIBProperties.getInstallDir() + File.separator + "content" + File.separatorChar
					+ "downloadcontent";
		}
		if (!downloadReadPath.endsWith(File.separator)) {
			downloadReadPath += File.separator;
		}
		return downloadReadPath;
	}

}
