package RAF.KiDSDomaci1.input;

import RAF.KiDSDomaci1.model.Cruncher;
import RAF.KiDSDomaci1.model.FileContent;
import RAF.KiDSDomaci1.model.SlashConverter;
import RAF.KiDSDomaci1.view.MainView;
import javafx.concurrent.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scheduler extends Task<String> {
    private MainView mainView;
    private BlockingQueue<String> files;
    private CopyOnWriteArrayList<Cruncher> crunchers;
    private AtomicBoolean stopped;
    private final Object stopLock;
    private String currentFile;

    public Scheduler(MainView mainView, BlockingQueue<String> files, CopyOnWriteArrayList<Cruncher> crunchers, AtomicBoolean stopped, Object stopLock) {
        this.mainView = mainView;
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
                updateMessage(SlashConverter.currentFileName(currentFile));
                Future<String> fileToRead = MainView.inputThreadPool.submit(new InputReader(mainView, currentFile));
                if (fileToRead.get() == null) {
                    updateMessage("Idle");
                    break;
                }
                if (!stopped.get()) {
                    for (Cruncher cruncher : crunchers) {
                        FileContent content = new FileContent(currentFile, fileToRead.get());
                        cruncher.getInputContent().put(content);
                    }
                }
                updateMessage("Idle");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                updateMessage("Idle");
                return;
            }
        }
        synchronized (stopLock) {
            stopLock.notifyAll();
        }
    }

    @Override
    protected String call() {
        return null;
    }

    public BlockingQueue<String> getFiles() {
        return files;
    }
}
