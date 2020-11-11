package com.polarbirds.gifcreator.image_selection;

import com.polarbirds.gifcreator.*;
import com.polarbirds.gifcreator.javafx.Disposable;
import com.polarbirds.gifcreator.javafx.FileNameCell;
import com.polarbirds.gifcreator.javafx.JavaFxController;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ImageSelectionController implements JavaFxController, Initializable, Disposable {

//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Properties//////////////////////////////////
//JAVAFX

    @FXML
    private TextField fieldPath;
    @FXML
    private ListView<File> leftList;
    @FXML
    private ListView<File> rightList;
    @FXML
    private Button btnGenerate;
    @FXML
    private ImageView imagePreview;

//GENERAL PROPERTIES
    private final FileManager fm;
    private Stage stage;

    private ResourceBundle resources;

    private String FOLDER_CHOOSER_DIALOG_TITLE = "Choose the folder containing your images";
    private ListChangeListener<? super File> fileListChangeListener;

//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Methods/////////////////////////////////////
//INITIALISATIONS

    public ImageSelectionController() {
        SharedState state = SharedState.getInstance();
        this.fm = state.imageFileManager;
    }

    /**
     * This method is called by the FXMLLoader when initialization is complete
     * Currently sends the ObservableLists for the lists to the FileManager.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        setLocalizedStrings();

        leftList.setItems(fm.getAllFilesList());
        rightList.setItems(fm.getSelectedFilesList());

        if (fm.getPath() != null) {
            fieldPath.setText(fm.getPath().getAbsolutePath());
        }

        leftList.setCellFactory(fileListView -> new FileNameCell());

        this.fileListChangeListener = change -> {
            btnGenerate.setDisable(change.getList().isEmpty());
        };
        fm.getSelectedFilesList().addListener(this.fileListChangeListener);
        btnGenerate.setDisable(fm.getSelectedFilesList().isEmpty());
    }

    @Override
    public void dispose() {
        fm.getSelectedFilesList().removeListener(this.fileListChangeListener);
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Methods/////////////////////////////////////
//EVENT HANDLERS

    @FXML
    public void openBrowser(Event event) {
        DirectoryChooser chooser = new DirectoryChooser();
        if (fieldPath.getText() != null && !fieldPath.getText().equals("")) {
            try {
                File file = new File(fieldPath.getText());
                if (file.isDirectory()) {
                    chooser.setInitialDirectory(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        chooser.setTitle(FOLDER_CHOOSER_DIALOG_TITLE);
        File dir = chooser.showDialog(stage);

        if (dir != null) {
            fieldPath.setText(dir.getAbsolutePath());
            fm.setPath(dir);
            fm.loadPath();
        }
    }

    /**
     * Called by the Load button to load images in a path.
     *
     * @param event
     */
    @FXML
    public void loadPath(Event event) {
        try {
            String path = fieldPath.getText();

            if (path != null && !path.isEmpty()) {
                fm.setPath(new File(path));
                fm.loadPath();
            } else {
                fieldPath.requestFocus();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void leftSelected(Event e) {
        File selected = leftList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Image img = new Image("file:" + selected.getAbsolutePath());
            imagePreview.setImage(img);
        }
    }

    @FXML
    public void addSelection(Event e) {
        int selected = leftList.getSelectionModel().getSelectedIndex();
        fm.selectFile(selected);
    }

    @FXML
    public void removeSelection(Event e) {
        int selected = rightList.getSelectionModel().getSelectedIndex();
        fm.removeSelection(selected);
    }

    @FXML
    public void addAll(Event e) {
        fm.addAll();
    }

    @FXML
    public void removeAll(Event e) {
        fm.clear();
    }

    @FXML
    public void moveUp(Event e) {
        int selected = rightList.getSelectionModel().getSelectedIndex();
        boolean succeeded = fm.moveSelectionUp(selected);
        if (succeeded) {
            rightList.getSelectionModel().select(selected - 1);
        }
    }

    @FXML
    public void moveDown(Event e) {
        int selected = rightList.getSelectionModel().getSelectedIndex();
        boolean succeeded = fm.moveSelectionDown(selected);
        if (succeeded) {
            rightList.getSelectionModel().select(selected + 1);
        }
    }

    @FXML
    public void sortAsc(Event e) {
        fm.sort(true);
    }

    @FXML
    public void sortDesc(Event e) {
        fm.sort(false);
    }

    @FXML
    public void nextMode(Event e) {
        ApplicationWindow.showPage(ApplicationPage.CONFIGURE_GIF);
    }


//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Methods/////////////////////////////////////
//UTILITY


    /**
     * Loads strings from <code>resources</code> and saves it to private strings.
     * If a value can't be loaded from <code>resources</code>, the private string keeps the default value.
     */
    private void setLocalizedStrings() {
        if (resources == null) {
            return;
        }

        String tmp;
        try {
            tmp = resources.getString("MFolderChooserDialogTitle");
            FOLDER_CHOOSER_DIALOG_TITLE = tmp;
        } catch (MissingResourceException | ClassCastException e) {
        }

    }
} //End of Controller class
