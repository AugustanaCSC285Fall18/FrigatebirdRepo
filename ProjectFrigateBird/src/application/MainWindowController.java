package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.opencv.core.Mat;

import autotracking.AutoTrackListener;
import autotracking.AutoTracker;
import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.Video;
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
import utils.UtilsForOpenCV;
//import utils.UtilsForOpenCV;
public class MainWindowController implements AutoTrackListener{
@FXML private ImageView videoView;
@FXML private Button selectVideoBtn;
@FXML private Slider vidSlider;
//@FXML private Button autoTrackBtn;

private ProjectData project;
private AutoTracker autotracker;
private Stage stage;


	
	@FXML public void initialize() {		
		loadVideo("S:/class/cs/285/sample_videos/sample1.mp4");		
		project.getVideo().setXPixelsPerCm(6.5); //  these are just rough estimates!
		project.getVideo().setYPixelsPerCm(6.7);
		
		vidSlider.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue())); 

	}
	
	@FXML
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
	
	public void loadVideo(String filePath) {
		try {
			project = new ProjectData(filePath);
			Video video = project.getVideo();
			vidSlider.setMax(video.getTotalNumFrames()-1);
			showFrameAt(0);
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}

	}

	public void showFrameAt(int frameNum) {
		if (autotracker == null || !autotracker.isRunning()) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			videoView.setImage(curFrame);

			
		}		
	}
	
	
	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double percentTrackingComplete) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		// TODO Auto-generated method stub
		
	}
}
