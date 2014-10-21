package ua.mariko.dupfinder;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class DupFinder {

	public Config config;
	
	public DupFinderInfo info;
	
	private Hashtable<Long, ArrayList<FileInfo>> hashTable;
	
	public void loadFiles(){
		
		System.out.println("Searching in " + config.root.getAbsolutePath());
		
		HashSet<File> filesHash = new HashSet<>();

		// load all files
		findFiles(config.root, filesHash, true, config.filter);
		
		info = new DupFinderInfo();
				
		//split by size
		hashTable = new Hashtable<Long, ArrayList<FileInfo>>();

		for (File file : filesHash) {
			
			Long size = file.length();
			
			++info.totalCount;
			
			info.sizeTotal +=  size;

			FileInfo fileInfo = new FileInfo(file);

			ArrayList<FileInfo> list = hashTable.get(size);

			if (list == null) {
				list = new ArrayList<FileInfo>();
				hashTable.put(size, list);
			}

			list.add(fileInfo);
		}
		
		//calculate possible duplicates
		for (Long key : hashTable.keySet()) {

			ArrayList<FileInfo> list = hashTable.get(key);

			if (list.size() > 1) {

				info.possibleDuplicatedFiles += (list.size() - 1);
				info.possibleDuplicatedFilesSize += (key) * (list.size() - 1);
			}
		}
		
		System.out.println("Founded " + info.totalCount + " files to check. Total size: "+ DupFinderInfo.formatBytes(info.sizeTotal));
	}
	
	public void findDuplicatedFiles(){
		
		for (Long key : hashTable.keySet()) {
			
			findDuplicatedFiles(hashTable.get(key));			
		}
		
		logDone();
	}
	
	public void findDuplicatedFiles(ArrayList<FileInfo> list){
		
		if(list.size() <=1){
			return;
		}
		
		for (int i = 0; i < list.size(); i++) {

			for (int j = 0; j < i; j++) {

				FileInfo fileInfo = list.get(j);

				if (fileInfo.equals(list.get(i))) {

					++info.duplicateCount;

					info.sizeDeleted += fileInfo.file.length();

					System.out.println((config.deleteDuplicated ? "Deleted duplicated"
									: "Founded duplicated")
									+ " " + fileInfo.file.getAbsolutePath());

					if (config.deleteDuplicated) {
						fileInfo.file.delete();
					}

					list.remove(j);

					--i;
					break;
				}
			}
		}
	}
	
	private void logDone(){
		System.out.println("Total files: " + info.totalCount + ". Size: "+ DupFinderInfo.formatBytes(info.sizeTotal));
		
		System.out.println((config.deleteDuplicated ? "Deleted duplicated"
				: "Founded duplicated")
				+ ": "
				+ info.duplicateCount
				+ ". Size: "
				+ DupFinderInfo.formatBytes(info.sizeDeleted));
	}
	
	private void findFiles(File file, HashSet<File> filesHash, boolean ignoreHidden, java.io.FileFilter fileFilter) {
		
		if(!file.exists() || !file.canRead()){
			return;
		}
		
		if(!ignoreHidden && file.isHidden()){
			return;
		}


		if (file.isDirectory()) {

			File files[] = file.listFiles(fileFilter);

			if (files != null && files.length > 0) {

				for (File f : files) {

					findFiles(f, filesHash, false, fileFilter);
				}
			}
		} else {

			if (file.length() != 0 && !file.isHidden()) {
				filesHash.add(file);
			}
		}
	}

	public void confirmDelete() {
		if (info.possibleDuplicatedFiles > 0) {

			System.out.println("Possibly duplicated files: "
					+ info.possibleDuplicatedFiles + ". Size: "
					+ DupFinderInfo.formatBytes(info.possibleDuplicatedFilesSize));

			if (info.possibleDuplicatedFiles > 0) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (config.deleteDuplicated) {

				System.out.println("Please type 'YES' if you want to delete duplicated files.");

				String typed = "";

				DataInputStream in = new DataInputStream(System.in);
				try {
					typed = in.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				config.deleteDuplicated = "YES".equalsIgnoreCase(typed);
			}
		}
		
	}
}
