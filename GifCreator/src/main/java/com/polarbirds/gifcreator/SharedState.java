package com.polarbirds.gifcreator;

import javafx.stage.Stage;

import java.io.File;

public class SharedState {


    private static final SharedState instance = new SharedState();
    public static SharedState getInstance() {
        return instance;
    }


    public final FileManager imageFileManager;
    public final ImageCombiner imageCombiner;
    private Stage primaryStage = null; // Must be set by Main
    public Object currentJavafxController = null;
    public File lastSaveLocation;

    public SharedState() {
        imageCombiner = new ImageCombiner();
        imageFileManager = new FileManager();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
