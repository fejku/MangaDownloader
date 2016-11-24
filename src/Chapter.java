import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Chapter implements MangaDownloaderElement {
	
	private String name;
	private String link;

	public Chapter(String name, String link) {
		this.name = name;
		this.link = link;
	}

	public String getName() {
		return name;
	}

	public String getLink() {
		return link;
	}
	
	public List<Page> getPages() {
		List<Page> pageList = new ArrayList<>();
		Elements pages = Util.getDoc(this.link).select("#pagenavigation a");
		for(Element page : pages) {
			pageList.add(new Page(page.text(), page.attr("href")));
		}
		
		return pageList;
	}
}
