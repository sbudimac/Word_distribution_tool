package RAF.KiDSDomaci1.model;

import java.io.File;

public class Disk {
	
	private File directory;
	
	public Disk(File directory) {
		this.directory = directory;
	}
	
	public File getDirectory() {
		return directory;
	}
	
	@Override
	public String toString() {
		return directory.toPath().toString();
	}
}

