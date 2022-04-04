package RAF.KiDSDomaci1.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Cruncher {
	private int arity;
	private String name;
	private static int number = 0;

	private BlockingQueue<FileContent> inputContent;
	
	public Cruncher(int arity) {
		this.arity = arity;
		this.name = "Counter " + number;
		number++;
		this.inputContent = new LinkedBlockingQueue<>();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public int getArity() {
		return arity;
	}

	public BlockingQueue<FileContent> getInputContent() {
		return inputContent;
	}
}
