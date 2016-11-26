package pl.fejku.model;

import java.util.ArrayList;
import java.util.List;

public class SiteList {
	private List<Site> siteList = new ArrayList<>();

	public List<Site> getSiteList() {
		return siteList;
	}
	
	public void add(Site site) {
		siteList.add(site);
	}
}
