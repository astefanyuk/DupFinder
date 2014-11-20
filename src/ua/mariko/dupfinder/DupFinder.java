package ua.mariko.dupfinder;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class DupFinder {

	public Config config;

	public DupFinderInfo info;

	private Hashtable<Long, ArrayList<FileInfo>> hashTable;

	public void loadFiles() {

		HashSet<File> filesHash = new HashSet<>();
		
		info = new DupFinderInfo();
		
		for(File root : config.files){
		
			System.out.println("Searching in " + root.getAbsolutePath());

			// load all files
			findFiles(root, filesHash, true, config.filter);			
		}
		
		hashTable = new Hashtable<Long, ArrayList<FileInfo>>();
		
		// split by size
		for (File file : filesHash) {

			Long size = file.length();

			++info.totalCount;

			info.sizeTotal += size;

			FileInfo fileInfo = new FileInfo(file, config);

			ArrayList<FileInfo> list = hashTable.get(size);

			if (list == null) {
				list = new ArrayList<FileInfo>();
				hashTable.put(size, list);
			}

			list.add(fileInfo);
		}

		// calculate possible duplicates
		for (Long key : hashTable.keySet()) {

			ArrayList<FileInfo> list = hashTable.get(key);

			if (list.size() > 1) {

				info.possibleDuplicatedFiles += (list.size() - 1);
				info.possibleDuplicatedFilesSize += (key) * (list.size() - 1);
			}
		}		
		
		System.out.println("Founded " + info.totalCount
				+ " files to check. Total size: "
				+ DupFinderInfo.formatBytes(info.sizeTotal));
		
	}

	public void findDuplicatedFiles() {
		
		List<Long> keys = new ArrayList<>(hashTable.keySet());
		
		if(!config.searchSmallFirst.equals(Config.Sort.None)){
			
			if(config.searchSmallFirst.equals(Config.Sort.Large) || config.searchSmallFirst.equals(Config.Sort.Small)){
			
				Collections.sort(keys, new Comparator<Long>() {

					@Override
					public int compare(Long o1, Long o2) {

						if(config.searchSmallFirst.equals(Config.Sort.Large)){
							return o1.compareTo(o2);
						}else if(config.searchSmallFirst.equals(Config.Sort.Small)){
							return o2.compareTo(o1);
						}
						
						return 0;
					}});
			}
			
		}
		
		int percentDone = 0;
		
		
		for (int i=0; i<keys.size(); i++) {
			
			Long key = keys.get(i);

			findDuplicatedFiles(hashTable.get(key));
			
			int newPercent = (int)( (i +1) * 100.0f / keys.size());
			
			if(newPercent  != percentDone){
				percentDone = newPercent;
				System.out.println("Done " + percentDone + "%");
			}
		}

		logDone();
	}

	public void findDuplicatedFiles(ArrayList<FileInfo> list) {

		if (list.size() <= 1) {
			return;
		}

		for (int i = 0; i < list.size(); i++) {

			FileInfo fileInfoMain = list.get(i);

			for (int j = (i + 1); j < list.size();) {

				if (fileInfoMain.sameFile(list.get(j))) {

					if (fileInfoMain.subItems.isEmpty()) {
						fileInfoMain.subItems.add(fileInfoMain);
					}

					fileInfoMain.subItems.add(list.remove(j));
				} else {
					j++;
				}
			}
		}

		for (FileInfo fileInfo : list) {

			if (fileInfo.subItems.size() >1) {
			
				//sort items by full path
				Collections.sort(fileInfo.subItems, new Comparator<FileInfo>() {

					@Override
					public int compare(FileInfo arg0, FileInfo arg1) {
						return arg0.file.getAbsolutePath().compareTo(
								arg1.file.getAbsolutePath());
					}
				});				
				

				if (!config.deleteDuplicated) {

					for (int i = 0; i < fileInfo.subItems.size(); i++) {

						FileInfo fi = fileInfo.subItems.get(i);
						

						if(i >0){
							System.out.println("Duplicated File: " + fi.getDetailedFileInfo());
							++info.duplicateCount;
							info.sizeDeleted += fi.file.length();
						}else{
							System.out.println("");
							System.out.println("########### " + " Count=" +  (fileInfo.subItems.size()) + " Total size=" + DupFinderInfo.formatBytes(fileInfo.subItems.size() * fileInfo.file.length()));
							System.out.println("Orinal File: " + fi.getDetailedFileInfo());
						}

					}

					continue;
				}
				
				
				String[] value;

				if (!config.silent) {
				
					System.out.println();
					System.out.println("########### " + " Count=" +  (fileInfo.subItems.size()) + " Total size=" + DupFinderInfo.formatBytes(fileInfo.subItems.size() * fileInfo.file.length()));

					System.out.println("Please select which file should be NOT deleted");
							
					System.out.println("0. Do NOT delete any file");

					for (int i = 0; i < fileInfo.subItems.size(); i++) {

						FileInfo fileInfoSub = fileInfo.subItems.get(i);

						System.out.println(""
								+ (i + 1)
								+ ". File: "
								+ fileInfoSub.getDetailedFileInfo());
					}
					
					value = readKeyboard().split(",");
				}else{
					value = new String[]{"1"};
				}				 

				List<FileInfo> listNotDeleted = new ArrayList<>();

				for (String v : value) {
					
					if(!"".equals(v)){
					
						int index = Integer.parseInt(v) - 1;

						if (index < 0 || index >= fileInfo.subItems.size()) {
							continue;
						}

						listNotDeleted.add(fileInfo.subItems.get(index));

					}
					
				}

				if (!listNotDeleted.isEmpty()) {
					
					boolean printentOriginal = false;

					for (int i = 0; i < fileInfo.subItems.size(); i++) {

						if (!listNotDeleted.contains(fileInfo.subItems.get(i))) {

							FileInfo fi = fileInfo.subItems.get(i);
							
							if (config.deleteDuplicated) {
								
								if(!printentOriginal){
									System.out.println();
									System.out.println("Original " + listNotDeleted.get(0).getDetailedFileInfo());
									printentOriginal = true;
								}
							
								++info.duplicateCount;
								info.sizeDeleted += fi.file.length();
							
								System.out.println("Deleted " + fi.getDetailedFileInfo());
							
								fi.file.delete();
								
							}
						}

					}
				}

			}
		}

	}

	private void logDone() {
	
		System.out.println();
	
		System.out.println("Total files: " + info.totalCount + ". Size: "
				+ DupFinderInfo.formatBytes(info.sizeTotal));

		System.out.println((config.deleteDuplicated ? "Deleted duplicated"
				: "Founded duplicated")
				+ ": "
				+ info.duplicateCount
				+ ". Size: " + DupFinderInfo.formatBytes(info.sizeDeleted));
	}

	private void findFiles(File file, HashSet<File> filesHash,
			boolean ignoreHidden, java.io.FileFilter fileFilter) {

		if (!file.exists() || !file.canRead()) {
			return;
		}

		if (!ignoreHidden && file.isHidden()) {
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

	private String readKeyboard() {

		DataInputStream in = new DataInputStream(System.in);
		try {
			return in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	public void confirmDelete() {
		if (info.possibleDuplicatedFiles > 0) {

			System.out.println("Possibly duplicated files: "
					+ info.possibleDuplicatedFiles
					+ ". Size: "
					+ DupFinderInfo
							.formatBytes(info.possibleDuplicatedFilesSize));

			if (info.possibleDuplicatedFiles > 0) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (config.deleteDuplicated && config.silent) {

				System.out
						.println("Please type 'YES' if you want to delete duplicated files.");

				String typed = readKeyboard();

				config.deleteDuplicated = "YES".equalsIgnoreCase(typed);
			}
		}

	}
}
