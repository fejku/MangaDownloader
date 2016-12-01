package pl.fejku;

import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.StringUtils;

import pl.fejku.model.Chapter;
import pl.fejku.model.Page;
import pl.fejku.utils.Util;

public class DownloadChapterWorker extends SwingWorker<Void, Void> {

	private TableModel chapterModel;
	private int row;
	private int column;
	
	public DownloadChapterWorker(TableModel chapterModel) {
		this.chapterModel = chapterModel;
		this.column = 0;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	@Override
	protected Void doInBackground() throws Exception {
		Chapter chapter = (Chapter)chapterModel.getValueAt(row, column);
		
		String mangaName = chapter.getLink().split("/")[4];				
		mangaName = mangaName.replaceAll("_", " ");
		mangaName = StringUtils.capitalize(mangaName);
		mangaName = mangaName.replaceAll("[^a-zA-Z0-9- ]", "");
		String chapterName = chapter.getName().replaceAll("[^a-zA-Z0-9- ]", "");
		String path = mangaName + "/" + chapterName; 

		if (Util.createFolders(path) == false) {
			chapterModel.setValueAt("<font color=#54af54><b>OK</b></font> ", 
					row, column);
			return null;						
		}	
		
		for(Page page: chapter.getPages()) {
			DownloadPageWorker downloadPageWorker = new DownloadPageWorker(page, chapterModel, chapter);
			downloadPageWorker.setPath(path);
			downloadPageWorker.setRow(row);
			downloadPageWorker.setColumn(column);
			downloadPageWorker.execute();					
		}				
		return null;
	}

}
