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
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Spacer;

public class TrafficAnalyse extends DefaultDataAnalyse{
	public Connection con;
	public String tablePrefix;
	public String startTime;
	public String endTime;
	public String detail;
	public String graph;
	public String xLabel;
	public String yLabel;
	
	public XYSeriesCollection trafficData;
	public DefaultPieDataset data1;
	public DefaultCategoryDataset data2;
	public ChartPanel traffic;
	
	public TrafficAnalyse(Connection con,String tablePrefix,String startTime,String endTime,String detail,String graph,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.startTime=startTime;
		this.endTime=endTime;
		this.detail=detail;
		this.graph=graph;
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		
		createChartPanel();
		
	}
	
	/**
	 * 
	 */
	private void createChartPanel(){		
		int num=0;
		Query query;
		ResultSet dataset;
		if(detail.equals("Packet size at diffrent time")){
			if(graph.equals("Line Chart")){	
				String getPktType=null;
				String getTraffic=null;
				String[] pktType;
				XYSeries[] allData;
				/*
				 * get the number of the package type the trace has
				 */
				if(DataRecognition.is_normal)
					getPktType="select count(distinct PN) from "+tablePrefix+"normal_tr";
				else if(DataRecognition.is_old_wireless)
					getPktType="select count(distinct PT) from "+tablePrefix+"old_wireless_tr";
				else if(DataRecognition.is_new_wireless)
					getPktType="select count(distinct IP_It) from "+tablePrefix+"new_wireless_tr where IP_It is not null";
				else
					return;
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
				pktType=new String[num];
				allData=new XYSeries[num];
				/*
				 * get the package type the trace has
				 */
				if(DataRecognition.is_normal)
					getPktType="select distinct PN from "+tablePrefix+"normal_tr";
				else if(DataRecognition.is_old_wireless)
					getPktType="select distinct PT from "+tablePrefix+"old_wireless_tr";
				else if(DataRecognition.is_new_wireless)
					getPktType="select distinct IP_It from "+tablePrefix+"new_wireless_tr where IP_It is not null";
				
				query=new Query(con,getPktType);
				dataset=query.doQuery();
				if(dataset!=null){
					try{
						int i=0;
						int j=0;
						String type;
						while(dataset.next()){
							type=dataset.getString(1);
							pktType[i++]=type;
							allData[j++]=new XYSeries(type);
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				/*
				 * get the traffic for every kind of package
				 */
				if(DataRecognition.is_normal)
					getTraffic="select distinct Time,PN,PS from "+tablePrefix+"normal_tr where Time>="+startTime+" and Time<="+endTime;
				else if(DataRecognition.is_old_wireless)
					getTraffic="select distinct Time,PT,PS from "+tablePrefix+"old_wireless_tr where Time>="+startTime+" and Time<="+endTime;
				else if(DataRecognition.is_new_wireless)
					getTraffic="select distinct Time,IP_It,IP_Il from "+tablePrefix+"new_wireless_tr where IP_It is not null and Time>="+startTime+" and Time<="+endTime;
				
				query=new Query(con,getTraffic);
				dataset=query.doQuery();
				if(dataset!=null){
					try{
						double time;
						String type;
						int size;
						while(dataset.next()){
							time=Double.parseDouble(dataset.getString(1));
							type=dataset.getString(2);
							size=Integer.parseInt(dataset.getString(3));
							for(int i=0;i<num;i++){
								if(pktType[i].equals(type)){
									allData[i].add(time,size);
									break;
								}
							}
						}
					}catch(SQLException e1){
						e1.printStackTrace();
					}
				}
				trafficData=new XYSeriesCollection();
				for(int i=0;i<num;i++)
					trafficData.addSeries(allData[i]);
				traffic=new ChartPanel(createLineChart(trafficData),true,true,true,true, true);
				traffic.setPreferredSize(new java.awt.Dimension(775, 400));
			}
		}
		else if(detail.equals("Packet num of every type")){
			String getPktType=null;
			String getPktNum=null;
			int[] pktNum;
			String[][] pktType;
			/*
			 * get the number of the package type the trace has
			 */
			if(DataRecognition.is_normal)
				getPktType="select count(distinct PN,Name) from "+tablePrefix+"normal_tr";
			else if(DataRecognition.is_old_wireless)
				getPktType="select count(distinct PT,Name) from "+tablePrefix+"old_wireless_tr";
			else if(DataRecognition.is_new_wireless)
				getPktType="select count(distinct IP_It) from "+tablePrefix+"new_wireless_tr where IP_It is not null";
			else
				return;
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
			pktNum=new int[num];
			/*
			 * get the package type the trace has
			 */
			if(DataRecognition.is_normal)
				getPktType="select distinct PN,Name from "+tablePrefix+"normal_tr";
			else if(DataRecognition.is_old_wireless)
				getPktType="select distinct PT,Name from "+tablePrefix+"old_wireless_tr";
			else if(DataRecognition.is_new_wireless)
				getPktType="select distinct IP_It,Name from "+tablePrefix+"new_wireless_tr where IP_It is not null";
			
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
			if(DataRecognition.is_normal){
				for(int i=0;i<num;i++){
//					getPktNum="select count(distinct UPI) from "+tablePrefix+"normal_tr where PN='"+pktType[i][0]+"' and Name='"+pktType[i][1]+"' and Time>="+startTime+" and Time<="+endTime;
					getPktNum="select count(distinct UPI) from "+tablePrefix+"normal_tr where PN='"+pktType[i][0]+"' and Time>="+startTime+" and Time<="+endTime;					
					query=new Query(con,getPktNum);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							while(dataset.next()){
								pktNum[i]=Integer.parseInt(dataset.getString(1));
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				}
			}
			else if(DataRecognition.is_old_wireless){
				for(int i=0;i<num;i++){
					if(pktType[i][1].equals("arp")){
						getPktNum="select count(time) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Time>="+startTime+" and Time<="+endTime;							
					}
					else if(pktType[i][1].equals("old")){
						getPktNum="select count(time) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and Name='old' and Time>="+startTime+" and Time<="+endTime;
					}
					else if(pktType[i][1].equals("aodv")){
						if(DataRecognition.hasRTR)
							getPktNum="select count(time) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and NI=IP_SIA and TN='MAC' and Time>="+startTime+" and Time<="+endTime;
						else if(DataRecognition.hasMAC)
							getPktNum="select count(time) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Event='s' and NI=IP_SIA and TN='RTR' and Time>="+startTime+" and Time<="+endTime;
					}
					else{
						getPktNum="select count(distinct EI) from "+tablePrefix+"old_wireless_tr where PT='"+pktType[i][0]+"' and Time>="+startTime+" and Time<="+endTime;
					}
					query=new Query(con,getPktNum);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							while(dataset.next()){
								pktNum[i]=Integer.parseInt(dataset.getString(1));
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				}
			}
			else if(DataRecognition.is_new_wireless){
				for(int i=0;i<num;i++){
					getPktNum="select count(distinct IP_Ii) from "+tablePrefix+"new_wireless_tr where IP_It='"+pktType[i][0]+"' and Time>="+startTime+" and Time<="+endTime;
					query=new Query(con,getPktNum);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							while(dataset.next()){
								pktNum[i]=Integer.parseInt(dataset.getString(1));
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				}
			}
			
			if(graph.equals("Pie Chart")||graph.equals("3D Pie Chart")){
				data1 = new DefaultPieDataset();
				for(int i=0;i<num;i++)
					data1.setValue(pktType[i][0],pktNum[i]);
				
//				System.out.println("data1:"+data1.getItemCount());
//				System.out.println(data1.getKey(0));
//				System.out.println(data1.getValue(0));
				
				if(graph.equals("Pie Chart"))
					traffic=new ChartPanel(createPieChart(data1),true,true,true,true,true);
				else if(graph.equals("3D Pie Chart"))
					traffic=new ChartPanel(create3DPieChart(data1),true,true,true,true,true);
			}
			else if(graph.equals("Bar Chart")||graph.equals("3D Bar Chart")){
				data2 = new DefaultCategoryDataset();
				for(int i=0;i<num;i++)
					data2.addValue(pktNum[i],"Packet Num",pktType[i][0]);
				
//				System.out.println("data2:"+data2.getRowCount());
//				System.out.println(data2.getColumnCount());
//				System.out.println(data2.getColumnKey(0));
//				System.out.println(data2.getValue(0,0));

				
				if(graph.equals("Bar Chart"))
					traffic=new ChartPanel(createBarChart(data2),true,true,true,true,true);
				else if(graph.equals("3D Bar Chart"))
					traffic=new ChartPanel(create3DBarChart(data2),true,true,true,true,true);
			}
		}
	}
	private JFreeChart createLineChart(XYDataset dataset){
//		 create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
		"Traffic in Bytes", // chart title
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
	
	private JFreeChart createPieChart(PieDataset dataset){
//		 create a chart...
		JFreeChart chart = ChartFactory.createPieChart(
		"Packet Number of Every Type",
		dataset,
		true, // legend?
		true, // tooltips?
		false // URLs?
		);
		return chart;		
	}
	
	private JFreeChart create3DPieChart(PieDataset dataset){
		JFreeChart chart = ChartFactory.createPieChart3D(
		"Packet Number of Every Type",
		dataset,
		true, // legend?
		true, // tooltips?
		false // URLs?
		);
		return chart;		
	}
	
	private JFreeChart createBarChart(CategoryDataset dataset){
//		 create the chart...
		JFreeChart chart = ChartFactory.createBarChart(
		"Packet Number of Every Type", // chart title
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
		"Packet Number of Every Type", // chart title
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
	public String getDataType(){
		if(graph.equals("Line Chart"))
		    return xySeriesCollection;
		else if(graph.equals("Pie Chart")||graph.equals("3D Pie Chart"))
			return pieDataset;
		else if(graph.equals("Bar Chart")||graph.equals("3D Bar Chart"))
			return categoryDataset;
		return null;
	}
	
	public XYSeriesCollection getXYSeriesCollection(){
	    return trafficData;
	}
	public ChartPanel getChartPanel() {
		return traffic;
	}
	 public PieDataset getPieDataset() {
		return data1;
	}

	public CategoryDataset getCategoryDataset() {
		return data2;
	}
}
