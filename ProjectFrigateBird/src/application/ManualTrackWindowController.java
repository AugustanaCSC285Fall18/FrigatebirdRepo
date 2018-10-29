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
//	@FXML
//	private ImageView videoView;
	@FXML
	private Button setPointBtn;
	@FXML
	private Button undoBtn;
	@FXML
	private Button forwardXFramesBtn;
	@FXML
	private Button backXFramesBtn;
	@FXML
	private Button exportBtn;
	@FXML
	private TextField frameJumpTextField;
	@FXML
	private Canvas overlayCanvas;
	@FXML
	private Canvas videoCanvas;
	@FXML
	private ComboBox<String> chickChooser;
	@FXML
	private Slider vidSlider;

	private boolean settingPoint = false;

	private ProjectData project;
	private Video video;

	public static final Color[] TRACK_COLORS = new Color[] { Color.RED, Color.BLUE, Color.GREEN, Color.CYAN,
			Color.MAGENTA, Color.BLUEVIOLET, Color.ORANGE };

	@FXML
	public void initializeWithStage(Stage stage) {
		vidSlider.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));
		vidSlider.setMax(video.getTotalNumFrames() - 1);
		vidSlider.setValue(video.getStartFrameNum());
		showFrameAt(video.getStartFrameNum()); // in case the slider didn't *change* value from the previous line
	}

	public void setProject(ProjectData project) {

		this.project = project;
		video = this.project.getVideo();
	}

	@FXML
	/**
	 * Draws a blue dot where the user clicked and adds the point to the nearest chick
	 * @param event - mouse click 
	 */
	public void addPointForChick(MouseEvent event) {

		if (settingPoint) {
			double x = event.getX();
			double y = event.getY();
			GraphicsContext g = overlayCanvas.getGraphicsContext2D();
			g.setFill(Color.BLUE);
			g.fillRoundRect(x, y, 10, 10, 10, 10);

			int chickIndex = chickChooser.getSelectionModel().getSelectedIndex();
			if (chickIndex >= 0) {
				AnimalTrack selectedTrack = project.getTracks().get(chickIndex);
				int curFrameNum = (int) vidSlider.getValue();

				double scalingRatio = getImageScalingRatio();
				double unscaledX = event.getX() / scalingRatio;
				double unscaledY = event.getY() / scalingRatio;

				if(selectedTrack.getTimePointAtTime(video.getCurrentFrameNum()) == null) {
					TimePoint addingPoint = new TimePoint(unscaledX, unscaledY, curFrameNum);
					selectedTrack.add(addingPoint);
				} else {
					selectedTrack.setTimePointAtTime(unscaledX, unscaledY, curFrameNum);
				}
				AnimalTrack closestSegment = project.getNearestUnassignedSegmentWithinDist(x, y, curFrameNum, curFrameNum+50, 25);
				//System.out.println("Unassigned: " + project.getUnassignedSegments());
				//System.out.println("Closest: " + closestSegment);
				if(closestSegment != null) {
					selectedTrack.addSegment(closestSegment);
					project.getUnassignedSegments().remove(closestSegment);
				}
				

				selectedTrack.setTimePointAtTime(unscaledX, unscaledY, curFrameNum);


			} else {
				new Alert(AlertType.WARNING, "You must ADD a chick first!").showAndWait();
			}			
				autoJumpForward();

			autoJumpForward();

			

			//System.out.println(project.getTracks().toString());

		}

	}

	public void handleSetPointBtn() {
		settingPoint = true;
	}

	/**
	 * Moves the video currentFrame either forward or backward (positive or negative)
	 * 
	 * @param framesForward - number of desired frames
	 */
	private void moveVideoForwardByAmount(int framesForward) {
//		video.setCurrentFrameNum(video.getCurrentFrameNum() + framesForward);
//
//		Image curFrame = UtilsForOpenCV.matToJavaFXImage(video.readFrame());

		vidSlider.setValue(project.getVideo().getCurrentFrameNum() + framesForward);

//		Platform.runLater(() -> {
//			videoView.setImage(curFrame);			
//		});
	}

	private void autoJumpForward() {
		moveVideoForwardByAmount(10);
	}

	private void moveForwardOrBackByUserAmount(boolean forward) {

		try {
			int frameJumpNum = video.convertSecondsToFrameNums(Integer.parseInt(frameJumpTextField.getText()));
			if (forward) {
				moveVideoForwardByAmount(frameJumpNum); // positive for forward
			} else {
				moveVideoForwardByAmount(-frameJumpNum); // negative for backward
			}
		} catch (NumberFormatException ex) {
			new Alert(AlertType.WARNING, "Please enter the # of seconds to move first.").showAndWait();
		}
	}

	@FXML
	private void moveForwardXSeconds() {
		moveForwardOrBackByUserAmount(true);
	}

	@FXML
	private void moveBackXSeconds() {
		moveForwardOrBackByUserAmount(false);
	}

	@FXML
	public void updateFrameJumpButtons() {
		backXFramesBtn.setText("Move Back " + frameJumpTextField.getText() + " Seconds");
		forwardXFramesBtn.setText("Move Forward " + frameJumpTextField.getText() + " Seconds");
	}

	public void undoPoint() {

		//TODO:
		showFrameAt(project.getTracks().get(chickChooser.getSelectionModel().getSelectedIndex()).getFinalTimePoint().getFrameNum());
		
		

		// TODO:
		moveVideoForwardByAmount(-10);

	}

	public ComboBox getChickChooser() {

		return chickChooser;
	}

	/**
	 * 
	 * @return image scaling ratio
	 */
	public double getImageScalingRatio() {
		double widthRatio = overlayCanvas.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = overlayCanvas.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);
	}

	public void showFrameAt(int frameNum) {

		project.getVideo().setCurrentFrameNum(frameNum);
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());

		GraphicsContext g = overlayCanvas.getGraphicsContext2D();

		g.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
		double scalingRatio = getImageScalingRatio();
		drawAssignedAnimalTracks(g, scalingRatio, frameNum);
		drawUnassignedSegments(g, scalingRatio, frameNum);

		double width = project.getVideo().getFrameWidth() * scalingRatio;
		double height = project.getVideo().getFrameHeight() * scalingRatio;
		videoCanvas.getGraphicsContext2D().drawImage(curFrame, 0, 0, width, height);
	}

	/**
	 * 
	 * @param g - Graphics pen used to draw objects on the canvas
	 * @param scalingRatio - scaling ratio
	 * @param frameNum - current frame number
	 */
	private void drawAssignedAnimalTracks(GraphicsContext g, double scalingRatio, int frameNum) {
		for (int i = 0; i < project.getTracks().size(); i++) {
			AnimalTrack track = project.getTracks().get(i);
			Color trackColor = TRACK_COLORS[i % TRACK_COLORS.length];
			Color trackPrevColor = trackColor.deriveColor(0, 0.5, 1.5, 1.0); // subtler variant

			g.setFill(trackPrevColor);
			// draw chick's recent trail from the last few seconds
			for (TimePoint prevPt : track.getTimePointsWithinInterval(frameNum - 90, frameNum)) {
				g.fillOval(prevPt.getX() * scalingRatio - 1, prevPt.getY() * scalingRatio - 1, 7, 7);
			}
			// draw the current point (if any) as a larger dot
			TimePoint currPt = track.getTimePointAtTime(frameNum);
			if (currPt != null) {
				g.setFill(trackColor);
				g.fillOval(currPt.getX() * scalingRatio - 5, currPt.getY() * scalingRatio - 5, 15, 15);
			}
		}
	}

	/**
	 * 
	 * @param g - Graphics pen used to draw objects on the canvas
	 * @param scalingRatio - scaling ratio
	 * @param frameNum - current frame number
	 */
	private void drawUnassignedSegments(GraphicsContext g, double scalingRatio, int frameNum) {
		for (AnimalTrack segment : project.getUnassignedSegments()) {

			g.setFill(Color.DARKGRAY);
			// draw this segments recent past & near future locations
			for (TimePoint prevPt : segment.getTimePointsWithinInterval(frameNum - 30, frameNum + 30)) {
				g.fillRect(prevPt.getX() * scalingRatio - 1, prevPt.getY() * scalingRatio - 1, 2, 2);
			}
			// draw the current point (if any) as a larger square
			TimePoint currPt = segment.getTimePointAtTime(frameNum);
			if (currPt != null) {
				g.fillRect(currPt.getX() * scalingRatio - 5, currPt.getY() * scalingRatio - 5, 11, 11);
			}
		}
	}
	
	@FXML
	private void handleExportBtn() {
		project.exportToCSV(project.getSaveFile());
	}
}
