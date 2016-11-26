package pl.fejku.model;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pl.fejku.MangaDownloaderElement;
import pl.fejku.utils.Util;

public class Chapter implements MangaDownloaderElement {
	
	private String name;
	private String link;
	private int pageAmount;
	private String arg;

	public Chapter(String name, String link) {
		this.name = name;
		this.link = link;
		arg = "";
	}

	public String getName() {
		return name;
	}

	public String getLink() {
		return link;
	}

	public int getPageAmount() {
		return pageAmount;
	}

	public void setPageAmount(int pageAmount) {
		this.pageAmount = pageAmount;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}
	
	
	public List<Page> getPages() {
		List<Page> pageList = new ArrayList<>();
		Elements pages = Util.getDoc(this.link).select("#pagenavigation a");
		pageAmount = pages.size();
		for(Element page : pages) {
			pageList.add(new Page(page.text(), page.attr("href")));
		}
		
		return pageList;
	}
}
