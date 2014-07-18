package com.polarbirds.gifcreator;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
import javafx.stage.Stage;

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
	private Button btnAdd; //FIXME Not needed
	@FXML
	private Button btnRemove; //FIXME Not needed
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
	
//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Properties//////////////////////////////////
//GENERAL PROPERTIES
	
	private FileManager fm;
	private ImageCombiner ic;
	private Stage stage;
	
	private modes mode = modes.FILESELECTION;
	
	private String FILESELECT_LABEL = "Select files";
	private String GENERATE_LABEL = "Adjust settings";
	private String LOADING_LABEL = "Loading...";
	

//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Properties//////////////////////////////////
//ENUMS & CLASSES
	
	private enum modes {
		FILESELECTION, GENERATING
	};
	
	
	
	
//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Methods/////////////////////////////////////
//INITIALISATIONS
	
	public Controller() {
		fm = new FileManager();
		ic = new ImageCombiner();
	}
	
	/**
	 * This method is called by the FXMLLoader when initialization is complete
	 * Currently sends the ObservableLists for the lists to the FileManager.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		fm.setAllFilesList(leftList.getItems());
		fm.setSelectedFilesList(rightList.getItems());	
		
		delaySlider.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				delaySliderChanged();				
			}
		});
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}

//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Methods/////////////////////////////////////
//EVENT HANDLERS
	
	@FXML
	public void openBrowser(Event e) {
		DirectoryChooser chooser = new DirectoryChooser();
		if (fieldPath.getText() != null && !fieldPath.getText().equals("")) {
			chooser.setInitialDirectory(new File(fieldPath.getText()));
		}
		chooser.setTitle("Choose the folder containing your images");
		File dir = chooser.showDialog(stage);
		
		if (dir != null) {
			fieldPath.setText(dir.getAbsolutePath());
			fm.setPath(dir);
			fm.loadPath();
		}
	}
	
	@FXML
	public void loadPath (Event e) {
		if (fm.getPath() != null) {
			fm.loadPath();
		} else {
			fieldPath.requestFocus();
		}
	}
	
	@FXML
	public void leftSelected (Event e) {
		File selected = leftList.getSelectionModel().getSelectedItem();
		if (selected != null) {
			Image img = new Image("file:" + selected.getAbsolutePath());
			imagePreview.setImage(img);
		}
	}
	
	@FXML
	public void addSelection (Event e) {
		int selected = leftList.getSelectionModel().getSelectedIndex();
		fm.selectFile(selected);
		toggleGenerateButton();
	}
	
	@FXML
	public void removeSelection (Event e) {
		int selected = rightList.getSelectionModel().getSelectedIndex();
		fm.removeSelection(selected);
		toggleGenerateButton();
	}
	
	@FXML
	public void moveUp (Event e) {
		int selected = rightList.getSelectionModel().getSelectedIndex();
		boolean succeeded = fm.moveSelectionUp(selected);
		if (succeeded) {
			rightList.getSelectionModel().select(selected-1);
		}
	}
	
	@FXML
	public void moveDown (Event e) {
		int selected = rightList.getSelectionModel().getSelectedIndex();
		boolean succeeded = fm.moveSelectionDown(selected);
		if (succeeded) {
			rightList.getSelectionModel().select(selected+1);
		}
	}
	
	@FXML
	public void nextMode (Event e) {
		
		switch (mode) {
			case FILESELECTION:
				mode = modes.GENERATING;
				generatePane.setLayoutY(0); //It is moved to y=390 in scene builder.
				generatePane.setVisible(true);
				loadPane.setVisible(false);
				title.setText(GENERATE_LABEL);
				loadingLabel.setText(LOADING_LABEL);
				fm.loadImages();
				break;
			default:
				break;
		}
		
	}
	
	@FXML
	public void prevMode (Event e) {
		switch(mode) {
			case GENERATING:
				mode = modes.FILESELECTION;
				generatePane.setVisible(false);
				loadPane.setVisible(true);
				title.setText(FILESELECT_LABEL);
				break;
			default:
				break;
		}
	}
	
	@FXML
	public void sliderReleased (Event e) {
		ic.generate();
	}
	
	@FXML
	public void saveGif (Event e) {
		//TODO WIP
	}
//////////////////////////////////////////////////////////////////////////////
/////////////////////////////////Methods/////////////////////////////////////
//UTILITY
	
	public void delaySliderChanged() {
		delayField.setText(
					String.format("%.2f", delaySlider.getValue()/100)); //2 decimals, display seconds.
	}
	
	public void toggleGenerateButton() {
		if (rightList.getItems().size() == 0) {
			btnGenerate.setDisable(true);
		} else {
			btnGenerate.setDisable(false);
		}
	}

	@Override
	public void actionComplete(ThreadActionCompleteListener.action e) {
		switch (e) {
		case FILES_LOADED:
			loadingLabel.setText("");
			ic.generate();
			break;
			
		case GIF_GENERATED:
			
			break;
			
		default:
				break;
		}
	}
	
} //End of Controller class
