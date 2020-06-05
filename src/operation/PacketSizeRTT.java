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

public class PacketSizeRTT extends DefaultDataAnalyse{
	public Connection con;
	public String tablePrefix;
	public String sTraceLevel;
	public String dTraceLevel;
	public String sentType;
	public String ackType;
	public String cn;
	public String on;
	public String detail;
	public String graph;
	public String xLabel;
	public String yLabel;
	
	public XYSeriesCollection rtt;
	public ChartPanel chartPanel;
	
	//this constructor is used for calculating end to end delay of normal format trace
	public PacketSizeRTT(Connection con,String tablePrefix,String sentType,String ackType,String detail,String graph,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.sentType=sentType;
		this.ackType=ackType;
		this.detail=detail;
		this.graph=graph;
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		
		createChartPanel();		
	}
	//this constructor is used for calculating current node to other node delay of normal format trace
	public PacketSizeRTT(Connection con,String tablePrefix,int cn,int on,String sentType,String ackType,String detail,String graph,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.cn=String.valueOf(cn);
		this.on=String.valueOf(on);
		this.sentType=sentType;
		this.ackType=ackType;
		this.detail=detail;
		this.graph=graph;
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		
		createChartPanel();		
	}
	//this constructor is used for calculating end to end delay of wireless format trace
	public PacketSizeRTT(Connection con,String tablePrefix,String sTraceLevel,String dTraceLevel,String sentType,String ackType,String detail,String graph,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.sTraceLevel=sTraceLevel;
		this.dTraceLevel=dTraceLevel;
		this.sentType=sentType;
		this.ackType=ackType;
		this.detail=detail;
		this.graph=graph;
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		
		createChartPanel();		
	}
	//this constructor is used for calculating current node  to other node delay of wireless format trace
	public PacketSizeRTT(Connection con,String tablePrefix,String cn,String on,String sTraceLevel,String dTraceLevel,String sentType,String ackType,String detail,String graph,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.cn=cn;
		this.on=on;
		this.sTraceLevel=sTraceLevel;
		this.dTraceLevel=dTraceLevel;
		this.sentType=sentType;
		this.ackType=ackType;
		this.detail=detail;
		this.graph=graph;
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		
		createChartPanel();		
	}
	private void createChartPanel(){
		XYSeries data = null;
		Query query;
		ResultSet dataset1;
		ResultSet dataset2;
		String getsentPkt = null;
		String getreceivePkt = null;

		
		int packetNum;
		double sum;
		int[] size = null;
		double[] minDelay = null;
		double[] maxDelay = null;
		double[] avgDelay = null;

		
		if(detail.equals("Packet size vs minimal RTT")||
			detail.equals("Packet size vs average RTT")||
			detail.equals("Packet size vs maximal RTT")){
			if(sentType.equals("tcp")&&ackType.equals("ack")){
				if(DataRecognition.is_normal){
					int num = 0;
					//get all the different packet size
					getsentPkt="select count(distinct PS) from "+tablePrefix+"normal_tr where SN=SAN and Event='-' and PN='tcp'";
	                query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							while(dataset1.next()){
								num=Integer.parseInt(dataset1.getString(1));
								size=new int[num];
								minDelay=new double[num];
								maxDelay=new double[num];
								avgDelay=new double[num];
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					for(int i=0;i<num;i++){
						minDelay[i]=Integer.MAX_VALUE;
						maxDelay[i]=0;
					}
					getsentPkt="select distinct PS from "+tablePrefix+"normal_tr where SN=SAN and Event='-' and PN='tcp'";
					query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							int i=0;
							while(dataset1.next()){
								size[i++]=Integer.parseInt(dataset1.getString(1));
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					//calculate minimal,maximal,avrage end to end delay
					for(int i=0;i<num;i++){
						sum=0;
						packetNum=0;
						getsentPkt="select Time,SN,SeqN from "+tablePrefix+"normal_tr where SN=SAN and Event='-' and PN='tcp' and DAN>=0 and PS="+size[i];
						query=new Query(con,getsentPkt);
						dataset1=query.doQuery();
						if(dataset1!=null){
							try{
								double time;
								int  sn;
								int seqn;
								while(dataset1.next()){
									time=Double.parseDouble(dataset1.getString(1));
									sn=Integer.parseInt(dataset1.getString(2));
									seqn=Integer.parseInt(dataset1.getString(3));
									getreceivePkt="select Time from "+tablePrefix+"normal_tr where Event='r' and DN=DAN and DN="+String.valueOf(sn)+" and PN='ack' and TCP_AN="+String.valueOf(seqn);
									query=new Query(con,getreceivePkt);
									dataset2=query.doQuery();
									if(dataset2!=null){
										try{
											while(dataset2.next()){
												double interval=Double.parseDouble(dataset2.getString(1))-time;
												sum+=interval;
												packetNum++;
												if(interval<minDelay[i])
													minDelay[i]=interval;
												if(interval>maxDelay[i])
													maxDelay[i]=interval;
											}
										}catch(SQLException e1){
											e1.printStackTrace();
										}
									}
								}
							}catch(SQLException e1){
								e1.printStackTrace();
							}
						}
						if(packetNum!=0)
							avgDelay[i]=sum/packetNum;
					}
				}
				else if(DataRecognition.is_old_wireless){
					int num=0;
					//get all the different packet size
					getsentPkt="select count(distinct PS) from "+tablePrefix+"old_wireless_tr where Event='s' and NI=IP_SIA  and PT='tcp' and TN='"+sTraceLevel+"'";
					query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							while(dataset1.next()){
								num=Integer.parseInt(dataset1.getString(1));
								size=new int[num];
								minDelay=new double[num];
								maxDelay=new double[num];
								avgDelay=new double[num];
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					for(int i=0;i<num;i++){
						minDelay[i]=Integer.MAX_VALUE;
						maxDelay[i]=0;
					}
					getsentPkt="select distinct PS from "+tablePrefix+"old_wireless_tr where Event='s' and NI=IP_SIA  and PT='tcp' and TN='"+sTraceLevel+"'";
					query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							int i=0;
							while(dataset1.next()){
								size[i++]=Integer.parseInt(dataset1.getString(1));
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				
					//calculate the delay
					for(int i=0;i<num;i++){
						sum=0;
						packetNum=0;
						getsentPkt="select Time,NI,TCP_SN from "+tablePrefix+"old_wireless_tr where Event='s' and NI=IP_SIA and IP_DIA>=0 and PT='tcp' and TN='"+sTraceLevel+"' and PS="+size[i];
		                query=new Query(con,getsentPkt);
						dataset1=query.doQuery();
						if(dataset1!=null){
							try{
								double time;
								int ni;
								int sn;
								while(dataset1.next()){
									time=Double.parseDouble(dataset1.getString(1));
									ni=Integer.parseInt(dataset1.getString(2));
									sn=Integer.parseInt(dataset1.getString(3));
									
									getreceivePkt="select Time from "+tablePrefix+"old_wireless_tr where Event='r' and NI=IP_DIA and PT='ack' and  NI="+String.valueOf(ni)+" and TCP_SN="+String.valueOf(sn)+" and TN='"+dTraceLevel+"'";
									query=new Query(con,getreceivePkt);
									dataset2=query.doQuery();
									if(dataset2!=null){
										try{
											while(dataset2.next()){
												double interval=Double.parseDouble(dataset2.getString(1))-time;
												sum+=interval;
												packetNum++;
												if(interval<minDelay[i])
													minDelay[i]=interval;
												if(interval>maxDelay[i])
													maxDelay[i]=interval;
											}
										}catch(SQLException e1){
											e1.printStackTrace();
										}
									}
									
								}
							}catch(SQLException e1){
								e1.printStackTrace();
							}
						}
						if(packetNum!=0)
							avgDelay[i]=sum/packetNum;
					}
				}
				else if(DataRecognition.is_new_wireless){
					int num=0;
					//get all the different packet size
					getsentPkt="select count(distinct IP_Il) from "+tablePrefix+"new_wireless_tr where Event='s' and IP_It='tcp' and Ni=IP_Isn and Nl='"+sTraceLevel+"'";
					query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							while(dataset1.next()){
								num=Integer.parseInt(dataset1.getString(1));
								size=new int[num];
								minDelay=new double[num];
								maxDelay=new double[num];
								avgDelay=new double[num];
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					for(int i=0;i<num;i++){
						minDelay[i]=Integer.MAX_VALUE;
						maxDelay[i]=0;
					}
					getsentPkt="select distinct IP_Il from "+tablePrefix+"new_wireless_tr where Event='s' and IP_It='tcp' and Ni=IP_Isn and Nl='"+sTraceLevel+"'";
					query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							int i=0;
							while(dataset1.next()){
								size[i++]=Integer.parseInt(dataset1.getString(1));
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
			
					//calculate the delay
					for(int i=0;i<num;i++){
						sum=0;
						packetNum=0;
						getsentPkt="select Time,Ni,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn and IP_Idn>=0 and IP_It='tcp' and Nl='"+sTraceLevel+"' and IP_Il="+size[i];
		                query=new Query(con,getsentPkt);
						dataset1=query.doQuery();
						if(dataset1!=null){
							try{
								double time;
								int ni;
								int seqn;
								while(dataset1.next()){
									time=Double.parseDouble(dataset1.getString(1));
									ni=Integer.parseInt(dataset1.getString(2));
									seqn=Integer.parseInt(dataset1.getString(3));
									
									getreceivePkt="select Time from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and Ni="+String.valueOf(ni)+" and IP_It='ack' and TCP_Ps="+String.valueOf(seqn)+" and Nl='"+dTraceLevel+"'";
									query=new Query(con,getreceivePkt);
									dataset2=query.doQuery();
									if(dataset2!=null){
										try{
											while(dataset2.next()){
												double interval=Double.parseDouble(dataset2.getString(1))-time;
												sum+=interval;
												packetNum++;
												if(interval<minDelay[i])
													minDelay[i]=interval;
												if(interval>maxDelay[i])
													maxDelay[i]=interval;
											}
										}catch(SQLException e1){
											e1.printStackTrace();
										}
									}
									
								}
							}catch(SQLException e1){
								e1.printStackTrace();
							}
						}
						if(packetNum!=0)
							avgDelay[i]=sum/packetNum;
					}
				}
			}
		
		}
		else if(detail.equals("Packet size vs minimal RTT between CN and ON")||
				 detail.equals("Packet size vs maximal RTT between CN and ON")||
				 detail.equals("Packet size vs average RTT between CN and ON")){
			if(sentType.equals("tcp")&&ackType.equals("ack")){
				if(DataRecognition.is_normal){
					int num = 0;
					//get all the different packet size
					getsentPkt="select count(distinct PS) from "+tablePrefix+"normal_tr where SN=SAN and PN='tcp' and Event='-' and SN="+cn+" and DAN="+on;
	                query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							while(dataset1.next()){
								num=Integer.parseInt(dataset1.getString(1));
								size=new int[num];
								minDelay=new double[num];
								maxDelay=new double[num];
								avgDelay=new double[num];
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					for(int i=0;i<num;i++){
						minDelay[i]=Integer.MAX_VALUE;
						maxDelay[i]=0;
					}
					getsentPkt="select distinct PS from "+tablePrefix+"normal_tr where SN=SAN and Event='-' and PN='tcp' and SN="+cn+" and DAN="+on;
					query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							int i=0;
							while(dataset1.next()){
								size[i++]=Integer.parseInt(dataset1.getString(1));
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					//calculate minimal,maximal,avrage end to end delay
					for(int i=0;i<num;i++){
						sum=0;
						packetNum=0;
						getsentPkt="select Time,SN,SeqN from "+tablePrefix+"normal_tr where SN=SAN and Event='-' and PN='tcp'  and PS="+size[i]+" and SN="+cn+" and DAN="+on;
						query=new Query(con,getsentPkt);
						dataset1=query.doQuery();
						if(dataset1!=null){
							try{
								double time;
								int  sn;
								int seqn;
								while(dataset1.next()){
									time=Double.parseDouble(dataset1.getString(1));
									sn=Integer.parseInt(dataset1.getString(2));
									seqn=Integer.parseInt(dataset1.getString(3));
									getreceivePkt="select Time from "+tablePrefix+"normal_tr where Event='r' and DN=DAN and DN="+String.valueOf(sn)+" and PN='ack' and TCP_AN="+String.valueOf(seqn);
									query=new Query(con,getreceivePkt);
									dataset2=query.doQuery();
									if(dataset2!=null){
										try{
											while(dataset2.next()){
												double interval=Double.parseDouble(dataset2.getString(1))-time;
												sum+=interval;
												packetNum++;
												if(interval<minDelay[i])
													minDelay[i]=interval;
												if(interval>maxDelay[i])
													maxDelay[i]=interval;
											}
										}catch(SQLException e1){
											e1.printStackTrace();
										}
									}
								}
							}catch(SQLException e1){
								e1.printStackTrace();
							}
						}
						if(packetNum!=0)
							avgDelay[i]=sum/packetNum;
					}
				}
				else if(DataRecognition.is_old_wireless){
					int num=0;
					//get all the different packet size
					getsentPkt="select count(distinct PS) from "+tablePrefix+"old_wireless_tr where Event='s' and PT='tcp' and NI=IP_SIA  and TN='"+sTraceLevel+"' and NI="+cn+" and IP_DIA="+on;
					query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							while(dataset1.next()){
								num=Integer.parseInt(dataset1.getString(1));
								size=new int[num];
								minDelay=new double[num];
								maxDelay=new double[num];
								avgDelay=new double[num];
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					for(int i=0;i<num;i++){
						minDelay[i]=Integer.MAX_VALUE;
						maxDelay[i]=0;
					}
					getsentPkt="select distinct PS from "+tablePrefix+"old_wireless_tr where Event='s' and PT='tcp' and NI=IP_SIA  and TN='"+sTraceLevel+"' and NI="+cn+" and IP_DIA="+on;
					query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							int i=0;
							while(dataset1.next()){
								size[i++]=Integer.parseInt(dataset1.getString(1));
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				
					//calculate the delay
					for(int i=0;i<num;i++){
						sum=0;
						packetNum=0;
						getsentPkt="select Time,NI,TCP_SN from "+tablePrefix+"old_wireless_tr where Event='s' and NI=IP_SIA  and PT='tcp' and TN='"+sTraceLevel+"' and PS="+size[i]+" and NI="+cn+" and IP_DIA="+on;
		                query=new Query(con,getsentPkt);
						dataset1=query.doQuery();
						if(dataset1!=null){
							try{
								double time;
								int ni;
								int sn;
								while(dataset1.next()){
									time=Double.parseDouble(dataset1.getString(1));
									ni=Integer.parseInt(dataset1.getString(2));
									sn=Integer.parseInt(dataset1.getString(3));
									
									getreceivePkt="select Time from "+tablePrefix+"old_wireless_tr where Event='r' and NI=IP_DIA and PT='ack' and  NI="+String.valueOf(ni)+" and TCP_SN="+String.valueOf(sn)+" and TN='"+dTraceLevel+"'";
									query=new Query(con,getreceivePkt);
									dataset2=query.doQuery();
									if(dataset2!=null){
										try{
											while(dataset2.next()){
												double interval=Double.parseDouble(dataset2.getString(1))-time;
												sum+=interval;
												packetNum++;
												if(interval<minDelay[i])
													minDelay[i]=interval;
												if(interval>maxDelay[i])
													maxDelay[i]=interval;
											}
										}catch(SQLException e1){
											e1.printStackTrace();
										}
									}
									
								}
							}catch(SQLException e1){
								e1.printStackTrace();
							}
						}
						if(packetNum!=0)
							avgDelay[i]=sum/packetNum;
					}
				}
				else if(DataRecognition.is_new_wireless){
					int num=0;
					//get all the different packet size
					getsentPkt="select count(distinct IP_Il) from "+tablePrefix+"new_wireless_tr where Event='s'and IP_It='tcp'  and Ni=IP_Isn and Nl='"+sTraceLevel+"' and Ni="+cn+" and IP_Idn="+on;
					query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							while(dataset1.next()){
								num=Integer.parseInt(dataset1.getString(1));
								size=new int[num];
								minDelay=new double[num];
								maxDelay=new double[num];
								avgDelay=new double[num];
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					for(int i=0;i<num;i++){
						minDelay[i]=Integer.MAX_VALUE;
						maxDelay[i]=0;
					}
					getsentPkt="select distinct IP_Il from "+tablePrefix+"new_wireless_tr where Event='s' and IP_It='tcp' and Ni=IP_Isn and Nl='"+sTraceLevel+"' and Ni="+cn+" and IP_Idn="+on;
					query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							int i=0;
							while(dataset1.next()){
								size[i++]=Integer.parseInt(dataset1.getString(1));
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
			
					//calculate the delay
					for(int i=0;i<num;i++){
						sum=0;
						packetNum=0;
						getsentPkt="select Time,Ni,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn  and IP_It='tcp' and Nl='"+sTraceLevel+"' and IP_Il="+size[i]+" and Ni="+cn+" and IP_Idn="+on;
		                query=new Query(con,getsentPkt);
						dataset1=query.doQuery();
						if(dataset1!=null){
							try{
								double time;
								int ni;
								int seqn;
								while(dataset1.next()){
									time=Double.parseDouble(dataset1.getString(1));
									ni=Integer.parseInt(dataset1.getString(2));
									seqn=Integer.parseInt(dataset1.getString(3));
									
									getreceivePkt="select Time from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and Ni="+String.valueOf(ni)+" and IP_It='ack' and TCP_Ps="+String.valueOf(seqn)+" and Nl='"+dTraceLevel+"'";
									query=new Query(con,getreceivePkt);
									dataset2=query.doQuery();
									if(dataset2!=null){
										try{
											while(dataset2.next()){
												double interval=Double.parseDouble(dataset2.getString(1))-time;
												sum+=interval;
												packetNum++;
												if(interval<minDelay[i])
													minDelay[i]=interval;
												if(interval>maxDelay[i])
													maxDelay[i]=interval;
											}
										}catch(SQLException e1){
											e1.printStackTrace();
										}
									}
									
								}
							}catch(SQLException e1){
								e1.printStackTrace();
							}
						}
						if(packetNum!=0)
							avgDelay[i]=sum/packetNum;
					}
				}
			}
		}
        if(sentType.equals("tcp")&&ackType.equals("ack")){
    		if(detail.equals("Packet size vs minimal RTT between CN and ON")||
    			    detail.equals("Packet size vs minimal RTT")){
    				if(graph.equals("Line Chart")){
    					rtt=new XYSeriesCollection();
    					if(detail.equals("Packet size vs minimal RTT"))
    						data=new XYSeries("Packet size vs minimal RTT");
    					else if(detail.equals("Packet size vs minimal RTT between CN and ON"))
    						data=new XYSeries("Packet size vs minimal RTT between "+cn+" and "+on);
    					for(int i=0;i<size.length;i++)
    						data.add(size[i],minDelay[i]);
    					rtt.addSeries(data);
    				}
    			}
    			if(detail.equals("Packet size vs maximal RTT between CN and ON")||
    					detail.equals("Packet size vs maximal RTT")){
    				if(graph.equals("Line Chart")){
    					rtt=new XYSeriesCollection();
    					if(detail.equals("Packet size vs maximal RTT"))
    						data=new XYSeries("Packet size vs maximal RTT");
    					else if(detail.equals("Packet size vs maximal RTT between CN and ON"))
    						data=new XYSeries("Packet size vs maximal RTT between "+cn+" and "+on);
    					for(int i=0;i<size.length;i++)
    						data.add(size[i],maxDelay[i]);
    					rtt.addSeries(data);
    				}
    			}
    			if(detail.equals("Packet size vs average RTT between CN and ON")||
    					detail.equals("Packet size vs average RTT")){
    				if(graph.equals("Line Chart")){
    					rtt=new XYSeriesCollection();
    					if(detail.equals("Packet size vs average RTT"))
    						data=new XYSeries("Packet size vs average RTT");
    					else if(detail.equals("Packet size vs average RTT between CN and ON"))
    						data=new XYSeries("Packet size vs average RTT between "+cn+" and "+on);
    					for(int i=0;i<size.length;i++)
    						data.add(size[i],avgDelay[i]);
    					rtt.addSeries(data);
    				}
    			}	
    			if(graph.equals("Line Chart"))
    				chartPanel=new ChartPanel(createLineChart(rtt),true,true,true,true,true);
        }		
	}
	
	private JFreeChart createLineChart(XYDataset dataset){
//		 create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
		"Packet Size vs Delay", // chart title
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
	public String getDataType(){
		if(graph.equals("Line Chart"))
		    return xySeriesCollection;
		return null;
	}
	
	public XYSeriesCollection getXYSeriesCollection(){
	    return rtt;
	}
	public ChartPanel getChartPanel() {
		return chartPanel;
	}

}
