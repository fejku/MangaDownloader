import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class MangaDownloaderListCellRenderer extends DefaultListCellRenderer {	
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof MangaDownloaderElement) {
			MangaDownloaderElement chapter = (MangaDownloaderElement) value;
			setText(chapter.getName());
		}
		return this;
	}
}