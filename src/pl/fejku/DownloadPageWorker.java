package pl.fejku;

import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

import pl.fejku.model.Chapter;
import pl.fejku.model.Page;
import pl.fejku.utils.Util;

public class DownloadPageWorker extends SwingWorker<Void, String> {
	
	private Page page;
	private TableModel chapterModel;
	private Chapter chapter;
	private static int pageNr;
	private String path;
	private int row;
	private int column;
	private boolean isDone;
	
	public DownloadPageWorker(Page page, TableModel chapterModel, Chapter chapter) {
		this.page = page;
		this.chapterModel = chapterModel;
		this.chapter = chapter;
		isDone = false;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public void setColumn(int column) {
		this.column = column;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		String fileExtansion = page.getImageUrl().substring(page.getImageUrl().lastIndexOf("."));
		String fileName = page.getPageNr() + fileExtansion;
		Util.saveFile(path, fileName, page.getImageUrl());
		publish("");
		return null;
	}

	@Override
	protected void process(List<String> chunks) {
		if (!isDone) {
			pageNr++;
			chapterModel.setValueAt(
					"<font color=#6272a4><b>"+pageNr+"/"+chapter.getPageAmount()+"</b></font> ",
					row, column);			
		}
	}
	
	@Override
	protected void done() {
		if (pageNr == chapter.getPageAmount()) {
			chapterModel.setValueAt("<font color=#54af54><b>OK</b></font> ", 
					row, column);
			isDone = true;
		}
	}		
}
