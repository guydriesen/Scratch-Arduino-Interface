package sai.ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class S2eFileFilter extends FileFilter {

	public static final String FILE_EXTENSION = ".s2e";
	public static final String DESCRIPTION = "Scratch 2.0 Extension";

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public boolean accept(File f) {
		return f.isDirectory() || f.getName().endsWith(FILE_EXTENSION);
	}

}