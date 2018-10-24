package application;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.opencv.core.Mat;
import org.opencv.videoio.Videoio;

import autotracking.AutoTrackListener;
import autotracking.AutoTracker;
import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import utils.UtilsForOpenCV;

//import utils.UtilsForOpenCV;
public class MainWindowController implements AutoTrackListener {
	@FXML
	private ImageView videoView;
	@FXML
	private Button videoSelectBtn;
	@FXML
	private Button addChickBtn;
	@FXML
	private Button playBtn;
	@FXML
	private Button trackingBtn;
	@FXML
	private Button originBtn;
	@FXML
	private Button horizontalBtn;
	@FXML
	private Button verticalBtn;
	@FXML
	private Button manualtrackBtn;
	@FXML
	private ComboBox<String> chickChooser;
	
	@FXML
	private ComboBox<String> chickChooserAnalysis;

	private Button setBlankFrameBtn;

	@FXML
	private Slider vidSlider;
	@FXML
	private TextField startFrameLabel;
	@FXML
	private TextField endFrameLabel;
	@FXML
	private TextField currentFrameText;
	@FXML
	private TextField timeText;
	@FXML
	private Canvas canvas;

	private ScheduledExecutorService timer;

	private ProjectData project;
	private AutoTracker autotracker;
	private Stage stage;


	private boolean isMouseSettingOrigin = false;
	private boolean isMouseSettingBounds = false;
	private Point topLeftPointForBounds = null;

	@FXML
	public void initializeWithStage(Stage stage) {

		vidSlider.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));
	}
	
	
	@FXML
	public void handleBrowse() {
		// System.out.println("BROWSERING");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Image File");
		Window mainWindow = videoView.getScene().getWindow();
		File chosenFile = fileChooser.showOpenDialog(mainWindow);
		if (chosenFile != null) {
			loadVideo(chosenFile.getPath());
		}

	}

	@FXML
	
	public void loadVideo(String filePath) {
		try {
			project = new ProjectData(filePath);
			Video video = project.getVideo();

			video.setXPixelsPerCm(6);
			video.setYPixelsPerCm(6);

			vidSlider.setMax(video.getTotalNumFrames() - 1);
			showFrameAt(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void showFrameAt(int frameNum) {
//		if (timer != null && !timer.isShutdown()) {
//			timer.shutdown();
//			try {
//				timer.awaitTermination(1000, TimeUnit.MILLISECONDS);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}

		if (autotracker == null || !autotracker.isRunning()) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			double timeInSeconds = project.getVideo().convertFrameNumsToSeconds(frameNum);
			int minutes = (int) (timeInSeconds / 60);
			int seconds = (int) (timeInSeconds % 60);

			Platform.runLater(() -> {
				videoView.setImage(curFrame);
				currentFrameText.setText(String.format("%05d", frameNum));
				if (seconds < 10) {
					timeText.setText(minutes + ":0" + seconds);
				} else {
					timeText.setText(minutes + ":" + seconds);
				}
			});
		}
	}

	@FXML
	public void handleStartAutotracking() throws InterruptedException {
		try {
			if (autotracker == null || !autotracker.isRunning()) {
			Video video = project.getVideo();
			video.setStartFrameNum(Integer.parseInt(startFrameLabel.getText()));
			video.setEndFrameNum(Integer.parseInt(endFrameLabel.getText()));
			autotracker = new AutoTracker();
			// Use Observer Pattern to give autotracker a reference to this object,
			// and call back to methods in this class to update progress.
			autotracker.addAutoTrackListener(this);

			// this method will start a new thread to run AutoTracker in the background
			// so that we don't freeze up the main JavaFX UI thread.
			autotracker.startAnalysis(video);
			trackingBtn.setText("CANCEL auto-tracking");
			} else {
				autotracker.cancelAnalysis();
				trackingBtn.setText("Start auto-tracking");
			}
		}catch (Exception e){
			JOptionPane.showMessageDialog(null, "Please load video");
		}
		

	}

	// this method will get called repeatedly by the Autotracker after it analyzes
	// each frame
	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		// this method is being run by the AutoTracker's thread, so we must
		// ask the JavaFX UI thread to update some visual properties
		Platform.runLater(() -> {
			videoView.setImage(imgFrame);
			vidSlider.setValue(frameNumber);
		});
	}

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		project.getUnassignedSegments().clear();
		project.getUnassignedSegments().addAll(trackedSegments);

		for (AnimalTrack track : trackedSegments) {
			System.out.println(track);
//			System.out.println("  " + track.getPositions());
		}
		Platform.runLater(() -> {
			trackingBtn.setText("Autotrack");
		});

	}

	@FXML
	public void playVideo() {
		// TODO make the button work or maybe remove?
		Video video = project.getVideo();
		video.setCurrentFrameNum(video.getCurrentFrameNum() + 1);

		Runnable frameGrabber = new Runnable() {
			@Override
			public void run() {
				// Platform.runLater(() -> vidSlider.setValue(vidSlider.getValue() + 1));

				showFrameAt(project.getVideo().getCurrentFrameNum());
				vidSlider.setValue(project.getVideo().getCurrentFrameNum());
			}
		};

		this.timer = Executors.newSingleThreadScheduledExecutor();
		this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

		playBtn.setText("Stop");

	}



	@SuppressWarnings("unchecked")
	@FXML
	public void handleManualTrackBtn() throws IOException {

		try {
			// stage.hide();
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ManualTrackWindow.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			ManualTrackWindowController controller = fxmlLoader.getController();
			Stage stage = new Stage();

			stage.setTitle("Manual Tracking Window");
			stage.setScene(new Scene(root1));

			controller.setProject(project);

			// TODO: controller.getXXX()
			// TODO: create a new AnimalTrack() with the right name, and stick it in the
			// projectdata
			
			for(int i = 0; i < chickChooser.getItems().size(); i++) {
				controller.getChickChooser().getItems().add(chickChooser.getItems().get(i));
				controller.getChickChooser().getSelectionModel().select(0);
			}
			
			
			controller.initializeWithStage(stage);

			stage.show();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Please load video");
		} finally {

		}
	}

	// Calibration
	// not sure where to call the methods with mouseEvents in the parameters
	// separate clicks or mouse drag for calibration?

	@FXML
	public void handleSetOrigin() {
		try {
			isMouseSettingOrigin = true;
			System.out.println("origin button clicked");
		}catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Please load video");

		}
	}

