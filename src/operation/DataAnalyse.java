/**
 * 
 */
package src.operation;

import javax.swing.JTable;

import org.jfree.chart.ChartPanel;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @author heng
 *
 */
public interface DataAnalyse {
	public final String xySeriesCollection="XYSeriesCollection";
	public final String jTabel="JTabel";
	public final String pieDataset="PieDataset";
	public final String categoryDataset="CategoryDataset";
	public JTable getTableData();
    public ChartPanel getChartPanel();
    public String getDataType();
    public XYSeriesCollection getXYSeriesCollection();
    public PieDataset getPieDataset();
    public CategoryDataset getCategoryDataset();
    public String getTitle();
}
