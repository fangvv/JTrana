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
public class NodeEnergy extends DefaultDataAnalyse{
	public Connection con;
	public String tablePrefix;
	public int nodeID;
	public String startTime;
	public String endTime;
	public String xLabel;
	public String yLabel;
	
	public XYSeriesCollection nodeEnergy;
	public ChartPanel chartPanel;
	
	public NodeEnergy(Connection con,String tablePrefix,int nodeID,String startTime,String endTime,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.nodeID=nodeID;
		this.startTime=startTime;
		this.endTime=endTime;
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		
		createChartPanel();
		
	}
	
	private void createChartPanel(){
		ResultSet dataset;
		Query query;
		String getNodeEnergy = null;
		XYSeries data=new XYSeries("Node Energy");
		if(DataRecognition.is_old_wireless)
			getNodeEnergy="select t,e from "+tablePrefix+"energy_tr where n="+nodeID+" and t>="+startTime+" and t<="+endTime;
		else if(DataRecognition.is_new_wireless)
			getNodeEnergy="select Time,Ne from "+tablePrefix+"new_wireless_tr where Ni="+nodeID+" and Time>="+startTime+" and Time<="+endTime;
		query=new Query(con,getNodeEnergy);
		dataset=query.doQuery();
		if(dataset!=null){
			try{
				while(dataset.next()){
					data.add(Double.parseDouble(dataset.getString(1)),Double.parseDouble(dataset.getString(2)));
				}
			}catch(SQLException e1){
				e1.printStackTrace();
			}
		}
		nodeEnergy=new XYSeriesCollection();
		nodeEnergy.addSeries(data);
		chartPanel=new ChartPanel(createLineChart(nodeEnergy),true,true,true,true,true);
	}
	private JFreeChart createLineChart(XYDataset dataset){
//		 create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
		"Node Energy", // chart title
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
		return xySeriesCollection;
	}
	public XYSeriesCollection getXYSeriesCollection(){
	    return nodeEnergy;
	}
	public ChartPanel getChartPanel() {
		if(DataRecognition.is_normal)
			return null;
		return chartPanel;
	}
}
