package com.polarbirds.gifcreator;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		try {
			//In order to get the controller, the static FXMLLoader may not be used.
			FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
			BorderPane root = (BorderPane)loader.load();
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			Controller controller = loader.<Controller>getController();
			controller.setStage(primaryStage);
			
			primaryStage.setTitle("GifCreator");
			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
	
	//Optional methods in Application.
	
	/**
	 * Called before start(Stage primaryStage), after launch(args).
	 */
	@Override
	public void init() {
		//getParameters() works beyond this point.
		//Do not create Scene or Stage objects here, this is the wrong thread.
	}
	
	/**
	 * Called after start(Stage primaryStage) exits.
	 */
	@Override
	public void stop() {}
}
