package pl.fejku;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MangaDownloaderTableCellRenderer extends DefaultTableCellRenderer {	
	  public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int row, int col) {
		super.getTableCellRendererComponent(table, value,
	               isSelected, hasFocus, row, col);
		if (value instanceof MangaDownloaderElement) {
			MangaDownloaderElement mangaDownloaderElement = (MangaDownloaderElement) value;
			setText("<html>"+mangaDownloaderElement.getArg()+mangaDownloaderElement.getName()+"</html>");
		}
		return this;
	}

}
