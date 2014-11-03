package ua.mariko.dupfinder;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.filefilter.WildcardFileFilter;

public class Config {

	public File root;
	public FileFilter filter;
	public boolean deleteDuplicated;
	public boolean byName;
	public boolean silent;
	
	public static Config readParameters(String[] args){
		
		Config config = new Config();
		
		for (int i = 0; i < args.length; i++) {

			String s = args[i];

			if (!s.trim().startsWith("-")) {

				config.root = new File(args[i].replace("\'",""));

			} else if (s.equals("-ds")) {

				config.deleteDuplicated = true;
				config.silent = true;
				
			} else if (s.equals("-d")) {

				config.deleteDuplicated = true;
			
			} else if (s.equals("-m")) {

				++i;
				config.filter = new WildcardFileFilter(args[i].replace("\'",""));
				
			} else if (s.equals("-n")) {

				++i;
				config.byName = true;
			} 

		}
		
		return config;
	}
}
