package RAF.KiDSDomaci1.cruncher;

import RAF.KiDSDomaci1.app.Config;
import RAF.KiDSDomaci1.model.Cruncher;
import RAF.KiDSDomaci1.model.FileContent;
import RAF.KiDSDomaci1.model.SlashConverter;
import RAF.KiDSDomaci1.view.MainView;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.Map;
import java.util.concurrent.Future;

public class CounterCruncher extends Task {

    private Cruncher cruncher;
    private ObservableList<String> statusList;

    public CounterCruncher(Cruncher cruncher, ObservableList<String> statusList) {
        this.cruncher = cruncher;
        this.statusList = statusList;
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
                        new CruncherWorker(contentToCrunch.getContent(), cruncher.getArity())
                );
                System.out.println("Crunching...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Object call() {
        return null;
    }
}
