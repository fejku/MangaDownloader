package pl.fejku.model;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ChapterTableModel extends AbstractTableModel {
	
	List<Chapter> chapterList;
	
    private String[] headerList = { "Nazwa chaptera" };
    private Class[] classes = { Chapter.class };
    
    public ChapterTableModel(List<Chapter> list) {
    	chapterList = list;
    }
    
	public int getColumnCount() {
		return headerList.length;
	}

	public int getRowCount() {
		return chapterList.size();
	}

	public Object getValueAt(int row, int col) {
        Chapter chapter = chapterList.get(row);
    	return chapter;      
	}
		

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		chapterList.get(rowIndex).setArg((String)aValue);
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
