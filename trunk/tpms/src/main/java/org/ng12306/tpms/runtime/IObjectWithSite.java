package org.ng12306.tpms.runtime;

public interface IObjectWithSite
{
	IServiceProvider getSite();
	void setSite(IServiceProvider value);
}
