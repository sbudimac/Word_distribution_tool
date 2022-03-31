package RAF.KiDSDomaci1.cruncher;

import RAF.KiDSDomaci1.input.FileInput;
import RAF.KiDSDomaci1.model.CrunchedFile;
import RAF.KiDSDomaci1.model.Cruncher;
import RAF.KiDSDomaci1.model.FileContent;
import RAF.KiDSDomaci1.model.SlashConverter;
import RAF.KiDSDomaci1.output.CacheOutput;
import RAF.KiDSDomaci1.view.MainView;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CounterCruncher extends Task {

    private Cruncher cruncher;
    private ObservableList<String> statusList;
    private CopyOnWriteArrayList<FileInput> inputComponents;
    private CopyOnWriteArrayList<CacheOutput> outputComponents;
    private ExecutorService notifierThreadPool;
    private final Object stopLock;

    public CounterCruncher(Cruncher cruncher, ObservableList<String> statusList) {
        this.cruncher = cruncher;
        this.statusList = statusList;
        this.inputComponents = new CopyOnWriteArrayList<>();
        this.outputComponents = new CopyOnWriteArrayList<>();
        this.notifierThreadPool = Executors.newCachedThreadPool();
        this.stopLock = new Object();
    }

    @Override
    public void run() {
        while (true) {
            try {
                FileContent contentToCrunch = cruncher.getInputContent().take();
                if (contentToCrunch.getName().equals("\\")) {
                    break;
                }
                String contentName = SlashConverter.currentFileName(contentToCrunch.getName());
                Platform.runLater(() -> statusList.add(contentName));
                Future<Map<String, Long>> crunchingResult = MainView.cruncherThreadPool.submit(
                        new CruncherWorker(contentToCrunch.getContent(), cruncher.getArity(), 0, contentToCrunch.getContent().length(), false)
                );
                System.out.println("Crunching...");
                for (CacheOutput outputComponent : outputComponents) {
                    String outputName = contentName + "-arity" + cruncher.getArity();
                    boolean isPresent = false;
                    CrunchedFile crunchedFile = new CrunchedFile(outputName, crunchingResult);
                    if (!outputComponent.getOutputResult().containsKey(contentName)) {
                        outputComponent.getInputContent().put(crunchedFile);
                        Platform.runLater(() -> outputComponent.getResultsList().add(SlashConverter.currentFileName(contentName) + "-arity" + cruncher.getArity() + "*"));
                    } else {
                        isPresent = true;
                    }
                    notifierThreadPool.submit(new CruncherNotifier(outputComponent, crunchedFile, statusList, contentName, isPresent));
                }
            } catch (InterruptedException e) {
                Platform.runLater(() -> statusList.clear());
                e.printStackTrace();
                return;
            }
        }
        synchronized (stopLock) {
            stopLock.notifyAll();
        }
    }

    @Override
    protected Object call() {
        return null;
    }

    public void stop() {
        synchronized (stopLock) {
            try {
                cruncher.getInputContent().put(new FileContent("\\", ""));
                stopLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public CopyOnWriteArrayList<FileInput> getInputComponents() {
        return inputComponents;
    }

    public CopyOnWriteArrayList<CacheOutput> getOutputComponents() {
        return outputComponents;
    }
}
