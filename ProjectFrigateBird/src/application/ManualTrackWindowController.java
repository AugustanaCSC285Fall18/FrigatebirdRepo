package application;

import java.awt.event.MouseEvent;

import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import utils.UtilsForOpenCV;

public class ManualTrackWindowController {
	@FXML
	private ImageView videoView;
	@FXML
	private Button setPointBtn;
	@FXML
	private Button undoBtn;
	@FXML
	private Button forwardFramesBtn;
	@FXML
	private Button backFramesBtn;
	@FXML
	private Canvas canvas;

	private boolean settingPoint = false;

	private ProjectData project;

	public void setProject(ProjectData project) {
		this.project = project;
		Video video = project.getVideo();
		video.setCurrentFrameNum(video.getCurrentFrameNum());
		
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(video.readFrame());
		//System.out.println(curFrame);
		
		Platform.runLater(() -> {
		videoView.setImage(curFrame);
		});
		//put in code to show the first video frame in the video view
	}

	public void addPointForChick(MouseEvent event) {

		if (settingPoint) {
			double x = event.getX();
			double y = event.getY();
			GraphicsContext g = canvas.getGraphicsContext2D();
			g.fillRoundRect(x, y, 1, 1, 1, 1);

			System.out.println("x,y = " + x + "," + y);
			// TODO: scale x and y based on real video size vs its displayed size
			TimePoint point1 = new TimePoint(x, y, 0);
			// add to the appropriate animaltrack list in the project
			// based on which chick the user has selected to be tracking right now

		}

	}

	public void handleSetPointBtn() {
		settingPoint = true;
	}

	public void moveForwardFrames() {

	}

	public void moveBackFrames() {

	}

	public void undoPoint() {

	}
}
