package datamodel;

import java.awt.Point;

public class TimePoint {
	private double x;     // location
	private double y;
	private Point pt;     // location
	private int frameNum; // time (measured in frames)
	
	public TimePoint(double x, double y, int frameNum) {
		this.x = x;
		this.y = y;
		this.frameNum = frameNum;
	}
	
	public int getX() {
		return pt.x;
	}
	
	public int getY() {
		return pt.y;
	}

	public int getFrameNum() {
		return frameNum;
	}

	public String toString() {
		return "("+pt.x+","+pt.y+"@T="+frameNum +")";
	}

	public double getDistanceTo(TimePoint other) {
		return pt.distance(other.pt);
	}
	
	public int getTimeDiffAfter(TimePoint other) {
		return this.frameNum - other.frameNum;
	}
}
