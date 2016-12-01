package pl.fejku;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.SwingWorker;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.thoughtworks.xstream.XStream;

import pl.fejku.model.Manga;
import pl.fejku.model.MangaList;
import pl.fejku.model.MangaTableModel;
import pl.fejku.utils.Util;

public class GetMangaFromSiteWorker extends SwingWorker<Void, Integer> {
	
	private int row;
	private int column;
	private int pagesCount;
	private int downloadedPageAmount;
	private XStream xStream;
	private MangaList mangaList;
	private JTable tabSite;	
	private String mangaListXMLFile;
	private MangaTableModel mangaModel;
	
	public GetMangaFromSiteWorker() {
		this.column = 0;
		this.mangaList = new MangaList();
	}

	public void setRow(int row) {
		this.row = row;
	}

	public void setTabSite(JTable tabSite) {
		this.tabSite = tabSite;
	}

	public void setMangaModel(MangaTableModel mangaModel) {
		this.mangaModel = mangaModel;
	}

	public void setMangaListXMLFile(String mangaListXMLFile) {
		this.mangaListXMLFile = mangaListXMLFile;
	}

	@Override
	protected Void doInBackground() throws Exception {					
		//Lista rozwijalna ze wszystkimi stronami z mang¹
		Element pagesCombobox = Util.getDoc("http://www.mangago.me/list/directory/all/1/").select(".pagination li").last();
		Elements pagesOption = pagesCombobox.getElementsByTag("option");
		
		pagesCount = pagesOption.size();
		
		for(Element pageOption : pagesOption) {
			SwingWorker<Void, String> insidePageWorker = new SwingWorker<Void, String>() {
				
				@Override
				protected Void doInBackground() throws Exception {
					Elements mangaLinks = Util.getDoc(pageOption.attr("value")).select(".title a");
					for(Element mangaLink : mangaLinks) {
						Manga manga = new Manga();
						manga.setName(mangaLink.text());
						manga.setLink(mangaLink.attr("href"));
						
						mangaList.add(manga);								
					}						
					publish("");
					return null;
				}

				@Override
				protected void process(List<String> chunks) {
					downloadedPageAmount += chunks.size();
					tabSite.setValueAt("<font color=#54af54><b>"+downloadedPageAmount+"/"+pagesCount+"</b></font> " , row, column);
				}

				@Override
				protected void done() {
					if (downloadedPageAmount == pagesCount) {
						
						xStream = new XStream();
						try {
							xStream.toXML(mangaList, new FileWriter(mangaListXMLFile));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						DefaultListModel<MangaDownloaderElement> mangaListModel = new DefaultListModel<>();
						for(Manga manga : mangaList.getMangaList()) {
							mangaListModel.addElement(manga);
						}
						mangaModel.setMangaList(mangaList.getMangaList());
						
						tabSite.setValueAt("<font color=#54af54><b>OK</b></font> " , row, column);
					}
				}
			};	
			insidePageWorker.execute();
		}				
		return null;
	}

}
