package org.ng12306.tpms.runtime;

import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.*;

public class Route extends Entity {

	private LocalDate _startDate;

	public LocalDate getStartDate() {
		return this._startDate;
	}

	public void setStartDate(LocalDate value) {
		this._startDate = value;
	}
	
	private LocalDate _endDate;

	public LocalDate getEndDate() {
		return this._endDate;
	}

	public void setEndDate(LocalDate value) {
		this._endDate = value;
	}
	
	
	private ArrayList<RouteStop> _stops = new ArrayList<RouteStop>();
	
	public Collection<RouteStop> getStops()
	{
	    return _stops;
	}
	
	
}
