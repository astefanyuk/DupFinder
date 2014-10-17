package ua.mariko.dupfinder;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class DupFinder {

	private static class FileInfo {
		public final File file;
		private Long hash = 0l;

		public FileInfo(File file) {
			this.file = file;
		}

		@Override
		public boolean equals(Object obj) {

			FileInfo fileInfo = (FileInfo) obj;
			
			boolean value = this.hash != 0 && this.hash.equals(fileInfo.hash);
			
			if(value){
				try {
					value = FileUtils.contentEquals(this.file, fileInfo.file);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}

			return value;
		}

		public void calculateHash() {

			if (hash == 0l) {

				try {
					this.hash = FileUtils.checksumCRC32(file);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static void main(String[] args) {

		File root = null;
		FileFilter filter = null;
		
		Boolean deleteDuplicated = false;

		for (int i = 0; i < args.length; i++) {

			String s = args[i];

			if (!s.trim().startsWith("-")) {

				root = new File(args[i].replace("\'",""));

			} else if (s.equals("-d")) {

				deleteDuplicated = true;
				
			} else if (s.equals("-m")) {

				++i;
				filter = new WildcardFileFilter(args[i].replace("\'",""));
			}


		}
		
		System.out.println("DupFinder is a program to find duplicated files in the directory specified");
		
		if (root == null) {
			
			System.out.println("Usage: DupFinder [options] DIRECTORY");
			System.out.println(" -d                                   delete duplicated files");
			System.out.println(" -m                                   filter mask");

			pressAnyKey();

			return;
		}

		System.out.println("Searching in " + root.getAbsolutePath());

		HashSet<File> filesHash = new HashSet<>();

		// load all files
		findFiles(root, filesHash, true, filter);

		int duplicateCount = 0;
		long sizeDeleted = 0l;
		long sizeTotal = 0l;
		int possibleDuplicatedFiles = 0;
		long possibleDuplicatedFilesSize = 0l;

		for (File file : filesHash) {
			sizeTotal += file.length();
		}

		System.out.println("Founded " + filesHash.size()
				+ " files to check. Total size: " + formatBytes(sizeTotal));

		// sort by size
		Hashtable<Long, ArrayList<FileInfo>> hashTable = new Hashtable<Long, ArrayList<FileInfo>>();

		for (File file : filesHash) {

			Long size = file.length();

			FileInfo fileInfo = new FileInfo(file);

			ArrayList<FileInfo> list = hashTable.get(size);

			if (list == null) {
				list = new ArrayList<FileInfo>();
				hashTable.put(size, list);
			}

			list.add(fileInfo);

		}

		for (Long key : hashTable.keySet()) {

			ArrayList<FileInfo> list = hashTable.get(key);

			if (list.size() > 1) {

				possibleDuplicatedFiles += (list.size() - 1);
				possibleDuplicatedFilesSize += (key) * (list.size() - 1);
			}
		}

		if (possibleDuplicatedFiles > 0) {

			System.out.println("Possibly duplicated files: "
					+ possibleDuplicatedFiles + ". Size: "
					+ formatBytes(possibleDuplicatedFilesSize));

			if (possibleDuplicatedFiles > 0) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (deleteDuplicated) {

				System.out
						.println("Please type 'YES' if you want to delete duplicated files.");

				String typed = "";

				DataInputStream in = new DataInputStream(System.in);
				try {
					typed = in.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				deleteDuplicated = "YES".equalsIgnoreCase(typed);
			}
		}

		for (Long key : hashTable.keySet()) {

			ArrayList<FileInfo> list = hashTable.get(key);

			if (list.size() <= 1) {
				continue;
			}

			for (int i = 0; i < list.size(); i++) {

				list.get(i).calculateHash();

				for (int j = 0; j < i; j++) {

					FileInfo fileInfo = list.get(j);

					if (fileInfo.equals(list.get(i))) {

						++duplicateCount;

						sizeDeleted += fileInfo.file.length();

						System.out
								.println((deleteDuplicated ? "Deleted duplicated"
										: "Founded duplicated")
										+ " " + fileInfo.file.getAbsolutePath());

						if (deleteDuplicated) {
							fileInfo.file.delete();
						}

						list.remove(j);

						--i;
						break;
					}
				}
			}
		}

		System.out.println("Total files: " + filesHash.size() + ". Size: "
				+ formatBytes(sizeTotal));
		
		System.out.println((deleteDuplicated ? "Deleted duplicated"
				: "Founded duplicated")
				+ ": "
				+ duplicateCount
				+ ". Size: "
				+ formatBytes(sizeDeleted));
		
		
		pressAnyKey();
	}

	private static void pressAnyKey() {
		System.out.println("Press any key to exit.");

		DataInputStream in = new DataInputStream(System.in);
		try {
			in.readByte();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String formatBytes(long value) {

		DecimalFormat format = (DecimalFormat) DecimalFormat
				.getNumberInstance();
		format.applyPattern("#,###.##");

		String suffix = "b";
		long divisor = 1024;
		String nbsp = "&nbsp;";
		String[] scale = { nbsp, "K", "M", "G", "T", };

		float scaledValue = 0;
		String scaleSuffix = scale[0];
		if (value != 0) {
			for (int i = scale.length - 1; i >= 0; i--) {
				long div = (long) Math.pow(divisor, i);
				if (value >= div) {
					scaledValue = (float) (1.0 * value / div);
					scaleSuffix = scale[i];
					break;
				}
			}
		}
		StringBuilder sb = new StringBuilder(3);
		sb.append(format.format(scaledValue));

		sb.append(" ");
		if (!scaleSuffix.equals(scale[0])) {
			sb.append(scaleSuffix);
		}

		sb.append(suffix);
		return sb.toString();
	}

	private static void findFiles(File file, HashSet<File> filesHash, boolean ignoreHidden, java.io.FileFilter fileFilter) {
		
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

}
