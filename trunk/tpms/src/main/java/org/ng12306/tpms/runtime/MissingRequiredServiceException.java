package org.ng12306.tpms.runtime;

import java.io.Serializable;



public class MissingRequiredServiceException extends RuntimeException implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1790383094638274892L;

	@SuppressWarnings("rawtypes")
	public MissingRequiredServiceException(java.lang.Class serviceType)
	{
		super(String.format("Cannot find required service %s", serviceType.getName()));

	}
}