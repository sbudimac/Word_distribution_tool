package RAF.KiDSDomaci1.cruncher;

import RAF.KiDSDomaci1.model.CrunchedFile;
import RAF.KiDSDomaci1.output.CacheOutput;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CruncherNotifier implements Runnable {
    private CacheOutput cacheOutput;
    private CrunchedFile crunchedFile;
    private ObservableList<String> statusList;
    private boolean isPresent;

    public CruncherNotifier(CacheOutput cacheOutput, CrunchedFile crunchedFile, ObservableList<String> statusList, boolean isPresent) {
        this.cacheOutput = cacheOutput;
        this.crunchedFile = crunchedFile;
        this.statusList = statusList;
        this.isPresent = isPresent;
    }

    @Override
    public void run() {
        try {
            crunchedFile.getRepetitions().get();
            if (isPresent) {
                cacheOutput.getInputContent().put(crunchedFile);
            } else {
                Platform.runLater(() -> {
                    cacheOutput.getResultsList().set(cacheOutput.getResultsList().indexOf(crunchedFile.getName() + "*"), crunchedFile.getName());
                });
            }
            System.out.println("Crunched file: " + crunchedFile.getName());
            Platform.runLater(() -> statusList.remove(crunchedFile.getName()));
        } catch (InterruptedException e) {
            Platform.runLater(() -> {
                statusList.clear();
                cacheOutput.getResultsList().clear();
            });
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
