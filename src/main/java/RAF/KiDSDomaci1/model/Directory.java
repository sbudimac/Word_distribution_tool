package RAF.KiDSDomaci1.model;

import java.io.File;

public class Directory {
	public File directory;
	public Directory(File directory) {
		this.directory = directory;
	}
	
	@Override
	public String toString() {
		return directory.getPath();
	}
}
