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

public class PacketIDRTT extends DefaultDataAnalyse{
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
	
	//this constructor is used for calculating RTT of normal format trace
	public PacketIDRTT(Connection con,String tablePrefix,String sentType,String ackType,String detail,String graph,String xLabel,String yLabel){
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
	//this constructor is used for calculating current node to other node RTT of normal format trace
	public PacketIDRTT(Connection con,String tablePrefix,int cn,int on,String sentType,String ackType,String detail,String graph,String xLabel,String yLabel){
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
	//this constructor is used for calculating RTT of wireless format trace
	public PacketIDRTT(Connection con,String tablePrefix,String sTraceLevel,String dTraceLevel,String sentType,String ackType,String detail,String graph,String xLabel,String yLabel){
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
	//this constructor is used for calculating current node  to other node RTT of wireless format trace
	public PacketIDRTT(Connection con,String tablePrefix,String cn,String on,String sTraceLevel,String dTraceLevel,String sentType,String ackType,String detail,String graph,String xLabel,String yLabel){
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

		
		if(detail.equals("Packet ID vs simulation RTT")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Packet ID vs RTT");
			if(sentType.equals("tcp")&&ackType.equals("ack")){
				if(DataRecognition.is_normal){
					getsentPkt="select Time,SN,SeqN,upi from "+tablePrefix+"normal_tr where SN=SAN and Event='-' and DAN>=0 and PN='tcp'";
					query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							double time;
							int  sn;
							int  seqn;
							int upi;
							while(dataset1.next()){
								time=Double.parseDouble(dataset1.getString(1));
								sn=Integer.parseInt(dataset1.getString(2));
								seqn=Integer.parseInt(dataset1.getString(3));
								upi=Integer.parseInt(dataset1.getString(4));
								getreceivePkt="select Time from "+tablePrefix+"normal_tr where Event='r' and and DN=DAN and PN='ack' and DN="+String.valueOf(sn)+" and Tcp_AN="+String.valueOf(seqn);
								query=new Query(con,getreceivePkt);
								dataset2=query.doQuery();
								if(dataset2!=null){
									try{
										while(dataset2.next()){
											if(graph.equals("Line Chart"))
												data.add(upi,Double.parseDouble(dataset2.getString(1))-time);
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
				}
				else if(DataRecognition.is_old_wireless){
					getsentPkt="select Time,EI,NI,TCP_SN from "+tablePrefix+"old_wireless_tr where Event='s' and NI=IP_SIA and IP_DIA>=0 and TN='"+sTraceLevel+"' and PT='tcp'";
	                query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							double time;
							int ei;
							int ni;
							int sn;
							while(dataset1.next()){
								time=Double.parseDouble(dataset1.getString(1));
								ei=Integer.parseInt(dataset1.getString(2));
								ni=Integer.parseInt(dataset1.getString(3));
								sn=Integer.parseInt(dataset1.getString(4));
							
								getreceivePkt="select Time from "+tablePrefix+"old_wireless_tr where Event='r' and NI=IP_DIA and NI="+String.valueOf(ni)+" and TCP_SN="+String.valueOf(sn)+" and TN='"+dTraceLevel+"' and PT='ack'";
								query=new Query(con,getreceivePkt);
								dataset2=query.doQuery();
								if(dataset2!=null){
									try{
										while(dataset2.next()){
											if(graph.equals("Line Chart"))
												data.add(ei,Double.parseDouble(dataset2.getString(1))-time);
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
				}
				else if(DataRecognition.is_new_wireless){
					getsentPkt="select Time,Ni,IP_Ii,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn and IP_Idn>=0 and Nl='"+sTraceLevel+"' and IP_It='tcp'";
	                query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							double time;
							int ni;
							int upi;
							int sn;
							while(dataset1.next()){
								time=Double.parseDouble(dataset1.getString(1));
								ni=Integer.parseInt(dataset1.getString(2));
								upi=Integer.parseInt(dataset1.getString(3));
								sn=Integer.parseInt(dataset1.getString(4));
								
								getreceivePkt="select Time from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and Ni="+String.valueOf(ni)+" and TCP_Ps="+String.valueOf(sn)+" and Nl='"+dTraceLevel+"' and IP_It='ack'";
								query=new Query(con,getreceivePkt);
								dataset2=query.doQuery();
								if(dataset2!=null){
									try{
										while(dataset2.next()){
											if(graph.equals("Line Chart"))
												data.add(upi,Double.parseDouble(dataset2.getString(1))-time);
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
				}
				if(graph.equals("Line Chart")){
					rtt=new XYSeriesCollection();
					rtt.addSeries(data);
				}	
			}
		}
		else if(detail.equals("Packet ID vs RTT between CN and ON")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Packet ID vs RTT");
			if(sentType.equals("tcp")&&ackType.equals("ack")){
				if(DataRecognition.is_normal){
					getsentPkt="select Time,SN,SeqN,upi from "+tablePrefix+"normal_tr where SN=SAN and Event='-'  and PN='tcp' and SN="+cn+" and DAN="+on;
					query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							double time;
							int  sn;
							int  seqn;
							int upi;
							while(dataset1.next()){
								time=Double.parseDouble(dataset1.getString(1));
								sn=Integer.parseInt(dataset1.getString(2));
								seqn=Integer.parseInt(dataset1.getString(3));
								upi=Integer.parseInt(dataset1.getString(4));
								getreceivePkt="select Time from "+tablePrefix+"normal_tr where Event='r' and and DN=DAN and DN="+String.valueOf(sn)+" and Tcp_AN="+String.valueOf(seqn);
								query=new Query(con,getreceivePkt);
								dataset2=query.doQuery();
								if(dataset2!=null){
									try{
										while(dataset2.next()){
											if(graph.equals("Line Chart"))
												data.add(upi,Double.parseDouble(dataset2.getString(1))-time);
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
				}
				else if(DataRecognition.is_old_wireless){
					getsentPkt="select Time,EI,NI,TCP_SN from "+tablePrefix+"old_wireless_tr where Event='s' and NI=IP_SIA  and TN='"+sTraceLevel+"' and PT='tcp' and NI="+cn+" and IP_DIA="+on;
	                query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							double time;
							int ei;
							int ni;
							int sn;
							while(dataset1.next()){
								time=Double.parseDouble(dataset1.getString(1));
								ei=Integer.parseInt(dataset1.getString(2));
								ni=Integer.parseInt(dataset1.getString(3));
								sn=Integer.parseInt(dataset1.getString(4));
								
								getreceivePkt="select Time from "+tablePrefix+"old_wireless_tr where Event='r' and NI=IP_DIA and NI="+String.valueOf(ni)+" and TCP_SN="+String.valueOf(sn)+" and TN='"+dTraceLevel+"' and PT='ack'";
								query=new Query(con,getreceivePkt);
								dataset2=query.doQuery();
								if(dataset2!=null){
									try{
										while(dataset2.next()){
											if(graph.equals("Line Chart"))
												data.add(ei,Double.parseDouble(dataset2.getString(1))-time);
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
				}
				else if(DataRecognition.is_new_wireless){
					getsentPkt="select Time,Ni,IP_Ii,TCP_Ps from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn and Nl='"+sTraceLevel+"' and IP_It='tcp' and Ni="+cn+" and IP_Idn="+on;
	                query=new Query(con,getsentPkt);
					dataset1=query.doQuery();
					if(dataset1!=null){
						try{
							double time;
							int ni;
							int upi;
							int sn;
							while(dataset1.next()){
								time=Double.parseDouble(dataset1.getString(1));
								ni=Integer.parseInt(dataset1.getString(2));
								upi=Integer.parseInt(dataset1.getString(3));
								sn=Integer.parseInt(dataset1.getString(4));
								
								getreceivePkt="select Time from "+tablePrefix+"new_wireless_tr where Event='r' and Ni=IP_Idn and Ni="+String.valueOf(ni)+" and TCP_Ps="+String.valueOf(sn)+" and Nl='"+dTraceLevel+"' and IP_It='ack'";
								query=new Query(con,getreceivePkt);
								dataset2=query.doQuery();
								if(dataset2!=null){
									try{
										while(dataset2.next()){
											if(graph.equals("Line Chart"))
												data.add(upi,Double.parseDouble(dataset2.getString(1))-time);
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
				}
				if(graph.equals("Line Chart")){
					rtt=new XYSeriesCollection();
					rtt.addSeries(data);
				}	
			}
		}
		if(graph.equals("Line Chart"))
			chartPanel=new ChartPanel(createLineChart(rtt),true,true,true,true,true);
	}
	private JFreeChart createLineChart(XYDataset dataset){
//		 create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
		"Packet ID vs RTT", // chart title
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
