package RAF.KiDSDomaci1.view;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import RAF.KiDSDomaci1.app.Config;
import RAF.KiDSDomaci1.model.Cruncher;
import RAF.KiDSDomaci1.model.Disk;
import RAF.KiDSDomaci1.input.FileInput;
import RAF.KiDSDomaci1.model.SlashConverter;
import RAF.KiDSDomaci1.output.CacheOutput;
import RAF.KiDSDomaci1.output.SortingWorker;
import RAF.KiDSDomaci1.output.SumWorker;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainView {
	private Stage stage;
	private ComboBox<Disk> disks;
	private HBox left;
	private VBox fileInput, cruncher;
	private Pane center, right;
	private ListView<String> results;
	private Button addFileInput, singleResult, sumResult;
	private ArrayList<FileInputView> fileInputViews;
	private ArrayList<CruncherView> cruncherViews;
	private LineChart<Number, Number> lineChart;
	private ArrayList<Cruncher> availableCrunchers;
	private ObservableList<String> resultsList;
	private CacheOutput cacheOutput;

	private Button addCruncher;

	public static ExecutorService inputThreadPool;
	public static ForkJoinPool cruncherThreadPool;
	public static ExecutorService outputThreadPool;

	public void initMainView(BorderPane borderPane, Stage stage) {
		this.stage = stage;
		stage.setOnCloseRequest((event) -> shutDown());

		fileInputViews = new ArrayList<>();
		cruncherViews = new ArrayList<>();
		availableCrunchers = new ArrayList<>();

		left = new HBox();

		borderPane.setLeft(left);

		initFileInput();

		initCruncher();

		initCenter(borderPane);

		initRight(borderPane);
	}

	private void initFileInput() {
		fileInput = new VBox();

		fileInput.getChildren().add(new Text("File inputs:"));
		VBox.setMargin(fileInput.getChildren().get(0), new Insets(0, 0, 10, 0));

		disks = new ComboBox<>();
		disks.getSelectionModel().selectedItemProperty().addListener(e -> updateEnableAddFileInput());
		disks.setMinWidth(120);
		disks.setMaxWidth(120);
		fileInput.getChildren().add(disks);

		addFileInput = new Button("Add FileInput");
		addFileInput.setOnAction(e -> addFileInput(new FileInput(this, disks.getSelectionModel().getSelectedItem())));
		VBox.setMargin(addFileInput, new Insets(5, 0, 10, 0));
		addFileInput.setMinWidth(120);
		addFileInput.setMaxWidth(120);
		fileInput.getChildren().add(addFileInput);

		int width = 210;

		VBox divider = new VBox();
		divider.getStyleClass().add("divider");
		divider.setMinWidth(width);
		divider.setMaxWidth(width);
		fileInput.getChildren().add(divider);
		VBox.setMargin(divider, new Insets(0, 0, 15, 0));

		Insets insets = new Insets(10);
		ScrollPane scrollPane = new ScrollPane(fileInput);
		scrollPane.setMinWidth(width + 35);
		fileInput.setPadding(insets);
		fileInput.getChildren().add(scrollPane);

		left.getChildren().add(scrollPane);

		
		try {
			String[] disksArray = Config.getProperty("disks").split(";");
			for (String disk : disksArray) {
				File file = new File(disk);
				if(!file.exists() || !file.isDirectory()) {
					throw new Exception("Bad directory path");
				}
				disks.getItems().add(new Disk(file));
			}
			if (disksArray.length > 0) {
				inputThreadPool = Executors.newFixedThreadPool(disksArray.length);
				disks.getSelectionModel().select(0);
			}
		} catch (Exception e) {
			Platform.runLater(() -> {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Closing");
				alert.setHeaderText("Bad config disks");
				alert.setContentText(null);

				alert.showAndWait();
				System.exit(0);
			});
		}

		updateEnableAddFileInput();
	}

	private void initCruncher() {
		cruncherThreadPool = ForkJoinPool.commonPool();

		cruncher = new VBox();
		cruncherViews = new ArrayList<>();

		Text text = new Text("Crunchers:");
		cruncher.getChildren().add(text);
		VBox.setMargin(text, new Insets(0, 0, 5, 0));

		addCruncher = new Button("Add cruncher");
		addCruncher.setOnAction(e -> addCruncher());
		cruncher.getChildren().add(addCruncher);
		VBox.setMargin(addCruncher, new Insets(0, 0, 15, 0));

		int width = 110;

		Insets insets = new Insets(10);
		ScrollPane scrollPane = new ScrollPane(cruncher);
		scrollPane.setMinWidth(width + 35);
		cruncher.setPadding(insets);
		left.getChildren().add(scrollPane);
	}

	private void initCenter(BorderPane borderPane) {
		center = new HBox();

		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("Bag of words");
		yAxis.setLabel("Frequency");
		lineChart = new LineChart<>(xAxis, yAxis);
		lineChart.setMinWidth(700);
		lineChart.setMinHeight(600);
		center.getChildren().add(lineChart);

		borderPane.setCenter(center);
	}

	private void initRight(BorderPane borderPane) {
		outputThreadPool = Executors.newCachedThreadPool();

		this.resultsList = FXCollections.observableArrayList();
		cacheOutput = new CacheOutput(resultsList);
		Thread cacheOutputThread = new Thread(cacheOutput);
		cacheOutputThread.start();

		right = new VBox();
		right.setPadding(new Insets(10));
		right.setMaxWidth(200);

		results = new ListView<>(resultsList);
		right.getChildren().add(results);
		VBox.setMargin(results, new Insets(0, 0, 10, 0));
		results.getSelectionModel().selectedItemProperty().addListener(e -> updateResultButtons());
		results.getSelectionModel().selectedIndexProperty().addListener(e -> updateResultButtons());
		results.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		singleResult = new Button("Single result");
		singleResult.setOnAction(e -> getSingleResult());
		singleResult.setDisable(true);
		right.getChildren().add(singleResult);
		VBox.setMargin(singleResult, new Insets(0, 0, 5, 0));

		sumResult = new Button("Sum results");
		sumResult.setDisable(true);
		sumResult.setOnAction(e -> sumResults());
		right.getChildren().add(sumResult);
		VBox.setMargin(sumResult, new Insets(0, 0, 10, 0));

		borderPane.setRight(right);
	}

	public void updateEnableAddFileInput() {
		Disk disk = disks.getSelectionModel().getSelectedItem();
		if (disk != null) {
			for (FileInputView fileInputView : fileInputViews) {
				if (fileInputView.getFileInput().getDisk() == disk) {
					addFileInput.setDisable(true);
					return;
				}
			}
			addFileInput.setDisable(false);
		} else {
			addFileInput.setDisable(true);
		}
	}

	public void updateResultButtons() {
		if (results.getSelectionModel().getSelectedItems() == null
				|| results.getSelectionModel().getSelectedItems().size() == 0) {
			singleResult.setDisable(true);
			sumResult.setDisable(true);
		} else if (results.getSelectionModel().getSelectedItems().size() == 1) {
			singleResult.setDisable(false);
			sumResult.setDisable(true);
		} else {
			singleResult.setDisable(true);
			sumResult.setDisable(false);
		}
	}

	private void getSingleResult() {
		String chosenFile = results.getSelectionModel().getSelectedItem();
		String labelName = chosenFile;
		if (!chosenFile.endsWith("*")) {
			for (String fileName : cacheOutput.getOutputResult().keySet()) {
				if (fileName.endsWith(chosenFile)) {
					chosenFile = fileName;
					break;
				}
			}
			Map<String, Long> result = cacheOutput.getResultsIfPresent(chosenFile);
			if (result != null) {
				ProgressBar progressBar = new ProgressBar();
				Label pbLabel = new Label(labelName);
				right.getChildren().add(progressBar);
				right.getChildren().add(pbLabel);
				cacheOutput.getSortingPool().submit(new SortingWorker(result, lineChart, progressBar, pbLabel, right));
			} else {
				errorPopout("Result not ready");
			}
		} else {
			errorPopout("Result not ready");
		}
	}

	private void sumResults() {
		TextInputDialog dialog = new TextInputDialog("sum");
		dialog.setTitle("Sum results");
		dialog.setHeaderText("Enter sum name");
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(s -> {
			commitSumResult(result.get());
		});
	}

	private void commitSumResult(String result) {
		if (resultsList.contains(result) || resultsList.contains(result + "*")) {
			errorPopout("Sum name is reserved");
			return;
		}
		ProgressBar progressBar = new ProgressBar();
		Label pbLabel = new Label(result);
		right.getChildren().add(progressBar);
		right.getChildren().add(pbLabel);
		resultsList.add(result + "*");
		List<String> toSum = new ArrayList<>();
		for (String s : results.getSelectionModel().getSelectedItems()) {
			for (String resultName : cacheOutput.getOutputResult().keySet()) {
				if (s.endsWith("*")) {
					if (s.substring(0, s.length() - 1).equals(SlashConverter.currentFileName(resultName))) {
						toSum.add(resultName);
					}
				} else {
					if (s.equals(SlashConverter.currentFileName(resultName))) {
						toSum.add(resultName);
					}
				}
			}
		}
		SumWorker sumWorker = new SumWorker(this, toSum, cacheOutput, right, progressBar, pbLabel, result, resultsList);
		cacheOutput.sum(result, sumWorker);
	}

	private void errorPopout(String message) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Error");
		alert.setHeaderText(message);
		alert.setContentText(null);
		alert.showAndWait();
	}

	public void addFileInput(FileInput fileInput) {
		FileInputView fileInputView = new FileInputView(fileInput, this);
		this.fileInput.getChildren().add(fileInputView.getFileInputView());
		VBox.setMargin(fileInputView.getFileInputView(), new Insets(0, 0, 30, 0));
		fileInputView.getFileInputView().getStyleClass().add("file-input");
		fileInputViews.add(fileInputView);
		if (availableCrunchers != null) {
			fileInputView.updateAvailableCrunchers(availableCrunchers);
		}
		updateEnableAddFileInput();
	}

	public void removeFileInputView(FileInputView fileInputView) {
		fileInput.getChildren().remove(fileInputView.getFileInputView());
		fileInputViews.remove(fileInputView);
		updateEnableAddFileInput();
	}

	public void updateCrunchers(ArrayList<Cruncher> crunchers) {
		for (FileInputView fileInputView : fileInputViews) {
			fileInputView.updateAvailableCrunchers(crunchers);
		}
		this.availableCrunchers = crunchers;
	}

	public Stage getStage() {
		return stage;
	}

	private void addCruncher() {
		TextInputDialog dialog = new TextInputDialog("1");
		dialog.setTitle("Add cruncher");
		dialog.setHeaderText("Enter cruncher arity");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(res -> {
			try {
				int arity = Integer.parseInt(res);
				for (Cruncher cruncher : availableCrunchers) {
					if (cruncher.getArity() == arity) {
						Alert alert = new Alert(AlertType.WARNING);
						alert.setTitle("Error");
						alert.setHeaderText("Cruncher with this arity already exists.");
						alert.setContentText(null);
						alert.showAndWait();
						return;
					}
				}
				Cruncher cruncher = new Cruncher(arity);
				CruncherView cruncherView = new CruncherView(this, cruncher, cacheOutput);
				this.cruncher.getChildren().add(cruncherView.getCruncherView());
				availableCrunchers.add(cruncher);
				updateCrunchers(availableCrunchers);
			} catch (NumberFormatException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Wrong input");
				alert.setHeaderText("Arity must be a number");
				alert.showAndWait();
			}
		});
	}

	public void stopApp() {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Error");
			alert.setHeaderText("Out of memory, application stopping");
			alert.setContentText(null);
			alert.setOnCloseRequest(dialogEvent -> {
				Platform.exit();
				System.exit(0);
			});
			inputThreadPool.shutdownNow();
			cruncherThreadPool.shutdownNow();
			outputThreadPool.shutdownNow();
			for (CruncherView cruncherView : cruncherViews) {
				cruncherView.getCounterCruncher().getNotifierThreadPool().shutdownNow();
			}
			for (FileInputView fileInputView : fileInputViews) {
				fileInputView.getFileInput().getSchedulerThread().interrupt();
				fileInputView.getFileInput().interrupt();
			}
			cacheOutput.getSortingPool().shutdownNow();
			alert.showAndWait();
		});
	}

	public void shutDown() {
		Stage stage = new Stage();
		stage.setTitle("Stopping everything");
		Thread shutDownThread = new Thread(() -> {
			for (FileInputView fileInputView : fileInputViews) {
				fileInputView.getFileInput().stop();
			}
			for (CruncherView cruncherView : cruncherViews) {
				cruncherView.getCounterCruncher().stop();
				cruncherView.getCounterCruncher().getNotifierThreadPool().shutdown();
			}
			cacheOutput.stop();
			cacheOutput.getSortingPool().shutdown();
			MainView.inputThreadPool.shutdown();
			MainView.cruncherThreadPool.shutdown();
			MainView.outputThreadPool.shutdown();
			MainView.cruncherThreadPool.awaitQuiescence(10, TimeUnit.MINUTES);
			try {
				MainView.inputThreadPool.awaitTermination(10, TimeUnit.MINUTES);
				MainView.outputThreadPool.awaitTermination(10, TimeUnit.MINUTES);
				for (CruncherView cruncherView : cruncherViews) {
					cruncherView.getCounterCruncher().getNotifierThreadPool().awaitTermination(10, TimeUnit.MINUTES);
				}
				cacheOutput.getSortingPool().awaitTermination(10, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Stopping everything...");
			Platform.runLater(stage::close);
			System.exit(0);
		});
		VBox vBox = new VBox();
		stage.setScene(new Scene(vBox, 300, 300));
		shutDownThread.start();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.showAndWait();
	}

	public void removeCruncher(CruncherView cruncherView) {
		for (FileInputView fileInputView : fileInputViews) {
			fileInputView.removeLinkedCruncher(cruncherView.getCruncher());
		}
		availableCrunchers.remove(cruncherView.getCruncher());
		updateCrunchers(availableCrunchers);
		cruncher.getChildren().remove(cruncherView.getCruncherView());
	}

	public ArrayList<FileInputView> getFileInputViews() {
		return fileInputViews;
	}

	public ArrayList<CruncherView> getCruncherViews() {
		return cruncherViews;
	}

	public CacheOutput getCacheOutput() {
		return cacheOutput;
	}
}
