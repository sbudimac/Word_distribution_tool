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
import java.util.concurrent.Future;

public class CounterCruncher extends Task {

    private Cruncher cruncher;
    private ObservableList<String> statusList;
    private CopyOnWriteArrayList<FileInput> inputComponents;
    private CopyOnWriteArrayList<CacheOutput> outputComponents;

    public CounterCruncher(Cruncher cruncher, ObservableList<String> statusList) {
        this.cruncher = cruncher;
        this.statusList = statusList;
        this.inputComponents = new CopyOnWriteArrayList<>();
        this.outputComponents = new CopyOnWriteArrayList<>();
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
                    CrunchedFile crunchedFile = new CrunchedFile(contentName + "-arity: " + cruncher.getArity(), crunchingResult);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Object call() {
        return null;
    }

    public CopyOnWriteArrayList<FileInput> getInputComponents() {
        return inputComponents;
    }

    public CopyOnWriteArrayList<CacheOutput> getOutputComponents() {
        return outputComponents;
    }
}
