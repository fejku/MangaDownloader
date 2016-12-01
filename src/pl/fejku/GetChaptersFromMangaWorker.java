package pl.fejku;

import javax.swing.JTable;
import javax.swing.SwingWorker;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pl.fejku.model.Chapter;
import pl.fejku.model.ChapterList;
import pl.fejku.model.ChapterTableModel;
import pl.fejku.model.Manga;
import pl.fejku.utils.Util;

public class GetChaptersFromMangaWorker extends SwingWorker<Void, Void>{
	
	private Manga manga;
	private ChapterList chapterList;
	private JTable tabChapter;
	
	public GetChaptersFromMangaWorker() {
		chapterList = new ChapterList();
	}
	
	public void setManga(Manga manga) {
		this.manga = manga;
	}

	public void setTabChapter(JTable tabChapter) {
		this.tabChapter = tabChapter;
	}

	@Override
	protected Void doInBackground() throws Exception {
		Elements chapterLinks = Util.getDoc(manga.getLink()).select("#chapter_table a");
		for(Element chapterLink: chapterLinks) {
			chapterList.add(new Chapter(chapterLink.text(), chapterLink.attr("href")));
		}										
		return null;
	}

	@Override
	protected void done() {
		tabChapter.setModel(new ChapterTableModel(chapterList.getChapterList()));
	}	
}
