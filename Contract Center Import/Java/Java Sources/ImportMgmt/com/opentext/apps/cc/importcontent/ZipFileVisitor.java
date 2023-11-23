package com.opentext.apps.cc.importcontent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFileVisitor extends SimpleFileVisitor<Path>
{
	private  ZipOutputStream zos;
	private Path zipContentPath;
	private boolean deleteFile = false;
	public ZipFileVisitor(Path zipContentPath,ZipOutputStream zos,boolean deleteFile)
	{
		this.zos = zos;
		this.zipContentPath = zipContentPath;
		this.deleteFile = deleteFile;
	}
	

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
	        throws IOException 
	{
	    if(attributes.isRegularFile())
	    {
	    	  addZipEntry(file);
	    	  if(deleteFile)
	    	  {
	    	  	Files.delete(file);
	    	  }
	    }
	    return FileVisitResult.CONTINUE;
	}

  @Override
  public FileVisitResult postVisitDirectory(Path directory, IOException ioe)
          throws IOException 
  {

    	if(deleteFile)
  	  {
        Files.delete(directory);
  	  }
      return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException ioe)
          throws IOException 
  {
      return FileVisitResult.CONTINUE;
  }
  
  private void addZipEntry(Path path) throws FileNotFoundException, IOException
  {
         byte[] buffer = new byte[8192];
         int read = 0;      
         	try(FileInputStream in = new FileInputStream(path.toFile());)
         	{
               ZipEntry entry = new ZipEntry(path.toString().substring(zipContentPath.toString().length() + 1));
               zos.putNextEntry(entry);
               while (-1 != (read = in.read(buffer))) 
               {
                   zos.write(buffer, 0, read);
               }
         	}
  }
  

  


}