//	//2 parameters? error comes up when I try though
//	@FXML
//	public void originCalibration(MouseEvent event) {
//		
//		if(originBtnClicked) {
//			double x = event.getX();
//			double y = event.getY();
//			
//			TimePoint origin = new TimePoint(x, y, 0);
//		}
//	}

	@FXML

	public void handleSetBounds() {
		try {
			isMouseSettingBounds = true;
			JOptionPane.showMessageDialog(null,
					"Please click the upper left corner of the box and then the bottom right corner.");
			System.out.println(isMouseSettingBounds);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Please load video");

		}
		

	}

	@SuppressWarnings("unused")
	// parameter or no?
	@FXML
	public void handleCanvasClicked(MouseEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		if (isMouseSettingBounds) {
			handleCanvasClickedSettingBounds(x, y);
		} else if (isMouseSettingOrigin) {
			handleCanvasClickedSettingOrigin(x, y);
		}
		
	}

	public void handleCanvasClickedSettingBounds(int x, int y) {
		if (topLeftPointForBounds == null) {
			topLeftPointForBounds = new Point(x, y);
		} else {

			Point bottomRightPoint = new Point(x, y);
			System.out.println("top left point: " + topLeftPointForBounds);
			System.out.println("bottom right point: " + bottomRightPoint);

			int width = (int) Math.abs(topLeftPointForBounds.getX() - bottomRightPoint.getX());
			int height = (int) Math.abs(topLeftPointForBounds.getY() - bottomRightPoint.getY());

			Rectangle bounds = new Rectangle((int) topLeftPointForBounds.getX(), (int) topLeftPointForBounds.getY(),
					width, height);
			project.getVideo().setArenaBounds(bounds);
		}

	}

	public void handleCanvasClickedSettingOrigin(int x, int y) {
		Point point = new Point(x, y);
		project.getVideo().setOriginPoint(point);
	}

	@FXML
	public void handleSetBlankFrameBtn() {
		Video video = project.getVideo();
		video.setEmptyFrameNum(video.getCurrentFrameNum());
		System.out.println(video.getEmptyFrameNum());
	}
	
	@FXML
	public void handleAddChickBtn() {
		TextInputDialog nameEnter = new TextInputDialog();
		nameEnter.setTitle("Add Chicks");
		nameEnter.setHeaderText("Chick Creation Menu");
		nameEnter.setContentText("Enter Chick's Name");
		
		Optional<String> result = nameEnter.showAndWait();
		if (result.isPresent()){
			String chickName = result.get(); 
			AnimalTrack chickToAdd = new AnimalTrack(chickName);
			project.getTracks().add(chickToAdd);
			chickChooser.getItems().add(chickName);
			chickChooser.getSelectionModel().select(chickName);
			chickChooserAnalysis.getItems().add(chickName);
			chickChooserAnalysis.getSelectionModel().select(chickName);
		}		
	}
	
	public void handleDeleteChickBtn() {
		project.getTracks().remove(chickChooser.getSelectionModel().getSelectedIndex());
		chickChooser.getItems().remove(chickChooser.getSelectionModel().getSelectedIndex());
		chickChooserAnalysis.getItems().remove(chickChooserAnalysis.getSelectionModel().getSelectedIndex());
	}

}
