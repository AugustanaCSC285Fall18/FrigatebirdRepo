package edu.augustana.csc285.Frigatebird;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class MainWindowController {
@FXML private ImageView videoView;
	
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
