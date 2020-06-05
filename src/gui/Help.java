package src.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.jfree.ui.about.SystemPropertiesPanel;

public class Help {
	private JDialog helpFrame;
	public Help(JFrame frame){
		helpFrame=new JDialog(frame,"Help",true);
		Container content=helpFrame.getContentPane();
		JTabbedPane help=new JTabbedPane(JTabbedPane.TOP);
		JEditorPane introduction=null;
		try{
			File file1=new File("resource/introduction.htm");
			String path=file1.getAbsolutePath();
			path="file:"+path;
			introduction=new JEditorPane();
			introduction.setPage(path);
			introduction.setEditable(false);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		JScrollPane sp1=new JScrollPane(introduction);
	
		JEditorPane gpl=null;
		try{
			File file1=new File("resource/gpl-license.html");
			String path=file1.getAbsolutePath();
			path="file:"+path;
			gpl=new JEditorPane();
			gpl.setPage(path);
			gpl.setEditable(false);
		
		}
		catch(IOException e){
			e.printStackTrace();
		}
		JScrollPane sp2=new JScrollPane(gpl);
	
		help.add("About",sp1);
		help.add("GPL License",sp2);
		help.add("System Information",new SystemPropertiesPanel());
		content.add(help);
		helpFrame.setSize(600,700);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		Dimension framesize=helpFrame.getSize();
		if(framesize.width>width)
			framesize.width=width;
		if(framesize.height>height)
			framesize.height=height;
		helpFrame.setLocation((width-framesize.width)/2,(height-framesize.height)/2);
		helpFrame.setVisible(true);
		
	}

}
