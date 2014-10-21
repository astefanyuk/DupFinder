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

public class DupFinderMain {
	
	public static void main(String[] args) {
		
		DupFinder dupFinder = new DupFinder();
		
		dupFinder.config = Config.readParameters(args);
		
		System.out.println("DupFinder is a program to find duplicated files in the directory specified");
		
		if (dupFinder.config.root == null) {
			
			System.out.println("Usage: DupFinder [options] DIRECTORY");
			System.out.println(" -d                                   delete duplicated files");
			System.out.println(" -m                                   filter mask");

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
