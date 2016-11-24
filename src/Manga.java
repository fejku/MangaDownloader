import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingWorker;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Manga {
	
	private String name;
	private String link;
	private int chaptersAmount;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	
	public List<Chapter> getChapters() {
		List<Chapter> chapters = new ArrayList<>();
		Elements chapterLinks = Util.getDoc(this.link).select("#chapter_table a");
		for(Element chapterLink: chapterLinks) {
			chapters.add(new Chapter(chapterLink.text(), chapterLink.attr("href")));
		}
		
		return chapters;
	}
	
	public void downloadChapters(JLabel label, JList list) {
		List<Chapter> chapters = new ArrayList<>();
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
						
			@Override
			protected Void doInBackground() throws Exception {
				Elements chapterLinks = Util.getDoc(link).select("#chapter_table a");
				for(Element chapterLink: chapterLinks) {
					chapters.add(new Chapter(chapterLink.text(), chapterLink.attr("href")));
				}
				return null;
			}
			
			@Override
			protected void done() {
				label.setForeground(Color.GREEN);
				label.setText("Pobrano");
				
				DefaultListModel l = new DefaultListModel();
				for(Chapter chapter: chapters) {					
					l.addElement(chapter);
				}
				list.setModel(l);
			}
		};
		
		worker.execute();
	}	
}
