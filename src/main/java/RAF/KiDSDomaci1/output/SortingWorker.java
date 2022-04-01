package RAF.KiDSDomaci1.output;

import RAF.KiDSDomaci1.app.Config;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

import java.util.*;

public class SortingWorker extends Task {
    private Map<String, Long> result;
    private LineChart<Number, Number> lineChart;
    private ProgressBar progressBar;
    private Label pbLabel;
    private Pane right;
    private static int compareCount = 0;
    private int sortProgressLimit;

    public SortingWorker(Map<String, Long> result, LineChart<Number, Number> lineChart, ProgressBar progressBar, Label pbLabel, Pane right) {
        this.result = result;
        this.lineChart = lineChart;
        this.progressBar = progressBar;
        this.pbLabel = pbLabel;
        this.right = right;
        this.sortProgressLimit = Integer.parseInt(Config.getProperty("sort_progress_limit"));
    }

    @Override
    protected Object call() throws Exception {
        Set<Map.Entry<String, Long>> resultSet = result.entrySet();
        Comparator<Map.Entry<String, Long>> comparator = (o1, o2) -> {
            compareCount++;
            if (compareCount % sortProgressLimit == 0) {
                Platform.runLater(() -> progressBar.setProgress((double)compareCount / (double)result.size()*(int)Math.log10(result.size() / Math.log10(2))));
            }
            Long val1 = o1.getValue();
            Long val2 = o2.getValue();
            return val2.compareTo(val1);
        };
        List<Map.Entry<String, Long>> resultList = new ArrayList<>(resultSet);
        resultList.sort(comparator);
        LinkedHashMap<String, Long> resultSorted = new LinkedHashMap<>(resultList.size());
        for (Map.Entry<String, Long> resultElement : resultList) {
            resultSorted.put(resultElement.getKey(), resultElement.getValue());
        }
        XYChart.Series<Number, Number> chart = new XYChart.Series<>();
        int i = 0;
        for (Map.Entry<String, Long> entry : resultSorted.entrySet()) {
            if (i >= 100) {
                break;
            }
            i++;
            chart.getData().add(new XYChart.Data<>(i, entry.getValue()));
        }
        Platform.runLater(() -> {
            lineChart.getData().clear();
            lineChart.getData().addAll(chart);
            right.getChildren().remove(progressBar);
            right.getChildren().remove(pbLabel);
        });
        return null;
    }
}
