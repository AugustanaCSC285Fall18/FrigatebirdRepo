package datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ProjectData {
	private Video video;
	private List<AnimalTrack> tracks;
	private List<AnimalTrack> unassignedSegments;
	
	private File saveFile;

	public ProjectData(String videoFilePath) throws FileNotFoundException {
		video = new Video(videoFilePath);
		tracks = new ArrayList<>();
		unassignedSegments = new ArrayList<>();
	}

	public Video getVideo() {
		return video;
	}

	public List<AnimalTrack> getTracks() {
		return tracks;
	}

	public List<AnimalTrack> getUnassignedSegments() {
		return unassignedSegments;
	}

	public void saveToFile(File saveFile) throws FileNotFoundException {
		String json = toJSON();
		PrintWriter out = new PrintWriter(saveFile);
		out.print(json);
		out.close();
		this.saveFile = saveFile;
	}

	public String toJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this);
	}

	public static ProjectData loadFromFile(File loadFile) throws FileNotFoundException {
		String json = new Scanner(loadFile).useDelimiter("\\Z").next();
		return fromJSON(json);
	}

	public static ProjectData fromJSON(String jsonText) throws FileNotFoundException {
		Gson gson = new Gson();
		ProjectData data = gson.fromJson(jsonText, ProjectData.class);
		data.getVideo().connectVideoCapture();
		
		System.out.println("loading tracks size: " + data.getTracks().size());
		System.out.println("after loading: " + data.getTracks());
		
		return data;
	}

	/**
	 * 
	 * @param x             - x coordinate of the new TimePoint
	 * @param y             - y coordinate of the new TimePoint
	 * @param startFrame    - beginning of window to look for nearest segment
	 * @param endFrame      - beginning of window to look for nearest segment
	 * @param distanceRange - minimum distance away from the point
	 * @return
	 */
	public AnimalTrack getNearestUnassignedSegmentWithinDist(double x, double y, int startFrame, int endFrame,
			double distanceRange) {
		double minDistance = distanceRange;
		AnimalTrack nearest = null;
		for (AnimalTrack segment : unassignedSegments) {
			List<TimePoint> ptsInInterval = segment.getTimePointsWithinInterval(startFrame, endFrame);
			for (TimePoint pt : ptsInInterval) {
				TimePoint addingPoint = new TimePoint(x, y, pt.getFrameNum());
				double dist = pt.getDistanceTo(addingPoint);
				if (dist < minDistance) {
					minDistance = dist;
					nearest = segment;
				}
			}
		}
		return nearest;
	}

	public void exportToCSV(File saveFile) {

		StringBuilder CSVbuilder = new StringBuilder();
		FileWriter CSVfile = null;
		
		int XPixelsPerCm = (int) video.getXPixelsPerCm();
		int YPixelsPerCm = (int) video.getYPixelsPerCm();
		
		double distanceInPixels = 0;
		try {
			CSVfile = new FileWriter(saveFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		CSVbuilder.append("\n");
		int maxPoints = 0;
		for (int i = 0; i < getTracks().size(); i++) {
			if (getTracks().get(i).getPositions().size() > maxPoints) {
				maxPoints = getTracks().get(i).getPositions().size();
			}
		}
		for (int a = 0; a < getTracks().size(); a++) {
			CSVbuilder.append("Chick: " + getTracks().get(a).getID());
			CSVbuilder.append(",,,,");
			CSVbuilder.append("\n");
			CSVbuilder.append("Time (In Seconds),X-Coordinate (In Cm),Y-Coordinate (In Cm),,");
			CSVbuilder.append("\n");
			for (int i = 1; i < maxPoints; i++) {
				if(getTracks().get(a).hasTimePointAtIndex(i)) {
					
					
					 distanceInPixels += getTracks().get(a).getTimePointAtIndex(i).getDistanceTo(getTracks().get(a).getTimePointAtIndex(i-1));
					int seconds = (int) video.convertFrameNumsToSeconds(getTracks().get(a).getTimePointAtIndex(i).getFrameNum());
					
					CSVbuilder.append(seconds + ",");
					CSVbuilder.append(Math.round((getTracks().get(a).getTimePointAtIndex(i).getX()/XPixelsPerCm)) + ",");
					CSVbuilder.append(Math.round((getTracks().get(a).getTimePointAtIndex(i).getY()/YPixelsPerCm)) + ",");					
				}
				CSVbuilder.append("\n");
			}
			CSVbuilder.append("Distance Traveled," + Math.round((distanceInPixels/video.getAvgPixelsPerCm())) + "\n");
			
		}
		
		try {
			CSVfile.append(CSVbuilder);
			CSVfile.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}

	}
	
	public File getSaveFile() {
		return saveFile;
	}
}
