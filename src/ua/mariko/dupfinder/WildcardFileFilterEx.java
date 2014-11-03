package ua.mariko.dupfinder;

import java.io.File;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class WildcardFileFilterEx extends WildcardFileFilter{

	public WildcardFileFilterEx(String wildcard, IOCase caseSensitivity) {
		super(wildcard, caseSensitivity);

	}
	
	@Override
	public boolean accept(File pathname){
		if(pathname.isDirectory()){
			return true;
		}
		return super.accept(pathname);
	}

}
