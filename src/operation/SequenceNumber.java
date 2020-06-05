/**
 * 
 */
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

/**
 * @author heng
 *
 */
public class SequenceNumber extends DefaultDataAnalyse{
	public Connection con;
	public String tablePrefix;
	public int nodeID;
	public String startTime;
	public String endTime;
	public String sentPkt;
	public String detail;
	public String graph;
	public String xLabel;
	public String yLabel;
	
	public XYSeriesCollection sequenceNumber;
	public ChartPanel chartPanel;
	
	public SequenceNumber(Connection con,String tablePrefix,int nodeID,String startTime,String endTime,String sentPkt,String detail,String graph,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.nodeID=nodeID;
		this.startTime=startTime;
		this.endTime=endTime;
		this.sentPkt=sentPkt;
		this.detail=detail;
		this.graph=graph;
		this.xLabel=xLabel;
		this.yLabel=yLabel;		
		
		sequenceNumber=new XYSeriesCollection();
		createChartPanel();
	}
	
	private void createChartPanel(){
		XYSeries data=null;
		Query query;
		ResultSet dataset;
		/*
		 *To old wireless trace format and new wireless trace format, we only care about cbr packets and tcp packets and it's ack packets  
		 */
		String getgeneratePkt = null;
		String getsentPkt = null;
		String getreceivePkt = null;
		String getreceivePkt1= null;
		String getforwardPkt = null;
		String getdropPkt = null;	
		
		if(detail.equals("Sequence Number of generating packets at CN")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Sequence Number of genereating packets at node "+nodeID);
			if(DataRecognition.is_normal)
				getgeneratePkt="select Time,SeqN from "+tablePrefix+"normal_tr where Event='+' and SN=SAN and PN='"+sentPkt+"' and SN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
			else if(DataRecognition.is_new_wireless||DataRecognition.is_old_wireless){
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack")){
					if(DataRecognition.is_old_wireless){
						if(DataRecognition.hasAGT)
							getgeneratePkt="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where   Event='s' and TN='AGT' and PT='"+sentPkt+"' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasRTR)
							getgeneratePkt="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where   Event='s' and TN='RTR' and PT='"+sentPkt+"' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasMAC)
							getgeneratePkt="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where  Event='s'  and TN='MAC' and PT='"+sentPkt+"' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						
					}
					else if(DataRecognition.is_new_wireless){
						if(DataRecognition.hasAGT)
							getgeneratePkt="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and  IP_It='"+sentPkt+"' and Nl='AGT' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasRTR)
							getgeneratePkt="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and  IP_It='"+sentPkt+"' and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasMAC)
							getgeneratePkt="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and  IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					}						
				}
				else if(sentPkt.equalsIgnoreCase("cbr")){
					if(DataRecognition.is_old_wireless){
						if(DataRecognition.hasAGT)
							getgeneratePkt="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where   Event='s' and TN='AGT' and PT='"+sentPkt+"' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasRTR)
							getgeneratePkt="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where   Event='s' and TN='RTR' and PT='"+sentPkt+"' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasMAC)
							getgeneratePkt="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where  Event='s'  and TN='MAC' and PT='"+sentPkt+"' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						
					}
					else if(DataRecognition.is_new_wireless){
						if(DataRecognition.hasAGT)
							getgeneratePkt="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and  IP_It='"+sentPkt+"' and Nl='AGT' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasRTR)
							getgeneratePkt="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and  IP_It='"+sentPkt+"' and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasMAC)
							getgeneratePkt="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and  IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					}	
				}				
			}
			query=new Query(con,getgeneratePkt);
			dataset=query.doQuery();
			if(dataset!=null){
				try{
					while(dataset.next()){
						if(graph.equals("Line Chart"))
							data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));
					}
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
			if(graph.equals("Line Chart"))
				sequenceNumber.addSeries(data);
		}
		/*
		 * sendt packets equals generated packets minus dropped packets
		 */
		else if(detail.equals("Sequence Number of sending packets at CN")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Sequence Number of sending packets at node "+nodeID);
			int[] drop=null;
			if(DataRecognition.is_old_wireless){
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack"))
					getdropPkt="select count(distinct TCP_SN) from "+tablePrefix+"old_wireless_tr where Event='D' and PT='"+sentPkt+"' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(sentPkt.equalsIgnoreCase("cbr"))
					getdropPkt="select count(distinct CBR_SN) from "+tablePrefix+"old_wireless_tr where Event='D' and PT='"+sentPkt+"' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				
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
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack"))
					getdropPkt="select distinct TCP_SN from "+tablePrefix+"old_wireless_tr where Event='D' and PT='"+sentPkt+"' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(sentPkt.equalsIgnoreCase("cbr"))
					getdropPkt="select distinct CBR_SN from "+tablePrefix+"old_wireless_tr where Event='D' and PT='"+sentPkt+"' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				
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
			}
			if(DataRecognition.is_new_wireless){
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack"))
					getdropPkt="select count(distinct TCP_Ps) from "+tablePrefix+"new_wireless_tr where Event='d' and IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(sentPkt.equalsIgnoreCase("cbr"))
					getdropPkt="select count(distinct CBR_Pi) from "+tablePrefix+"new_wireless_tr where Event='d' and IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				
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
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack"))
					getdropPkt="select distinct TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='d' and IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(sentPkt.equalsIgnoreCase("cbr"))
					getdropPkt="select distinct CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='d' and IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				
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
			}
			
			if(DataRecognition.is_normal){
				getsentPkt="select Time,SeqN from "+tablePrefix+"normal_tr where Event='-' and SN=SAN and PN='"+sentPkt+"'and SN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				query=new Query(con,getsentPkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{							
	    				while(dataset.next()){
	    					if(graph.equals("Line Chart"))
	    						data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));		    					
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				if(graph.equals("Line Chart"))
					sequenceNumber.addSeries(data);
			}
			else if(DataRecognition.is_new_wireless||DataRecognition.is_old_wireless){
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack")){
					if(DataRecognition.is_old_wireless){
						if(DataRecognition.hasAGT)
							getgeneratePkt="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where   Event='s' and TN='AGT' and PT='"+sentPkt+"' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasRTR)
							getgeneratePkt="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where   Event='s' and TN='RTR' and PT='"+sentPkt+"' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasMAC)
							getgeneratePkt="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where  Event='s'  and TN='MAC' and PT='"+sentPkt+"' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						
					}
					else if(DataRecognition.is_new_wireless){
						if(DataRecognition.hasAGT)
							getgeneratePkt="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and  IP_It='"+sentPkt+"' and Nl='AGT' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasRTR)
							getgeneratePkt="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and  IP_It='"+sentPkt+"' and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasMAC)
							getgeneratePkt="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and  IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					}						
				}
				else if(sentPkt.equalsIgnoreCase("cbr")){
					if(DataRecognition.is_old_wireless){
						if(DataRecognition.hasAGT)
							getgeneratePkt="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where   Event='s' and TN='AGT' and PT='"+sentPkt+"' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasRTR)
							getgeneratePkt="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where   Event='s' and TN='RTR' and PT='"+sentPkt+"' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasMAC)
							getgeneratePkt="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where  Event='s'  and TN='MAC' and PT='"+sentPkt+"' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						
					}
					else if(DataRecognition.is_new_wireless){
						if(DataRecognition.hasAGT)
							getgeneratePkt="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and  IP_It='"+sentPkt+"' and Nl='AGT' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasRTR)
							getgeneratePkt="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and  IP_It='"+sentPkt+"' and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasMAC)
							getgeneratePkt="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and  IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					}	
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
	    					if(!flag){
	    						if(graph.equals("Line Chart"))
	    							data.add(time,id);
	    					}
	    				}		    			
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				if(graph.equals("Line Chart"))
					sequenceNumber.addSeries(data);
			}
		}
		/*
		 * received packets include the broadcasting packets
		 */
		else if(detail.equals("Sequence Number receiving packets at CN")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Sequence Number of receiving packets at node "+nodeID);
			if(DataRecognition.is_normal){
				getreceivePkt="select Time,SeqN from "+tablePrefix+"normal_tr where Event='r' and DN=DAN and PN='"+sentPkt+"'  and DN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				getreceivePkt1="select Time,SeqN from "+tablePrefix+"normal_tr where Event='r' and DAN<0 and PN='"+sentPkt+"'and DN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;					
			}
			else if(DataRecognition.is_new_wireless||DataRecognition.is_old_wireless){
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack")){
					if(DataRecognition.is_old_wireless){
						if(DataRecognition.hasMAC){
							getreceivePkt="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where PT='"+sentPkt+"' and Event='r' and TN='MAC' and Ni=IP_DIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where PT='"+sentPkt+"' and Event='r' and TN='MAC' and IP_DIA<0 and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							
						}
						else if(DataRecognition.hasRTR){
							getreceivePkt="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where PT='"+sentPkt+"' and Event='r' and TN='RTR' and Ni=IP_DIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where PT='"+sentPkt+"' and Event='r' and TN='RTR' and IP_DIA<0 and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							
						}
						else if(DataRecognition.hasAGT){
							getreceivePkt="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where PT='"+sentPkt+"' and Event='r' and TN='AGT' and Ni=IP_DIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where PT='"+sentPkt+"' and Event='r' and TN='AGT' and IP_DIA<0 and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							
						}				   
					}
					else if(DataRecognition.is_new_wireless){
						if(DataRecognition.hasMAC){
							getreceivePkt="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and  IP_It='"+sentPkt+"' and Nl='MAC' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and  IP_It='"+sentPkt+"' and Nl='MAC' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
						}
						else if(DataRecognition.hasRTR){
							getreceivePkt="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and  IP_It='"+sentPkt+"' and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and  IP_It='"+sentPkt+"' and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
						}
						else{
							getreceivePkt="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and  IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and  IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
						}
					}
				}
				else if(sentPkt.equalsIgnoreCase("cbr")){
					if(DataRecognition.is_old_wireless){
						if(DataRecognition.hasMAC){
							getreceivePkt="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where PT='"+sentPkt+"' and Event='r' and TN='MAC' and Ni=IP_DIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where PT='"+sentPkt+"' and Event='r' and TN='MAC' and IP_DIA<0 and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							
						}
						else if(DataRecognition.hasRTR){
							getreceivePkt="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where PT='"+sentPkt+"' and Event='r' and TN='RTR' and Ni=IP_DIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where PT='"+sentPkt+"' and Event='r' and TN='RTR' and IP_DIA<0 and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							
						}
						else if(DataRecognition.hasAGT){
							getreceivePkt="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where PT='"+sentPkt+"' and Event='r' and TN='AGT' and Ni=IP_DIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where PT='"+sentPkt+"' and Event='r' and TN='AGT' and IP_DIA<0 and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							
						}				   
					}
					else if(DataRecognition.is_new_wireless){
						if(DataRecognition.hasMAC){
							getreceivePkt="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and  IP_It='"+sentPkt+"' and Nl='MAC' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and  IP_It='"+sentPkt+"' and Nl='MAC' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
						}
						else if(DataRecognition.hasRTR){
							getreceivePkt="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and  IP_It='"+sentPkt+"' and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and  IP_It='"+sentPkt+"' and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
						}
						else{
							getreceivePkt="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and  IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and  IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
						}
					}
				}
			}
			query=new Query(con,getreceivePkt);
    		dataset=query.doQuery();
    		if(dataset!=null){
    			try{
    				while(dataset.next()){
    					if(graph.equals("Line Chart"))
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
    					if(graph.equals("Line Chart"))
    						data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));
    				}
    			}catch(SQLException e1){
    				e1.printStackTrace();
    			}
    		}
    		if(graph.equals("Line Chart"))
    			sequenceNumber.addSeries(data);
		}
		/*
		 * we should pay attention to the packets that are dropped
		 */
		else if(detail.equals("Sequence Number forwarding packets at CN")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Sequence Number of forwarding packets at node "+nodeID);
			int[] drop=null;
			if(DataRecognition.is_old_wireless){
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack"))
					getdropPkt="select count(distinct TCP_SN) from "+tablePrefix+"old_wireless_tr where Event='D' and PT='"+sentPkt+"' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(sentPkt.equalsIgnoreCase("cbr"))
					getdropPkt="select count(distinct CBR_SN) from "+tablePrefix+"old_wireless_tr where Event='D' and PT='"+sentPkt+"' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				
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
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack"))
					getdropPkt="select distinct TCP_SN from "+tablePrefix+"old_wireless_tr where Event='D' and PT='"+sentPkt+"' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(sentPkt.equalsIgnoreCase("cbr"))
					getdropPkt="select distinct CBR_SN from "+tablePrefix+"old_wireless_tr where Event='D' and PT='"+sentPkt+"' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				
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
			}
			if(DataRecognition.is_new_wireless){
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack"))
					getdropPkt="select count(distinct TCP_Ps) from "+tablePrefix+"new_wireless_tr where Event='d' and IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(sentPkt.equalsIgnoreCase("cbr"))
					getdropPkt="select count(distinct CBR_Pi) from "+tablePrefix+"new_wireless_tr where Event='d' and IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				
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
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack"))
					getdropPkt="select distinct TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='d' and IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(sentPkt.equalsIgnoreCase("cbr"))
					getdropPkt="select distinct CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='d' and IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				
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
			}
			if(DataRecognition.is_normal){
				getforwardPkt="select Time,SeqN from "+tablePrefix+"normal_tr where Event='-' and SN!=SAN and SN!=DAN and PN='"+sentPkt+"'and SN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				query=new Query(con,getforwardPkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{							
	    				while(dataset.next()){
	    					if(graph.equals("Line Chart"))
	    						data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));		    					
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				if(graph.equals("Line Chart"))
					sequenceNumber.addSeries(data);
			}
			else if(DataRecognition.is_new_wireless||DataRecognition.is_old_wireless){
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack")){
					if(DataRecognition.is_old_wireless){
						if(DataRecognition.hasRTR){
							getforwardPkt="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where Event='f' and PT='"+sentPkt+"' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;							
						}
						else if(DataRecognition.hasMAC){
							getforwardPkt="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where Event='s' and PT='"+sentPkt+"' and NI!=IP_DIA and NI!=IP_SIA and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;		
						}										   
					}
					else if(DataRecognition.is_new_wireless){
						if(DataRecognition.hasRTR){
							getforwardPkt="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='f' and IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;							
						}
						else if(DataRecognition.hasMAC){
							getforwardPkt="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='s' and IP_It='"+sentPkt+"' and Ni!=IP_Idn and Ni!=IP_Isn and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;							
						}
					}
				}
				else if(sentPkt.equalsIgnoreCase("cbr")){
					if(DataRecognition.is_old_wireless){
						if(DataRecognition.hasRTR){
							getforwardPkt="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where Event='f' and PT='"+sentPkt+"' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;							
						}
						else if(DataRecognition.hasMAC){
							getforwardPkt="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where Event='s' and PT='"+sentPkt+"' and NI!=IP_DIA and NI!=IP_SIA and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;		
						}										   
					}
					else if(DataRecognition.is_new_wireless){
						if(DataRecognition.hasRTR){
							getforwardPkt="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='f' and IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;							
						}
						else if(DataRecognition.hasMAC){
							getforwardPkt="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='s' and IP_It='"+sentPkt+"' and Ni!=IP_Idn and Ni!=IP_Isn and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;							
						}
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
	    					if(!flag){
	    						if(graph.equals("Line Chart"))
	    							data.add(time,id);
	    					}
	    				}		    			
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				if(graph.equals("Line Chart"))
					sequenceNumber.addSeries(data);
			}
		}
		else if(detail.equals("Sequence Number of dropping packets at CN")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Sequence Number of dropping packets at node "+nodeID);
			if(DataRecognition.is_normal)
				getdropPkt="select Time,SeqN from "+tablePrefix+"normal_tr where Event='d' and SN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;	
			else if(DataRecognition.is_new_wireless||DataRecognition.is_old_wireless){
				if(sentPkt.equalsIgnoreCase("tcp")||sentPkt.equalsIgnoreCase("ack")){
				    if(DataRecognition.is_old_wireless)
				    	getdropPkt="select Time,TCP_SN from "+tablePrefix+"old_wireless_tr where Event='D' and PT='"+sentPkt+"' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					if(DataRecognition.is_new_wireless)
						getdropPkt="select Time,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='d' and IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				}
				else if(sentPkt.equalsIgnoreCase("cbr")){
					if(DataRecognition.is_old_wireless)
						getdropPkt="select Time,CBR_SN from "+tablePrefix+"old_wireless_tr where Event='D' and PT='"+sentPkt+"' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					if(DataRecognition.is_new_wireless)
						getdropPkt="select Time,CBR_Pi from "+tablePrefix+"new_wireless_tr where Event='d' and IP_It='"+sentPkt+"' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				}
			}	
			query=new Query(con,getdropPkt);
			dataset=query.doQuery();
			if(dataset!=null){
				try{					
					while(dataset.next()){
						if(graph.equals("Line Chart"))
							data.add(Double.parseDouble(dataset.getString(1)),Integer.parseInt(dataset.getString(2)));	        		
					}
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
			if(graph.equals("Line Chart"))
				sequenceNumber.addSeries(data);
		}
		if(graph.equals("Line Chart")){
			chartPanel=new ChartPanel(createLineChart(sequenceNumber),true,true,true,true, true);
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
   	return sequenceNumber;
   }
	public ChartPanel getChartPanel() {
		return chartPanel;
	}
}
