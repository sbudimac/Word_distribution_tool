package RAF.KiDSDomaci1.input;

import RAF.KiDSDomaci1.model.Cruncher;
import RAF.KiDSDomaci1.model.FileContent;
import RAF.KiDSDomaci1.view.MainView;
import javafx.concurrent.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scheduler extends Task<String> {
    private BlockingQueue<String> files;
    private CopyOnWriteArrayList<Cruncher> crunchers;
    private AtomicBoolean stopped;
    private final Object stopLock;
    private String currentFile;

    public Scheduler(BlockingQueue<String> files, CopyOnWriteArrayList<Cruncher> crunchers, AtomicBoolean stopped, Object stopLock) {
        this.files = files;
        this.crunchers = crunchers;
        this.stopped = stopped;
        this.stopLock = stopLock;
        this.currentFile = "Idle";
    }

    @Override
    public void run() {
        updateMessage(currentFile);
        while (true) {
            try {
                currentFile = files.take();
                if (currentFile.equals("\\")) {
                    break;
                }
                updateMessage(currentFileName(currentFile));
                Future<String> fileToRead = MainView.inputThreadPool.submit(new InputReader(currentFile));
                String readFile = fileToRead.get();
                if (readFile == null) {
                    updateMessage("Idle");
                    break;
                }
                if (!stopped.get()) {
                    FileContent content;
                    for (Cruncher cruncher : crunchers) {
                        content = new FileContent(currentFile, readFile);
                        cruncher.getInputContent().put(content);
                    }
                }
                currentFile = "Idle";
                updateMessage(currentFile);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        synchronized (stopLock) {
            stopLock.notifyAll();
        }
    }

    private String currentFileName(String filePath) {
        String regexPath = filePath.replace("\\", "/");
        return regexPath.split("/")[regexPath.split("/").length - 1];
    }

    @Override
    protected String call() {
        return null;
    }

    public BlockingQueue<String> getFiles() {
        return files;
    }
}
