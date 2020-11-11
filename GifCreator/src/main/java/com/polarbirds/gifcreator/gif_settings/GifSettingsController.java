package com.polarbirds.gifcreator.gif_settings;

import com.polarbirds.gifcreator.*;
import com.polarbirds.gifcreator.javafx.Disposable;
import com.polarbirds.gifcreator.javafx.JavaFxController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class GifSettingsController implements Initializable, Disposable, JavaFxController {


    @FXML
    public ImageView gifPreview;
    @FXML
    public Label loadingLabel;

    /**
     * getValue() returns delay in 100ths of a second. From 1 to 1000 hundreds, or 0.01 to 10.0 seconds.
     */
    @FXML
    public Slider delaySlider;
    @FXML
    public TextField delayField;
    @FXML public Button btnSave;

    private final ImageCombiner ic;
    private final FileManager fm;

    private FileManager.OnLoadCompleteListener onLoadCompleteListener;
    private ImageCombiner.OnImageCombineCompleteListener onImageCombineCompleteListener;
    private Stage stage;

    private BooleanProperty gifIsReady = new SimpleBooleanProperty(false);

    public GifSettingsController() {
        SharedState state = SharedState.getInstance();
        this.ic = state.imageCombiner;
        this.fm = state.imageFileManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.onLoadCompleteListener = () -> {
            System.out.println("" + fm.getImages().length + " files loaded.");
            gifIsReady.setValue(false);
            ic.setImages(fm.getImages());
            ic.generate();
        };
        fm.addListener(this.onLoadCompleteListener);

        this.onImageCombineCompleteListener = (success) -> {
            if (success) {
                System.out.println("Gif generated.");
                gifPreview.setImage(ic.getGif());
                gifIsReady.setValue(true);
            } else {
                System.err.println("Gif generation failed.");
            }
        };
        ic.addListener(this.onImageCombineCompleteListener);

        delaySliderChanged();
        delaySlider.valueProperty().addListener((observable, oldValue, newValue) -> delaySliderChanged());
        btnSave.disableProperty().bind(gifIsReady.not());
        loadingLabel.visibleProperty().bind(gifIsReady.not());

        fm.loadImages();
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void dispose() {
        fm.removeListener(this.onLoadCompleteListener);
        ic.removeListener(this.onImageCombineCompleteListener);
    }

    @FXML
    public void sliderReleased(MouseEvent mouseEvent) {
        System.out.println("Slider released. Generating gif...");
        gifIsReady.setValue(false);
        delaySliderChanged();
        ic.cancelGeneration();
        ic.generate();
    }

    @FXML
    public void saveGif(Event e) {
        final SharedState state = SharedState.getInstance();
        final File lastSaveLocation = state.lastSaveLocation;

        FileChooser chooser = new FileChooser();
        File currentFilePath = fm.getPath();
        if (lastSaveLocation != null) {
            currentFilePath = lastSaveLocation.getParentFile();
            chooser.setInitialFileName(lastSaveLocation.getName());
        }

        if (currentFilePath != null) {
            chooser.setInitialDirectory(currentFilePath);
        }

        chooser.setTitle("Save the gif...");
        chooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("Gif", "*.gif"));

        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            // User canceled
            return;
        }
        if (!file.getName().endsWith(".gif")) {
            file = new File(file.getPath() + ".gif");
        }
        state.lastSaveLocation = file;
        fm.saveGif(ic.getByteArray(), file);
    }

    public void prevMode(ActionEvent actionEvent) {
        fm.cancelLoading();
        ic.cancelGeneration();
        ApplicationWindow.showPage(ApplicationPage.SELECT_IMAGES);
    }

    public void delaySliderChanged() {
        delayField.setText(
                String.format("%.2f", delaySlider.getValue() / 100)); //2 decimals, display seconds.
        ic.setDelay((int) delaySlider.getValue() * 10);
    }
}
