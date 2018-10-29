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

import com.google.gson.Gson;

import autotracking.AutoTrackListener;
import autotracking.AutoTracker;
import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
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

public class MainWindowController implements AutoTrackListener {
//	@FXML
//	private ImageView videoView;
	@FXML
	private Button videoSelectBtn;
	@FXML
	private Button addChickBtn;
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
	private Button instructionsBtn;
	@FXML
	private Button aboutBtn;
	@FXML
	private Button loadBtn;
	@FXML
	private Button saveBtn;
	@FXML
	private ComboBox<String> chickChooser;
	@FXML
	private ComboBox<String> chickChooserAnalysis;
	@FXML
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
	private Canvas overlayCanvas;
	@FXML
	private Canvas videoCanvas;

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
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		File chosenFile = fileChooser.showOpenDialog(stage);
		if (chosenFile != null) {
			loadVideo(chosenFile.getPath());
		}

	}

	@FXML
	/**
	 * 
	 * @param filePath - location where the video is located
	 */
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

	/**
	 * 
	 * @param frameNum - desired frame to show
	 */
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
				GraphicsContext g  = videoCanvas.getGraphicsContext2D();
				double width = project.getVideo().getFrameWidth();
				double height = project.getVideo().getFrameHeight();
				double scalingRatio = getImageScalingRatio();
				g.drawImage(curFrame, 0, 0, width * scalingRatio, height * scalingRatio);
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

	@Override
	/**
	 * this method will get called repeatedly by the Autotracker after it analyzes each frame
	 * 
	 */
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		// this method is being run by the AutoTracker's thread, so we must
		// ask the JavaFX UI thread to update some visual properties
		Platform.runLater(() -> {
				GraphicsContext g = videoCanvas.getGraphicsContext2D();
				g.clearRect(0, 0, videoCanvas.getWidth(), videoCanvas.getHeight());
				double scalingRatio = getImageScalingRatio();
				g.drawImage(imgFrame, 0, 0, imgFrame.getWidth() * scalingRatio, imgFrame.getHeight() * scalingRatio);

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
		}
	}

	@FXML
	public void handleSetOrigin() {
		if (project == null) {
			JOptionPane.showMessageDialog(null, "Please select a video before setting its origin!");
		} else {
			try {
				isMouseSettingOrigin = true;
				System.out.println("origin button clicked");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Please load video");
			}
		}
	}

	@FXML
	public void handleSetBounds() {
		if (project == null) {
			JOptionPane.showMessageDialog(null, "Please select a video before setting its bounds!");
		} else {
			try {
				isMouseSettingBounds = true;
				JOptionPane.showMessageDialog(null,
						"Please click the upper left corner of the box and then the bottom right corner.");
				System.out.println(isMouseSettingBounds);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Please load video");

			}
		}

	}

	@SuppressWarnings("unused")
	@FXML
	/**
	 * Depending on what the user is setting, bounds or origin, the mouse click coordinates 
	 * are sent to the respective handle method
	 * @param event - mouse click
	 */
	public void handleCanvasClicked(MouseEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		System.out.println("x: " + x + " y: "+ y);

		if (isMouseSettingBounds) {
			handleCanvasClickedSettingBounds(x, y);

			isMouseSettingBounds = false;

		} else if (isMouseSettingOrigin) {
			handleCanvasClickedSettingOrigin(x, y);

			isMouseSettingOrigin = false;

		}
		
	}

	/**
	 * 
	 * @param x - x coordinate of the Point being made for the bounds
	 * @param y - y coordinate of the Point being made for the bounds
	 */
	public void handleCanvasClickedSettingBounds(int x, int y) {
		
		if (topLeftPointForBounds == null) {
			topLeftPointForBounds = new Point(x, y);
		} else {

			Point bottomRightPoint = new Point(x, y);
			System.out.println("top left point: " + topLeftPointForBounds);
			System.out.println("bottom right point: " + bottomRightPoint);

			int width = (int) Math.abs(topLeftPointForBounds.getX() - bottomRightPoint.getX());
			int height = (int) Math.abs(topLeftPointForBounds.getY() - bottomRightPoint.getY());

			Rectangle bounds = new Rectangle((int) (topLeftPointForBounds.getX()*getImageScalingRatio()), (int) (topLeftPointForBounds.getY()*getImageScalingRatio()),
					(int) (width*getImageScalingRatio()), (int) (height*getImageScalingRatio()));

			project.getVideo().setArenaBounds(bounds);
		}
	

	}

	/**
	 * 
	 * @param x - x coordinate of the Point being made for the origin
	 * @param y - y coordinate of the Point being made for the bounds
	 */
	public void handleCanvasClickedSettingOrigin(int x, int y) {

		int scaledX = (int) (x / getImageScalingRatio());
		int scaledY = (int) (y / getImageScalingRatio());
		Point point = new Point(scaledX, scaledY);
		project.getVideo().setOriginPoint(point);

	}

	@FXML
	public void handleSetBlankFrameBtn() {
		if(project == null) {
			JOptionPane.showMessageDialog(null, "Please select a video before setting the blank frame!");
		}else {
			Video video = project.getVideo();
			video.setEmptyFrameNum(video.getCurrentFrameNum());
			System.out.println(video.getEmptyFrameNum());
		}
		
	}
	
	@FXML
	/**
	 * Adds a chick (AnimalTrack object) with a name
	 */
	public void handleAddChickBtn() {
		if(project != null) {
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
		} else {
			JOptionPane.showMessageDialog(null, "Pleas create a project by selecting a video before adding chicks!");
		}
	}
	
	/**
	 * Deletes a chick
	 */
	public void handleDeleteChickBtn() {
		if(project != null || chickChooser.getItems().isEmpty()) {
			JOptionPane.showMessageDialog(null, "You haven't added any chicks!");
		}else {
			System.out.println(chickChooser.getItems().isEmpty());
			project.getTracks().remove(chickChooser.getSelectionModel().getSelectedIndex());
			chickChooser.getItems().remove(chickChooser.getSelectionModel().getSelectedIndex());
			chickChooserAnalysis.getItems().remove(chickChooserAnalysis.getSelectionModel().getSelectedIndex());
		}
	}
	
	/**
	 * 
	 * @return - image scaling ratio
	 */
	public double getImageScalingRatio() {
		double widthRatio = overlayCanvas.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = overlayCanvas.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);
	}
	
	@FXML
	public void handleInstructionsBtn() {
		JOptionPane.showMessageDialog(null,
				"1. Please select a video from the Select Video button to the left.\n"
				+ "2. Please select the callibration tab in the drop down tabs to the right and complete each of the Callibration tools."
				+ "\n\t-Select a blank frame to help with the auto-tracking process by clicking the button when the screen is showing a frame with no chicks."
				+ "\n\t-Select an origin point point on the screen by clicking the button then selecting a point on the screen to measure distance from."
				+ "\n\t-Select the chicks' arena by pressing the set bounds button and then clicking the top right corner of the box and then the bottom left corner."
				+ "\n3. Add your chicks to the program by pressing the add chick button then typing the name of your chicks."
				+ "\n\t-You can delete a chick from the project by selecting it in the combo box and pressing delete chick."
				+ "\n4. Enter a start and end frame for your tracking.\n\t-The current frame can be found in the top right corner of the screen."
				+ "\n\t-Try selecting a start fram after all the chicks have been added and an end frame to stop measuring before they are removed."
				+ "\n5. Select proceed to manual tracking.\n\t-Begin adding points to each of the chicks until an autoTrack segment is assigned to the chick."
				+ "\n5. Select the analyze button to save your progress and to get some info about the data in the project.");
	}
	
	@FXML
	public void handleAboutBtn() {
		JOptionPane.showMessageDialog(null,
				"Creators: Chase Fahy, Jason Palmer & Connor McGing\n"
				+ "Project Supervisor: Forrest Stonedahl\n"
				+ "CSC285 Software Development - Augustana College");
	}
	
	@FXML
	public void handleSaveBtn() throws FileNotFoundException {
		
		if(project == null) {
			JOptionPane.showMessageDialog(null, "Saving is only available after you have created a project by selecting a video!");
		} else {
			File file = new File("H:\\newfile.csv");
			project.saveToFile(file);
			project.exportToCSV(file);
			JOptionPane.showMessageDialog(null, "Save Successful!");

		}
		
	}
	
	@FXML
	public void handleLoadBtn() throws FileNotFoundException {
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		File chosenFile = fileChooser.showOpenDialog(stage);
		if (chosenFile != null && chosenFile.getName().contains(".txt")) {
			project.loadFromFile(chosenFile);
		} else {
			JOptionPane.showMessageDialog(null, "Please select the .txt file that you saved to.");

		}
	}

}
