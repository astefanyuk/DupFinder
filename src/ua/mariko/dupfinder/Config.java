package ua.mariko.dupfinder;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOCase;

public class Config {

	public List<File> files = new ArrayList<>();
	public WildcardFileFilterEx filter;
	public boolean deleteDuplicated;
	public boolean byName;
	public boolean silent;
	public Sort searchSmallFirst = Sort.None;
	
	public static enum Sort{
		None,
		Small,
		Large
	}
	
	public static Config readParameters(String[] args){
		
		Config config = new Config();
		
		for (int i = 0; i < args.length; i++) {

			String s = args[i];

			if (!s.trim().startsWith("-")) {

				config.files.add(new File(args[i].replace("\'","")));

			} else if (s.equals("-ds")) {

				config.deleteDuplicated = true;
				config.silent = true;
				
			} else if (s.equals("-d")) {

				config.deleteDuplicated = true;
			
			} else if (s.equals("-m")) {

				++i;
				config.filter = new WildcardFileFilterEx(args[i].replace("\'",""), IOCase.INSENSITIVE);
				
			} else if (s.equals("-n")) {

				++i;
				config.byName = true;
			} else if (s.equals("-az")) {

				++i;
				config.searchSmallFirst = Sort.Small;
			} else if (s.equals("-za")) {

				++i;
				config.searchSmallFirst = Sort.Large;
			}

		}
		
		return config;
	}
}
