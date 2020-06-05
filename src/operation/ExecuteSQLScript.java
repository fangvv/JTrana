/**
 * 
 */
package src.operation;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

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

import src.gui.FileIcon;
import src.gui.MYFileFilter;



/**
 * @author heng
 *
 */
public class ExecuteSQLScript extends DefaultDataAnalyse{	
	public String tablePrefix;
	public JFrame frame;
	public Connection con;
	
	private String title="";
	private String sql;
	
	private JTable table;
	private XYSeriesCollection line;
	private ChartPanel chartPanel;
	
	private String flag;
	private int col;
	private String[] colName;
    
	public ExecuteSQLScript(Connection con,JFrame frame,String tablePrefix){
		this.frame=frame;
		this.con=con;
		this.tablePrefix=tablePrefix;
		
		openSQLScript();
		executeQuery();
	}

	private void openSQLScript(){
		File file;
		String row;
		JFileChooser filechooser=new JFileChooser();
		filechooser.setDialogTitle("Open SQL Script");
		filechooser.addChoosableFileFilter(new MYFileFilter("sql"));
		filechooser.setFileView(new FileIcon());
		int result =filechooser.showOpenDialog(frame);
		
		if(result==JFileChooser.APPROVE_OPTION){
			file=filechooser.getSelectedFile();
			if(file.exists()){
				try{
					FileReader filereader=new FileReader(file);
					BufferedReader br=new BufferedReader(filereader);
					try{
						while((row=br.readLine())!=null){
							if(row.startsWith("#")||row.startsWith("//"))
								continue;
							else if(row.trim().toLowerCase().startsWith("select "))
								sql=row;
							else if(row.equalsIgnoreCase("Y")||row.equalsIgnoreCase("N"))
								flag=row;
							else
								title=row;
						}
						//we should change the name of the table
						//that is we should add tablePrefix to the name of the table
						int offset=sql.indexOf("from");
						char[] temp=sql.toCharArray();
						for(offset+=4;offset<temp.length;offset++){
							if(temp[offset]!=' ')
								break;
						}
						StringBuffer str=new StringBuffer(sql);
						str=str.insert(offset,tablePrefix);
						sql=new String(str);
//						System.out.println(sql);
					} catch (IOException e) {
						// TODO 自动生成 catch 块
//						e.printStackTrace();
						JOptionPane.showMessageDialog(null,"IO ERROR!","Error Message", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null,file.getName()+" not found!","Error Message",JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					return;
				}
			}
			else{
				JOptionPane.showMessageDialog(null,file.getName()+" not exists!","ERROR",JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}
	
	private void executeQuery() {
		XYSeries data;
		ResultSet dataset;
		Query query;
		if(sql==null)
			return;
		query=new Query(con,sql);
		dataset=query.doQuery();
		if(dataset==null){
			System.out.println("Error occuses when executing a sql file");
			return;
		}
		else{			
			try{
				ResultSetMetaData mdrs=dataset.getMetaData();
				col=mdrs.getColumnCount();
				colName=new String[col];
				for(int i=0;i<col;i++)
					colName[i]=mdrs.getColumnName(i+1);
				if(col==2&&flag.equalsIgnoreCase("Y")){
					data=new XYSeries(title);
					while(dataset.next()){
						data.add(Double.parseDouble(dataset.getString(1)),Double.parseDouble(dataset.getString(2)));
					}
					line=new XYSeriesCollection();
					line.addSeries(data);
					chartPanel=new ChartPanel(createLineChart(line),true,true,true,true, true);
				}
				else{
					int count=0;
					while(dataset.next())
						count++;
					Object[][] object=new Object[count][col];
					query=new Query(con,sql);
					dataset=query.doQuery();
					count=0;
					while(dataset.next()){
						for(int j=1;j<=col;j++)
							object[count][j-1]=dataset.getString(j);
						count++;
					}
					table=new JTable(new TableModel(object,colName));
					table.setPreferredScrollableViewportSize(new Dimension(750,(count+1)*15));
				}
			}catch(SQLException e1){
				e1.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,"Can not display the result as a chart!","ERROR",JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}
	
	private JFreeChart createLineChart(XYDataset dataset){
//		 create the chart...
		JFreeChart chart = ChartFactory.createXYLineChart(
		title, // chart title
		colName[0], // x axis label
		colName[1], // y axis label
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
		if(col==2&&flag.equalsIgnoreCase("Y"))
		    return xySeriesCollection;
		else
			return jTabel;
	}
	
	public XYSeriesCollection getXYSeriesCollection(){
	    return line;
	}
	public ChartPanel getChartPanel() {
		return chartPanel;
	}
	 public JTable getTableData() {
		return table;
	}
	public String getTitle(){
		return title;
	}
}
