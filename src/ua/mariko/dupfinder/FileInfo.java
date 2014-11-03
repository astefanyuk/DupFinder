package ua.mariko.dupfinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FileInfo {
	public final File file;
	private long hash;
	private final static long INVALID_HASH = Long.MAX_VALUE;
	private Config config;
	
	public List<FileInfo> subItems = new ArrayList<FileInfo>();

	public FileInfo(File file, Config config) {
		this.file = file;
		this.config = config;
	}
	
	private boolean isValidHash(){
		return hash != INVALID_HASH && this.hash!= 0l;
	}
	
	public String getDetailedFileInfo(){
		return file.getAbsolutePath() + " Size=" + DupFinderInfo.formatBytes(file.length()) + " Modified Date=" + new Date(file.lastModified());
	}
	
	
	public boolean sameFile(Object obj) {

		FileInfo fileInfo = (FileInfo) obj;
		
		if(this == fileInfo){
			return false;
		}
		
		try {
			if(this.file.getCanonicalPath().equalsIgnoreCase(fileInfo.file.getCanonicalPath())){
				return false;
			}
		} catch (IOException ex) {
			return false;
		}
		
		if(this.config.byName){
			if(!fileInfo.file.getName().equals(this.file.getName())){
				return false;
			}
		}
		
		this.calculateHash();
		
		if(!this.isValidHash()){
			return false;
		}
		
		fileInfo.calculateHash();
		
		if(!fileInfo.isValidHash()){
			return false;
		}
		
		if(this.hash != fileInfo.hash){
			return false;
		}
		
		if(this.file.length() != fileInfo.file.length()){
			return false;
		}
		
		
		try {
			return FileUtils.contentEquals(this.file, fileInfo.file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	private void calculateHash() {

		if (hash == 0l) {

			try {
				this.hash = FileUtils.checksumCRC32(file);
			} catch (Throwable e) {
				
				this.hash = INVALID_HASH;
				e.printStackTrace();
			}finally{
				
				if (hash == 0l) {
					this.hash = INVALID_HASH;
				}
			}
		}
	}

}
