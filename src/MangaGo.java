import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MangaGo {
	
	public Manga getManga(String pageUrl) {
		Manga manga = new Manga();
		manga.setLink(pageUrl);
		manga.setName(Util.getDoc(pageUrl).select(".w-title h1").first().text());
		
		return manga;
	}
}
