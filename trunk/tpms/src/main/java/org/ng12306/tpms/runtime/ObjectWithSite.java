package org.ng12306.tpms.runtime;

public class ObjectWithSite implements IObjectWithSite
{
	private IServiceProvider privateSite;
	public final IServiceProvider getSite()
	{
		return privateSite;
	}
	public final void setSite(IServiceProvider value)
	{
		privateSite = value;
	}
}