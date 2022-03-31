package RAF.KiDSDomaci1.output;

import RAF.KiDSDomaci1.model.CrunchedFile;
import RAF.KiDSDomaci1.model.Cruncher;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.concurrent.*;

public class CacheOutput implements Runnable {
    private BlockingQueue<CrunchedFile> inputContent;
    private ConcurrentHashMap<String, Future<Map<String, Long>>> outputResult;
    private CopyOnWriteArrayList<Cruncher> crunchers;
    private ObservableList<String> resultsList;
    private final Object stopLock;

    public CacheOutput(ObservableList<String> resultsList) {
        this.inputContent = new LinkedBlockingQueue<>();
        this.outputResult = new ConcurrentHashMap<>();
        this.crunchers = new CopyOnWriteArrayList<>();
        this.resultsList = resultsList;
        this.stopLock = new Object();
    }

    @Override
    public void run() {
        while (true) {
            try {
                CrunchedFile file = inputContent.take();
                if (file.getName().equals("\\")) {
                    break;
                }
                outputResult.put(file.getName(), file.getRepetitions());
                System.out.println("Output file arrived: " + file.getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> resultsList.clear());
                return;
            }
        }
        synchronized (stopLock) {
            stopLock.notifyAll();
        }
    }

    public void stop() {
        synchronized (stopLock) {
            try {
                inputContent.put(new CrunchedFile("\\", null));
                stopLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public BlockingQueue<CrunchedFile> getInputContent() {
        return inputContent;
    }

    public ConcurrentHashMap<String, Future<Map<String, Long>>> getOutputResult() {
        return outputResult;
    }

    public ObservableList<String> getResultsList() {
        return resultsList;
    }
}
