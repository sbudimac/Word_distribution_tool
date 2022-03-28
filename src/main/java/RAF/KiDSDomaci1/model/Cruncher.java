package RAF.KiDSDomaci1.model;

public class Cruncher {
	
	private int arity;
	private String name;
	
	public Cruncher(int arity) {
		this.arity = arity;
		this.name = "Counter 0";
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public int getArity() {
		return arity;
	}
	
}
