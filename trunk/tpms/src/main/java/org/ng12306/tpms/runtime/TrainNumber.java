package org.ng12306.tpms.runtime;

import java.util.ArrayList;
import java.util.Collection;

public class TrainNumber extends Entity {
	private String _name;

	public String getName() {
		return this._name;
	}

	public void setName(String value) {
		this._name = value;
	}

	private Collection<Route> _routes = new ArrayList<Route>();

	public Collection<Route> getRoutes() {
		return this._routes;
	}
	

}
