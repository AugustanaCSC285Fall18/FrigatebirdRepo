package application;

import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import datamodel.AnimalTrack;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;
import javafx.scene.canvas.*;

public class ManualTrackWindowController {
	@FXML
	private ImageView videoView;
	@FXML
	private Button setPointBtn;
	@FXML
	private Button undoBtn;
	@FXML
	private Button forwardXFramesBtn;
	@FXML
	private Button backXFramesBtn;
	@FXML
	private TextField frameJumpTextField;
	@FXML
	private Canvas canvas;
	@FXML 
	private ComboBox<String> chickChooser;
	@FXML
	private Slider vidSlider;

	private boolean settingPoint = false;

	private ProjectData project;
	private Video video;
	

	
	@FXML
	public void initializeWithStage(Stage stage) {

		vidSlider.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));
		vidSlider.setMax(video.getTotalNumFrames() - 1);

	}
	public void setProject(ProjectData project) {
		
		this.project = project;
		video = this.project.getVideo();
		video.setCurrentFrameNum(video.getCurrentFrameNum());
		
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(video.readFrame());
		//System.out.println(curFrame);
		
	
		
		Platform.runLater(() -> {
		videoView.setImage(curFrame);
		});
		//put in code to show the first video frame in the video view
	}
	
	

	@FXML
	public void addPointForChick(MouseEvent event) {

		if (settingPoint) {
			double x = event.getX();
			double y = event.getY();
			GraphicsContext g = canvas.getGraphicsContext2D();
			g.setFill(Color.BLUE);
			g.fillRoundRect(x, y, 10, 10, 10, 10);


			TimePoint addingPoint = new TimePoint(x, y, 0);
			
			int chickIndex = chickChooser.getSelectionModel().getSelectedIndex();
			if (chickIndex >= 0) {
				AnimalTrack selectedTrack = project.getTracks().get(chickIndex);
				int curFrameNum = (int) vidSlider.getValue();

				double scalingRatio = setImageScalingRatio();
				double unscaledX = event.getX() / scalingRatio;
				double unscaledY = event.getY() / scalingRatio;
				selectedTrack.setTimePointAtTime(unscaledX, unscaledY, curFrameNum);
				moveForwardXSeconds();
			} else {
				new Alert(AlertType.WARNING, "You must ADD a chick first!").showAndWait();
			}

			settingPoint = false;

		}

		
	}

	public void handleSetPointBtn() {
		settingPoint = true;
	}

	@FXML
	public void moveForwardXSeconds() {
		int frameJumpNum = video.convertSecondsToFrameNums(Integer.parseInt(frameJumpTextField.getText()));
		
		video.setCurrentFrameNum(video.getCurrentFrameNum()+ frameJumpNum);
		
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(video.readFrame());
		
		vidSlider.setValue(project.getVideo().getCurrentFrameNum());
		
		Platform.runLater(() -> {
		videoView.setImage(curFrame);
		});
		
	}
	
	public void autoJumpForward() {
		
		video.setCurrentFrameNum(video.getCurrentFrameNum()+ 20);
		
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(video.readFrame());
		
		vidSlider.setValue(project.getVideo().getCurrentFrameNum());
		
		Platform.runLater(() -> {
		videoView.setImage(curFrame);
		});
	}

	@FXML
	public void moveBackXSeconds() {
		int frameJumpNum = video.convertSecondsToFrameNums(Integer.parseInt(frameJumpTextField.getText()));
		
		video.setCurrentFrameNum(video.getCurrentFrameNum() - frameJumpNum);
		
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(video.readFrame());
		
		vidSlider.setValue(project.getVideo().getCurrentFrameNum());
		
		Platform.runLater(() -> {
		videoView.setImage(curFrame);
		});
	}
	
	@FXML
	public void updateFrameJumpButtons() {
		backXFramesBtn.setText("Move Back " + frameJumpTextField.getText() + " Seconds");
		forwardXFramesBtn.setText("Move Forward " + frameJumpTextField.getText() + " Seconds");
	}


	public void undoPoint() {

	}

	public ComboBox getChickChooser() {
		// TODO Auto-generated method stub
		return chickChooser;
	}
	
	public double setImageScalingRatio() {
		double widthRatio = canvas.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = canvas.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);
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

			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			double timeInSeconds = project.getVideo().convertFrameNumsToSeconds(frameNum);
			int minutes = (int) (timeInSeconds / 60);
			int seconds = (int) (timeInSeconds % 60);

			Platform.runLater(() -> {
				videoView.setImage(curFrame);
	
			});
		}
	}
	