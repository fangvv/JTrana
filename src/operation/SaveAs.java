/**
 * 
 */
package src.operation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;


import src.gui.MYFileFilter;

/**
 * @author heng
 *
 */
public class SaveAs extends DefaultDataAnalyse implements ActionListener{
	private int imageWidth;
	private int imageHeight;
	
	private JDialog setWH;
	private JTextField inputWidth;
	private JTextField inputHeight;
	private JLabel error;
	private ChartPanel chartPanel;
	
    public JFrame topFrame;
	public SaveAs(JFrame topFrame,DataAnalyse analyser){
		this.topFrame=topFrame;
		doSaveAs(topFrame,analyser);
	}
	
	private void doSaveAs(JFrame topFrame,DataAnalyse analyser){
		if(analyser==null||analyser.getDataType().equals(jTabel))
			JOptionPane.showMessageDialog(null,"Can not be saved as a image!","Error",JOptionPane.ERROR_MESSAGE);
		
		else if((chartPanel=analyser.getChartPanel())!=null){
			
			setWH=new JDialog(topFrame,"Set Width and Height",true);
			 Container contentPane=setWH.getContentPane();
			
			JPanel parameter=new JPanel();
			parameter.setLayout(new GridBagLayout());
			parameter.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),"Set parameters:",TitledBorder.LEFT,TitledBorder.TOP,new Font("bold",Font.BOLD,16),Color.BLUE));
			
			GridBagConstraints gbc=new GridBagConstraints();
			gbc.anchor=GridBagConstraints.EAST;
			gbc.insets=new Insets(2,2,2,2);
			
			JLabel label1=new JLabel("Width:");
			JLabel label2=new JLabel("Height:");
			
			inputWidth=new JTextField(10);
			inputHeight=new JTextField(10);
		    gbc.gridx=0;
		    gbc.gridy=0;
		    parameter.add(label1,gbc);
		    
		    gbc.gridx=1;
		    parameter.add(inputWidth,gbc);
		    
		    gbc.gridy=1;
		    gbc.gridx=0;
		    parameter.add(label2,gbc);
		    
		    gbc.gridx=1;
		    parameter.add(inputHeight,gbc);
		    
		    JPanel panel2=new JPanel();
		    
		    Dimension size=new Dimension(60,30);
		    JButton ok=new JButton("OK");
		    ok.setSize(size);
		    ok.addActionListener(this);
		    JButton exit=new JButton("Cancel");
		    exit.setSize(size);
		    exit.addActionListener(this);
		    JPanel button=new JPanel();
//		    button.setSize(200,50);
//		    button.setLayout(new GridLayout(1,2));
		    button.add(ok);
		    button.add(exit);
		    setWH.getRootPane().setDefaultButton(ok);
		    error=new JLabel("Set the width and height of the image!",JLabel.CENTER);
		    error.setForeground(new Color(88,120,164));
		    error.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		    
		    panel2.setLayout(new GridLayout(2,1));
		    panel2.add(button);
		    panel2.add(error);
		    
		    contentPane.add(parameter,BorderLayout.CENTER);
		    contentPane.add(panel2,BorderLayout.SOUTH);
		    
		    setWH.setSize(250,200);
		    Toolkit kit = Toolkit.getDefaultToolkit();
			Dimension screenSize = kit.getScreenSize();
			int width = (int) screenSize.getWidth();
			int height = (int) screenSize.getHeight();
			Dimension framesize=setWH.getSize();
			if(framesize.width>width)
				framesize.width=width;
			if(framesize.height>height)
				framesize.height=height;
			setWH.setLocation((width-framesize.width)/2,(height-framesize.height)/2);
			setWH.setVisible(true);

		}
	}

	public void actionPerformed(ActionEvent e) {
		// TODO 自动生成方法存根
		if(e.getActionCommand()=="OK"){
			try{
				imageWidth=Integer.parseInt(inputWidth.getText());
				imageHeight=Integer.parseInt(inputHeight.getText());
				setWH.dispose();
				File file;
				JFileChooser filechooser=new JFileChooser();
				filechooser.setDialogTitle("Save as a image");
				filechooser.addChoosableFileFilter(new MYFileFilter("png"));
				filechooser.addChoosableFileFilter(new MYFileFilter("jpeg"));
				int result =filechooser.showSaveDialog(topFrame);
				
				if(result==JFileChooser.APPROVE_OPTION){
					if(filechooser.getFileFilter() instanceof MYFileFilter){
						MYFileFilter fileFilter=(MYFileFilter)filechooser.getFileFilter();
						file=new File(filechooser.getSelectedFile()+"."+fileFilter.ext);
						if(fileFilter.ext.equals("png")){
							try {
								ChartUtilities.saveChartAsPNG(file,chartPanel.getChart(),imageWidth,imageHeight);
							} catch (IOException e1) {
								// TODO 自动生成 catch 块
								e1.printStackTrace();
							}
						}
						else if(fileFilter.ext.equals("jpeg")){
							try {
								ChartUtilities.saveChartAsJPEG(file,chartPanel.getChart(),imageWidth,imageHeight);
							} catch (IOException e1) {
								// TODO 自动生成 catch 块
								e1.printStackTrace();
							}
						}
					}
					else{
						file=filechooser.getSelectedFile();
						try {
							ChartUtilities.saveChartAsJPEG(file,chartPanel.getChart(),imageWidth,imageHeight);
						} catch (IOException e1) {
							// TODO 自动生成 catch 块
							e1.printStackTrace();
						}
					}
				}
				else 
					;
				
			}catch(NumberFormatException e2){
				inputWidth.setText("");
				inputHeight.setText("");
				error.setText("The width and height should be integers!");
			}
		}
		else if(e.getActionCommand()=="Cancel"){
			setWH.dispose();
		}
	}
}
