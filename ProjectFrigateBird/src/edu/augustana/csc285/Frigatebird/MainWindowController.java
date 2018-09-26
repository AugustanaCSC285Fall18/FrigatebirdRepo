package edu.augustana.csc285.Frigatebird;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.opencv.core.Mat;

//import autotracking.AutoTrackListener;
//import autotracking.AutoTracker;
//import datamodel.AnimalTrack;
//import datamodel.ProjectData;
//import datamodel.TimePoint;
//import datamodel.Video;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
//import utils.UtilsForOpenCV;
public class MainWindowController {
@FXML private ImageView videoView;
@FXML private Button selectVideoBtn;
@FXML private Slider vidSlider;
@FXML private Button autoTrackBtn;

	
	@FXML public void initialize() {		
	}
	
	public void handleBrowse()  {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Image File");
		Window mainWindow = videoView.getScene().getWindow();
		File chosenFile = fileChooser.showOpenDialog(mainWindow);
		if (chosenFile != null) {
			try {
				videoView.setImage(new Image(new FileInputStream(chosenFile)));
				
			} catch (FileNotFoundException e) {			
				e.printStackTrace();
			}
		}
		
	}
}
