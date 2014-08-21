package com.polarbirds.gifcreator;
	
import java.util.Locale;
import java.util.PropertyResourceBundle;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		try {
			//In order to get the controller, the static FXMLLoader may not be used.
			FXMLLoader loader = new FXMLLoader();
			
//			loader.setLocation(getClass().getResource("main.fxml")); //Uncomment this, and comment the next line to swap UI.
			loader.setLocation(getClass().getResource("/res/gui.fxml")); //Rework of main.fxml. WIP. Enables internationalization.
			loader.setResources(PropertyResourceBundle.getBundle("locale.TextBundle", Locale.getDefault()));
			
			Pane root = (Pane)loader.load();
			
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
