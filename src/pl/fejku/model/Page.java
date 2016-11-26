package pl.fejku.model;

import pl.fejku.utils.Util;

public class Page {
	private int pageNr;
	private String link;

	public Page(String pageNr, String link) {
		this.pageNr = Integer.parseInt(pageNr);
		this.link = link;
	}

	public int getPageNr() {
		return pageNr;
	}

	public String getLink() {
		return link;
	}
	
	public String getImageUrl() {
		return Util.getDoc(this.link).select("#pic_container img").attr("src");
	}
}
