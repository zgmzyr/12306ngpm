package org.ng12306.tpms.runtime;

import java.util.ArrayList;
import java.util.Collection;

public class Car extends Entity {

	private CarType _carType;

	public CarType getCarTyp() {
		return this._carType;
	}

	public void setCarTyp(CarType value) {
		this._carType = value;
	}

	
	private int _carNumber;

	public int getCarNumber() {
		return this._carNumber;
	}

	public void setCarNumber(int value) {
		this._carNumber = value;
	}

	private ArrayList<OperatingSeat> _seats = new ArrayList<OperatingSeat>();;

	public Collection<OperatingSeat> getSeats() {
		return this._seats;
	}

	private Train _train;

	public Train getTrain() {
		return this._train;
	}

	public void setTrain(Train value) {
		this._train = value;
	}

	
	
	

	
	
}
