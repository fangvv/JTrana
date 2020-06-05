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

public class NetworkEnergy extends DefaultDataAnalyse{
	public Connection con;
	public String tablePrefix;
	public double startTime;
	public double endTime;
	public String xLabel;
	public String yLabel;
	
	public XYSeriesCollection energyData;
	
	
	private int[] node;
	private double[] nodeEnergy1;
	private double[] nodeEnergy2;
	private int num;
	
	
	private ChartPanel energy=null;
	
	public NetworkEnergy(Connection con,String tablePrefix,String startTime,String endTime,String xLabel,String yLabel){
		this.con=con;
		this.tablePrefix=tablePrefix;
		this.startTime=Double.parseDouble(startTime);
		this.endTime=Double.parseDouble(endTime);
		this.xLabel=xLabel;
		this.yLabel=yLabel;
		
		createChartPanel();
		
	}
    
	/**
	 * create a chartpanel to display the energy 
	 */
	private void  createChartPanel(){
		String getNode=null;
		String getTime=null;
		XYSeries series = new XYSeries("Energy");
//		System.out.println(series.getName());
		ResultSet dataset,dataset1;
		Query query,query1;
		if(DataRecognition.is_old_wireless){
			/*
			 * get the number of node
			 */
			getNode="select count(distinct n) from "+tablePrefix+"energy_tr";
			query=new Query(con,getNode);
			dataset=query.doQuery();
			if(dataset==null){
				return;
			}
			else{
				try{
					while(dataset.next()){
						num=Integer.parseInt(dataset.getString(1));
					}	
					if(num==0)
						return;
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
			/*
			 * get all node
			 */
			node=new int[num];
			getNode="select distinct n from "+tablePrefix+"energy_tr order by n";
			query=new Query(con,getNode);
			dataset=query.doQuery();
			try{
				int i=0;
				while(dataset.next()){
					node[i++]=Integer.parseInt(dataset.getString(1));
				}											
			}catch(SQLException e1){
				e1.printStackTrace();
			}
			/*
			 * get time and the energy of node at every time
			 */
			getTime="select distinct t from "+tablePrefix+"energy_tr order by t";
			query=new Query(con,getTime);
			dataset=query.doQuery();
			if(dataset==null)
				return;
			else{
				try{
					boolean flag=true;
                    nodeEnergy1=new double[num];
                    nodeEnergy2=new double[num];
                    for(int i=0;i<num;i++){
                    	nodeEnergy1[i]=-1;
                    }
					String time;
					while(dataset.next()){
						time=dataset.getString(1);
						double timeNum=Double.parseDouble(time);
						String getNodeEnergy="select n,e from "+tablePrefix+"energy_tr where t="+time;
						query1=new Query(con,getNodeEnergy);
						dataset1=query1.doQuery();
						if(flag){
							flag=false;
							try{
								while(dataset1.next()){
									int nodeID=Integer.parseInt(dataset1.getString(1));
									double energy=Double.parseDouble(dataset1.getString(2));
									for(int j=0;j<num;j++){
										if(nodeID==node[j])
											nodeEnergy1[j]=energy;
									}
								}											
							}catch(SQLException e1){
								e1.printStackTrace();
							}
							int j;
							for(j=0;j<num;j++)
								if(nodeEnergy1[j]>0)
									break;
							
							double sumEnergy=0;
							for(int k=0;k<num;k++){
								if(nodeEnergy1[k]==-1)
									nodeEnergy1[k]=nodeEnergy1[j];
								sumEnergy+=nodeEnergy1[k];
							}							
							if(timeNum>startTime&&timeNum<endTime)
								series.add(timeNum,sumEnergy);
								
						}
						else{
							for(int i=0;i<num;i++){
		                    	nodeEnergy2[i]=-1;
		                    }
							try{
								while(dataset1.next()){
									int nodeID=Integer.parseInt(dataset1.getString(1));
									double energy=Double.parseDouble(dataset1.getString(2));
									for(int j=0;j<num;j++){
										if(nodeID==node[j])
											nodeEnergy2[j]=energy;
									}
								}											
							}catch(SQLException e1){
								e1.printStackTrace();
							}
							double sumEnergy=0;
							/*
							 * if there is no record about a node at the time
							 * we will think that its energy is the same as the energy it has at the latest time
							 */
							for(int j=0;j<num;j++){
								if(nodeEnergy2[j]==-1)
									nodeEnergy2[j]=nodeEnergy1[j];
								sumEnergy+=nodeEnergy2[j];
							}
							for(int j=0;j<num;j++)
								nodeEnergy1[j]=nodeEnergy2[j];
							
							if(timeNum>startTime&&timeNum<endTime)
								series.add(timeNum,sumEnergy);
						}						
					}					
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}			
		}
		else if(DataRecognition.is_new_wireless){
			/*
			 * get the number of node
			 */
			getNode="select count(distinct Ni) from "+tablePrefix+"new_wireless_tr ";
			query=new Query(con,getNode);
			dataset=query.doQuery();
			if(dataset==null){
				return;
			}
			else{
				try{
					while(dataset.next()){
						num=Integer.parseInt(dataset.getString(1));
					}											
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
			/*
			 * get all node
			 */
			node=new int[num];
			getNode="select distinct Ni from "+tablePrefix+"new_wireless_tr order by Ni";
			query=new Query(con,getNode);
			dataset=query.doQuery();
			try{
				int i=0;
				while(dataset.next()){
					node[i++]=Integer.parseInt(dataset.getString(1));
				}											
			}catch(SQLException e1){
				e1.printStackTrace();
			}
			/*
			 * get time and the energy of node at every time
			 */
			getTime="select distinct Time from "+tablePrefix+"new_wireless_tr order by Time";
			query=new Query(con,getTime);
			dataset=query.doQuery();
			if(dataset==null)
				return;
			else{				
				try{
					boolean flag=true;
	                nodeEnergy1=new double[num];
	                nodeEnergy2=new double[num];
	                for(int i=0;i<num;i++){
	                	nodeEnergy1[i]=-1;
	                }
					String time;
					while(dataset.next()){
						time=dataset.getString(1);
						double timeNum=Double.parseDouble(time);
						String getNodeEnergy="select Ni,Ne from "+tablePrefix+"new_wireless_tr where Time="+time;
						query1=new Query(con,getNodeEnergy);
						dataset1=query1.doQuery();
						if(flag){
							flag=false;
							try{
								while(dataset1.next()){
									int nodeID=Integer.parseInt(dataset1.getString(1));
									double energy=Double.parseDouble(dataset1.getString(2));
									if(energy<0)
										return;										
									for(int j=0;j<num;j++){
										if(nodeID==node[j])
											nodeEnergy1[j]=energy;
									}
								}											
							}catch(SQLException e1){
								e1.printStackTrace();
							}
							int j;
							for(j=0;j<num;j++)
								if(nodeEnergy1[j]>0)
									break;
							
							double sumEnergy=0;
							for(int k=0;k<num;k++){
								if(nodeEnergy1[k]==-1)
									nodeEnergy1[k]=nodeEnergy1[j];
								sumEnergy+=nodeEnergy1[k];
							}							
							if(timeNum>=startTime&&timeNum<=endTime)
								series.add(timeNum,sumEnergy);
								
						}
						else{
							for(int i=0;i<num;i++){
		                    	nodeEnergy2[i]=-1;
		                    }
							try{
								while(dataset1.next()){
									int nodeID=Integer.parseInt(dataset1.getString(1));
									double energy=Double.parseDouble(dataset1.getString(2));
									for(int j=0;j<num;j++){
										if(nodeID==node[j])
											nodeEnergy2[j]=energy;
									}
								}											
							}catch(SQLException e1){
								e1.printStackTrace();
							}
							double sumEnergy=0;
							/*
							 * if there is no record about a node at the time
							 * we will think that its energy is the same as the energy it has at the latest time
							 */
							for(int j=0;j<num;j++){
								if(nodeEnergy2[j]==-1)
									nodeEnergy2[j]=nodeEnergy1[j];
								sumEnergy+=nodeEnergy2[j];
							}
							for(int j=0;j<num;j++)
								nodeEnergy1[j]=nodeEnergy2[j];
							
							if(timeNum>=startTime&&timeNum<=endTime)
								series.add(timeNum,sumEnergy);
						}						
						
					}
				}catch(SQLException e1){
					e1.printStackTrace();
				}
			}
		}
		energyData = new XYSeriesCollection();
		energyData.addSeries(series);
//		System.out.println(series.getItemCount());
//		System.out.println(series.getX(77));
//		System.out.println(series.getY(77));
//		System.out.println(energyData.getSeriesCount());
//		XYSeries a=energyData.getSeries(0);
//		System.out.println(a.getX(0));
		energy = new ChartPanel(createLineChart(energyData),true, true, true, true, true);
		energy.setPreferredSize(new java.awt.Dimension(775, 400));
	}
	
	private JFreeChart createLineChart(XYDataset dataset){
//		 create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
		"Energy remained of the whole network", // chart title
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
	    	return energyData;
	}
	public ChartPanel getChartPanel() {
		// TODO 自动生成方法存根
		if(DataRecognition.is_normal)
			return null;
		else
			return energy;
	}

}
