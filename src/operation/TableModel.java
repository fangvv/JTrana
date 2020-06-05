package src.operation;

import javax.swing.table.DefaultTableModel;

public class TableModel extends DefaultTableModel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1629067518169138164L;
	Object[][] data;
	Object[] columnNames;
	public TableModel(Object[][] data,Object[] columnNames){
		this.data=data;
		this.columnNames=columnNames;
		super.setDataVector(data,columnNames);
	}
	public boolean isCellEditable(int row,int column){
	    return false;
	}
}
