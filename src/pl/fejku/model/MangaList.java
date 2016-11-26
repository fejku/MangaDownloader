package pl.fejku.model;

import java.util.ArrayList;
import java.util.List;

public class MangaList {
	private List<Manga> mangaList = new ArrayList<>();

	public List<Manga> getMangaList() {
		return mangaList;
	}

	public void setMangaList(List<Manga> mangaList) {
		this.mangaList = mangaList;
	}
	
	public void add(Manga manga) {
		mangaList.add(manga);
	}
}
