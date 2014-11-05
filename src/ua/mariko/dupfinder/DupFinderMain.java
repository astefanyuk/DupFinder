package ua.mariko.dupfinder;

import java.io.DataInputStream;
import java.io.IOException;

public class DupFinderMain {
	
	public static void main(String[] args) {
		
		DupFinder dupFinder = new DupFinder();
		
		dupFinder.config = Config.readParameters(args);
		
		System.out.println("DupFinder is a program to find duplicated files in the directory specified");
		
		if (dupFinder.config.files.isEmpty()) {
			
			System.out.println("Usage: DupFinder [options] DIRECTORY [DIRECTORYN..]");
			System.out.println(" -d                                   delete duplicated files");
			System.out.println(" -ds                                  delete duplicated files. Silent mode");
			System.out.println(" -m                                   filter mask. Example: *.jpg");
			System.out.println(" -n                                   file name should be equal");
			System.out.println(" -za                                  search large files first");
			System.out.println(" -az                                  search small files first");			

			pressAnyKey();

			return;
		}
		
		
		dupFinder.loadFiles();
		
		dupFinder.confirmDelete();
		
		dupFinder.findDuplicatedFiles();
		
		pressAnyKey();
	}

	private static void pressAnyKey() {
		System.out.println("Press any key to exit.");

		DataInputStream in = new DataInputStream(System.in);
		try {
			in.readByte();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}		

}
