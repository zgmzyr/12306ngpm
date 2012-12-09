package org.ng12306.tpms.runtime;

import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.*;

public class Route extends Entity {

	private DateTime _startDateTime;

	public DateTime getStartDateTime() {
		return this._startDateTime;
	}

	public void setStartDateTime(DateTime value) {
		this._startDateTime = value;
	}
	
	private DateTime _endDateTime;

	public DateTime getEndDateTime() {
		return this._endDateTime;
	}

	public void setEndDateTime(DateTime value) {
		this._endDateTime = value;
	}
	
	
	private ArrayList<RouteStop> _stops = new ArrayList<RouteStop>();
	
	public Collection<RouteStop> getStops()
	{
	    return _stops;
	}
	
	
}
