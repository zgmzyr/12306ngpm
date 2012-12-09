package org.ng12306.tpms.runtime;

import java.util.ArrayList;
import java.util.Collection;

public class TrainNumber extends Entity {
	private String _trainNumber;

	public String getTrainNumber() {
		return this._trainNumber;
	}

	public void setTrainNumber(String value) {
		this._trainNumber = value;
	}

	private Collection<Route> _routes = new ArrayList<Route>();

	public Collection<Route> getRoutes() {
		return this._routes;
	}
	

}
