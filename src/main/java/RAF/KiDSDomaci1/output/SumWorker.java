package RAF.KiDSDomaci1.output;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class SumWorker implements Callable<Map<String, Long>> {
    private List<String> toSum;
    private CacheOutput cacheOutput;
    private Pane right;
    private ProgressBar progressBar;
    private Label pbLabel;
    private String sumName;
    private ObservableList<String> resultsList;
    private static int progress;

    public SumWorker(List<String> toSum, CacheOutput cacheOutput, Pane right, ProgressBar progressBar, Label pbLabel, String sumName, ObservableList<String> resultsList) {
        this.toSum = toSum;
        this.cacheOutput = cacheOutput;
        this.right = right;
        this.progressBar = progressBar;
        this.pbLabel = pbLabel;
        this.sumName = sumName;
        this.resultsList = resultsList;
    }

    @Override
    public Map<String, Long> call() throws Exception {
        System.out.println("WOOOOO1");
        Map<String, Long> sumResult = new HashMap<>();
        List<Map<String, Long>> results = new ArrayList<>();
        for (String name : toSum) {
            results.add(cacheOutput.getResultsForName(name));
        }
        System.out.println("WOOOOOO2");
        for (Map<String, Long> result : results) {
            for (Map.Entry<String, Long> res : result.entrySet()) {
                sumResult.merge(res.getKey(), res.getValue(), Long::sum);
            }
            progress++;
            Platform.runLater(() -> progressBar.setProgress((double) progress / (double) results.size()));
        }
        System.out.println("WOOOOOOOO3");
        Platform.runLater(() -> {
            right.getChildren().remove(progressBar);
            right.getChildren().remove(pbLabel);
            resultsList.set(resultsList.indexOf(sumName + "*"), sumName);
        });
        return sumResult;
    }
}
