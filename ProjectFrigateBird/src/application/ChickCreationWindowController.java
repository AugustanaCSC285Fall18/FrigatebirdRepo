package application;

import java.io.File;

import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.Video;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import utils.UtilsForOpenCV;

public class ChickCreationWindowController {

	@FXML
	private TextField nameTextField;

	@FXML
	private ColorPicker colorChooser;

	@FXML
	private Button addBtn;

	private ProjectData project;

	private Video video;

	public void setProject(ProjectData project) {

		this.project = project;
		// put in code to show the first video frame in the video view
	}

	@FXML
	public void handleAddBtn() {
		String chickName = nameTextField.getText();
		AnimalTrack chick = new AnimalTrack(nameTextField.getText());
		
		project.getTracks().add(chick);


		System.out.println(project.getTracks());
	}	
}
