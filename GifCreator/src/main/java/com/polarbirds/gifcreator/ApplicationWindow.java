package com.polarbirds.gifcreator;

import com.polarbirds.gifcreator.javafx.Disposable;
import com.polarbirds.gifcreator.javafx.JavaFxController;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.PropertyResourceBundle;

public final class ApplicationWindow {

    /**
     * Shows a fresh instance of a page.
     * @param page which page to show.
     */
    public static void showPage(final ApplicationPage page) {
        FXMLLoader loader = new FXMLLoader();

        loader.setLocation(jarResource(page.fxmlPath));
        loader.setResources(PropertyResourceBundle.getBundle(page.resourcesBundlePath, Locale.getDefault()));

        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            System.err.println("Failed to change page to " + page.name());
            e.printStackTrace();
            return;
        }

        Scene scene = new Scene(root);
        ObservableList<String> stylesheets = scene.getStylesheets();
        for (final String stylesheetPath : page.stylesheetPaths) {
            stylesheets.add(jarResource(stylesheetPath).toExternalForm());
        }

        final SharedState state = SharedState.getInstance();
        Object controller = loader.getController();
        Stage primaryStage = state.getPrimaryStage();
        if (controller instanceof JavaFxController) {
            ((JavaFxController) controller).setStage(primaryStage);
        }

        primaryStage.setScene(scene);
        disposeController(state.currentJavafxController);
        state.currentJavafxController = controller;

    }

    protected static void disposeController(Object controller) {
        if (controller instanceof Disposable) {
            ((Disposable) controller).dispose();
        }
    }

    protected static URL jarResource(String jarPath) {
        return ApplicationWindow.class.getResource(jarPath);
    }

}
