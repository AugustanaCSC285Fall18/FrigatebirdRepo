package datamodel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TimePointTest {

	@Test
	void testDistanceTo() {
		TimePoint point1 = new TimePoint(100,200,0);
		TimePoint point2 = new TimePoint(100,300,25);
		
		assertEquals(100, point1.getDistanceTo(point2));
	}
	
	@Test
	void testTimeDiff() {
		TimePoint point1 = new TimePoint(100,200,0);
		TimePoint point2 = new TimePoint(100,300,25);
		
		assertEquals(-25, point1.getTimeDiffAfter(point2));
	}

}
