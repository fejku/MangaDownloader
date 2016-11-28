package pl.fejku.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class MangaTableModel extends AbstractTableModel {
	
	List<Manga> mangaList;
	
    private String[] headerList = { "Nazwa mangii" };
    private Class[] classes = { Manga.class };    
    
	public MangaTableModel() {
		mangaList = new ArrayList<>();
	}

	public void setMangaList(List<Manga> mangaList) {
		this.mangaList = mangaList;
		fireTableDataChanged();
	}

	public List<Manga> getMangaList() {
		return mangaList;
	}

	public int getColumnCount() {
		return headerList.length;
	}

	public int getRowCount() {
		return mangaList.size();
	}

	public Object getValueAt(int row, int col) {
        Manga manga = mangaList.get(row);
    	return manga;      
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		mangaList.get(rowIndex).setArg((String)aValue);
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell. If we didn't implement this method, then the last column would
	 * contain text ("true"/"false"), rather than a check box.
	 */
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	/*
	 * Don't need to implement this method unless your table's editable.
	 */
	public boolean isCellEditable(int row, int col) {
		return false;
	}
}
