package RAF.KiDSDomaci1.output;

import RAF.KiDSDomaci1.model.CrunchedFile;
import RAF.KiDSDomaci1.view.MainView;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.concurrent.*;

public class CacheOutput implements Runnable {
    private BlockingQueue<CrunchedFile> inputContent;
    private ConcurrentHashMap<String, Future<Map<String, Long>>> outputResult;
    private ObservableList<String> resultsList;
    private ExecutorService sortingPool;
    private final Object stopLock;

    public CacheOutput(ObservableList<String> resultsList) {
        this.inputContent = new LinkedBlockingQueue<>();
        this.outputResult = new ConcurrentHashMap<>();
        this.resultsList = resultsList;
        this.sortingPool = Executors.newCachedThreadPool();
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

    public Map<String, Long> getResultsIfPresent(String fileName) {
        if (outputResult.get(fileName).isDone()) {
            try {
                return outputResult.get(fileName).get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Map<String, Long> getResultsForName(String fileName) {
        try {
            return outputResult.get(fileName).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sum(String name, SumWorker sumWorker) {
        outputResult.put(name, MainView.outputThreadPool.submit(sumWorker));
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

    public ExecutorService getSortingPool() {
        return sortingPool;
    }
}
