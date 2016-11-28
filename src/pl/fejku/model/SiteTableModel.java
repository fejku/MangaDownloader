package pl.fejku.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class SiteTableModel extends AbstractTableModel{

	private List<Site> siteList;
	
    private String[] headerList = { "Page name" };
    private Class[] classes = { Site.class };
	
	public SiteTableModel() {
		siteList = new ArrayList<>();
	}

	public void setSiteList(List<Site> siteList) {
		this.siteList = siteList;
	}

	@Override
	public int getRowCount() {
		return siteList.size();
	}

	@Override
	public int getColumnCount() {
		return headerList.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Site site = siteList.get(rowIndex);
		return site;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		siteList.get(rowIndex).setArg((String)aValue);
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	@Override
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

}
