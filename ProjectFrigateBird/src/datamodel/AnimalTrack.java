package datamodel;

import java.util.ArrayList;
import java.util.List;

public class AnimalTrack {
	public static final String UNNAMED_ID = "<<unassigned>>"; 
	private String animalID = UNNAMED_ID;
	
	private List<TimePoint> positions;
	
	public AnimalTrack(String id) {
		this.animalID = id;
		positions = new ArrayList<TimePoint>();
	}
	
	public AnimalTrack() {
		positions = new ArrayList<TimePoint>();
	}
	
	public void add(TimePoint pt) {
		positions.add(pt);
	}
	
	public boolean hasIDAssigned() {
		return !animalID.equals(UNNAMED_ID);
	}
	
	public List<TimePoint> getPositions() {
		return positions;
	}
	
	public TimePoint getFinalTimePoint() {
		return positions.get(positions.size()-1);
	}
	
	public String toString() {
		return "AnimalTrack[id="+ animalID + ",len=" + positions.size()+"]"; 
	}
}
