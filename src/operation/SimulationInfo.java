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
import java.util.TreeSet;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;





public class SimulationInfo extends DefaultDataAnalyse{
	private JTable table;
	public Connection con;
	public String tablePrefix;
	
	 private JPopupMenu copyPopupMenu;
	 private JMenuItem copyMenuItem;
	 private PopupListener copyPopupListener;
	 
	private double maxTime=0;
	private double minTime=0;
	private int nodeNum=0;
	private int sNodeNum=0;
	private int rNodeNum=0;
	private int dNodeNum=0;
	private int generatePkt=0;
	private int sentPkt=0;
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
	
	public SimulationInfo(Connection con,String tablePrefix){
		this.con=con;
		this.tablePrefix=tablePrefix;
		copyMenuItem=new JMenuItem("Copy");
		copyMenuItem.setAccelerator(KeyStroke.getKeyStroke('C',java.awt.Event.CTRL_MASK));
		copyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                copySystemPropertiesToClipboard();
            }
        });
		
		createSimulationInfoTable();
		
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
    /*
     * create the table to display simulation information
     */
	public void createSimulationInfoTable(){
		ResultSet dataset;
		Query query;
		int[] drop = null;
		String getMinTime = null;
		String getMaxTime = null;
		String getNodeNum = null;
		/*
		 * 
		 */
		String getsNodeNum = null;
		/*
		 * 
		 */
		String getrNodeNum = null;
		String getdNodeNum = null;
		/*
		 * 
		 */
		String getgeneratePkt = null;
		String getsentPkt = null;
		String getreceivePkt = null;
		String getreceivePkt1 = null;
		String getforwardPkt = null;
		String getdropNum = null;
		String getdropPkt = null;
//		String getlostPkt = null;
		
		if(DataRecognition.is_normal){
			getMinTime="select min(Time) from "+tablePrefix+"normal_tr";
			getMaxTime="select max(Time) from "+tablePrefix+"normal_tr";
			getNodeNum="select distinct SN,DN from "+tablePrefix+"normal_tr ";
			getsNodeNum="select count(distinct SN) from "+tablePrefix+"normal_tr where Event='-' and SN=SAN";
			getrNodeNum="select count(distinct DN) from "+tablePrefix+"normal_tr where Event='r' and DN=DAN";
			getdNodeNum="select count(distinct SN) from "+tablePrefix+"normal_tr where Event='d'";
			getgeneratePkt="select distinct UPI,PS from "+tablePrefix+"normal_tr where Event='+' and SN=SAN";
			getsentPkt="select distinct UPI,PS from "+tablePrefix+"normal_tr where Event='-' and SN=SAN";//not include forward
			getreceivePkt="select distinct UPI,PS from "+tablePrefix+"normal_tr where Event='r' and DN=DAN";
			getreceivePkt1="select distinct UPI,PS from "+tablePrefix+"normal_tr where Event='r' and DAN<0 ";
			getforwardPkt="select distinct UPI,PS from "+tablePrefix+"normal_tr where Event='-' and SN!=SAN and SN!=DAN";
			getdropPkt="select distinct UPI,PS from "+tablePrefix+"normal_tr where Event='d'";
//			getlostPkt="select distinct UPI,PS from "+tablePrefix+"normal_tr t1 where Event='-' and Not Exists (select Event from "+tablePrefix+"normal_tr t2 where Event='r' and t1.SN=t2.SN and t1.DN=t2.DN and t1.UPI=t2.UPI)";
		}
		/*
		 * if a packet is forwarded by more than one node 
		 * we calculate it only once
		 */
		else if(DataRecognition.is_old_wireless){
			getMinTime="select min(Time) from "+tablePrefix+"old_wireless_tr";
			getMaxTime="select max(Time) from "+tablePrefix+"old_wireless_tr";
			getNodeNum="select count(distinct NI) from "+tablePrefix+"old_wireless_tr";
			getsNodeNum="select count(distinct NI) from "+tablePrefix+"old_wireless_tr where Event='s' and NI=IP_SIA";
			getrNodeNum="select count(distinct NI) from "+tablePrefix+"old_wireless_tr where Event='r' and IP_DIA=NI";
			getdNodeNum="select count(distinct NI) from "+tablePrefix+"old_wireless_tr where Event='d'";
			if(DataRecognition.hasRTR)
				getforwardPkt="select distinct EI,PS from "+tablePrefix+"old_wireless_tr where Event='f'";
			else if(DataRecognition.hasMAC)
				getforwardPkt="select distinct EI,PS from "+tablePrefix+"old_wireless_tr where Event='s' and NI!=IP_DIA and NI!=IP_SIA";
			getdropNum="select count(distinct EI,PS) from "+tablePrefix+"old_wireless_tr where Event='D'";
			getdropPkt="select distinct EI,PS from "+tablePrefix+"old_wireless_tr where Event='d'";
//			getlostPkt="select distinct EI,PS from "+tablePrefix+"old_wireless_tr t1 where Not Exists (select Event from "+tablePrefix+"old_wireless_tr t2 where t1.IP_DIA=t2.NI and t1.EI=t2.EI)";
		}
		
		else if(DataRecognition.is_new_wireless){
			getMinTime="select min(Time) from "+tablePrefix+"new_wireless_tr";
			getMaxTime="select max(Time) from "+tablePrefix+"new_wireless_tr";
			getNodeNum="select count(distinct Ni) from "+tablePrefix+"new_wireless_tr";
			getsNodeNum="select count(distinct Ni) from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn";
			getrNodeNum="select count(distinct Ni) from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn=Ni";
			getdNodeNum="select count(distinct Ni) from "+tablePrefix+"new_wireless_tr where Event='d'";
			
			getreceivePkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn";
			getreceivePkt1="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0";
			
			if(DataRecognition.hasRTR)
				getforwardPkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='f'";
			else if(DataRecognition.hasMAC)
				getforwardPkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='s' and Ni!=IP_Idn and Ni!=IP_Isn";
			if(DataRecognition.hasRTR)
				getgeneratePkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Nl='RTR'";
			else if(DataRecognition.hasMAC)
				getgeneratePkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Nl='MAC'";
			else if(DataRecognition.hasAGT)
				getgeneratePkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn";
			getdropNum="select count(distinct IP_Ii,IP_Il) from "+tablePrefix+"new_wireless_tr where Event='d'";
			getdropPkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr where Event='d'";
//			getlostPkt="select distinct IP_Ii,IP_Il from "+tablePrefix+"new_wireless_tr t1 where Event='s' and Not Exists (select Event from "+tablePrefix+"new_wireless_tr t2 where Event='r' and t1.IP_Idn=Ni and t1.IP_Ii=t2.IP_Ii)";
		}
		if(DataRecognition.is_new_wireless||DataRecognition.is_old_wireless){
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
		}
		query=new Query(con,getdropPkt);
		dataset=query.doQuery();
		if(dataset!=null){
			try{	
				int i=0;
				while(dataset.next()){
					dropPkt++;
					if(DataRecognition.is_new_wireless||DataRecognition.is_old_wireless)
						drop[i++]=Integer.parseInt(dataset.getString(1));
					dropBytes+=Integer.parseInt(dataset.getString(2));
				}	
//				System.out.println("DROP:"+dropPkt);
//				System.out.println(dropBytes);
			}catch(SQLException e1){
				e1.printStackTrace();
			}
		}
		query=new Query(con,getMinTime);
		dataset=query.doQuery();
//		System.out.println(dataset==null);
		if(dataset!=null){
			try{
				while(dataset.next()){
					minTime=Double.parseDouble(dataset.getString(1));
//					System.out.println(minTime);
				}											
			}catch(SQLException e1){
				e1.printStackTrace();
			}
		}
		query=new Query(con,getMaxTime);
		dataset=query.doQuery();
		if(dataset!=null){
			try{
				while(dataset.next()){
					maxTime=Double.parseDouble(dataset.getString(1));
//					System.out.println(maxTime);
				}											
			}catch(SQLException e1){
				e1.printStackTrace();
			}
		}
		query=new Query(con,getNodeNum);
		dataset=query.doQuery();
		if(DataRecognition.is_normal){
			if(dataset!=null){
				try{
					TreeSet node=new TreeSet();	
					while(dataset.next()){
						node.add((Object)dataset.getString(1));
						node.add((Object)dataset.getString(2));
					}
					nodeNum=node.size();
//					System.out.println("nodenum:"+nodeNum);
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
		}
		else{
			if(dataset!=null){
				try{
					while(dataset.next()){
						nodeNum=Integer.parseInt(dataset.getString(1));
					}
//					System.out.println("nodenum:"+nodeNum);
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
		}
		query=new Query(con,getsNodeNum);
		dataset=query.doQuery();
		if(dataset!=null){
			try{
				while(dataset.next()){
					sNodeNum=Integer.parseInt(dataset.getString(1));
				}
//				System.out.println("snodeNum:"+sNodeNum);
			}catch(SQLException e1){
				e1.printStackTrace();
			}
		}
		query=new Query(con,getrNodeNum);
		dataset=query.doQuery();
		if(dataset!=null){
			try{
				while(dataset.next()){
					rNodeNum=Integer.parseInt(dataset.getString(1));
				}
//				System.out.println("rnodenum:"+rNodeNum);
			}catch(SQLException e1){
				e1.printStackTrace();
			}
		}
		query=new Query(con,getdNodeNum);
		dataset=query.doQuery();
		if(dataset!=null){
			try{
				
				while(dataset.next()){
					dNodeNum=Integer.parseInt(dataset.getString(1));				
				}	
//				System.out.println("DnodeNUM:"+dNodeNum);
			}catch(SQLException e1){
				e1.printStackTrace();
			}
		}
        if(DataRecognition.is_normal||DataRecognition.is_new_wireless){
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
    				}
    				if(generatePkt!=0)
    					avgPktSize=generateBytes/generatePkt;
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
					while(dataset.next()){
						sentPkt++;
						sentBytes+=Integer.parseInt(dataset.getString(2));
					}		
//					System.out.println("SENTPKT:"+sentPkt);
//					System.out.println("SENTBYES:"+sentBytes);
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
		}
		if(getforwardPkt!=null){
			if(DataRecognition.is_normal){
				query=new Query(con,getforwardPkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{					
						while(dataset.next()){
							forwardPkt++;
							forwardBytes+=Integer.parseInt(dataset.getString(2));
						}
//						System.out.println("FORWARDPKT:"+forwardPkt);
//						System.out.println("FORWARDBYTES:"+forwardBytes);
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
			}
			if(DataRecognition.is_new_wireless||DataRecognition.is_old_wireless){
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
		}

		if(DataRecognition.is_new_wireless||DataRecognition.is_normal){
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
//		query=new Query(getlostPkt,url,name,usrpwd);
//		dataset=query.doQuery();
//		if(dataset!=null){
//			try{					
//				while(dataset.next()){
//					lostPkt++;
//					lostBytes+=Integer.parseInt(dataset.getString(2));
//				}	
//				System.out.println("LOST:"+lostPkt);
//				System.out.println(lostBytes);
//			}catch(SQLException e1){
//				e1.printStackTrace();
//			}
//		}
		if(DataRecognition.is_old_wireless){
			String getPktType=null;
			String[][] pktType;
			int num=0;
			getPktType="select count(distinct PT,Name) from "+tablePrefix+"old_wireless_tr where Event='s'";
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
			getPktType="select distinct PT,Name from "+tablePrefix+"old_wireless_tr where Event='s'";
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
					getgeneratePkt="select PS,EI  from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='arp'";							
				}
				else if(pktType[i][1].equals("old")){
					getgeneratePkt="select PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='old'";	
				}
				else{
					if(DataRecognition.hasRTR)
						getgeneratePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='RTR' and Ni=IP_SIA";
					else if(DataRecognition.hasMAC)
						getgeneratePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='MAC' and Ni=IP_SIA";
					else if(DataRecognition.hasAGT)
						getgeneratePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='AGT' and Ni=IP_SIA";
					
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
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
			}
			if(generatePkt!=0)
				avgPktSize=generateBytes/generatePkt;
			
			getPktType="select count(distinct PT,Name) from "+tablePrefix+"old_wireless_tr where Event='r'";
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
			getPktType="select distinct PT,Name from "+tablePrefix+"old_wireless_tr where Event='r'";
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
					getreceivePkt="select PS  from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and Name='arp'";							
				}
				else if(pktType[i][1].equals("old")){
					getreceivePkt="select PS from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and Name='old'";	
				}
				else{
					if(DataRecognition.hasMAC)
						getreceivePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and TN='MAC' and Ni=IP_DIA";
					else if(DataRecognition.hasRTR)
						getreceivePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and TN='RTR' and Ni=IP_DIA";
					else if(DataRecognition.hasAGT)
						getreceivePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and TN='AGT' and Ni=IP_DIA";
					
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

			getreceivePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where Event='r' and IP_DIA<0";
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
		if(DataRecognition.is_new_wireless||DataRecognition.is_old_wireless){
			sentPkt=generatePkt-dropPkt;
			sentBytes=generateBytes-dropBytes;
		}
		Object [][] simulationInfo={
				{"Simulation length in seconds:",String.valueOf(maxTime-minTime)},
				{"Number of nodes:",String.valueOf(nodeNum)},
				{"Number of sending nodes:",String.valueOf(sNodeNum)},
				{"Number of receiving nodes:",String.valueOf(rNodeNum)},
				{"Number of dropping nodes:",String.valueOf(dNodeNum)},
				{"Number of generated packets:",String.valueOf(generatePkt)},
				{"Number of sent packets:",String.valueOf(sentPkt)},
				{"Number of received packets:",String.valueOf(receivePkt)},
				{"Number of forwarded packets:",String.valueOf(forwardPkt)},
				{"Number of dropped packets:",String.valueOf(dropPkt)},
//				{"Number of lost packets:",String.valueOf(lostPkt)},
				{"Minimal generated packet size:",String.valueOf(minPktSize)},
				{"Maximal generated packet size:",String.valueOf(maxPktSize)},
				{"Average generated packet size:",String.valueOf(avgPktSize)},
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
