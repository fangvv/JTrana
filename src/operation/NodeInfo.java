package src.operation;

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
public class NodeInfo extends DefaultDataAnalyse{
	public Connection con;
	public String tablePrefix;
	public int nodeID;
	
	public JTable table;
	
	private JPopupMenu copyPopupMenu;
	private JMenuItem copyMenuItem;
	private PopupListener copyPopupListener;
	/*
	 * the generated packets include broadcasting packets
	 */
	private int generatePkt=0;
	/*
	 * the sent packets include broadcasting packets
	 */
	private int sentPkt=0;
	/*
	 * the received packets include broadcasting packets
	 */
	private int receivePkt=0;
	private int forwardPkt=0;
	private int dropPkt=0;
//	private int lostPkt=0;
	private int minPktSize=Integer.MAX_VALUE;
	private int maxPktSize=0;
	private double avgPktSize=0;
	private int generateBytes=0;
	private int sentBytes=0;
	private int receiveBytes=0;
	private int forwardBytes=0;
	private int dropBytes=0;
//	private int lostBytes=0;
	public NodeInfo(Connection con,String tablePrefix,int nodeID){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.nodeID=nodeID;
		
		createNodeInfoTable();
		
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
	private void createNodeInfoTable(){
		ResultSet dataset;
		Query query;
		String getgeneratePkt = null;
		String getsentPkt = null;
		String getreceivePkt = null;
		String getreceivePkt1 = null;
		String getforwardPkt = null;
//		String getforwardPkt1 = null;
		String getdropNum = null;
		String getdropPkt = null;
		
		int[] drop = null;
		
		if(DataRecognition.is_normal){
			getgeneratePkt="select distinct UPI,PS from "+tablePrefix+"normal_tr where Event='+' and SN=SAN and SN="+nodeID;
			getsentPkt="select distinct UPI,PS from "+tablePrefix+"normal_tr where Event='-' and SN=SAN and SN="+nodeID;
			getreceivePkt="select distinct UPI,PS from "+tablePrefix+"normal_tr where Event='r' and DN=DAN and DN="+nodeID;
			getreceivePkt1="select distinct UPI,PS from "+tablePrefix+"normal_tr where Event='r' and DAN<0 and DN="+nodeID;
			getforwardPkt="select distinct UPI,PS from "+tablePrefix+"normal_tr where Event='-' and SN!=SAN and SN!=DAN and SN="+nodeID;
			getdropNum="select count(distinct UPI,PS) from "+tablePrefix+"normal_tr where Event='d' and SN="+nodeID;		
			getdropPkt="select distinct UPI,PS from "+tablePrefix+"normal_tr where Event='d' and SN="+nodeID;			
		}
		else if(DataRecognition.is_old_wireless){
			if(DataRecognition.hasRTR)
				getforwardPkt="select distinct EI,PS from "+tablePrefix+"old_wireless_tr where Event='f' and NI="+nodeID;
			else if(DataRecognition.hasMAC)
				getforwardPkt="select distinct EI,PS from "+tablePrefix+"old_wireless_tr where Event='s' and NI!=IP_DIA and NI!=IP_SIA and NI="+nodeID;
			getdropNum="select count(distinct EI,PS) from "+tablePrefix+"old_wireless_tr where Event='D' and NI="+nodeID;
			getdropPkt="select distinct EI,PS from "+tablePrefix+"old_wireless_tr where Event='D' and NI="+nodeID;			
		}
		else if(DataRecognition.is_new_wireless){
			if(DataRecognition.hasRTR)
				getgeneratePkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Nl='RTR' and Ni="+nodeID;
			else if(DataRecognition.hasMAC)
				getgeneratePkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Nl='MAC' and Ni="+nodeID;
			else if(DataRecognition.hasAGT)
				getgeneratePkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Ni="+nodeID;
			
			getreceivePkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and Ni="+nodeID;
			getreceivePkt1="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and Ni="+nodeID;
			
			if(DataRecognition.hasRTR)
				getforwardPkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='f' and Ni="+nodeID;
			else if(DataRecognition.hasMAC)
				getforwardPkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='s' and Ni!=IP_Idn and Ni!=IP_Isn and Ni="+nodeID;
			getdropNum="select count(distinct IP_Ii,IP_Il) from "+tablePrefix+"new_wireless_tr where Event='d' and Ni="+nodeID;
			getdropPkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='d' and Ni="+nodeID;
		}
		
		query=new Query(con,getdropNum);
		dataset=query.doQuery();
		if(dataset!=null){
			try{
				while(dataset.next()){
					drop=new int[Integer.parseInt(dataset.getString(1))];
				}
			}catch(SQLException e1){
				e1.printStackTrace();
			}
		}
		
		query=new Query(con,getdropPkt);
		dataset=query.doQuery();
		if(dataset!=null){
			try{
				int size;
				int i=0;
				while(dataset.next()){
					drop[i++]=Integer.parseInt(dataset.getString(1));
					dropPkt++;
					size=Integer.parseInt(dataset.getString(2));
					dropBytes+=size;
				}
			}catch(SQLException e1){
				e1.printStackTrace();
			}
		}
		
        if(DataRecognition.is_new_wireless||DataRecognition.is_normal){
    		if(DataRecognition.is_new_wireless){
    			query=new Query(con,getgeneratePkt);
        		dataset=query.doQuery();
        		if(dataset!=null){
        			try{
        				int size;
        				while(dataset.next()){
        					generatePkt++;
        					size=Integer.parseInt(dataset.getString(2));
        					generateBytes+=size;
        					if(size<minPktSize)
        						minPktSize=size;
        					if(size>maxPktSize)
        						maxPktSize=size;
        					boolean flag=false;
        					int id=Integer.parseInt(dataset.getString(1));
        					for(int i=0;i<drop.length;i++)
        						if(id==drop[i])
        							flag=true;
        					if(!flag){
        						sentPkt++;
        						sentBytes+=size;
        					}
        				}
        				if(generatePkt!=0)
        					avgPktSize=generateBytes/generatePkt;
//        				System.out.println("generatepkt:"+generatePkt);
//        				System.out.println("generateBytes:"+generateBytes);
//        				System.out.println("minPktsize:"+minPktSize);
//        				System.out.println("maxPktsize:"+maxPktSize);
//        				System.out.println("avgPktsize:"+avgPktSize);
        			}catch(SQLException e1){
        				e1.printStackTrace();
        			}
        		}
    		}
    		
    		if(DataRecognition.is_normal){
    			query=new Query(con,getsentPkt);
    			dataset=query.doQuery();
    			if(dataset!=null){
    				try{
    					int size;
    					while(dataset.next()){
    						sentPkt++;
    						size=Integer.parseInt(dataset.getString(2));
    						sentBytes+=size;
    					}
    				}catch(SQLException e1){
    					e1.printStackTrace();
    				}
    			}
    		}
    		query=new Query(con,getreceivePkt);
    		dataset=query.doQuery();
    		if(dataset!=null){
    			try{
    				int size;
    				while(dataset.next()){
    					receivePkt++;
    					size=Integer.parseInt(dataset.getString(2));
    					receiveBytes+=size;
    				}
    			}catch(SQLException e1){
    				e1.printStackTrace();
    			}
    		}
    		query=new Query(con,getreceivePkt1);
    		dataset=query.doQuery();
    		if(dataset!=null){
    			try{
    				int size;
    				while(dataset.next()){
    					receivePkt++;
    					size=Integer.parseInt(dataset.getString(2));
    					receiveBytes+=size;
    				}
    			}catch(SQLException e1){
    				e1.printStackTrace();
    			}
    		}
        }
        if(DataRecognition.is_normal){
        	query=new Query(con,getforwardPkt);
        	dataset=query.doQuery();
        	if(dataset!=null){
        		try{
        			int size;
        			while(dataset.next()){
        				forwardPkt++;
						size=Integer.parseInt(dataset.getString(2));
						forwardBytes+=size;
        			}
        		}catch(SQLException e1){
        			e1.printStackTrace();
        		}
        	}
        }
        else{
        	query=new Query(con,getforwardPkt);
        	dataset=query.doQuery();
        	if(dataset!=null){
        		try{
        			int size;
        			while(dataset.next()){
        				int id=Integer.parseInt(dataset.getString(1));
        				boolean flag=false;
        				for(int i=0;i<drop.length;i++)
        					if(id==drop[i])
        						flag=true;
        				if(!flag){
        					forwardPkt++;
						    size=Integer.parseInt(dataset.getString(2));
						    forwardBytes+=size;
        				}
        			}
        		}catch(SQLException e1){
        			e1.printStackTrace();
        		}
        	}
        }
		
		if(DataRecognition.is_old_wireless){
			String getPktType=null;
			String[][] pktType;
			int num=0;
			getPktType="select count(distinct PT,Name) from "+tablePrefix+"old_wireless_tr where Event='s' and Ni="+nodeID;
			query=new Query(con,getPktType);
			dataset=query.doQuery();
			if(dataset!=null){
				try{
					while(dataset.next()){
						num=Integer.parseInt(dataset.getString(1));
					}
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
			pktType=new String[num][2];
			getPktType="select distinct PT,Name from "+tablePrefix+"old_wireless_tr where Event='s' and Ni="+nodeID;
			query=new Query(con,getPktType);
			dataset=query.doQuery();
			if(dataset!=null){
				try{
					int i=0;
					while(dataset.next()){
						pktType[i][0]=dataset.getString(1);
						pktType[i][1]=dataset.getString(2);
						i++;
					}
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
			for(int i=0;i<num;i++){
				if(pktType[i][1].equals("arp")){
					getgeneratePkt="select PS,EI  from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='arp' and Ni="+nodeID;							
				}
				else if(pktType[i][1].equals("old")){
					getgeneratePkt="select PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='old' and Ni="+nodeID;	
				}
				else{
					if(DataRecognition.hasRTR)
						getgeneratePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='RTR' and Ni=IP_SIA and Ni="+nodeID;
					else if(DataRecognition.hasMAC)
						getgeneratePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='MAC' and Ni=IP_SIA and Ni="+nodeID;
					else if(DataRecognition.hasAGT)
						getgeneratePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='AGT' and Ni=IP_SIA and Ni="+nodeID;
					
				}
				query=new Query(con,getgeneratePkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{
						int size;
	    				while(dataset.next()){
	    					generatePkt++;
	    					size=Integer.parseInt(dataset.getString(1));
	    					generateBytes+=size;
	    					if(size<minPktSize)
	    						minPktSize=size;
	    					if(size>maxPktSize)
	    						maxPktSize=size;
	    					boolean flag=false;
	    					int id=Integer.parseInt(dataset.getString(2));
	    					for(int j=0;j<drop.length;j++)
	    						if(id==drop[j])
	    							flag=true;
	    					if(!flag){
	    						sentPkt++;
	    						sentBytes+=size;
	    					}
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
			}
			if(generatePkt!=0)
				avgPktSize=generateBytes/generatePkt;
			
			getPktType="select count(distinct PT,Name) from "+tablePrefix+"old_wireless_tr where Event='r' and Ni="+nodeID;
			query=new Query(con,getPktType);
			dataset=query.doQuery();
			if(dataset!=null){
				try{
					while(dataset.next()){
						num=Integer.parseInt(dataset.getString(1));
					}
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
			pktType=new String[num][2];
			getPktType="select distinct PT,Name from "+tablePrefix+"old_wireless_tr where Event='r' and Ni="+nodeID;
			query=new Query(con,getPktType);
			dataset=query.doQuery();
			if(dataset!=null){
				try{
					int i=0;
					while(dataset.next()){
						pktType[i][0]=dataset.getString(1);
						pktType[i][1]=dataset.getString(2);
						i++;
					}
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
			for(int i=0;i<num;i++){
				if(pktType[i][1].equals("arp")){
					getreceivePkt="select PS  from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and Name='arp' and Ni="+nodeID;							
				}
				else if(pktType[i][1].equals("old")){
					getreceivePkt="select PS from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and Name='old' and Ni="+nodeID;	
				}
				else{
					if(DataRecognition.hasMAC)
						getreceivePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and TN='MAC' and Ni=IP_DIA and Ni="+nodeID;
					else if(DataRecognition.hasRTR)
						getreceivePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and TN='RTR' and Ni=IP_DIA and Ni="+nodeID;
					else if(DataRecognition.hasAGT)
						getreceivePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and TN='AGT' and Ni=IP_DIA and Ni="+nodeID;
					
				}
				query=new Query(con,getreceivePkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{
						int size;
	    				while(dataset.next()){
	    					receivePkt++;
	    					size=Integer.parseInt(dataset.getString(1));
	    					receiveBytes+=size;
	    				}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
			}

			getreceivePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where Event='r' and IP_DIA<0 and Ni="+nodeID;
			query=new Query(con,getreceivePkt);
			dataset=query.doQuery();
			if(dataset!=null){
				try{
					int size;
    				while(dataset.next()){
    					receivePkt++;
    					size=Integer.parseInt(dataset.getString(1));
    					receiveBytes+=size;
    				}
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
		}
//		if(DataRecognition.is_new_wireless||DataRecognition.is_old_wireless){
//			sentPkt=generatePkt-dropPkt;
//			sentBytes=generateBytes-dropBytes;
//		}
		Object [][] simulationInfo={
				{"Number of generated packets:",String.valueOf(generatePkt)},
				{"Number of sent packets:",String.valueOf(sentPkt)},
				{"Number of received packets:",String.valueOf(receivePkt)},
				{"Number of forwarded packets:",String.valueOf(forwardPkt)},
				{"Number of dropped packets:",String.valueOf(dropPkt)},
//				{"Number of lost packets:",String.valueOf(lostPkt)},
				{"Minimal packet size:",String.valueOf(minPktSize)},
				{"Maximal packet size:",String.valueOf(maxPktSize)},
				{"Average packet size:",String.valueOf(avgPktSize)},
				{"Number of generated Bytes:",String.valueOf(generateBytes)},
				{"Number of sent Bytes:",String.valueOf(sentBytes)},
				{"Number of received Bytes:",String.valueOf(receiveBytes)},
				{"Number of forwarded Bytes:",String.valueOf(forwardBytes)},
				{"Number of drop Bytes:",String.valueOf(dropBytes)},
//				{"Number of lost Bytes:",String.valueOf(lostBytes)}
		};
		String[] columnNames={"Property Name:","Value:"};
		table=new JTable(new TableModel(simulationInfo,columnNames));
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

