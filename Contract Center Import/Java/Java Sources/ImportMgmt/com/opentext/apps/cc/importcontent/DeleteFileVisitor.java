package com.opentext.apps.cc.importcontent;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;


public class DeleteFileVisitor extends SimpleFileVisitor<Path>{
	public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
	        throws IOException 
	{
	    if(attributes.isRegularFile())
	    {
	        Files.delete(file);
	    }
	    return FileVisitResult.CONTINUE;
	}
 
    @Override
    public FileVisitResult postVisitDirectory(Path directory, IOException ioe)
            throws IOException 
    {

        Files.delete(directory);
        return FileVisitResult.CONTINUE;
    }
 
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException ioe)
            throws IOException 
    {
        return FileVisitResult.CONTINUE;
    }
    
    public static DeleteFileVisitor getInstance()
    {
    	return FileVisitorTon.fileVisistor;
    }
    
    //Creates a SingleTon Object for Parent Class
    private static class FileVisitorTon
    {
    	private static DeleteFileVisitor fileVisistor = new DeleteFileVisitor();
    }
}
