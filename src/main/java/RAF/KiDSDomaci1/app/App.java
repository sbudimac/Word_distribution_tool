package RAF.KiDSDomaci1.app;

import RAF.KiDSDomaci1.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
    	BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 1300, 800);
		MainView mainView = new MainView();
		mainView.initMainView(root, stage);
		stage.setScene(scene);
		stage.show();
    }

}