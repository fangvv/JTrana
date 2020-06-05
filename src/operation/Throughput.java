package src.operation;

import java.awt.Color;
import java.awt.GradientPaint;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Spacer;

public class Throughput extends DefaultDataAnalyse{
	public Connection con;
	public String tablePrefix;
	public int nodeID;
	public String startTime;
	public String endTime;
	public double interval;
	public String detail;
	public String graph;
	public String xLabel;
	public String yLabel;
	
	private double start;
	private double end;
	
	public XYSeriesCollection throughput1;
	public DefaultCategoryDataset throughput2;
	public ChartPanel chartPanel;
	
	public Throughput(Connection con,String tablePrefix,int nodeID,String startTime,String endTime,double interval,String detail,String graph,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.nodeID=nodeID;
		this.startTime=startTime;
		this.endTime=endTime;
		this.interval=interval;
		this.detail=detail;
		this.graph=graph;
		this.xLabel=xLabel;
		this.yLabel=yLabel;	
		try{
			start=Double.parseDouble(startTime);
			end=Double.parseDouble(endTime);
			createThroughputChart();
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		
	}
		
	private void createThroughputChart(){
		XYSeries data = null;
		Query query;
		ResultSet dataset;
		String getgeneratePkt = null;
		String getsentPkt = null;
		String getreceivePkt = null;
		String getreceivePkt1 = null;
		String getforwardPkt = null;
		String getdropPkt = null;		
		
		int dataNum=(int)((end-start)/interval);
		if(graph.equals("Line Chart"))
			throughput1=new XYSeriesCollection();
		
		if(graph.equals("Bar Chart")||graph.equals("3D Bar Chart"))
			throughput2=new DefaultCategoryDataset();
		/*
		 * calculate throughput of generating packets 
		 */
		if(detail.equals("Throughput of generating packets at CN")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Throughput of generating packets");
			
			for(int k=1;k<=dataNum;k++){
				
				startTime=String.valueOf(start+(k-1)*interval);
				endTime=String.valueOf(start+k*interval);
				
				if(DataRecognition.is_new_wireless||DataRecognition.is_normal){
					if(DataRecognition.is_normal){
						getgeneratePkt="select count(UPI) from "+tablePrefix+"normal_tr where Event='+' and SN=SAN and SN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						query=new Query(con,getgeneratePkt);
						dataset=query.doQuery();
						if(dataset!=null){
							try{
								while(dataset.next()){
									if(graph.equals("Line Chart"))
										data.add(Double.parseDouble(endTime),Integer.parseInt(dataset.getString(1)));
									else
										throughput2.addValue(Integer.parseInt(dataset.getString(1)),"Throughput of generating packets",endTime);
								}
							}catch(SQLException e1){
								e1.printStackTrace();
							}
						}
					}
					else if(DataRecognition.is_new_wireless){
						int total=0;
						if(DataRecognition.hasRTR)
							getgeneratePkt="select count(IP_Ii) from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasMAC)
							getgeneratePkt="select count(IP_Ii) from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Nl='MAC' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasAGT)
							getgeneratePkt="select count(IP_Ii) from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						query=new Query(con,getgeneratePkt);
						dataset=query.doQuery();
						if(dataset!=null){
							try{
								while(dataset.next()){
									total+=Integer.parseInt(dataset.getString(1));
								}
							}catch(SQLException e1){
								e1.printStackTrace();
							}
						}
//						if(DataRecognition.hasMAC){
//							getgeneratePkt="select count(Time) from "+tablePrefix+"new_wireless_tr where Event='s' and Name='new' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
//							query=new Query(getgeneratePkt,url,name,usrpwd);
//							dataset=query.doQuery();
//							if(dataset!=null){
//								try{
//									while(dataset.next()){
//										total+=Integer.parseInt(dataset.getString(1));
//									}
//								}catch(SQLException e1){
//									e1.printStackTrace();
//								}
//							}
//							getgeneratePkt="select count(Time) from "+tablePrefix+"new_wireless_tr where Event='s' and Name='arp' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
//							query=new Query(getgeneratePkt,url,name,usrpwd);
//							dataset=query.doQuery();
//							if(dataset!=null){
//								try{
//									while(dataset.next()){
//										total+=Integer.parseInt(dataset.getString(1));
//									}
//								}catch(SQLException e1){
//									e1.printStackTrace();
//								}
//							}
//						}
						if(graph.equals("Line Chart"))
							data.add(Double.parseDouble(endTime),total);
						else
							throughput2.addValue(total,"Throughput of generating packets",endTime);
					
					}
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
							int j=0;
							while(dataset.next()){
								pktType[j][0]=dataset.getString(1);
								pktType[j][1]=dataset.getString(2);
								j++;
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					int total=0;					
					for(int i=0;i<num;i++){
						if(pktType[i][1].equals("arp")){
							continue;
//							getgeneratePkt="select count(EI)  from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='arp' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;						
						}
						else if(pktType[i][1].equals("old")){
							continue;
//							getgeneratePkt="select count(EI) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='old' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;	
						}
						else{
							if(DataRecognition.hasRTR)
								getgeneratePkt="select count(EI) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='RTR' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							else if(DataRecognition.hasMAC)
								getgeneratePkt="select count(EI) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='MAC' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							else if(DataRecognition.hasAGT)
								getgeneratePkt="select count(EI) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='AGT' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							
						}
						query=new Query(con,getgeneratePkt);
						dataset=query.doQuery();
						if(dataset!=null){
							try{							
			    				while(dataset.next()){
			    					total+=Integer.parseInt(dataset.getString(1));
			    				}
							}catch(SQLException e1){
								e1.printStackTrace();
							}
						}
					}
					if(graph.equals("Line Chart"))
						data.add(Double.parseDouble(endTime),total);
					else
						throughput2.addValue(total,"Throughput of generating packets",endTime);					
				}
				
			}
			if(graph.equals("Line Chart"))
				throughput1.addSeries(data);
			
		}
		/*
		 * calculate throughput of sending packets
		 */
		else if(detail.equals("Throughput of sending packets at CN")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Throughput of sending packets");
			int[] drop=null;
			if(DataRecognition.is_old_wireless){
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
			}
			if(DataRecognition.is_new_wireless){
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
			}
			/*
			 * deal with the case of that the trace format is normal 
			 */
			for(int k=1;k<=dataNum;k++){
				
				startTime=String.valueOf(start+(k-1)*interval);
				endTime=String.valueOf(start+k*interval);
				
				if(DataRecognition.is_normal){
										
					getsentPkt="select count(UPI) from "+tablePrefix+"normal_tr where Event='-' and SN=SAN and SN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					query=new Query(con,getsentPkt);
					dataset=query.doQuery();
					if(dataset!=null){
						try{							
		    				while(dataset.next()){
		    					if(graph.equals("Line Chart"))
									data.add(Double.parseDouble(endTime),Integer.parseInt(dataset.getString(1)));
								else
									throughput2.addValue(Integer.parseInt(dataset.getString(1)),"Throughput of sending packets",endTime);							
		    				}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				}
				/*
				 * deal with the case of that the trace format is old wireless 
				 * sent packets equal generated packets minus drop packets
				 */
				else if(DataRecognition.is_old_wireless){
//					int[] drop = null;
					
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
					int n=0;
					for(int i=0;i<num;i++){
						if(pktType[i][1].equals("arp")){
							continue;
//							getgeneratePkt="select EI  from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='arp' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;						
						}
						else if(pktType[i][1].equals("old")){
							continue;
//							getgeneratePkt="select EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='old' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;	
						}
						else{
							if(DataRecognition.hasMAC)
								getgeneratePkt="select EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='MAC' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							else if(DataRecognition.hasRTR)
								getgeneratePkt="select EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='RTR' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							else if(DataRecognition.hasAGT)
								getgeneratePkt="select EI from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and TN='AGT' and Ni=IP_SIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							
						}
						query=new Query(con,getgeneratePkt);
						dataset=query.doQuery();
						if(dataset!=null){
							try{
								int id;
			    				while(dataset.next()){
			    					id=Integer.parseInt(dataset.getString(1));
			    					boolean flag=false;
			    					for(int j=0;j<drop.length;j++){
			    						if(id==drop[j])
			    							flag=true;
			    					}
			    					if(!flag){
			    						n++;
			    					}
			    				}		    			
							}catch(SQLException e1){
								e1.printStackTrace();
							}
						}
					}
					if(graph.equals("Line Chart"))
						data.add(Double.parseDouble(endTime),n);
					else
						throughput2.addValue(n,"Throughput of sending packets",endTime);
					
				}
				/*
				 * deal with the case of that the trace format is new wireless 
				 *  sent packets equal generated packets minus drop packets
				 */
				else if(DataRecognition.is_new_wireless){
					int total=0;
//					int[] drop = null;
					
					if(DataRecognition.hasMAC)
						getgeneratePkt="select IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Nl='MAC' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					else if(DataRecognition.hasRTR)
						getgeneratePkt="select IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					else if(DataRecognition.hasAGT)
						getgeneratePkt="select IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
									
					query=new Query(con,getgeneratePkt);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							int id;
							while(dataset.next()){
		    					id=Integer.parseInt(dataset.getString(1));
		    					boolean flag=false;
		    					for(int i=0;i<drop.length;i++){
		    						if(id==drop[i])
		    							flag=true;
		    					}
		    					if(!flag){
		    						total++;
		    					}		    							    					
							}							
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
//					if(DataRecognition.hasMAC){
//						getgeneratePkt="select count(Time) from "+tablePrefix+"new_wireless_tr where Event='s' and Name='new' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
//						query=new Query(getgeneratePkt,url,name,usrpwd);
//						dataset=query.doQuery();
//						if(dataset!=null){
//							try{
//								while(dataset.next()){
//									total+=Integer.parseInt(dataset.getString(1));
//								}
//							}catch(SQLException e1){
//								e1.printStackTrace();
//							}
//						}
//						getgeneratePkt="select count(Time) from "+tablePrefix+"new_wireless_tr where Event='s' and Name='arp' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
//						query=new Query(getgeneratePkt,url,name,usrpwd);
//						dataset=query.doQuery();
//						if(dataset!=null){
//							try{
//								while(dataset.next()){
//									total+=Integer.parseInt(dataset.getString(1));
//								}
//							}catch(SQLException e1){
//								e1.printStackTrace();
//							}
//						}
//					}
					if(graph.equals("Line Chart"))
						data.add(Double.parseDouble(endTime),total);
					else
						throughput2.addValue(total,"Throughput of sending packets",endTime);		    			
				
				}
			}
			if(graph.equals("Line Chart"))
				throughput1.addSeries(data);
		}
		/*
		 * calculate Throughput of receiving packets at current node
		 */
		else if(detail.equals("Throughput of receiving packets at CN")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Throughput of receiving packets");
			
			for(int k=1;k<=dataNum;k++){
				
				startTime=String.valueOf(start+(k-1)*interval);
				endTime=String.valueOf(start+k*interval);
				
				if(DataRecognition.is_new_wireless||DataRecognition.is_normal){
					int total=0;
					if(DataRecognition.is_normal){
						getreceivePkt="select count(UPI) from "+tablePrefix+"normal_tr where Event='r' and DN=DAN and DN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						getreceivePkt1="select count(UPI) from "+tablePrefix+"normal_tr where Event='r' and DAN<0 and DN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;					
					}
					else if(DataRecognition.is_new_wireless){
						if(DataRecognition.hasMAC){
							getreceivePkt="select count(IP_Ii) from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and Nl='MAC' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select count(IP_Ii) from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and Nl='MAC' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
						}
						else if(DataRecognition.hasRTR){
							getreceivePkt="select count(IP_Ii) from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select count(IP_Ii) from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and Nl='RTR' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
						}
						else{
							getreceivePkt="select count(IP_Ii) from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							getreceivePkt1="select count(IP_Ii) from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Idn<0 and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;			
						}
					}
					query=new Query(con,getreceivePkt);
		    		dataset=query.doQuery();
		    		if(dataset!=null){
		    			try{
		    				while(dataset.next()){
		    					total+=Integer.parseInt(dataset.getString(1));
//		    					System.out.println("total:"+total);
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
		    					total+=Integer.parseInt(dataset.getString(1));
//		    					System.out.println("total:"+total);
		    				}
		    			}catch(SQLException e1){
		    				e1.printStackTrace();
		    			}
		    		}
//		    		if(DataRecognition.is_new_wireless){
//						if(DataRecognition.hasMAC){
//							getreceivePkt="select count(Time) from "+tablePrefix+"new_wireless_tr where Event='r' and Name='new' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
//							query=new Query(getreceivePkt,url,name,usrpwd);
//							dataset=query.doQuery();
//							if(dataset!=null){
//								try{
//									while(dataset.next()){
//										total+=Integer.parseInt(dataset.getString(1));
//									}
//								}catch(SQLException e1){
//									e1.printStackTrace();
//								}
//							}
//							getreceivePkt="select count(Time) from "+tablePrefix+"new_wireless_tr where Event='r' and Name='arp' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
//							query=new Query(getreceivePkt,url,name,usrpwd);
//							dataset=query.doQuery();
//							if(dataset!=null){
//								try{
//									while(dataset.next()){
//										total+=Integer.parseInt(dataset.getString(1));
//									}
//								}catch(SQLException e1){
//									e1.printStackTrace();
//								}
//							}
//						}
//		    		}
		    		if(graph.equals("Line Chart"))
						data.add(Double.parseDouble(endTime),total);
					else
						throughput2.addValue(total,"Throughput of receiving packets",endTime);			    		
				}
				else if(DataRecognition.is_old_wireless){
					String getPktType=null;
					String[][] pktType;
					int num=0;
					int total=0;
//					System.out.println("AAAAAAAAA");
					
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
							continue;
//							getreceivePkt="select count(EI)  from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and Name='arp' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;						
						}
						else if(pktType[i][1].equals("old")){
							continue;
//							getreceivePkt="select count(EI) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and Name='old' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
						}
						else{
							if(DataRecognition.hasMAC)
								getreceivePkt="select count(EI) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and TN='MAC' and Ni=IP_DIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							else if(DataRecognition.hasRTR)
								getreceivePkt="select count(EI) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and TN='RTR' and Ni=IP_DIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
							else if(DataRecognition.hasAGT)
								getreceivePkt="select count(EI) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='r' and TN='AGT' and Ni=IP_DIA and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;						
						}
						query=new Query(con,getreceivePkt);
						dataset=query.doQuery();
						if(dataset!=null){
							try{
			    				while(dataset.next()){
			    					total+=Integer.parseInt(dataset.getString(1));
			    				}
							}catch(SQLException e1){
								e1.printStackTrace();
							}
						}
					}

					getreceivePkt="select count(EI) from "+tablePrefix+"old_wireless_tr where Event='r' and IP_DIA<0 and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					query=new Query(con,getreceivePkt);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
		    				while(dataset.next()){
		    					total+=Integer.parseInt(dataset.getString(1));
		    				}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					if(graph.equals("Line Chart"))
						data.add(Double.parseDouble(endTime),total);
					else
						throughput2.addValue(total,"Throughput of receiving packets",endTime);	
				}
			}
			if(graph.equals("Line Chart"))
				throughput1.addSeries(data);
		}
		/*
		 * calculate Throughput of forwarding packets
		 */
		else if(detail.equals("Throughput of forwarding packets at CN")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Throughput of forwarding packets");
			
			int total=0;
			int[] drop=null;
			if(DataRecognition.is_old_wireless){
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
			}
			if(DataRecognition.is_new_wireless){
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
			}
			for(int k=1;k<=dataNum;k++){
				
				startTime=String.valueOf(start+(k-1)*interval);
				endTime=String.valueOf(start+k*interval);
				
				if(DataRecognition.is_normal)
					getforwardPkt="select count(UPI) from "+tablePrefix+"normal_tr where Event='-' and SN!=SAN and SN!=DAN and SN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				else if(DataRecognition.is_old_wireless){
					if(DataRecognition.hasRTR)
						getforwardPkt="select EI from "+tablePrefix+"old_wireless_tr where Event='f' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					else if(DataRecognition.hasMAC)
						getforwardPkt="select EI from "+tablePrefix+"old_wireless_tr where Event='s' and NI!=IP_DIA and NI!=IP_SIA and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;				
				}
				else if(DataRecognition.is_new_wireless){
					if(DataRecognition.hasRTR)
						getforwardPkt="select IP_Ii from "+tablePrefix+"new_wireless_tr where Event='f' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
					else if(DataRecognition.hasMAC)
						getforwardPkt="select IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni!=IP_Idn and Ni!=IP_Isn and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				}
				if(DataRecognition.is_normal){
			      	query=new Query(con,getforwardPkt);
		        	dataset=query.doQuery();
		        	if(dataset!=null){
		        		try{
		        			while(dataset.next()){
		        				total+=Integer.parseInt(dataset.getString(1));
		        			}
		        		}catch(SQLException e1){
		        			e1.printStackTrace();
		        		}
		        	}
				}
				else if(DataRecognition.is_old_wireless){
					query=new Query(con,getforwardPkt);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							int id;
		    				while(dataset.next()){
		    					id=Integer.parseInt(dataset.getString(1));
		    					boolean flag=false;
		    					for(int j=0;j<drop.length;j++){
		    						if(id==drop[j])
		    							flag=true;
		    					}
		    					if(!flag)
		    						total++;	    					
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				}
				else if(DataRecognition.is_new_wireless){
					query=new Query(con,getforwardPkt);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							int id;
							while(dataset.next()){
		    					id=Integer.parseInt(dataset.getString(2));
		    					boolean flag=false;
		    					for(int i=0;i<drop.length;i++){
		    						if(id==drop[i])
		    							flag=true;
		    					}
		    					if(!flag)
		    						total++;		    					
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				}
				if(graph.equals("Line Chart"))
					data.add(Double.parseDouble(endTime),total);
				else
					throughput2.addValue(total,"Throughput of forwarding packets",endTime);				
			}
			if(graph.equals("Line Chart"))
				throughput1.addSeries(data);
		}
		/*
		 * calculate throughput of dropping packets
		 */
		else if(detail.equals("Throughput of dropping packets at CN")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Throughput of dropping packets");
			for(int k=1;k<=dataNum;k++){
				
				startTime=String.valueOf(start+(k-1)*interval);
				endTime=String.valueOf(start+k*interval);
				if(DataRecognition.is_normal)
					getdropPkt="select count(UPI) from "+tablePrefix+"normal_tr where Event='d' and SN="+nodeID+" and Time>="+startTime+" and Time<="+endTime;	
				if(DataRecognition.is_old_wireless)
					getdropPkt="select count(EI) from "+tablePrefix+"old_wireless_tr where Event='D' and NI="+nodeID+" and Time>="+startTime+" and Time<="+endTime;	
				if(DataRecognition.is_new_wireless)
					getdropPkt="select count(IP_Ii) from "+tablePrefix+"new_wireless_tr where Event='d' and Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
				query=new Query(con,getdropPkt);
				dataset=query.doQuery();
				if(dataset!=null){
					try{					
						while(dataset.next()){
							if(graph.equals("Line Chart"))
								data.add(Double.parseDouble(endTime),Integer.parseInt(dataset.getString(1)));
							else
								throughput2.addValue(Integer.parseInt(dataset.getString(1)),"Throughput of dropping packets",endTime);
					
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
			}
			if(graph.equals("Line Chart"))
				throughput1.addSeries(data);
		}
		if(graph.equals("Line Chart"))
			chartPanel=new ChartPanel(createLineChart(throughput1),true,true,true,true,true);
		else if(graph.equals("Bar Chart"))
			chartPanel=new ChartPanel(createBarChart(throughput2),true,true,true,true,true);
		else if(graph.equals("3D Bar Chart"))
			chartPanel=new ChartPanel(create3DBarChart(throughput2),true,true,true,true,true);
	}
	
	private JFreeChart createLineChart(XYDataset dataset){
//		 create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
		"Throughput", // chart title
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
		renderer.setPlotShapes(true);
		renderer.setShapesFilled(true);
		renderer.setItemLabelsVisible(true);
//		 change the auto tick unit selection to integer units only...
//		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//		 OPTIONAL CUSTOMISATION COMPLETED.
		return chart;
	}
	
	private JFreeChart createBarChart(CategoryDataset dataset){
//		 create the chart...
		JFreeChart chart = ChartFactory.createBarChart(
		"Throughput", // chart title
		"Category", // domain axis label
		"Value", // range axis label
		dataset, // data
		PlotOrientation.VERTICAL, // orientation
		true, // include legend
		true, // tooltips?
		false // URLs?
		);
//		 NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
//		 set the background color for the chart...
		chart.setBackgroundPaint(Color.white);
//		 get a reference to the plot for further customisation...
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);
//		 set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//		 disable bar outlines...
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);
//		 set up gradient paints for series...
		GradientPaint gp0 = new GradientPaint(
		0.0f, 0.0f, Color.blue,
		0.0f, 0.0f, new Color(0, 0, 64)
		);
		renderer.setSeriesPaint(0, gp0);
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(
		CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
		);
//		 OPTIONAL CUSTOMISATION COMPLETED.
		return chart;
	}
	
	private JFreeChart create3DBarChart(CategoryDataset dataset){
//		 create the chart...
		JFreeChart chart = ChartFactory.createBarChart3D(
		"Throughput", // chart title
		"Category", // domain axis label
		"Value", // range axis label
		dataset, // data
		PlotOrientation.VERTICAL, // orientation
		true, // include legend
		true, // tooltips?
		false // URLs?
		);
//		 NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
//		 set the background color for the chart...
		chart.setBackgroundPaint(Color.white);
//		 get a reference to the plot for further customisation...
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);
//		 set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//		 disable bar outlines...
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);
//		 set up gradient paints for series...
		GradientPaint gp0 = new GradientPaint(
		0.0f, 0.0f, Color.blue,
		0.0f, 0.0f, new Color(0, 0, 64)
		);
		renderer.setSeriesPaint(0, gp0);
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(
		CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
		);
//		 OPTIONAL CUSTOMISATION COMPLETED.
		return chart;
	}
	
	public ChartPanel getChartPanel(){
	    return chartPanel;
	}
	public String getDataType(){
		if(graph.equals("Line Chart"))
			return xySeriesCollection;
		else
			return categoryDataset;
    }
    public XYSeriesCollection getXYSeriesCollection(){
	    return throughput1;
    }

	public CategoryDataset getCategoryDataset(){
	    return throughput2;
	}
}
