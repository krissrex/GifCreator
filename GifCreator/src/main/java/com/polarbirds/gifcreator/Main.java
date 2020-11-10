package com.polarbirds.gifcreator;

import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		SharedState.getInstance().setPrimaryStage(primaryStage);

		try {
			primaryStage.setTitle("GifCreator");
			primaryStage.setResizable(false);
			ApplicationWindow.showPage(ApplicationPage.SELECT_IMAGES);
			primaryStage.show();
		} catch(Exception e) {
			System.err.println("Failed to start program:");
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
