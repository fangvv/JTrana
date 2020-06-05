/**
 * 
 */
package src.operation;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

/**
 * @author heng
 *
 */
public class NodeMovement extends DefaultDataAnalyse{
	public Connection con;
	public String tablePrefix;
	public String startTime;
	public String endTime;
	public int nodeID;
	
	public JTable table;
	
	private JPopupMenu copyPopupMenu;
	private JMenuItem copyMenuItem;
	private PopupListener copyPopupListener;
	
	public NodeMovement(Connection con,String tablePrefix,String startTime,String endTime,int nodeID){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.startTime=startTime;
		this.endTime=endTime;
		this.nodeID=nodeID;
		
		createMovementTable();
		copyMenuItem=new JMenuItem("Copy");
		copyMenuItem.setAccelerator(KeyStroke.getKeyStroke('C',java.awt.Event.CTRL_MASK));
		copyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                copySystemPropertiesToClipboard();
            }
        });
		
        copyPopupMenu = new JPopupMenu();
		
		copyPopupMenu.add(copyMenuItem);

	        // add popup Listener to the table
	    copyPopupListener = new PopupListener();
	    table.addMouseListener(copyPopupListener);
		
	}
	 /**
     * Copies the selected cells in the table to the clipboard, in tab-delimited format.
     */
    public void copySystemPropertiesToClipboard() {

        final StringBuffer buffer = new StringBuffer();
        final ListSelectionModel selection = this.table.getSelectionModel();
        final int firstRow = selection.getMinSelectionIndex();
        final int lastRow = selection.getMaxSelectionIndex();
        if ((firstRow != -1) && (lastRow != -1)) {
            for (int r = firstRow; r <= lastRow; r++) {
                for (int c = 0; c < this.table.getColumnCount(); c++) {
                    buffer.append(this.table.getValueAt(r, c));
                    if (c != 2) {
                        buffer.append("\t");
                    }
                }
                buffer.append("\n");
            }
        }
        final StringSelection ss = new StringSelection(buffer.toString());
        final Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        cb.setContents(ss, ss);

    }
    
    private void createMovementTable(){
    	ResultSet dataset;
		Query query;
		String getMovement=null;
		Object[][] movement=null;
		int rowCount=0;
		getMovement="select distinct Time from "+tablePrefix+"movement_tr where Time>="+startTime+" and Time<="+endTime+" and NodeID="+nodeID;
		query=new Query(con,getMovement);
		dataset=query.doQuery();
		if(dataset!=null){
			try{
				while(dataset.next()){
					movement=new Object[Integer.parseInt(dataset.getString(1))][8];
				}
			}catch(SQLException e1){
				e1.printStackTrace();
			}
		}
		getMovement="select Time,X,Y,Z,DestinationX,DestinationY,Speed from "+tablePrefix+"movement_tr where Time>="+startTime+" and Time<="+endTime+" and NodeID="+nodeID;
		query=new Query(con,getMovement);
		dataset=query.doQuery();
		if(dataset!=null){
			try{
				int i=0;
				String time,x,y,z,dx,dy,speed;
				double timeNeeded=0;
				while(dataset.next()){
					rowCount++;
					time=dataset.getString(1);
					x=dataset.getString(2);
					y=dataset.getString(3);
					z=dataset.getString(4);
					dx=dataset.getString(5);
					dy=dataset.getString(6);
					speed=dataset.getString(7);
					if(Double.parseDouble(speed)!=0)
					     timeNeeded=(Math.sqrt(Math.pow(Double.parseDouble(dy)-Double.parseDouble(y),2)+Math.pow(Double.parseDouble(dx)-Double.parseDouble(x),2)))/Double.parseDouble(speed);
					movement[i][0]=time;
					movement[i][1]=x;
					movement[i][2]=y;
					movement[i][3]=z;
					movement[i][4]=dx;
					movement[i][5]=dy;
					movement[i][6]=speed;
					movement[i][7]=String.valueOf(timeNeeded);
					i++;
				}
			}catch(SQLException e1){
				e1.printStackTrace();
			}
		}
		String[] columnNames={"Start Time","Current X","Current Y","Current Z","Destination X","Destination Y","Speed","Time Needed(s)"};
		table=new JTable(new TableModel(movement,columnNames));
		table.setPreferredScrollableViewportSize(new Dimension(750,15*rowCount));
    }
	public String getDataType(){
		return jTabel;
	}
	public JTable getTableData(){
		return table;
	}
	
    /**
     * A popup listener.
     */
    class PopupListener extends MouseAdapter {

        /**
         * Mouse pressed event.
         *
         * @param e  the event.
         */
        public void mousePressed(final MouseEvent e) {
            maybeShowPopup(e);
        }

        /**
         * Mouse released event.
         *
         * @param e  the event.
         */
        public void mouseReleased(final MouseEvent e) {
            maybeShowPopup(e);
        }

        /**
         * Event handler.
         *
         * @param e  the event.
         */
        private void maybeShowPopup(final MouseEvent e) {
            if (e.isPopupTrigger()) {
                copyPopupMenu.show(table, e.getX(), e.getY());
            }
        }
    }

}
