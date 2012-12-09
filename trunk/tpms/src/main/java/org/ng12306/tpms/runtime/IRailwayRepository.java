package org.ng12306.tpms.runtime;

public interface IRailwayRepository {

	public Station[] getStations();
	
	public TrainNumber[] getTrainNumbers();
	
	public SeatType[] getSeatTypes();
	
	public CarType[] getCarTypes();
}
