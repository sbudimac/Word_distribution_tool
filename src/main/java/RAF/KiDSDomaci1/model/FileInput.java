package RAF.KiDSDomaci1.model;

public class FileInput {
	private Disk disk;
	private String name;
	
	public FileInput(Disk disk) {
		this.name = "0";
		this.disk = disk;
	}
	
	public Disk getDisk() {
		return disk;
	}
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
