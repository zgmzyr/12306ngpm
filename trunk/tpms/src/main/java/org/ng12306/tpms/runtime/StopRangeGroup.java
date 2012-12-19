package org.ng12306.tpms.runtime;

import java.util.*;

  class StopRangeGroup {
	
	  
    private StopRange Range;

	public StopRange getRange() {
		return this.Range;
	}

	public void setRange(StopRange value) {
		this.Range = value;
	}

	

	
	
	private ArrayList<PlanTicket> _tickets = new ArrayList<PlanTicket>();

	public Collection<PlanTicket> getTickets() {
		return this._tickets;
	}

	
	

}
