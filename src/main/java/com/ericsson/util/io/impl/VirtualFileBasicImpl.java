package com.ericsson.util.io.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ericsson.util.io.VirtualFile;

public class VirtualFileBasicImpl implements VirtualFile {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private File file;
	
	public VirtualFileBasicImpl(String path) {
		file = new File(path);
	}
	
	public VirtualFileBasicImpl(File file) {
		this.file = file;
	}
	
	@Override
	public int compareTo(VirtualFile o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public VirtualFile[] listFiles() throws SecurityException {
		File[] files = file.listFiles();
		VirtualFile[] result = new VirtualFile[files.length];
		for (int index = 0; index < files.length; index++) {
			result[index] = new VirtualFileBasicImpl(files[index]);
		}
		return result;
	}

	@Override
	public boolean isDirectory() throws SecurityException {
		return this.file.isDirectory();
	}

	@Override
	public boolean isFile() throws SecurityException {
		return this.file.isFile();
	}

	@Override
	public boolean exists() throws SecurityException {
		return this.file.exists();
	}

	@Override
	public InputStream getInputStream() throws SecurityException, IOException {
		return new FileInputStream(this.file);
	}

	@Override
	public OutputStream getOutputStream() throws SecurityException, IOException {
		return new FileOutputStream(this.file);
	}

	@Override
	public boolean canRead() throws SecurityException {
		return this.file.canRead();
	}
	
	@Override
	public String getPath() throws SecurityException {
		return this.file.getPath();
	}

}
