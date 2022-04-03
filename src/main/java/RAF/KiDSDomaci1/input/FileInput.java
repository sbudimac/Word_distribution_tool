package RAF.KiDSDomaci1.input;

import RAF.KiDSDomaci1.app.Config;
import RAF.KiDSDomaci1.model.Cruncher;
import RAF.KiDSDomaci1.model.Disk;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileInput implements Runnable {
	private Disk disk;
	private String name;

	private BlockingQueue<String> files;
	private CopyOnWriteArrayList<Cruncher> crunchers;
	private CopyOnWriteArrayList<String> directoryPaths;

	private ConcurrentHashMap<String, Long> lastModifiedMap;

	private AtomicBoolean paused;
	private AtomicBoolean stopped;
	private final Object pauseLock;
	private final Object stopLock;

	private Scheduler scheduler;
	private Thread schedulerThread;

	private volatile boolean working;
	private final int sleepTime;

	public FileInput(Disk disk) {
		this.name = "0";
		this.disk = disk;
		this.files = new LinkedBlockingQueue<>();
		this.crunchers = new CopyOnWriteArrayList<>();
		this.directoryPaths = new CopyOnWriteArrayList<>();
		this.lastModifiedMap = new ConcurrentHashMap<>();
		this.paused = new AtomicBoolean(true);
		this.stopped = new AtomicBoolean(false);
		this.pauseLock = new Object();
		this.stopLock = new Object();
		this.scheduler = new Scheduler(this.files, this.crunchers, this.stopped, this.stopLock);
		this.working = true;
		this.sleepTime = Integer.parseInt(Config.getProperty("file_input_sleep_time"));
	}
	
	public Disk getDisk() {
		return disk;
	}
	public String getName() {
		return name;
	}

	@Override
	public void run() {
		schedulerThread = new Thread(scheduler);
		schedulerThread.start();
		while (working) {
			synchronized (this.pauseLock) {
				if (paused.get()) {
					try {
						this.pauseLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
					if (!working) {
						break;
					}
				}
			}
			System.out.println("Scannig...");
			for (String directoryPath : directoryPaths) {
				File directory = new File(directoryPath);
				try {
					readDirectory(directory);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
			System.out.println("Scan over.");
			synchronized (this.pauseLock) {
				if (!paused.get()) {
					try {
						this.pauseLock.wait(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}
	}

	private void readDirectory(File directory) throws InterruptedException {
		for (File file : Objects.requireNonNull(directory.listFiles())) {
			if (file.isDirectory()) {
				readDirectory(file);
			} else {
				if (lastModifiedMap.containsKey(file.getAbsolutePath())) {
					if (lastModifiedMap.get(file.getAbsolutePath()) < file.lastModified()) {
						scheduler.getFiles().put(file.getAbsolutePath());
						lastModifiedMap.put(file.getAbsolutePath(), file.lastModified());
					}
				} else {
					scheduler.getFiles().put(file.getAbsolutePath());
					lastModifiedMap.put(file.getAbsolutePath(), file.lastModified());
				}
			}
		}
	}

	public void interrupt() {
		working = false;
	}

	public void stop() {
		try {
			working = false;
			stopped.compareAndSet(false, true);
			synchronized (stopLock) {
				scheduler.getFiles().put("\\");
				this.stopLock.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return name;
	}

	public AtomicBoolean getPaused() {
		return paused;
	}

	public Object getPauseLock() {
		return pauseLock;
	}

	public CopyOnWriteArrayList<String> getDirectoryPaths() {
		return directoryPaths;
	}

	public CopyOnWriteArrayList<Cruncher> getCrunchers() {
		return crunchers;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public Thread getSchedulerThread() {
		return schedulerThread;
	}
}
