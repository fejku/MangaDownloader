package pl.fejku;

import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

public class DownloadChaptersWorker extends SwingWorker<Void, Void>{

	private int selectedChapterNr;
	private TableModel chapterModel;
	
	public DownloadChaptersWorker(int selectedChapterNr) {
		this.selectedChapterNr = selectedChapterNr;
	}

	public void setChapterModel(TableModel chapterModel) {
		this.chapterModel = chapterModel;
	}

	@Override
	protected Void doInBackground() {
		DownloadChapterWorker downloadChapterWorker = new DownloadChapterWorker(chapterModel);
		downloadChapterWorker.setRow(selectedChapterNr);
		downloadChapterWorker.execute();
		return null;
	}		

}
