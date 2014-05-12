package iop.storet.marshaler.impl;
import java.io.File;
import java.io.FileFilter;

public class CDAFilter implements FileFilter {
	public boolean accept(File pathname) {
		return (pathname.getName().endsWith("xml") || pathname.isDirectory());
	}
}
