package src.operation;

import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Spacer;

public class PacketID extends DefaultDataAnalyse{
	public Connection con;
	public String tablePrefix;
	public int nodeID;
	public String startTime;
	public String endTime;
	public String detail;
	public String graph;
	public String xLabel;
	public String yLabel;
	
	public XYSeriesCollection PID;
	public ChartPanel chartPanel;
	
	public PacketID(Connection con,String tablePrefix,int nodeID,String startTime,String endTime,String detail,String graph,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.nodeID=nodeID;
		this.startTime=startTime;
		this.endTime=endTime;
		this.detail=detail;
		this.graph=graph;
		this.xLabel=xLabel;
		this.yLabel=yLabel;		
		
		createPIDChart();
	}
	
	/**
	 * 
	 */
	private void createPIDChart(){
		XYSeries data=null;
		Query query;
		ResultSet dataset;
		String getgeneratePkt = null;
		String getsentPkt = null;
		String getreceivePkt = null;
		String getreceivePkt1 = null;
		String getforwardPkt = null;
		String getdropPkt = null;		
        /*
         *calculate generated packet ID
         */
		if(detail.equals("IDs of generated packets at CN")){
			if(DataRecognition.is_new_wireless||DataRecognition.is_normal){
				if(DataRecognition.is_normal){
					getgeneratePkt="select Time,UPI from "+tablePrefix+"normal_tr where Event='+' and SN=SAN and SN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				}
				else if(DataRecognition.is_new_wireless){
					if(DataRecognition.hasRTR)
						getgeneratePkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					else if(DataRecognition.hasMAC)
						getgeneratePkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Nl='MAC' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					else if(DataRecognition.hasAGT)
						getgeneratePkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				}
				PID=new XYSeriesCollection();
				data=new XYSeries("generated Packets ID");
				
				query=new Query(con,getgeneratePkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{
						while(dataset.next()){
							data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				PID.addSeries(data);
			}
			else if(DataRecognition.is_old_wireless){
				String getPktType=null;
				String[][] pktType;
				int num=0;
				getPktType="select count(distinct PT,Name) from "+tablePrefix+"old_wireless_tr where Event='s' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
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
				getPktType="select distinct PT,Name from "+tablePrefix+"old_wireless_tr where Event='s' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
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
				
				PID=new XYSeriesCollection();
				data=new XYSeries("generated Packets ID");
				
				for(int i=0;i<num;i++){
					if(pktType[i][1].equals("arp")){
						getgeneratePkt="select Time,EI  from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='arp' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;						
					}
					else if(pktType[i][1].equals("old")){
						getgeneratePkt="select Time,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='old' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;	
					}
					else{
						if(DataRecognition.hasRTR)
							getgeneratePkt="select Time,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='RTR' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasMAC)
							getgeneratePkt="select Time,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='MAC' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasAGT)
							getgeneratePkt="select Time,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='AGT' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						
					}
					query=new Query(con,getgeneratePkt);
					dataset=query.doQuery();
					if(dataset!=null){
						try{							
		    				while(dataset.next()){
		    					data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));		    					
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				}
				PID.addSeries(data);
			}
		}
		/*
		 * calculate sent packet ID
		 */
		else if(detail.equals("IDs of sent packets at CN")){
			/*
			 * deal with the case of that the trace format is normal 
			 */
			if(DataRecognition.is_normal){
				PID=new XYSeriesCollection();
				data=new XYSeries("sent Packets ID");
				
				getsentPkt="select Time,UPI from "+tablePrefix+"normal_tr where Event='-' and SN=SAN and SN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				query=new Query(con,getsentPkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{							
	    				while(dataset.next()){
	    					data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));		    					
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				PID.addSeries(data);
			}
			/*
			 * deal with the case of that the trace format is old wireless 
			 * sent packets equal generated packets minus drop packets
			 */
			else if(DataRecognition.is_old_wireless){
				int[] drop = null;
				getdropPkt="select count(distinct EI) from "+tablePrefix+"old_wireless_tr where Event='D' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
				query=new Query(con,getdropPkt);
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
				getdropPkt="select distinct EI from "+tablePrefix+"old_wireless_tr where Event='D' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
				query=new Query(con,getdropPkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{
						int i=0;
						while(dataset.next()){
							drop[i++]=Integer.parseInt(dataset.getString(1));
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				
				String getPktType=null;
				String[][] pktType;
				int num=0;
				getPktType="select count(distinct PT,Name) from "+tablePrefix+"old_wireless_tr where Event='s' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
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
				getPktType="select distinct PT,Name from "+tablePrefix+"old_wireless_tr where Event='s' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
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
				
				PID=new XYSeriesCollection();
				data=new XYSeries("sent Packets ID");
				
				for(int i=0;i<num;i++){
					if(pktType[i][1].equals("arp")){
						getgeneratePkt="select Time,EI  from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='arp' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;						
					}
					else if(pktType[i][1].equals("old")){
						getgeneratePkt="select Time,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='old' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;	
					}
					else{
						if(DataRecognition.hasMAC)
							getgeneratePkt="select Time,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='MAC' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasRTR)
							getgeneratePkt="select Time,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='RTR' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasAGT)
							getgeneratePkt="select Time,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='AGT' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						
					}
					query=new Query(con,getgeneratePkt);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							double time;
							int id;
		    				while(dataset.next()){
		    					time=Double.parseDouble(dataset.getString(1));
		    					id=Integer.parseInt(dataset.getString(2));
		    					boolean flag=false;
		    					for(int j=0;j<drop.length;j++){
		    						if(id==drop[j])
		    							flag=true;
		    					}
		    					if(!flag)
		    						data.add(time,id);		    					
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				}
				PID.addSeries(data);
			}
			/*
			 * deal with the case of that the trace format is new wireless 
			 *  sent packets equal generated packets minus drop packets
			 */
			else if(DataRecognition.is_new_wireless){
				int[] drop = null;
				getdropPkt="select count(distinct IP_Ii) from "+tablePrefix+"new_wireless_tr where Event='d' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				query=new Query(con,getdropPkt);
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
				getdropPkt="select distinct IP_Ii from "+tablePrefix+"new_wireless_tr where Event='d' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				query=new Query(con,getdropPkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{	
						int i=0;
						while(dataset.next()){
							drop[i++]=Integer.parseInt(dataset.getString(1));
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				if(DataRecognition.hasMAC)
					getgeneratePkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Nl='MAC' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(DataRecognition.hasRTR)
					getgeneratePkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(DataRecognition.hasAGT)
					getgeneratePkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				
				PID=new XYSeriesCollection();
				data=new XYSeries("sent Packets ID");
				
				query=new Query(con,getgeneratePkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{
						double time;
						int id;
						while(dataset.next()){
							time=Double.parseDouble(dataset.getString(1));
	    					id=Integer.parseInt(dataset.getString(2));
	    					boolean flag=false;
	    					for(int i=0;i<drop.length;i++){
	    						if(id==drop[i])
	    							flag=true;
	    					}
	    					if(!flag)
	    						data.add(time,id);		    					
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				PID.addSeries(data);
			}
		}
		/*
		 * calculate received packet id
		 */
		else if(detail.equals("IDs of received packets at CN")){
			if(DataRecognition.is_new_wireless||DataRecognition.is_normal){
				if(DataRecognition.is_normal){
					getreceivePkt="select Time,UPI from "+tablePrefix+"normal_tr where Event='r' and DN=DAN and DN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					getreceivePkt1="select Time,UPI from "+tablePrefix+"normal_tr where Event='r' and DAN<0 and DN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;					
				}
				else if(DataRecognition.is_new_wireless){
					if(DataRecognition.hasMAC){
						getreceivePkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and Nl='MAC' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						getreceivePkt1="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and Nl='MAC' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
					}
					else if(DataRecognition.hasRTR){
						getreceivePkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						getreceivePkt1="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
					}
					else{
						getreceivePkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						getreceivePkt1="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
					}
				}
				
				PID=new XYSeriesCollection();
				data=new XYSeries("received Packets ID");
				
				query=new Query(con,getreceivePkt);
	    		dataset=query.doQuery();
	    		if(dataset!=null){
	    			try{
	    				while(dataset.next()){
	    					data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));
	    				}
	    			}catch(SQLException e1){
	    				e1.printStackTrace();
	    			}
	    		}
	    		query=new Query(con,getreceivePkt1);
	    		dataset=query.doQuery();
	    		if(dataset!=null){
	    			try{
	    				while(dataset.next()){
	    					data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));
	    				}
	    			}catch(SQLException e1){
	    				e1.printStackTrace();
	    			}
	    		}
	    		PID.addSeries(data);
			}
			else if(DataRecognition.is_old_wireless){
				String getPktType=null;
				String[][] pktType;
				int num=0;
				
				PID=new XYSeriesCollection();
				data=new XYSeries("received Packets ID");
				
				getPktType="select count(distinct PT,Name) from "+tablePrefix+"old_wireless_tr where Event='r' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
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
				getPktType="select distinct PT,Name from "+tablePrefix+"old_wireless_tr where Event='r' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
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
						getreceivePkt="select Time,EI  from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and Name='arp' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;						
					}
					else if(pktType[i][1].equals("old")){
						getreceivePkt="select Time,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and Name='old' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					}
					else{
						if(DataRecognition.hasMAC)
							getreceivePkt="select Time,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and TN='MAC' and Ni=IP_DIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasRTR)
							getreceivePkt="select Time,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and TN='RTR' and Ni=IP_DIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasAGT)
							getreceivePkt="select Time,EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and TN='AGT' and Ni=IP_DIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;						
					}
					query=new Query(con,getreceivePkt);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
		    				while(dataset.next()){
		    					data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));		    	    			
		    				}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				}

				getreceivePkt="select distinct PS,EI from "+tablePrefix+"old_wireless_tr where Event='r' and IP_DIA<0 and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				query=new Query(con,getreceivePkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{
	    				while(dataset.next()){
	    					data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));	    	    			
	    				}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				PID.addSeries(data);
			}
		}
		/*
		 * calculate forwarded packets id
		 * we should pay attention to the packets that were forwarded in RTR but dropped in IFQ
		 */
		else if(detail.equals("IDs of forwarded packets at CN")){
			PID=new XYSeriesCollection();
			data=new XYSeries("forwarded Packets ID");
			if(DataRecognition.is_normal)
				getforwardPkt="select Time,UPI from "+tablePrefix+"normal_tr where Event='-' and SN!=SAN and SN!=DAN and SN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
			else if(DataRecognition.is_old_wireless){
				if(DataRecognition.hasRTR)
					getforwardPkt="select Time,EI from "+tablePrefix+"old_wireless_tr where Event='f' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(DataRecognition.hasMAC)
					getforwardPkt="select Time,EI from "+tablePrefix+"old_wireless_tr where Event='s' and NI!=IP_DIA and NI!=IP_SIA and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;				
			}
			else if(DataRecognition.is_new_wireless){
				if(DataRecognition.hasRTR)
					getforwardPkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='f' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(DataRecognition.hasMAC)
					getforwardPkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni!=IP_Idn and Ni!=IP_Isn and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
			}
			if(DataRecognition.is_normal){
		      	query=new Query(con,getforwardPkt);
	        	dataset=query.doQuery();
	        	if(dataset!=null){
	        		try{
	        			while(dataset.next()){
	        				data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));	    	    			
	        			}
	        		}catch(SQLException e1){
	        			e1.printStackTrace();
	        		}
	        	}
			}
			else if(DataRecognition.is_old_wireless){
				int[] drop = null;
				getdropPkt="select count(distinct EI) from "+tablePrefix+"old_wireless_tr where Event='D' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
				query=new Query(con,getdropPkt);
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
				getdropPkt="select distinct EI from "+tablePrefix+"old_wireless_tr where Event='D' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
				query=new Query(con,getdropPkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{
						int i=0;
						while(dataset.next()){
							drop[i++]=Integer.parseInt(dataset.getString(1));
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				query=new Query(con,getforwardPkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{
						double time;
						int id;
	    				while(dataset.next()){
	    					time=Double.parseDouble(dataset.getString(1));
	    					id=Integer.parseInt(dataset.getString(2));
	    					boolean flag=false;
	    					for(int j=0;j<drop.length;j++){
	    						if(id==drop[j])
	    							flag=true;
	    					}
	    					if(!flag)
	    						data.add(time,id);		    					
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
			}
			else if(DataRecognition.is_new_wireless){
				int[] drop = null;
				getdropPkt="select count(distinct IP_Ii) from "+tablePrefix+"new_wireless_tr where Event='d' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				query=new Query(con,getdropPkt);
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
				getdropPkt="select distinct IP_Ii from "+tablePrefix+"new_wireless_tr where Event='d' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				query=new Query(con,getdropPkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{	
						int i=0;
						while(dataset.next()){
							drop[i++]=Integer.parseInt(dataset.getString(1));
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				query=new Query(con,getforwardPkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{
						double time;
						int id;
						while(dataset.next()){
							time=Double.parseDouble(dataset.getString(1));
	    					id=Integer.parseInt(dataset.getString(2));
	    					boolean flag=false;
	    					for(int i=0;i<drop.length;i++){
	    						if(id==drop[i])
	    							flag=true;
	    					}
	    					if(!flag)
	    						data.add(time,id);		    					
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
			}
			PID.addSeries(data);
		}
		/*
		 * calculate dropped packets id
		 */
		else if(detail.equals("IDs of dropped packets at CN")){
			PID=new XYSeriesCollection();
			data=new XYSeries("dropped Packets ID");
			if(DataRecognition.is_normal)
				getdropPkt="select Time,UPI from "+tablePrefix+"normal_tr where Event='d' and SN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;	
			if(DataRecognition.is_old_wireless)
				getdropPkt="select Time,EI from "+tablePrefix+"old_wireless_tr where Event='D' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;	
			if(DataRecognition.is_new_wireless)
				getdropPkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='d' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
			query=new Query(con,getdropPkt);
			dataset=query.doQuery();
			if(dataset!=null){
				try{					
					while(dataset.next()){
						data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));	        		
					}
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
//			System.out.println("data:"+data.getItemCount());
//			System.out.println(data.getX(1));
//			System.out.println(data.getY(1));
			PID.addSeries(data);
		}
		if(graph.equals("Line Chart")){
			chartPanel=new ChartPanel(createLineChart(PID),true,true,true,true, true);
			chartPanel.setPreferredSize(new java.awt.Dimension(775, 400));
		}
	}
	private JFreeChart createLineChart(XYDataset dataset){
//		 create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
		"Packet ID", // chart title
		xLabel, // x axis label
		yLabel, // y axis label
		dataset, // data
		PlotOrientation.VERTICAL,
		true, // include legend
		true, // tooltips
		false // urls
		);
//		 NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);
		StandardLegend legend = (StandardLegend) chart.getLegend();
		legend.setDisplaySeriesShapes(true);
//		 get a reference to the plot for further customisation...
		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setDomainCrosshairLockedOnData(true);
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairLockedOnData(true);
		plot.setRangeCrosshairVisible(true);
		StandardXYItemRenderer renderer = (StandardXYItemRenderer) plot.getRenderer();
		renderer.setPlotShapes(false);
		renderer.setShapesFilled(true);
		renderer.setItemLabelsVisible(true);
//		 change the auto tick unit selection to integer units only...
//		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//		 OPTIONAL CUSTOMISATION COMPLETED.
		return chart;
	}
	
    public String getDataType(){
    	return xySeriesCollection;
    }
    public XYSeriesCollection getXYSeriesCollection(){
    	return PID;
    }
	public ChartPanel getChartPanel() {
		return chartPanel;
	}
}
