package RAF.KiDSDomaci1.view;

import RAF.KiDSDomaci1.cruncher.CounterCruncher;
import RAF.KiDSDomaci1.model.Cruncher;
import RAF.KiDSDomaci1.output.CacheOutput;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class CruncherView {

	private MainView mainView;
	private Cruncher cruncher;
	private ListView<String> status;
	private ObservableList<String> statusList;
	private Pane main;
	private CacheOutput cacheOutput;

	public CruncherView(MainView mainView, Cruncher cruncher) {
		this.mainView = mainView;
		this.cruncher = cruncher;
		this.statusList = FXCollections.observableArrayList();
		cacheOutput = new CacheOutput(mainView.getResultsList());
		CounterCruncher counterCruncher = new CounterCruncher(cruncher, statusList);
		counterCruncher.getOutputComponents().add(cacheOutput);
		Thread cruncherThread = new Thread(counterCruncher);
		cruncherThread.start();
		
		main = new VBox();

		Text text = new Text("Name: " + cruncher.toString());
		main.getChildren().add(text);
		VBox.setMargin(text, new Insets(0, 0, 2, 0));

		text = new Text("Arity: " + cruncher.getArity());
		main.getChildren().add(text);
		VBox.setMargin(text, new Insets(0, 0, 5, 0));

		Button remove = new Button("Remove cruncher");
		remove.setOnAction(e -> removeCruncher());
		main.getChildren().add(remove);
		VBox.setMargin(remove, new Insets(0, 0, 5, 0));

		status = new ListView<>(statusList);
		main.getChildren().add(status);

		VBox.setMargin(main, new Insets(0, 0, 15, 0));
	}

	public Pane getCruncherView() {
		return main;
	}

	private void removeCruncher() {
		mainView.removeCruncher(this);
	}

	public Cruncher getCruncher() {
		return cruncher;
	}

	public MainView getMainView() {
		return mainView;
	}
}
