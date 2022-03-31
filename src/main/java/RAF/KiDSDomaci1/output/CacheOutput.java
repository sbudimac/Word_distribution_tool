package RAF.KiDSDomaci1.output;

import RAF.KiDSDomaci1.model.CrunchedFile;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class CacheOutput implements Runnable {
    private BlockingQueue<CrunchedFile> inputContent;
    private ConcurrentHashMap<String, Future<Map<String, Long>>> outputResult;
    private ObservableList<String> resultsList;

    public CacheOutput(ObservableList<String> resultsList) {
        this.inputContent = new LinkedBlockingQueue<>();
        this.outputResult = new ConcurrentHashMap<>();
        this.resultsList = resultsList;
    }

    @Override
    public void run() {

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
