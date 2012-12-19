package org.ng12306.tpms.runtime;

import java.util.*;

public class StopRangeComparator
    implements Comparator<StopRange> {

	private StopRangeComparator()
	{
		
	}
	
	@Override
	public int compare(StopRange arg0, StopRange arg1) 
	{
	     
		int stops1 = arg0.end - arg1.start;
		int stops2 = arg1.end - arg1.start;
		
		int rs = Integer.compare(stops1, stops2);
		
		if(rs == 0)
		{
		    rs = Integer.compare(arg0.start, arg1.start);
		}
		
		return rs;
	}
	
	public static final StopRangeComparator Default = new StopRangeComparator();

}
