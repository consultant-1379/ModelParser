package com.ericsson.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public interface VirtualFile extends Serializable, Comparable<VirtualFile>  {
	
	public String getName();
	
	public VirtualFile[] listFiles() throws SecurityException;
	
	public boolean isDirectory() throws SecurityException;
	
	public boolean isFile() throws SecurityException;
	
	public boolean exists() throws SecurityException;
	
	public InputStream getInputStream() throws SecurityException, IOException;
	
	public OutputStream getOutputStream() throws SecurityException, IOException;

	public boolean canRead() throws SecurityException;

	public String getPath() throws SecurityException;

}
