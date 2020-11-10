package com.polarbirds.gifcreator;

import java.io.File;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Controller implements Initializable, ThreadActionCompleteListener {

//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Properties//////////////////////////////////
//JAVAFX

    @FXML
    private Text title;
    @FXML
    private TextField fieldPath;
    @FXML
    private ListView<File> leftList;
    @FXML
    private ListView<File> rightList;
    @FXML
    private Tooltip leftTooltip;

    /**
     * getValue() returns delay in 100ths of a second. From 1 to 1000 hundreds, or 0.01 to 10.0 seconds.
     */
    @FXML
    private Slider delaySlider;
    @FXML
    private TextField delayField;
    @FXML
    private Label loadingLabel;

    @FXML
    private Button btnGenerate;


    @FXML
    private Pane generatePane;
    @FXML
    private Pane loadPane;

    @FXML
    private ImageView imagePreview;
    @FXML
    private ImageView gifPreview;
    @FXML
    private Pagination imagePagination;
//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Properties//////////////////////////////////
//GENERAL PROPERTIES

    private FileManager fm;
    private ImageCombiner ic;
    private Stage stage;

    private modes mode = modes.FILESELECTION;

    private ResourceBundle resources;

    private String FILESELECT_LABEL = "Select files";
    private String SETTINGS_LABEL = "Adjust settings";
    private String LOADING_LABEL = "Loading...";
    private String GENERATING_LABEL = "Generating...";
    private String FOLDER_CHOOSER_DIALOG_TITLE = "Choose the folder containing your images";
//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Properties//////////////////////////////////
//ENUMS & CLASSES

    private enum modes {
        FILESELECTION, GENERATING
    }


//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Methods/////////////////////////////////////
//INITIALISATIONS

    public Controller() {
        fm = new FileManager();
        ic = new ImageCombiner();

        fm.addListener(this);
        ic.addListener(this);
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

        leftList.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {

            @Override
            public ListCell<File> call(ListView<File> param) {
                return new FileNameCell();
            }
        });

        delaySliderChanged();
        delaySlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable,
                                Number oldValue, Number newValue) {
                delaySliderChanged();
            }
        });

        fm.getSelectedFilesList().addListener((ListChangeListener<? super File>) change -> this.toggleGenerateButton());
    }

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

        switch (mode) {
            case FILESELECTION:
                mode = modes.GENERATING;
                generatePane.setLayoutY(0); //It is moved to y=390 in scene builder.
                generatePane.setVisible(true);
                loadPane.setVisible(false);
                title.setText(SETTINGS_LABEL);
                loadingLabel.setText(LOADING_LABEL);
                fm.loadImages();
                break;
            default:
                break;
        }

    }

    @FXML
    public void prevMode(Event e) {
        switch (mode) {
            case GENERATING:
                mode = modes.FILESELECTION;
                fm.cancelLoading();
                ic.cancelGeneration();
                generatePane.setVisible(false);
                loadPane.setVisible(true);
                title.setText(FILESELECT_LABEL);
                break;
            default:
                break;
        }
    }

    @FXML
    public void sliderReleased(Event e) {
        System.out.println("Slider released. Generating gif...");
        loadingLabel.setText(GENERATING_LABEL);
        delaySliderChanged();
        ic.cancelGeneration();
        ic.generate();
    }

    @FXML
    public void saveGif(Event e) {
        FileChooser chooser = new FileChooser();
        if (fieldPath.getText() != null && !fieldPath.getText().equals("")) {
            chooser.setInitialDirectory(new File(fieldPath.getText()));
        }

        chooser.setTitle("Save the gif...");
        chooser.getExtensionFilters().setAll(new ExtensionFilter("Gif", "gif"));
        File file = chooser.showSaveDialog(stage);
        if (!file.getName().endsWith(".gif")) {
            file = new File(file.getPath() + ".gif");
        }
        fm.saveGif(ic.getByteArray(), file);
    }

//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Methods/////////////////////////////////////
//UTILITY

    public void delaySliderChanged() {
        delayField.setText(
                String.format("%.2f", delaySlider.getValue() / 100)); //2 decimals, display seconds.
        ic.setDelay((int) delaySlider.getValue() * 10);
    }

    public void toggleGenerateButton() {
        if (rightList.getItems().size() == 0) {
            btnGenerate.setDisable(true);
        } else {
            btnGenerate.setDisable(false);
        }
    }

    /**
     * From ThreadActionCompleteListener. Gets called when a thread is done working.
     */
    @Override
    public void actionComplete(ThreadActionEvent event) {
        switch (event.getAction()) {
            case FILES_LOADED:
                System.out.println("" + fm.getImages().length + " files loaded.");
                loadingLabel.setText(GENERATING_LABEL);
                ic.setImages(fm.getImages());
                ic.generate();
                break;

            case GIF_GENERATED:
                loadingLabel.setText("");
                if (event.succeeded()) {
                    System.out.println("Gif generated.");
                    gifPreview.setImage(ic.getGif());
                } else {
                    System.err.println("Gif generation failed.");

                }
                break;

            default:
                break;
        }
    }

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
            tmp = resources.getString("LbFileSelect");
            FILESELECT_LABEL = tmp;
        } catch (MissingResourceException | ClassCastException e) {
        }

        try {
            tmp = resources.getString("LbSettings");
            SETTINGS_LABEL = tmp;
        } catch (MissingResourceException | ClassCastException e) {
        }
        try {
            tmp = resources.getString("LbGenerating");
            GENERATING_LABEL = tmp;
        } catch (MissingResourceException | ClassCastException e) {
        }
        try {
            tmp = resources.getString("LbLoading");
            LOADING_LABEL = tmp;
        } catch (MissingResourceException | ClassCastException e) {
        }
        try {
            tmp = resources.getString("MFolderChooserDialogTitle");
            FOLDER_CHOOSER_DIALOG_TITLE = tmp;
        } catch (MissingResourceException | ClassCastException e) {
        }

    }
} //End of Controller class
