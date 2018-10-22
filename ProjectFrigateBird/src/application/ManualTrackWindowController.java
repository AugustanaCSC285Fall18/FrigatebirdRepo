package application;

import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
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
	@FXML private ComboBox chickChooser;

	private boolean settingPoint = false;

	private ProjectData project;
	private Video video;

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
		System.out.println("click: "  + event.getX() +  " " + event.getY());
		System.out.println("settingPt: " + settingPoint);

		if (settingPoint) {
			double x = event.getX();
			double y = event.getY();
			GraphicsContext g = canvas.getGraphicsContext2D();
			g.setFill(Color.BLUE);
			g.fillRoundRect(x, y, 10, 10, 10, 10);

			System.out.println("x,y = " + x + "," + y);
			// TODO: scale x and y based on real video size vs its displayed size
			TimePoint point1 = new TimePoint(x, y, 0);
			// add to the appropriate animaltrack list in the project
			// based on which chick the user has selected to be tracking right now
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
		
		Platform.runLater(() -> {
		videoView.setImage(curFrame);
		});
	}

	@FXML
	public void moveBackXSeconds() {
		int frameJumpNum = video.convertSecondsToFrameNums(Integer.parseInt(frameJumpTextField.getText()));
		
		video.setCurrentFrameNum(video.getCurrentFrameNum() - frameJumpNum);
		
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(video.readFrame());
		
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

	
}
