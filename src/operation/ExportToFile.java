/**
 * 
 */
package src.operation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import src.gui.MYFileFilter;

/**
 * @author heng
 *
 */
public class ExportToFile extends DefaultDataAnalyse{
	
	public ExportToFile(JFrame topFrame,DataAnalyse analyser){
		doExport(topFrame,analyser);
	}
	
	private void doExport(JFrame topFrame,DataAnalyse analyser){
		if(analyser==null)
			return;
//			JOptionPane.showMessageDialog(null,"No Data!","Error",JOptionPane.ERROR_MESSAGE);
		
		File file;
		JFileChooser filechooser=new JFileChooser();
		filechooser.setDialogTitle("Save the data into a file");
		filechooser.addChoosableFileFilter(new MYFileFilter("txt"));
		filechooser.addChoosableFileFilter(new MYFileFilter("doc"));
		int result =filechooser.showSaveDialog(topFrame);
		
		if(result==JFileChooser.APPROVE_OPTION){
			FileWriter fw;
			BufferedWriter bw;
			if(filechooser.getFileFilter() instanceof MYFileFilter){
				MYFileFilter fileFilter=(MYFileFilter)filechooser.getFileFilter();				
				file=new File(filechooser.getSelectedFile()+"."+fileFilter.ext);				
			}
			else
				file=filechooser.getSelectedFile();
			try {
				fw=new FileWriter(file);
				bw=new BufferedWriter(fw);
			} catch (IOException e) {
				// TODO 自动生成 catch 块
				JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
				return;
			}
			if(analyser.getDataType().equals(xySeriesCollection)){				
				XYSeriesCollection data=analyser.getXYSeriesCollection();				
				if(data==null){
					try {
						bw.close();
					} catch (IOException e) {
						// TODO 自动生成 catch 块
//						e.printStackTrace();
						JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
						return;
					}
					JOptionPane.showMessageDialog(null,"NO DATA!","Error",JOptionPane.ERROR_MESSAGE);
					
					return;
				}
				int seriesNum=data.getSeriesCount();
				for(int i=0;i<seriesNum;i++){
					int row=data.getSeries(i).getItemCount();
					String name=data.getSeriesName(i);
					try {
						bw.write("#");
						bw.newLine();
						bw.write("#"+"\t"+name);
						bw.newLine();
						bw.write("#");
						bw.newLine();
					} catch (IOException e) {
						// TODO 自动生成 catch 块
//						e.printStackTrace();
						JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
						return;
					}
					XYSeries series=(XYSeries)data.getSeries(i);
					for(int j=0;j<row;j++){
						String str=String.valueOf(series.getX(j))+"\t"+String.valueOf(series.getY(j));
						try {
							bw.write(str);
							bw.newLine();
						} catch (IOException e) {
							// TODO 自动生成 catch 块
//							e.printStackTrace();
							JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
							return;
						}							
					}
				}
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					// TODO 自动生成 catch 块
//					e.printStackTrace();
					JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}				
			}
			else if(analyser.getDataType().equals(pieDataset)){
				DefaultPieDataset data=(DefaultPieDataset) analyser.getPieDataset();
				if(data==null){
					try {
						bw.close();
					} catch (IOException e) {
						// TODO 自动生成 catch 块
//						e.printStackTrace();
						JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
						return;
					}
					JOptionPane.showMessageDialog(null,"NO DATA!","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				int num=data.getItemCount();
				for(int i=0;i<num;i++){
					String str=data.getKey(i)+"\t"+String.valueOf(data.getValue(i));
					try {
						bw.write(str);
						bw.newLine();
					} catch (IOException e) {
						// TODO 自动生成 catch 块
//						e.printStackTrace();
						JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
						return;
					}	
				}
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					// TODO 自动生成 catch 块
//					e.printStackTrace();
					JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}		
			}
			else if(analyser.getDataType().equals(categoryDataset)){
				DefaultCategoryDataset data=(DefaultCategoryDataset)analyser.getCategoryDataset();
				if(data==null){
					try {
						bw.close();
					} catch (IOException e) {
						// TODO 自动生成 catch 块
//						e.printStackTrace();
						JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
						return;
					}
					JOptionPane.showMessageDialog(null,"NO DATA!","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				int numRow=data.getRowCount();
				int numCol=data.getColumnCount();
				for(int i=0;i<numRow;i++){
					try {
						bw.write("#");
						bw.newLine();
						bw.write("#"+"\t"+data.getRowKey(i));
						bw.newLine();
						bw.write("#");
						bw.newLine();
					} catch (IOException e) {
						// TODO 自动生成 catch 块
//						e.printStackTrace();
						JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
						return;
					}
					for(int j=0;j<numCol;j++){
						String str=data.getColumnKey(j)+"\t"+String.valueOf(data.getValue(i,j));
						try {
							bw.write(str);
							bw.newLine();
						} catch (IOException e) {
							// TODO 自动生成 catch 块
//							e.printStackTrace();
							JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
							return;
						}	
					}
				}
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					// TODO 自动生成 catch 块
//					e.printStackTrace();
					JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}		
			}
			else if(analyser.getDataType().equals(jTabel)){
				JTable data=analyser.getTableData();
				if(data==null){
					try {
						bw.close();
					} catch (IOException e) {
						// TODO 自动生成 catch 块
//						e.printStackTrace();
						JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
						return;
					}
					JOptionPane.showMessageDialog(null,"NO DATA!","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				int numRow=data.getRowCount();
				int numCol=data.getColumnCount();
				String name="#";
				for(int i=0;i<numCol;i++){
					name+=data.getColumnName(i)+"\t";
				}
				try {
					bw.write(name);
					bw.newLine();
				} catch (IOException e) {
					// TODO 自动生成 catch 块
//					e.printStackTrace();
					JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}	
				for(int i=0;i<numRow;i++){
					String str = "";
					for(int j=0;j<numCol;j++){
						str+=data.getValueAt(i,j)+"\t";						
					}
					try {
						bw.write(str);
						bw.newLine();
					} catch (IOException e) {
						// TODO 自动生成 catch 块
//						e.printStackTrace();
						JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
						return;
					}	
				}
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					// TODO 自动生成 catch 块
//					e.printStackTrace();
					JOptionPane.showMessageDialog(null,"IO Error!","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}	
			}
			else
				JOptionPane.showMessageDialog(null,"Unknown Data Type!","Error",JOptionPane.ERROR_MESSAGE);
		}	
	    else
		    ;
	}
}
