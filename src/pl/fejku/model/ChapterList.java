package pl.fejku.model;

import java.util.ArrayList;
import java.util.List;

public class ChapterList {
	private List<Chapter> chapterList = new ArrayList<>();

	public List<Chapter> getChapterList() {
		return chapterList;
	}

	public void setChapterList(List<Chapter> chapterList) {
		this.chapterList = chapterList;
	}
	
	public void add(Chapter chapter) {
		chapterList.add(chapter);
	}
}
