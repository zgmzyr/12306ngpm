package org.ng12306.tpms.runtime;

import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.*;

public class Train extends Entity {

	private LocalDate _departureDate;

	public LocalDate getDepartureDate() {
		return this._departureDate;
	}

	public void setDepartureDate(LocalDate value) {
		this._departureDate = value;
	}
	
	private TrainNumber _trainNumber;

	public TrainNumber getTrainNumber() {
		return this._trainNumber;
	}

	public void setTrainNumber(TrainNumber value) {
		this._trainNumber = value;
	}
	
	private ArrayList<Car> _cars = new ArrayList<Car>();

	public Collection<Car> getCars() {
		return this._cars;
	}

	

	

	
	
	
}


