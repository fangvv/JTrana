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
public class PacketIDDelay extends DefaultDataAnalyse{
	public Connection con;
	public String tablePrefix;
	public String sTraceLevel;
	public String dTraceLevel;
	public String cn;
	public String on;
	public String detail;
	public String graph;
	public String xLabel;
	public String yLabel;
	
	public XYSeriesCollection delay;
	public ChartPanel chartPanel;
	//this constructor is used for calculating end to end delay of normal format trace
	public PacketIDDelay(Connection con,String tablePrefix,String detail,String graph,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.detail=detail;
		this.graph=graph;
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		
		createChartPanel();		
	}
	//this constructor is used for calculating current node to other node delay of normal format trace
	public PacketIDDelay(Connection con,String tablePrefix,int cn,int on,String detail,String graph,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.cn=String.valueOf(cn);
		this.on=String.valueOf(on);
		this.detail=detail;
		this.graph=graph;
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		
		createChartPanel();		
	}
	//this constructor is used for calculating end to end delay of wireless format trace
	public PacketIDDelay(Connection con,String tablePrefix,String sTraceLevel,String dTraceLevel,String detail,String graph,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.sTraceLevel=sTraceLevel;
		this.dTraceLevel=dTraceLevel;
		this.detail=detail;
		this.graph=graph;
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		
		createChartPanel();		
	}
	//this constructor is used for calculating current node  to other node delay of wireless format trace
	public PacketIDDelay(Connection con,String tablePrefix,String cn,String on,String sTraceLevel,String dTraceLevel,String detail,String graph,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.cn=cn;
		this.on=on;
		this.sTraceLevel=sTraceLevel;
		this.dTraceLevel=dTraceLevel;
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

		
		/*
		 * packet Id vs simulation end to end delay
		 */
		if(detail.equals("Packet ID vs END TO END Delay")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Packet ID vs END TO END Delay");
			if(DataRecognition.is_normal){
				/*
				 * calculate the delay of normal packets
				 */
				getsentPkt="select Time,DAN,UPI from "+tablePrefix+"normal_tr where SN=SAN and Event='-' and DAN>=0";
				query=new Query(con,getsentPkt);
				dataset1=query.doQuery();
				if(dataset1!=null){
					try{
						double time;
						int  dest;
						int  upi;
						while(dataset1.next()){
							time=Double.parseDouble(dataset1.getString(1));
							dest=Integer.parseInt(dataset1.getString(2));
							upi=Integer.parseInt(dataset1.getString(3));
							getreceivePkt="select Time from "+tablePrefix+"normal_tr where Event='r' and DN="+String.valueOf(dest)+" and UPI="+String.valueOf(upi);
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
				/*
				 * calculate the delay of broadcasting packtes
				 */
				getsentPkt="select Time,SN,DN,UPI from "+tablePrefix+"normal_tr where SN=SAN and Event='-' and DAN<0";
				query=new Query(con,getsentPkt);
				dataset1=query.doQuery();
				if(dataset1!=null){
					try{
						double time;
						int  sn;
						int dn;
						int  upi;
						while(dataset1.next()){
							time=Double.parseDouble(dataset1.getString(1));
							sn=Integer.parseInt(dataset1.getString(2));
							dn=Integer.parseInt(dataset1.getString(3));
							upi=Integer.parseInt(dataset1.getString(4));
							getreceivePkt="select Time from "+tablePrefix+"normal_tr where Event='r' and SN="+String.valueOf(sn)+" and DN="+String.valueOf(dn)+" and UPI="+String.valueOf(upi);
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
			//
			/*
			 * we only care about the packet that is based on IP
			 */
			else if(DataRecognition.is_old_wireless){
				getsentPkt="select Time,EI,IP_DIA from "+tablePrefix+"old_wireless_tr where Event='s' and NI=IP_SIA and IP_DIA>=0 and TN='"+sTraceLevel+"'";
                query=new Query(con,getsentPkt);
				dataset1=query.doQuery();
				if(dataset1!=null){
					try{
						double time;
						int ei;
						int dest;
						while(dataset1.next()){
							time=Double.parseDouble(dataset1.getString(1));
							ei=Integer.parseInt(dataset1.getString(2));
							dest=Integer.parseInt(dataset1.getString(3));		
							getreceivePkt="select Time from "+tablePrefix+"old_wireless_tr where Event='r' and NI=IP_DIA and NI="+String.valueOf(dest)+" and EI="+String.valueOf(ei)+" and TN='"+dTraceLevel+"'";
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
				getsentPkt="select Time,NI,EI from "+tablePrefix+"old_wireless_tr where Event='s' and NI=IP_SIA and IP_DIA<0 and TN='"+sTraceLevel+"'";
				query=new Query(con,getsentPkt);
				dataset1=query.doQuery();
				if(dataset1!=null){
					try{
						double time;
						int ni;
						int ei;
						while(dataset1.next()){
							time=Double.parseDouble(dataset1.getString(1));
							ni=Integer.parseInt(dataset1.getString(2));
							ei=Integer.parseInt(dataset1.getString(3));
						
							getreceivePkt="select Time from "+tablePrefix+"old_wireless_tr where Event='r' and IP_SIA="+String.valueOf(ni)+" and EI="+String.valueOf(ei)+" and TN='"+dTraceLevel+"'";
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
			/*
			 * we only care about the packet that is based on IP
			 */
			else if(DataRecognition.is_new_wireless){
				getsentPkt="select Time,IP_Idn,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn and IP_Idn>=0 and Nl='"+sTraceLevel+"'";
                query=new Query(con,getsentPkt);
				dataset1=query.doQuery();
				if(dataset1!=null){
					try{
						double time;
						int dest;
						int upi;
						while(dataset1.next()){
							time=Double.parseDouble(dataset1.getString(1));
							dest=Integer.parseInt(dataset1.getString(2));
							upi=Integer.parseInt(dataset1.getString(3));
							
							getreceivePkt="select Time from "+tablePrefix+"new_wireless_tr where Event='r' and Ni="+String.valueOf(dest)+" and IP_Ii="+String.valueOf(upi)+" and Nl='"+dTraceLevel+"'";
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
				getsentPkt="select Time,Ni,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn and IP_Idn<0 and Nl='"+sTraceLevel+"'";
                query=new Query(con,getsentPkt);
				dataset1=query.doQuery();
				if(dataset1!=null){
					try{
						double time;
						int ni;
						int upi;
						while(dataset1.next()){
							time=Double.parseDouble(dataset1.getString(1));
							ni=Integer.parseInt(dataset1.getString(2));
							upi=Integer.parseInt(dataset1.getString(3));
						
							getreceivePkt="select Time from "+tablePrefix+"new_wireless_tr where Event='r' and IP_Isn="+String.valueOf(ni)+" and IP_Ii="+String.valueOf(upi)+" and Nl='"+dTraceLevel+"'";
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
				delay=new XYSeriesCollection();
				delay.addSeries(data);
			}				
		}
		/*
		 * calculate the current node to other node delay
		 */
		else if(detail.equals("Packets ID vs CN TO ON Delay")){
			if(graph.equals("Line Chart"))
				data=new XYSeries("Packet ID vs CN TO ON Delay");
			if(DataRecognition.is_normal){
				getsentPkt="select Time,UPI from "+tablePrefix+"normal_tr where SN=SAN and Event='-' and SN="+cn+" and DAN="+on;
				query=new Query(con,getsentPkt);
				dataset1=query.doQuery();
				if(dataset1!=null){
					try{
						double time;
						int  upi;
						while(dataset1.next()){
							time=Double.parseDouble(dataset1.getString(1));
							upi=Integer.parseInt(dataset1.getString(2));
							getreceivePkt="select Time from "+tablePrefix+"normal_tr where Event='r' and DN="+on+" and UPI="+String.valueOf(upi);
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
				getsentPkt="select Time,EI from "+tablePrefix+"old_wireless_tr where Event='s' and NI=IP_SIA and NI="+cn+" and IP_DIA="+on+" and TN='"+sTraceLevel+"'";
                query=new Query(con,getsentPkt);
				dataset1=query.doQuery();
				if(dataset1!=null){
					try{
						double time;
						int ei;
						while(dataset1.next()){
							time=Double.parseDouble(dataset1.getString(1));
							ei=Integer.parseInt(dataset1.getString(2));							
						
							getreceivePkt="select Time from "+tablePrefix+"old_wireless_tr where Event='r' and NI="+on+" and EI="+String.valueOf(ei)+" and TN='"+dTraceLevel+"'";
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
				getsentPkt="select Time,IP_Ii from "+tablePrefix+"new_wireless_tr where Event='s' and Ni=IP_Isn and Ni="+cn+" and IP_Idn="+on+" and Nl='"+sTraceLevel+"'";
                query=new Query(con,getsentPkt);
				dataset1=query.doQuery();
				if(dataset1!=null){
					try{
						double time;
						int upi;
						while(dataset1.next()){
							time=Double.parseDouble(dataset1.getString(1));						
							upi=Integer.parseInt(dataset1.getString(2));
						
							getreceivePkt="select Time from "+tablePrefix+"new_wireless_tr where Event='r' and Ni="+on+" and IP_Ii="+String.valueOf(upi)+" and Nl='"+dTraceLevel+"'";
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
				delay=new XYSeriesCollection();
				delay.addSeries(data);
			}	
		}
		
		if(graph.equals("Line Chart"))
			chartPanel=new ChartPanel(createLineChart(delay),true,true,true,true,true);
	}
	
	private JFreeChart createLineChart(XYDataset dataset){
//		 create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
		"Packet ID vs End To End Delay", // chart title
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
	    return delay;
	}
	public ChartPanel getChartPanel() {
		return chartPanel;
	}
}
