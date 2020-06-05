package src.gui;

/*
 * JTracePlot is used to analyse the trace files created by NS2.
 * @auther hengheng qian
 * version 1.0
*/
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
//import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartPanel;

import src.operation.*;



public class MainFrame implements ActionListener,ItemListener{
    public String tablePrefix;
    public static String name;
    public static String usrpwd;
    public String url;
    public JFrame topFrame;
    public JTextField username;
    public JTextField starttime;
    public JTextField endtime;
    public JLabel mintime;
    public JLabel maxtime;
    public JTextField xaxis;
    public JTextField yaxis;
    public JPasswordField password;
    public JLabel error;
    public JDialog log;
	private JMenuBar menuBar;
	private JToolBar toolBar;	
	private JSplitPane mainView;
	private JPanel parameter;
	private JPanel display;
	private JPanel statusBar;
	private JTextField statusField;
	private JComboBox selectnode;
	private JComboBox othernode;
	private JComboBox slevel;
	private JComboBox dlevel;
	private JComboBox sentpack;
	private JComboBox ackpack;
	private JComboBox displaydetail;
	private JComboBox graph;
	private JButton create;
	private Container contentPane;
	private DBConnection connector;
	private boolean connected;
	public  String selectedItem;
	public JLabel displaySelectedItem;
	public DataAnalyse analyser;
	
	public Connection con;
	
	private String flag="";
	

	
	public MainFrame(String tablePrefix){
		this.tablePrefix=tablePrefix;
		selectedItem="Display:";
		url="jdbc:mysql://localhost/test";
		topFrame=new JFrame("JTrana-1.0");
		contentPane=topFrame.getContentPane();
		topFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("../../icons/ImageEditor.gif")));
		//display the mainframe in the center of the screen
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();  
		
		topFrame.setSize(800,height - 100);  //suitable for display (fww)
		Dimension framesize=topFrame.getSize();
		if(framesize.width>width)
			framesize.width=width;
		if(framesize.height>height)
			framesize.height=height;
		topFrame.setLocation((width-framesize.width)/2,(height-framesize.height)/2);
		
		menuBar=buildMenuBar();
		toolBar=buildToolBar();
		mainView=buildMainView();
		statusBar=buildStatusBar();
		
		topFrame.setJMenuBar(menuBar);
		contentPane.add(toolBar,BorderLayout.NORTH);
		contentPane.add(mainView,BorderLayout.CENTER);
		contentPane.add(statusBar,BorderLayout.SOUTH);

		
		conToMySQL();
		
/*		try{
			Thread.sleep(10000);
		}catch(Exception e){}
		contentPane.remove(statusBar);
		topFrame.validate();
		*/
		topFrame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				deleteTables();
				try {					
					con.close();					
				} catch (SQLException e1) {	}					
				System.exit(0);
			}
		});
	}
	
	//build the menubar
	private JMenuBar buildMenuBar(){
		JMenuBar menuBar1=new JMenuBar();
		
		//build FILE menu
		JMenu mFile=new JMenu("File");
		mFile.setMnemonic('F');
		JMenuItem open=new JMenuItem("Open",new ImageIcon("icons/open24.gif"));
		JMenuItem saveas=new JMenuItem("Save as",new ImageIcon("icons/Save24.gif"));
		JMenuItem export=new JMenuItem("Export to File",new ImageIcon("icons/export24.gif"));
		JMenuItem exit=new JMenuItem("Exit",new ImageIcon("icons/exit24.gif"));
		
		open.setMnemonic('O');
		saveas.setMnemonic('S');
		export.setMnemonic('E');
		exit.setMnemonic('X');
		
		open.setAccelerator(KeyStroke.getKeyStroke('O',java.awt.Event.CTRL_MASK));
		saveas.setAccelerator(KeyStroke.getKeyStroke('S',java.awt.Event.CTRL_MASK));
		export.setAccelerator(KeyStroke.getKeyStroke('E',java.awt.Event.CTRL_MASK));
		exit.setAccelerator(KeyStroke.getKeyStroke('X',java.awt.Event.CTRL_MASK));
		
		open.setActionCommand("Open");
		saveas.setActionCommand("Save as");
		export.setActionCommand("Export to File");
		exit.setActionCommand("Exit");
		
		open.addActionListener(this);
		
		saveas.addActionListener(this);
		
		export.addActionListener(this);
		
		exit.addActionListener(this);
		
		mFile.add(open);
		mFile.add(saveas);
		mFile.add(export);
		mFile.addSeparator();
		mFile.add(exit);
		
		//build NETWORK menu
		JMenu network=new JMenu("Network");
		network.setMnemonic('N');
		JMenuItem siminfo=new JMenuItem("Simulation Information");
		JMenuItem netenergy=new JMenuItem("Energy Remained");
		JMenuItem traffic=new JMenuItem("Packet Stat.");
		
		siminfo.setMnemonic('S');
		netenergy.setMnemonic('E');
		traffic.setMnemonic('T');
		
		siminfo.setActionCommand("Simulation Information");
		netenergy.setActionCommand("Energy Remained");
		traffic.setActionCommand("Packet Stat.");
		
		siminfo.addActionListener(this);
		
		netenergy.addActionListener(this);
		
		traffic.addActionListener(this);
		
		network.add(siminfo);
		network.add(netenergy);
		network.add(traffic);
		
		//build NODE menu
		JMenu node=new JMenu("Node");
		node.setMnemonic('O');
		JMenuItem curnode=new JMenuItem("Current Node Information");
		JMenuItem packid=new JMenuItem("Packets ID");
		JMenuItem throughput=new JMenuItem("Throughput");
		JMenuItem nodeenergy=new JMenuItem("Node Energy");
		JMenuItem seqnum=new JMenuItem("Sequence Number");
		
		JMenu delay=new JMenu("Delay");
		delay.setMnemonic('D');
		JMenuItem packiddelay=new JMenuItem("Packet ID vs Delay");
		JMenuItem packsizedelay=new JMenuItem("Packet Size vs Delay");
//		JMenuItem throughputdelay=new JMenuItem("Throughput vs Delay");
		
		JMenu rtt=new JMenu("RTT");
		rtt.setMnemonic('R');
		JMenuItem packsizertt=new JMenuItem("Packet Size vs RTT");
//		JMenuItem throughputrtt=new JMenuItem("Throughput vs RTT");
		JMenuItem packidrtt=new JMenuItem("Sent Packet ID vs RTT");
		
//		JMenu time=new JMenu("Process Time");
//		time.setMnemonic('P');
//		JMenuItem packidtime=new JMenuItem("Packet ID vs Process Time");
//		JMenuItem packsizetime=new JMenuItem("Packet Size vs Process Time");
//		JMenuItem throughputtime=new JMenuItem("Throughput vs Process Time");
		
		JMenuItem movement=new JMenuItem("Display Movement");
		
		curnode.setMnemonic('N');
		packid.setMnemonic('I');
		throughput.setMnemonic('T');
		nodeenergy.setMnemonic('E');
		seqnum.setMnemonic('S');
		packiddelay.setMnemonic('P');
		packsizedelay.setMnemonic('S');
//		throughputdelay.setMnemonic('T');
		packsizertt.setMnemonic('S');
//		throughputrtt.setMnemonic('T');
		packidrtt.setMnemonic('I');
//		packidtime.setMnemonic('I');
//		packsizetime.setMnemonic('S');
//		throughputtime.setMnemonic('T');
		movement.setMnemonic('D');
		
		curnode.setActionCommand("Current Node Information");
		packid.setActionCommand("Packets ID");
		throughput.setActionCommand("Throughput");
		nodeenergy.setActionCommand("Node Energy");
		seqnum.setActionCommand("Sequence Number");
		packiddelay.setActionCommand("Packet ID vs Delay");
		packsizedelay.setActionCommand("Packet Size vs Delay");
//		throughputdelay.setActionCommand("Throughput vs Delay");
		packsizertt.setActionCommand("Packet Size vs RTT");
//		throughputrtt.setActionCommand("Throughput vs RTT");
		packidrtt.setActionCommand("Sent Packet ID vs RTT");
//		packidtime.setActionCommand("Packet ID vs Process Time");
//		packsizetime.setActionCommand("Packet Size vs Process Time");
//		throughputtime.setActionCommand("Throughput vs Process Time");
		movement.setActionCommand("Display Movement");
		
		curnode.addActionListener(this);
		
		packid.addActionListener(this);
		
		throughput.addActionListener(this);
		
		nodeenergy.addActionListener(this);
		
		seqnum.addActionListener(this);
		
		packiddelay.addActionListener(this);
		
		packsizedelay.addActionListener(this);
		
//		throughputdelay.addActionListener(this);
		
		packsizertt.addActionListener(this);
		
		packidrtt.addActionListener(this);
		
//		throughputrtt.addActionListener(this);
		
//		packidtime.addActionListener(this);
		
//		packsizetime.addActionListener(this);
		
//		throughputtime.addActionListener(this);
		
		movement.addActionListener(this);
		
		node.add(curnode);
		node.add(packid);
		node.add(throughput);
		node.add(nodeenergy);
		node.add(seqnum);
		node.addSeparator();
		delay.add(packiddelay);
		delay.add(packsizedelay);
//		delay.add(throughputdelay);
		node.add(delay);
		node.addSeparator();
		rtt.add(packsizertt);
//		rtt.add(throughputrtt);
		rtt.add(packidrtt);
		node.add(rtt);
		node.addSeparator();
//		time.add(packidtime);
//		time.add(packsizetime);
//		time.add(throughputtime);
//		node.add(time);
		node.addSeparator();
		node.add(movement);
		
		//build EXTEND menu
		JMenu extend=new JMenu("Extend");
		extend.setMnemonic('E');
		JMenuItem excute=new JMenuItem("Excute SQLscript",new ImageIcon("icons/import24.gif"));
		
		excute.setMnemonic('E');
		
		excute.setActionCommand("Excute SQLscript");
		
		excute.addActionListener(this);
		
		extend.add(excute);
		
		//build HELP menu
		JMenu help=new JMenu("Help");
		help.setMnemonic('H');
		JMenuItem about=new JMenuItem("About",new ImageIcon("icons/About16.gif"));
		
		about.setMnemonic('A');
		
		about.setActionCommand("About");
		
		about.addActionListener(this);
		
		help.add(about);
		
		menuBar1.add(mFile);
		menuBar1.add(network);
		menuBar1.add(node);
		menuBar1.add(extend);
		menuBar1.add(help);
		
		return menuBar1;
	}
	
	//build toolbar
	private JToolBar buildToolBar(){
		JToolBar toolBar1=new JToolBar();
		toolBar1.setFloatable(false);
		
		ToolBarAction open=new ToolBarAction("Open",new ImageIcon("icons/open24.gif"));
		ToolBarAction export=new ToolBarAction("Export to File",new ImageIcon("icons/export24.gif"));
		ToolBarAction saveas=new ToolBarAction("Save as",new ImageIcon("icons/Save24.gif"));
		ToolBarAction excute=new ToolBarAction("Excute SQLscript",new ImageIcon("icons/import24.gif"));
		ToolBarAction about=new ToolBarAction("About",new ImageIcon("icons/About16.gif"));
		
		JButton button;
		
		button=toolBar1.add(open);
		button.setActionCommand((String)open.getValue(Action.NAME));
		button.setToolTipText((String)open.getValue(Action.NAME));
		
		toolBar1.addSeparator();
		
		button=toolBar1.add(saveas);
		button.setActionCommand((String)saveas.getValue(Action.NAME));
		button.setToolTipText((String)saveas.getValue(Action.NAME));
		
		button=toolBar1.add(export);
		button.setActionCommand((String)export.getValue(Action.NAME));
		button.setToolTipText((String)export.getValue(Action.NAME));
		
		toolBar1.addSeparator();
		
		button=toolBar1.add(excute);
		button.setActionCommand((String)excute.getValue(Action.NAME));
		button.setToolTipText((String)excute.getValue(Action.NAME));
		
		toolBar1.addSeparator();
		
		button=toolBar1.add(about);
		button.setActionCommand((String)about.getValue(Action.NAME));
		button.setToolTipText((String)about.getValue(Action.NAME));
		
		return toolBar1;
		
	}
	
    //	build mainview
	private JSplitPane buildMainView(){
		parameter=new JPanel();
		parameter.setLayout(new GridBagLayout());
		GridBagConstraints gbc=new GridBagConstraints();
		gbc.anchor=GridBagConstraints.EAST;
		gbc.insets=new Insets(2,2,2,2);
		
		parameter.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),"Set the display parameters:",TitledBorder.LEFT,TitledBorder.TOP,new Font("bold",Font.BOLD,16),Color.BLUE));
		JLabel l1=new JLabel("Current node:");
		JLabel l2=new JLabel("Other node:");
		JLabel l3=new JLabel("Source trace level:");
		JLabel l4=new JLabel("Destination trace level:");
		JLabel l5=new JLabel("Sent packet:");
		JLabel l6=new JLabel("ACK packet:");
		JLabel l7=new JLabel("Display detial:");
		JLabel l8=new JLabel("Gragh:");
		JLabel l9=new JLabel("Start time:");
		JLabel l10=new JLabel("End time:");
		JLabel l11=new JLabel("X axis label:");
		JLabel l12=new JLabel("Y axis label:");
		mintime=new JLabel("0");
		mintime.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
		maxtime=new JLabel("0");
		maxtime.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
		l1.setFont(new Font("bold",Font.BOLD,12));
		l2.setFont(new Font("bold",Font.BOLD,12));
		l3.setFont(new Font("bold",Font.BOLD,12));
		l4.setFont(new Font("bold",Font.BOLD,12));
		l5.setFont(new Font("bold",Font.BOLD,12));
		l6.setFont(new Font("bold",Font.BOLD,12));
		l7.setFont(new Font("bold",Font.BOLD,12));
		l8.setFont(new Font("bold",Font.BOLD,12));
		l9.setFont(new Font("bold",Font.BOLD,12));
		l10.setFont(new Font("bold",Font.BOLD,12));
		l11.setFont(new Font("bold",Font.BOLD,12));
		l12.setFont(new Font("bold",Font.BOLD,12));		
	
		starttime=new JTextField(20);
		endtime=new JTextField(20);
		xaxis=new JTextField(20);
		yaxis=new JTextField(20);
		selectnode=new JComboBox();
		othernode=new JComboBox();
		slevel=new JComboBox();
		dlevel=new JComboBox();
		sentpack=new JComboBox();
		ackpack=new JComboBox();
		displaydetail=new JComboBox();
		displaydetail.addItemListener(this);
		graph=new JComboBox();
		create=new JButton("Display");
		create.addActionListener(this);
		
		
		Dimension dimension1=new Dimension(226,25);
		selectnode.setPreferredSize(dimension1);
		othernode.setPreferredSize(dimension1);
		slevel.setPreferredSize(dimension1);
		dlevel.setPreferredSize(dimension1);
		sentpack.setPreferredSize(dimension1);
		ackpack.setPreferredSize(dimension1);
		displaydetail.setPreferredSize(dimension1);
		graph.setPreferredSize(dimension1);
		
		gbc.gridy=1;
		gbc.gridx=0;
		parameter.add(l9,gbc);
		
		gbc.gridx=1;
		parameter.add(starttime,gbc);
		
		gbc.gridx=2;
		parameter.add(l10,gbc);
		
		gbc.gridx=3;
		parameter.add(endtime,gbc);
		
		gbc.gridy=2;
		gbc.gridx=1;
		parameter.add(mintime,gbc);
		
		gbc.gridx=3;
		parameter.add(maxtime,gbc);
		
		gbc.gridy=3;
		gbc.gridx=0;
		parameter.add(l1,gbc);
		
		gbc.gridx=1;
		parameter.add(selectnode,gbc);
		
		gbc.gridx=2;
		parameter.add(l2,gbc);
		
		gbc.gridx=3;
		parameter.add(othernode,gbc);
		
		gbc.gridy=4;
		gbc.gridx=0;
		parameter.add(l3,gbc);
		
		gbc.gridx=1;
		parameter.add(slevel,gbc);
		
		gbc.gridx=2;
		parameter.add(l4,gbc);
		
		gbc.gridx=3;
		parameter.add(dlevel,gbc);
		
		gbc.gridy=5;
		gbc.gridx=0;
		parameter.add(l5,gbc);
		
		gbc.gridx=1;
		parameter.add(sentpack,gbc);
		
		gbc.gridx=2;
		parameter.add(l6,gbc);
		
		gbc.gridx=3;
		parameter.add(ackpack,gbc);
		
		gbc.gridy=6;
		gbc.gridx=0;
		parameter.add(l7,gbc);
		
		gbc.gridx=1;
		parameter.add(displaydetail,gbc);
		
		gbc.gridx=2;
		parameter.add(l8,gbc);
		
		gbc.gridx=3;
		parameter.add(graph,gbc);
		
		gbc.gridy=7;
		gbc.gridx=0;
		parameter.add(l11,gbc);
		
		gbc.gridx=1;
		parameter.add(xaxis,gbc);
		
		gbc.gridx=2;
		parameter.add(l12,gbc);
		
		gbc.gridx=3;
		parameter.add(yaxis,gbc);
		
        JPanel panel1=new JPanel();
        Box baseBox=Box.createVerticalBox();
        
        displaySelectedItem=new JLabel(selectedItem);
        displaySelectedItem.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        displaySelectedItem.setForeground(Color.BLUE);
        
        JPanel panel2=new JPanel();
        panel2.add(create);
        panel1.add(baseBox);
        baseBox.add(displaySelectedItem);
        baseBox.add(parameter);
        baseBox.add(panel2);
        
		display=new JPanel();
		JScrollPane pane1=new JScrollPane(panel1);
		JScrollPane pane2=new JScrollPane(display);
		JSplitPane mainView1=new JSplitPane(JSplitPane.VERTICAL_SPLIT,false,pane1,pane2);
		mainView1.setDividerLocation(0);
		mainView1.setOneTouchExpandable(true);
		mainView1.setDividerSize(5);
		
		return mainView1;
	}
	
	//build status bar
	private JPanel buildStatusBar(){
		JPanel statusBar1=new JPanel();
		//statusField use to display the path of the trace file
		statusField = new JTextField();
		statusField.setFont(new Font("Bold",Font.BOLD,12));
        statusField.setEditable(false);
        statusField.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        statusField.setForeground(new Color(88,120,164));
        statusField.setText("Not open any trace file");

        statusBar1.setLayout(new GridBagLayout());
        statusBar1.setBorder(new CompoundBorder(new EmptyBorder(new Insets(4, 4, 4, 4)), new ShadowBorder()));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 8, 4, 4);
        statusBar1.add(statusField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new Insets(4, 0, 4, 6);
        statusBar1.add(new MemoryMonitor(), gridBagConstraints);
        
		return statusBar1;
		
	}
	
	/*
	 * when open a new trace file 
	 * the old data should be deleted
	 */
	
	public void deleteData(){
		try{		
		    Statement stm1=con.createStatement();	
			stm1.execute("delete from  "+tablePrefix+"normal_tr");
			stm1.execute("delete from  "+tablePrefix+"old_wireless_tr");
			stm1.execute("delete from  "+tablePrefix+"new_wireless_tr");
			stm1.execute("delete from  "+tablePrefix+"movement_tr");
			stm1.execute("delete from  "+tablePrefix+"energy_tr");
		}
		catch(Exception e1){
			System.out.println("Error occurred when delete data from tables!");
			e1.printStackTrace();
		}
	}
	/*
	 * when exit the software
	 * we should delete the tables we create in MySql
	 */
	public  void deleteTables(){
		try{		
		    Statement stm1=con.createStatement();	
			stm1.execute("drop table "+tablePrefix+"normal_tr");
			stm1.execute("drop table "+tablePrefix+"old_wireless_tr");
			stm1.execute("drop table "+tablePrefix+"new_wireless_tr");
			stm1.execute("drop table "+tablePrefix+"movement_tr");
			stm1.execute("drop table "+tablePrefix+"energy_tr");
		}
		catch(Exception e1){
			System.out.println("Error occurred when delete tables!");
			e1.printStackTrace();
		}
	}
	
    /*
     * test to connect ot mysql
     * let user input the username and password 
     * and then use them to connect to mysql
     */
	public void conToMySQL(){
		log=new JDialog(topFrame,"Login",true);
	    Container contentPane=log.getContentPane();
	    JPanel tip=new JPanel();
	    JLabel label;
	    System.out.println(getClass().getClassLoader().getResource("./"));
	    System.out.println(System.getProperty("user.dir"));
//	    URL url = getClass().getClassLoader().getResource("./icons/logo.gif"); 
//	    label=new JLabel("",new ImageIcon("icons/logo.gif"),JLabel.CENTER);
	    label=new JLabel("",new ImageIcon(getClass().getResource("/icons/logo.gif")),JLabel.CENTER);
//	    label=new JLabel("",new ImageIcon(Toolkit.getDefaultToolkit().getImage(url)),JLabel.CENTER);
	    label.setToolTipText("A java-based NS2 Trace Analyzer");
	    tip.add(label);
    
    
	    JPanel login=new JPanel();
	    login.setLayout(new GridBagLayout());
	    login.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),"Connect to MySQL:",TitledBorder.LEFT,TitledBorder.TOP,new Font("bold",Font.BOLD,16),Color.BLUE));
	    
	    GridBagConstraints gbc=new GridBagConstraints();
		gbc.anchor=GridBagConstraints.EAST;
		gbc.insets=new Insets(2,2,2,2);
		
	    JLabel inputuser=new JLabel("Username:");
	    JLabel inputpwd=new JLabel("Password:");
	    
	    username=new JTextField(16);
	    password=new JPasswordField(16);
	    
	    gbc.gridx=0;
	    gbc.gridy=0;
	    login.add(inputuser,gbc);
	    
	    gbc.gridx=1;
	    login.add(username,gbc);
	    
	    gbc.gridy=1;
	    gbc.gridx=0;
	    login.add(inputpwd,gbc);
	    
	    gbc.gridx=1;
	    login.add(password,gbc);
	    
	    JPanel panel2=new JPanel();
	    
	    JButton ok=new JButton("OK");
	    ok.addActionListener(this);
	    JButton exit=new JButton("Exit");
	    exit.addActionListener(this);
	    JPanel button=new JPanel();
	    button.add(ok);
	    button.add(exit);
	    log.getRootPane().setDefaultButton(ok);
	    error=new JLabel("press OK to connect to MySQL!",JLabel.CENTER);
	    error.setForeground(new Color(88,120,164));
	    error.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	    
	    panel2.setLayout(new GridLayout(2,1));
	    panel2.add(button);
	    panel2.add(error);
	    
	    contentPane.add(tip,BorderLayout.NORTH);
	    contentPane.add(login,BorderLayout.CENTER);
	    contentPane.add(panel2,BorderLayout.SOUTH);
	    
	    log.setSize(300,300);
	    Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		Dimension framesize=log.getSize();
		if(framesize.width>width)
			framesize.width=width;
		if(framesize.height>height)
			framesize.height=height;
		log.setLocation((width-framesize.width)/2,(height-framesize.height)/2);
		
		log.setVisible(true);
       
	}
	
	/*
	 * open a trace file
	 * import the data into MySql
	 * and initialize the parameters that will be selected
	 */
	public void open(){
		File file;
		JFileChooser filechooser=new JFileChooser();
		filechooser.setDialogTitle("Open a trace file");
		filechooser.addChoosableFileFilter(new MYFileFilter("tr"));
		filechooser.setFileView(new FileIcon());
		
		int result =filechooser.showOpenDialog(topFrame);
		if(result==JFileChooser.APPROVE_OPTION){
			deleteData();
			mainView.setDividerLocation(0);
			display.removeAll();
			display.revalidate();
			selectnode.setEnabled(true);
			othernode.setEnabled(true);
			slevel.setEnabled(true);
			dlevel.setEnabled(true);
			sentpack.setEnabled(true);
			ackpack.setEnabled(true);
			displaydetail.setEnabled(true);
			graph.setEnabled(true);
			xaxis.setText("");
			yaxis.setText("");
			DataRecognition.is_new_wireless=false;
			DataRecognition.is_normal=false;
			DataRecognition.is_old_wireless=false;
			DataRecognition.hasAGT=false;
			DataRecognition.hasRTR=false;
			DataRecognition.hasMAC=false;
			file=filechooser.getSelectedFile();

	        if(file.exists()){
	        	statusField.setText(file.getPath());
				new DataRecognition(con,topFrame,file,tablePrefix,statusField);
				if(DataRecognition.is_new_wireless){
					removeAllItemsOfJComboBox();
					/*
					 * initialize the parameters that will be selected
					 */
					String getmintime="select min(Time) from "+tablePrefix+"new_wireless_tr";
					String getmaxtime="select max(Time) from "+tablePrefix+"new_wireless_tr";
					String getnode="select distinct Ni from "+tablePrefix+"new_wireless_tr order by Ni";
					String getlevel="select distinct Nl from "+tablePrefix+"new_wireless_tr";
					String getpkttype="select distinct IP_It from "+tablePrefix+"new_wireless_tr";
					ResultSet dataset;
					Query query;
					query=new Query(con,getmintime);
					dataset=query.doQuery();
					if(dataset!=null){
						try {
							String time="0";
							while(dataset.next())
							   time=dataset.getString(1);
							mintime.setText(">="+time);
							starttime.setText(time);
						} catch (SQLException e1) {
							// TODO 自动生成 catch 块
							e1.printStackTrace();
						}
					}
					query=new Query(con,getmaxtime);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							String time="0";
							while(dataset.next())
								time=dataset.getString(1);
							maxtime.setText("<="+time);
							endtime.setText(time);
						}catch(SQLException e1){
							e1.printStackTrace();
						}
						
					}
					query=new Query(con,getnode);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							Object nodeid=null;
							while(dataset.next()){
								nodeid=(Object)dataset.getString(1);
								selectnode.addItem(nodeid);
								othernode.addItem(nodeid);
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					query=new Query(con,getlevel);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							Object level=null;
							while(dataset.next()){
								level=(Object)dataset.getString(1);
								slevel.addItem(level);
								dlevel.addItem(level);
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					query=new Query(con,getpkttype);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							Object type=null;
							while(dataset.next()){
							   type=(Object)dataset.getString(1);
							   if(type!=null){
							       sentpack.addItem(type);
							       ackpack.addItem(type);
							   }
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				    statusField.setText(file.getPath()+"----new wireless format");
				}
				else if(DataRecognition.is_normal){
					removeAllItemsOfJComboBox();
					/*
					 * initialize the parameters that will be selected
					 */
					String getmintime="select min(Time) from "+tablePrefix+"normal_tr";
					String getmaxtime="select max(Time) from "+tablePrefix+"normal_tr";
					String getnode="select distinct SN,DN from "+tablePrefix+"normal_tr ";
					String getpkttype="select distinct PN from "+tablePrefix+"normal_tr";
					ResultSet dataset;
					Query query;
					query=new Query(con,getmintime);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							String time="0";
							while(dataset.next())
								time=dataset.getString(1);
							mintime.setText(">="+time);	
							starttime.setText(time);
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					query=new Query(con,getmaxtime);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							String time="0";
							while(dataset.next())
								time=dataset.getString(1);
							maxtime.setText("<="+time);
							endtime.setText(time);
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					query=new Query(con,getnode);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							TreeSet node=new TreeSet();	
							while(dataset.next()){
								node.add((Object)dataset.getString(1));
								node.add((Object)dataset.getString(2));
							}
							Iterator it=node.iterator();
							while(it.hasNext()){
								Object nodeid=it.next();
								if(Integer.parseInt((String)nodeid)<0)
									continue;
								selectnode.addItem(nodeid);
								othernode.addItem(nodeid);
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					query=new Query(con,getpkttype);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							Object type=null;
							while(dataset.next()){
								type=dataset.getString(1);
								sentpack.addItem(type);
								ackpack.addItem(type);
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					 statusField.setText(file.getPath()+"----normal format");
				}
				else if(DataRecognition.is_old_wireless){
					removeAllItemsOfJComboBox();
					/*
					 * initialize the parameters that will be selected
					 */
					String getmintime="select min(Time) from "+tablePrefix+"old_wireless_tr";
					String getmaxtime="select max(Time) from "+tablePrefix+"old_wireless_tr";
					String getnode="select distinct NI from "+tablePrefix+"old_wireless_tr order by NI";
					String getlevel="select distinct TN from "+tablePrefix+"old_wireless_tr";
					String getpkttype="select distinct PT,Name from "+tablePrefix+"old_wireless_tr";
					ResultSet dataset;
					Query query;
					query=new Query(con,getmintime);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							String time="0";
							while(dataset.next())
								time=dataset.getString(1);
							mintime.setText(">="+time);
							starttime.setText(time);
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					query=new Query(con,getmaxtime);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							String time="0";
							while(dataset.next())
								time=dataset.getString(1);
							maxtime.setText("<="+time);
							endtime.setText(time);
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				    query=new Query(con,getnode);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							Object node=null;
							while(dataset.next()){
								node=(Object)dataset.getString(1);
								selectnode.addItem(node);
								othernode.addItem(node);
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					query=new Query(con,getlevel);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							Object level=null;
							while(dataset.next()){
								level=(Object)dataset.getString(1);
								if(!level.equals("MAC")&&!level.equals("RTR")&&!level.equals("AGT"))
									   continue;
								else{
								    slevel.addItem(level);
								    dlevel.addItem(level);
								}
							}						
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					query=new Query(con,getpkttype);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							Object type=null;
							while(dataset.next()){
							   type=(Object)dataset.getString(1);
							   if(type!=null){
							       sentpack.addItem(type);
							       ackpack.addItem(type);
							   }
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}				
					 statusField.setText(file.getPath()+"----old wireless format");
				}
				else
				     statusField.setText("Not open any trace file");
	        }
	        else{
	        	/*
	        	 * file not exists
	        	 */
	        	JOptionPane.showMessageDialog(null,file.getName()+" not exists!","ERROR",JOptionPane.ERROR_MESSAGE);
	        }			
		}
	}
	
	/*
	 * when open a new trace file 
	 * the old items of JComboBox should be deleted
	 */
	public void removeAllItemsOfJComboBox(){
		selectnode.removeAllItems();
		othernode.removeAllItems();
		slevel.removeAllItems();
		dlevel.removeAllItems();
		sentpack.removeAllItems();
		ackpack.removeAllItems();
		displaydetail.removeAllItems();
		graph.removeAllItems();
	}
	
	/*
	 * display simulation information of the whole network
	 */
	public void displaySimulationInfo(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		mainView.setDividerLocation(0);
		analyser=new SimulationInfo(con,tablePrefix);
		JScrollPane sp=new JScrollPane(analyser.getTableData());
		sp.setPreferredSize(new Dimension(775,310));
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Simulation Information");
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	
	/*
	 * display the energy of the whole network
	 */
	public void displayNetworkEnergy(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		try{
			Double.parseDouble(starttime.getText().trim());
			Double.parseDouble(endtime.getText().trim());
		}catch(NumberFormatException e){
			JOptionPane.showMessageDialog(null,"Start Time and End Time should be Numbers!","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		analyser=new NetworkEnergy(con,tablePrefix,starttime.getText().trim(),endtime.getText().trim(),xaxis.getText().trim(),yaxis.getText().trim());
		ChartPanel chartPanel=analyser.getChartPanel();
//		System.out.println(analyser.getXYSeriesCollection().getSeriesCount());
		if(chartPanel==null){
			display.removeAll();
			display.repaint();
			Box baseBox=Box.createVerticalBox();
			JLabel title=new JLabel("No energy entries");			
			display.add(baseBox);
			title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			baseBox.add(title);
			display.revalidate();
		}
		else{
			JScrollPane sp=new JScrollPane(chartPanel);
			Box baseBox=Box.createVerticalBox();
			display.removeAll();
			display.repaint();
			display.add(baseBox);
			JLabel title=new JLabel("Energy remained of the whole network");
			title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			baseBox.add(title);
			baseBox.add(sp);
			display.revalidate();
		}
	}
	/*
	 * display the traffic of the whole network
	 */
	public void displayTraffic(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		try{
			Double.parseDouble(starttime.getText().trim());
			Double.parseDouble(endtime.getText().trim());
		}catch(NumberFormatException e){
			JOptionPane.showMessageDialog(null,"Start Time and End Time should be Numbers!","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		analyser=new TrafficAnalyse(con,tablePrefix,starttime.getText().trim(),endtime.getText().trim(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		JScrollPane sp=new JScrollPane(analyser.getChartPanel());
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Traffic");
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	
	/*
	 * display the simulation information of the selected node
	 */
	public void displayNodeInfo(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		analyser=new NodeInfo(con,tablePrefix,Integer.parseInt((String) selectnode.getSelectedItem()));
		JScrollPane sp=new JScrollPane(analyser.getTableData());
		sp.setPreferredSize(new Dimension(775,230));
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Simulation Information of node "+(String)selectnode.getSelectedItem());
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	/*
	 * display the movement record of the selected node between the selected time interval
	 */
	public void displayNodeMovement(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		try{
			Double.parseDouble(starttime.getText().trim());
			Double.parseDouble(endtime.getText().trim());
		}catch(NumberFormatException e){
			JOptionPane.showMessageDialog(null,"Start Time and End Time should be Numbers!","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		analyser=new NodeMovement(con,tablePrefix,starttime.getText(),endtime.getText(),Integer.parseInt((String) selectnode.getSelectedItem()));
		JTable table=analyser.getTableData();
		JScrollPane sp=new JScrollPane(table);
//		sp.setPreferredSize(new Dimension(765,table.getRowCount()*15));
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Movement of node "+(String)selectnode.getSelectedItem());
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	/*
	 * display packet id
	 */
	public void displayPID(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		try{
			Double.parseDouble(starttime.getText().trim());
			Double.parseDouble(endtime.getText().trim());
		}catch(NumberFormatException e){
			JOptionPane.showMessageDialog(null,"Start Time and End Time should be Numbers!","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		analyser=new PacketID(con,tablePrefix,Integer.parseInt((String) selectnode.getSelectedItem()),starttime.getText(),endtime.getText(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		JScrollPane sp=new JScrollPane(analyser.getChartPanel());
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Packet ID of node "+(String)selectnode.getSelectedItem());
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	/*
	 * display throughput
	 */
	public void displayThroughput(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		try{
			Double.parseDouble(starttime.getText().trim());
			Double.parseDouble(endtime.getText().trim());
		}catch(NumberFormatException e){
			JOptionPane.showMessageDialog(null,"Start Time and End Time should be Numbers!","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		analyser=new Throughput(con,tablePrefix,Integer.parseInt((String) selectnode.getSelectedItem()),starttime.getText(),endtime.getText(),1,(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		JScrollPane sp=new JScrollPane(analyser.getChartPanel());
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Throughput of node "+(String)selectnode.getSelectedItem());
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	/*
	 * display the energy of a node
	 */
	public void displayNodeEnergy(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		try{
			Double.parseDouble(starttime.getText().trim());
			Double.parseDouble(endtime.getText().trim());
		}catch(NumberFormatException e){
			JOptionPane.showMessageDialog(null,"Start Time and End Time should be Numbers!","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		analyser=new NodeEnergy(con,tablePrefix,Integer.parseInt((String) selectnode.getSelectedItem()),starttime.getText().trim(),endtime.getText().trim(),xaxis.getText().trim(),yaxis.getText().trim());
		ChartPanel chartPanel=analyser.getChartPanel();
//		System.out.println(analyser.getXYSeriesCollection().getSeriesCount());
		if(chartPanel==null){
			display.removeAll();
			display.repaint();
			Box baseBox=Box.createVerticalBox();
			JLabel title=new JLabel("No energy entries");			
			display.add(baseBox);
			title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			baseBox.add(title);
			display.revalidate();
		}
		else{
			JScrollPane sp=new JScrollPane(chartPanel);
			Box baseBox=Box.createVerticalBox();
			display.removeAll();
			display.repaint();
			display.add(baseBox);
			JLabel title=new JLabel("Energy remained of Node"+selectnode.getSelectedItem());
			title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			baseBox.add(title);
			baseBox.add(sp);
			display.revalidate();
		}
	}
	/*
	 * display sequence number
	 */
	public void displaySequenceNumber(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		try{
			Double.parseDouble(starttime.getText().trim());
			Double.parseDouble(endtime.getText().trim());
		}catch(NumberFormatException e){
			JOptionPane.showMessageDialog(null,"Start Time and End Time should be Numbers!","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		analyser=new SequenceNumber(con,tablePrefix,Integer.parseInt((String) selectnode.getSelectedItem()),starttime.getText(),endtime.getText(),(String)sentpack.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		JScrollPane sp=new JScrollPane(analyser.getChartPanel());
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel((String)sentpack.getSelectedItem()+" Sequence Number of node "+(String)selectnode.getSelectedItem());
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	/*
	 * display packet id vs delay
	 */
	public void displayEndToEndDelay(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		if(DataRecognition.is_normal)
			analyser=new PacketIDDelay(con,tablePrefix,(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		else
			analyser=new PacketIDDelay(con,tablePrefix,(String)slevel.getSelectedItem(),(String)dlevel.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		JScrollPane sp=new JScrollPane(analyser.getChartPanel());
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Packet ID VS END TO END Delay");
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	public void displayCNToONDelay(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		if(DataRecognition.is_normal)
			analyser=new PacketIDDelay(con,tablePrefix,Integer.parseInt((String) selectnode.getSelectedItem()),Integer.parseInt((String) othernode.getSelectedItem()),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		else
			analyser=new PacketIDDelay(con,tablePrefix,(String)selectnode.getSelectedItem(),(String)othernode.getSelectedItem(),(String)slevel.getSelectedItem(),(String)dlevel.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		JScrollPane sp=new JScrollPane(analyser.getChartPanel());
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Packet ID VS CN TO ON Delay");
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	public void displaySizeEndToEndDelay(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		if(DataRecognition.is_normal)
			analyser=new PacketSizeDelay(con,tablePrefix,(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		else
			analyser=new PacketSizeDelay(con,tablePrefix,(String)slevel.getSelectedItem(),(String)dlevel.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		JScrollPane sp=new JScrollPane(analyser.getChartPanel());
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Packet size VS END TO END Delay");
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	public void displaySizeCNToONDelay(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		if(DataRecognition.is_normal)
			analyser=new PacketSizeDelay(con,tablePrefix,Integer.parseInt((String) selectnode.getSelectedItem()),Integer.parseInt((String) othernode.getSelectedItem()),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		else
			analyser=new PacketSizeDelay(con,tablePrefix,(String)selectnode.getSelectedItem(),(String)othernode.getSelectedItem(),(String)slevel.getSelectedItem(),(String)dlevel.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		JScrollPane sp=new JScrollPane(analyser.getChartPanel());
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Packet Size VS CN TO ON Delay");
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	/*
	 * display RTT
	 */
	public void displayPacketIDRTT(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		if(DataRecognition.is_normal)
			analyser=new PacketIDRTT(con,tablePrefix,(String)sentpack.getSelectedItem(),(String)ackpack.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		else
			analyser=new PacketIDRTT(con,tablePrefix,(String)slevel.getSelectedItem(),(String)dlevel.getSelectedItem(),(String)sentpack.getSelectedItem(),(String)ackpack.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		JScrollPane sp=new JScrollPane(analyser.getChartPanel());
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Packet ID VS Simulation RTT");
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	public void displayPacketIDCNToONRTT(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		if(DataRecognition.is_normal)
			analyser=new PacketIDRTT(con,tablePrefix,Integer.parseInt((String) selectnode.getSelectedItem()),Integer.parseInt((String) othernode.getSelectedItem()),(String)sentpack.getSelectedItem(),(String)ackpack.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		else
			analyser=new PacketIDRTT(con,tablePrefix,(String)selectnode.getSelectedItem(),(String)othernode.getSelectedItem(),(String)slevel.getSelectedItem(),(String)dlevel.getSelectedItem(),(String)sentpack.getSelectedItem(),(String)ackpack.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		JScrollPane sp=new JScrollPane(analyser.getChartPanel());
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Packet ID VS RTT between CN and ON");
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	public void displaySizeRTT(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		if(DataRecognition.is_normal)
			analyser=new PacketSizeRTT(con,tablePrefix,(String)sentpack.getSelectedItem(),(String)ackpack.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		else
			analyser=new PacketSizeRTT(con,tablePrefix,(String)slevel.getSelectedItem(),(String)dlevel.getSelectedItem(),(String)sentpack.getSelectedItem(),(String)ackpack.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		JScrollPane sp=new JScrollPane(analyser.getChartPanel());
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Packet size VS RTT");
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	public void displaySizeCNToONRTT(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		if(DataRecognition.is_normal)
			analyser=new PacketSizeRTT(con,tablePrefix,Integer.parseInt((String) selectnode.getSelectedItem()),Integer.parseInt((String) othernode.getSelectedItem()),(String)sentpack.getSelectedItem(),(String)ackpack.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		else
			analyser=new PacketSizeRTT(con,tablePrefix,(String)selectnode.getSelectedItem(),(String)othernode.getSelectedItem(),(String)slevel.getSelectedItem(),(String)dlevel.getSelectedItem(),(String)sentpack.getSelectedItem(),(String)ackpack.getSelectedItem(),(String)displaydetail.getSelectedItem(),(String)graph.getSelectedItem(),xaxis.getText().trim(),yaxis.getText().trim());
		JScrollPane sp=new JScrollPane(analyser.getChartPanel());
		Box baseBox=Box.createVerticalBox();
		display.removeAll();
		display.repaint();
		display.add(baseBox);
		JLabel title=new JLabel("Packet Size VS RTT between CN and ON");
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		baseBox.add(title);
		baseBox.add(sp);
		display.revalidate();
	}
	public void executeSQL(){
		if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
			return;
		analyser=new ExecuteSQLScript(con,topFrame,tablePrefix);
		if(analyser.getDataType().equals("JTabel")){
			JTable table=analyser.getTableData();
			JScrollPane sp=new JScrollPane(table);
//			sp.setPreferredSize(new Dimension(765,table.getRowCount()*15));
			Box baseBox=Box.createVerticalBox();
			display.removeAll();
			display.repaint();
			display.add(baseBox);
			JLabel title=new JLabel(analyser.getTitle());
			title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			baseBox.add(title);
			baseBox.add(sp);
			display.revalidate();
		}
		if(analyser.getDataType().equals("XYSeriesCollection")){
			JScrollPane sp=new JScrollPane(analyser.getChartPanel());
			Box baseBox=Box.createVerticalBox();
			display.removeAll();
			display.repaint();
			display.add(baseBox);
			JLabel title=new JLabel(analyser.getTitle());
			title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			baseBox.add(title);
			baseBox.add(sp);
			display.revalidate();
		}
	}
	/**
	 * @param e
	 */
	public void itemStateChanged(ItemEvent e) {
		// TODO 自动生成方法存根
		if(e.getStateChange()==ItemEvent.SELECTED){
			if(e.getItem().equals("Packet size at diffrent time")){
				xaxis.setText("Time in seconds");
				yaxis.setText("Traffic in Bytes");
				graph.removeAllItems();
				graph.addItem("Line Chart");
			}
			else if(e.getItem().equals("Packet num of every type")){
				xaxis.setText("Packet Type");
				yaxis.setText("Packet Num");
				graph.removeAllItems();
				graph.addItem("Pie Chart");
				graph.addItem("3D Pie Chart");
				graph.addItem("Bar Chart");
				graph.addItem("3D Bar Chart");
			}
			else if(e.getItem().equals("IDs of generated packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("generated Packets ID");
			}
			else if(e.getItem().equals("IDs of sent packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("sent Packets ID");
			}
			else if(e.getItem().equals("IDs of received packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("received Packets ID");
			}
			else if(e.getItem().equals("IDs of forwarded packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("forwarded Packets ID");
			}
			else if(e.getItem().equals("IDs of dropped packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("dropped Packets ID");
			}
			else if(e.getItem().equals("Throughput of generating packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("Throughput of generating packets [NO. Packets/S]");
			}
			else if(e.getItem().equals("Throughput of sending packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("Throughput of sending packets [NO. Packets/S]");
			}
			else if(e.getItem().equals("Throughput of receiving packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("Throughput of receiving packets [NO. Packets/S]");
			}
			else if(e.getItem().equals("Throughput of forwarding packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("Throughput of forwarding packets [NO. Packets/S]");
			}
			else if(e.getItem().equals("Throughput of dropping packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("Throughput of dropping packets [NO. Packets/S]");
			}
			else if(e.getItem().equals("Packet ID vs END TO END Delay")){
				flag="Packet ID vs END TO END Delay";
				xaxis.setText("Packet ID");
				yaxis.setText("End To End Delay [sec]");
			}
			else if(e.getItem().equals("Packets ID vs CN TO ON Delay")){
				flag="Packets ID vs CN TO ON Delay";
				selectnode.setEnabled(true);
				othernode.setEnabled(true);
				xaxis.setText("Packet ID");
				yaxis.setText("Current Node To Other Node Delay [sec]");
			}
			else if(e.getItem().equals("Packet size vs minimal End To End Delay")||
					 e.getItem().equals("Packet size vs maximal End To End Delay")||
					 e.getItem().equals("Packet size vs average End To End Delay")){
				flag="Packet size vs End To End Delay";
				xaxis.setText("Packet Size");
				if(e.getItem().equals("Packet size vs minimal End To End Delay"))
					yaxis.setText("Minimal End To End Delay [sec]");
				if(e.getItem().equals("Packet size vs maximal End To End Delay"))
					yaxis.setText("Maximal End To End Delay [sec]");
				if(e.getItem().equals("Packet size vs average End To End Delay"))
					yaxis.setText("Average End To End Delay [sec]");
			}
			else if(e.getItem().equals("Packet size vs minimal CN To ON Delay")||
					 e.getItem().equals("Packet size vs maximal CN To ON Delay")||
					 e.getItem().equals("Packet size vs average CN To ON Delay")){
				flag="Packet size vs CN TO ON Delay";
				selectnode.setEnabled(true);
				othernode.setEnabled(true);
				xaxis.setText("Packet Size");
				if(e.getItem().equals("Packet size vs minimal CN To ON Delay"))
					yaxis.setText("Minimal CN To ON Delay [sec]");
				if(e.getItem().equals("Packet size vs maximal CN To ON Delay"))
					yaxis.setText("Maximal CN To ON Delay [sec]");
				if(e.getItem().equals("Packet size vs average CN To ON Delay"))
					yaxis.setText("Average CN To ON Delay [sec]");
			}
			else if(e.getItem().equals("Packet ID vs simulation RTT")){
				flag="Packet ID vs simulation RTT";
				xaxis.setText("Packet ID");
				yaxis.setText("Simulation RTT [sec]");
			}
			else if(e.getItem().equals("Packet ID vs RTT between CN and ON")){
				flag="Packet ID vs RTT between CN and ON";
				selectnode.setEnabled(true);
				othernode.setEnabled(true);
				xaxis.setText("Packet ID");
				yaxis.setText("RTT between CN and ON [sec]");
			}
//			displaydetail.addItem("Sequence Number of generating packets at CN");
//			displaydetail.addItem("Sequence Number of sending packets at CN");
//			displaydetail.addItem("Sequence Number receiving packets at CN");
//			displaydetail.addItem("Sequence Number forwarding packets at CN");
//			displaydetail.addItem("Sequence Number of dropping packets at CN");
			else if(e.getItem().equals("Sequence Number of generating packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("Sequence Number of generating Packets");
			}
			else if(e.getItem().equals("Sequence Number of sending packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("Sequence Number of sending Packets");
			}
			else if(e.getItem().equals("Sequence Number of receiving packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("Sequence Number of receiving Packets");
			}
			else if(e.getItem().equals("Sequence Number of forwarding packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("Sequence Number of forwarding Packets");
			}
			else if(e.getItem().equals("Sequence Number of dropping packets at CN")){
				xaxis.setText("Time");
				yaxis.setText("Sequence Number of dropping Packets");
			}
			else if(e.getItem().equals("Packet size vs minimal RTT")||
					 e.getItem().equals("Packet size vs maximal RTT")||
					 e.getItem().equals("Packet size vs average RTT")){
				flag="Packet size vs RTT";
				xaxis.setText("Packet Size");
				if(e.getItem().equals("Packet size vs minimal RTT"))
					yaxis.setText("Minimal RTT [sec]");
				if(e.getItem().equals("Packet size vs maximal RTT"))
					yaxis.setText("Maximal RTT [sec]");
				if(e.getItem().equals("Packet size vs average RTT"))
					yaxis.setText("Average RTT [sec]");
			}
			else if(e.getItem().equals("Packet size vs minimal RTT between CN and ON")||
					 e.getItem().equals("Packet size vs maximal RTT between CN and ON")||
					 e.getItem().equals("Packet size vs average RTT between CN and ON")){
				flag="Packet size vs RTT between CN and ON";
				selectnode.setEnabled(true);
				othernode.setEnabled(true);
				xaxis.setText("Packet Size");
				if(e.getItem().equals("Packet size vs minimal RTT between CN and ON"))
					yaxis.setText("Minimal RTT between CN and ON [sec]");
				if(e.getItem().equals("Packet size vs maximal RTT between CN and ON"))
					yaxis.setText("Maximal RTT between CN and ON [sec]");
				if(e.getItem().equals("Packet size vs average RTT between CN and ON"))
					yaxis.setText("Average RTT between CN and ON [sec]");
			}
		}
	}
	/*
	 *  （not Javadoc）
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e){
		/*
		 * test to connect to mySql
		 */
		if(e.getActionCommand()=="OK"){
			name=username.getText();
			usrpwd=new String(password.getPassword());
			connector=new DBConnection(url,name,usrpwd);
			con=connector.makeConnection();
			if(con==null){
				connected=false;
				error.setText("Wrong username or password! Please retry!");
			}
			else{
				connected=true;
				log.dispose();
				new CreateTables(con,tablePrefix);
				topFrame.setVisible(true);			
			}			
		}
		/*
		 * open a trace file and import the data to mysql
		 */
		if(e.getActionCommand()=="Open"){
			open();
		}
		/*
		 * display simulation information
		 */
		if(e.getActionCommand()=="Simulation Information"){
			displaySimulationInfo();
		}
		/*
		 * display the energy of the whole network
		 */
		if(e.getActionCommand()=="Energy Remained"){
			mainView.setDividerLocation(0.4);
			flag="Energy Remained";
			selectedItem="Set time to display the energy remained of the whole network";
			displaySelectedItem.setText(selectedItem);
			xaxis.setText("Time in seconds");
			yaxis.setText("Energy remained");
			starttime.setEnabled(true);
			endtime.setEnabled(true);
			xaxis.setEnabled(true);
			yaxis.setEnabled(true);
			selectnode.setEnabled(false);
			othernode.setEnabled(false);
			slevel.setEnabled(false);
			dlevel.setEnabled(false);
			sentpack.setEnabled(false);
			ackpack.setEnabled(false);
			displaydetail.setEnabled(false);
			graph.setEnabled(false);
		}
		/*
		 * display the traffic of the whole network in bytes
		 */
		if(e.getActionCommand()=="Packet Stat."){
			mainView.setDividerLocation(0.4);
			flag="Packet Stat.";
			selectedItem="Set time to display the traffic of the whole network";
			displaySelectedItem.setText(selectedItem);
			starttime.setEnabled(true);
			endtime.setEnabled(true);
			selectnode.setEnabled(false);
			othernode.setEnabled(false);
			slevel.setEnabled(false);
			dlevel.setEnabled(false);
			sentpack.setEnabled(false);
			ackpack.setEnabled(false);
			displaydetail.setEnabled(true);
			graph.setEnabled(true);
			xaxis.setEnabled(true);
			yaxis.setEnabled(true);
			displaydetail.removeAllItems();
			displaydetail.addItem("Packet size at diffrent time");
			displaydetail.addItem("Packet num of every type");
//			graph.addItem("Line Chart");
		}
		/*
		 * display the simulation information of the selected node
		 */
		if(e.getActionCommand()=="Current Node Information"){
			mainView.setDividerLocation(0.4);
			flag="Current Node Information";
			selectedItem="Please select a node ";
			displaySelectedItem.setText(selectedItem);
			starttime.setEnabled(false);
			endtime.setEnabled(false);
			selectnode.setEnabled(true);
			othernode.setEnabled(false);
			slevel.setEnabled(false);
			dlevel.setEnabled(false);
			sentpack.setEnabled(false);
			ackpack.setEnabled(false);
			displaydetail.setEnabled(false);
			graph.setEnabled(false);
			xaxis.setEnabled(false);
			yaxis.setEnabled(false);
		}
		/*
		 * display the movement record of the selected node between the selected time interval
		 */
		if(e.getActionCommand()=="Display Movement"){
			mainView.setDividerLocation(0.4);
			flag="Display Movement";
			selectedItem="Please set the parameters to display a node's movement ";
			displaySelectedItem.setText(selectedItem);
			starttime.setEnabled(true);
			endtime.setEnabled(true);
			selectnode.setEnabled(true);
			othernode.setEnabled(false);
			slevel.setEnabled(false);
			dlevel.setEnabled(false);
			sentpack.setEnabled(false);
			ackpack.setEnabled(false);
			displaydetail.setEnabled(false);
			graph.setEnabled(false);
			xaxis.setEnabled(false);
			yaxis.setEnabled(false);
		}
		/*
		 * display packets id
		 */
		if(e.getActionCommand()=="Packets ID"){
			mainView.setDividerLocation(0.4);
			flag="Packet ID";
			selectedItem="Please set the parameters to display Packet ID ";
			displaySelectedItem.setText(selectedItem);
			starttime.setEnabled(true);
			endtime.setEnabled(true);
			selectnode.setEnabled(true);
			othernode.setEnabled(false);
			slevel.setEnabled(false);
			dlevel.setEnabled(false);
			sentpack.setEnabled(false);
			ackpack.setEnabled(false);
			displaydetail.setEnabled(true);
			graph.setEnabled(true);
			xaxis.setEnabled(true);
			yaxis.setEnabled(true);
			displaydetail.removeAllItems();
			displaydetail.addItem("IDs of generated packets at CN");
			displaydetail.addItem("IDs of sent packets at CN");
			displaydetail.addItem("IDs of received packets at CN");
			displaydetail.addItem("IDs of forwarded packets at CN");
			displaydetail.addItem("IDs of dropped packets at CN");
			graph.removeAllItems();
			graph.addItem("Line Chart");
			
		}
		/*
		 * display throughput
		 */
		if(e.getActionCommand()=="Throughput"){
			mainView.setDividerLocation(0.4);
			flag="Throughput";
			selectedItem="Please set the parameters to display Throughput ";
			displaySelectedItem.setText(selectedItem);
			starttime.setEnabled(true);
			endtime.setEnabled(true);
			selectnode.setEnabled(true);
			othernode.setEnabled(false);
			slevel.setEnabled(false);
			dlevel.setEnabled(false);
			sentpack.setEnabled(false);
			ackpack.setEnabled(false);
			displaydetail.setEnabled(true);
			graph.setEnabled(true);
			xaxis.setEnabled(true);
			yaxis.setEnabled(true);
			displaydetail.removeAllItems();
			displaydetail.addItem("Throughput of generating packets at CN");
			displaydetail.addItem("Throughput of sending packets at CN");
			displaydetail.addItem("Throughput of receiving packets at CN");
			displaydetail.addItem("Throughput of forwarding packets at CN");
			displaydetail.addItem("Throughput of dropping packets at CN");
			graph.removeAllItems();
			graph.addItem("Line Chart");
			graph.addItem("Bar Chart");
			graph.addItem("3D Bar Chart");
		}
		/*
		 * display the energy of a node
		 */
		if(e.getActionCommand()=="Node Energy"){
			mainView.setDividerLocation(0.4);
			flag="Node Energy";
			selectedItem="Please set the parameters to display the Energy of a node ";
			displaySelectedItem.setText(selectedItem);
			starttime.setEnabled(true);
			endtime.setEnabled(true);
			selectnode.setEnabled(true);
			othernode.setEnabled(false);
			slevel.setEnabled(false);
			dlevel.setEnabled(false);
			sentpack.setEnabled(false);
			ackpack.setEnabled(false);
			displaydetail.setEnabled(false);
			graph.setEnabled(false);
			xaxis.setEnabled(true);
			yaxis.setEnabled(true);
			xaxis.setText("Time");
			yaxis.setText("Energy of Node");
		}
		/*
		 * display sequence number
		 */
		if(e.getActionCommand()=="Sequence Number"){
			mainView.setDividerLocation(0.4);
			flag="Sequence Number";
			selectedItem="Please set the parameters to display the Sequence Number of a node ";
			displaySelectedItem.setText(selectedItem);
			starttime.setEnabled(true);
			endtime.setEnabled(true);
			selectnode.setEnabled(true);
			othernode.setEnabled(false);
			slevel.setEnabled(false);
			dlevel.setEnabled(false);
			sentpack.setEnabled(true);
			ackpack.setEnabled(false);
			displaydetail.setEnabled(true);
			graph.setEnabled(true);
			xaxis.setEnabled(true);
			yaxis.setEnabled(true);
			displaydetail.removeAllItems();
			displaydetail.addItem("Sequence Number of generating packets at CN");
			displaydetail.addItem("Sequence Number of sending packets at CN");
			displaydetail.addItem("Sequence Number receiving packets at CN");
			displaydetail.addItem("Sequence Number forwarding packets at CN");
			displaydetail.addItem("Sequence Number of dropping packets at CN");
			graph.removeAllItems();
			graph.addItem("Line Chart");
//			xaxis.setText("Time");
//			yaxis.setText("Energy of Node");
		}
		/*
		 * display end to end delay
		 */
		if(e.getActionCommand()=="Packet ID vs Delay"){
			mainView.setDividerLocation(0.4);
			selectedItem="Please set the parameters to display End To End Delay ";
			displaySelectedItem.setText(selectedItem);
			starttime.setEnabled(false);
			endtime.setEnabled(false);
			selectnode.setEnabled(false);
			othernode.setEnabled(false);
			if(DataRecognition.is_normal){
				slevel.setEnabled(false);
				dlevel.setEnabled(false);
			}
			else{
				slevel.setEnabled(true);
				dlevel.setEnabled(true);
			}
			sentpack.setEnabled(false);
			ackpack.setEnabled(false);
			displaydetail.setEnabled(true);
			graph.setEnabled(true);
			xaxis.setEnabled(true);
			yaxis.setEnabled(true);
			displaydetail.removeAllItems();
			displaydetail.addItem("Packet ID vs END TO END Delay");
			displaydetail.addItem("Packets ID vs CN TO ON Delay");
			graph.removeAllItems();
			graph.addItem("Line Chart");
		}
		if(e.getActionCommand()=="Packet Size vs Delay"){
			mainView.setDividerLocation(0.4);
			selectedItem="Please set the parameters to display End To End Delay ";
			displaySelectedItem.setText(selectedItem);
			starttime.setEnabled(false);
			endtime.setEnabled(false);
			selectnode.setEnabled(false);
			othernode.setEnabled(false);
			if(DataRecognition.is_normal){
				slevel.setEnabled(false);
				dlevel.setEnabled(false);
			}
			else{
				slevel.setEnabled(true);
				dlevel.setEnabled(true);
			}
			sentpack.setEnabled(false);
			ackpack.setEnabled(false);
			displaydetail.setEnabled(true);
			graph.setEnabled(true);
			xaxis.setEnabled(true);
			yaxis.setEnabled(true);
			displaydetail.removeAllItems();
			displaydetail.addItem("Packet size vs minimal End To End Delay");
			displaydetail.addItem("Packet size vs maximal End To End Delay");
			displaydetail.addItem("Packet size vs average End To End Delay");
			displaydetail.addItem("Packet size vs minimal CN To ON Delay");
			displaydetail.addItem("Packet size vs maximal CN To ON Delay");
			displaydetail.addItem("Packet size vs average CN To ON Delay");
			graph.removeAllItems();
			graph.addItem("Line Chart");
		}
		/*
		 * to display RTT
		 */
		if(e.getActionCommand()=="Sent Packet ID vs RTT"){
			mainView.setDividerLocation(0.4);
			selectedItem="Please set the parameters to display RTT ";
			displaySelectedItem.setText(selectedItem);
			starttime.setEnabled(false);
			endtime.setEnabled(false);
			selectnode.setEnabled(false);
			othernode.setEnabled(false);
			if(DataRecognition.is_normal){
				slevel.setEnabled(false);
				dlevel.setEnabled(false);
			}
			else{
				slevel.setEnabled(true);
				dlevel.setEnabled(true);
			}
			sentpack.setEnabled(true);
			ackpack.setEnabled(true);
			displaydetail.setEnabled(true);
			graph.setEnabled(true);
			xaxis.setEnabled(true);
			yaxis.setEnabled(true);
			displaydetail.removeAllItems();
			displaydetail.addItem("Packet ID vs simulation RTT");
			displaydetail.addItem("Packet ID vs RTT between CN and ON");
			graph.removeAllItems();
			graph.addItem("Line Chart");
		}
		if(e.getActionCommand()=="Packet Size vs RTT"){
			mainView.setDividerLocation(0.4);
			selectedItem="Please set the parameters to display RTT ";
			displaySelectedItem.setText(selectedItem);
			starttime.setEnabled(false);
			endtime.setEnabled(false);
			selectnode.setEnabled(false);
			othernode.setEnabled(false);
			if(DataRecognition.is_normal){
				slevel.setEnabled(false);
				dlevel.setEnabled(false);
			}
			else{
				slevel.setEnabled(true);
				dlevel.setEnabled(true);
			}
			sentpack.setEnabled(true);
			ackpack.setEnabled(true);
			displaydetail.setEnabled(true);
			graph.setEnabled(true);
			xaxis.setEnabled(true);
			yaxis.setEnabled(true);
			displaydetail.removeAllItems();
			displaydetail.addItem("Packet size vs minimal RTT");
			displaydetail.addItem("Packet size vs maximal RTT");
			displaydetail.addItem("Packet size vs average RTT");
			displaydetail.addItem("Packet size vs minimal RTT between CN and ON");
			displaydetail.addItem("Packet size vs maximal RTT between CN and ON");
			displaydetail.addItem("Packet size vs average RTT between CN and ON");
			graph.removeAllItems();
			graph.addItem("Line Chart");
		}
		/*
		 * extend
		 */
		if(e.getActionCommand()=="Excute SQLscript"){
			mainView.setDividerLocation(0);
			executeSQL();
		}
		if(e.getActionCommand()=="Display"){
			if(flag.equals("Energy Remained"))
				displayNetworkEnergy();
			if(flag.equals("Packet Stat."))
				displayTraffic();
			if(flag.equals("Current Node Information"))
				displayNodeInfo();
			if(flag.equals("Display Movement"))
				displayNodeMovement();
			if(flag.equals("Packet ID"))
				displayPID();
			if(flag.equals("Throughput"))
				displayThroughput();
			if(flag.equals("Node Energy"))
				displayNodeEnergy();
			if(flag.equals("Sequence Number"))
				displaySequenceNumber();
			if(flag.equals("Packet ID vs END TO END Delay"))
				displayEndToEndDelay();
			if(flag.equals("Packets ID vs CN TO ON Delay"))
				displayCNToONDelay();
			if(flag.equals("Packet size vs End To End Delay"))
				displaySizeEndToEndDelay();
			if(flag.equals("Packet size vs CN TO ON Delay"))
				displaySizeCNToONDelay();
			if(flag.equals("Packet ID vs simulation RTT"))
				displayPacketIDRTT();
			if(flag.equals("Packet ID vs RTT between CN and ON"))
				displayPacketIDCNToONRTT();
			if(flag.equals("Packet size vs RTT"))
				displaySizeRTT();
			if(flag.equals("Packet size vs RTT between CN and ON"))
				displaySizeCNToONRTT();
		}
		/*
		 * Save the chart as a image file 
		 */
		if(e.getActionCommand()=="Save as"){
			if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
				;
			else
				new SaveAs(topFrame,analyser);
		}
		/*
		 * export the data to a file
		 */
		if(e.getActionCommand()=="Export to File"){
			if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
				;
			else
				new ExportToFile(topFrame,analyser);
		}
		if(e.getActionCommand()=="Exit"){
			if(connected){
				deleteTables();
				try {					
					con.close();					
				} catch (SQLException e1) {	}					
			}
			System.exit(0);//not only do this ,should do more things, those codes will be written later
		}
		else if(e.getActionCommand()=="About")
			new Help(topFrame);
	}
	
	/*
	 * toolbar will use this class
	 */
	class ToolBarAction extends AbstractAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 7144523459796473772L;
		public ToolBarAction(String name,Icon icon){
			super(name,icon);
		}
		public void actionPerformed(ActionEvent e){
			if(e.getActionCommand()=="Open"){
				open();
			}
			if(e.getActionCommand()=="Save as")
				new SaveAs(topFrame,analyser);
			if(e.getActionCommand()=="Export to File"){
				if(!DataRecognition.is_new_wireless&&!DataRecognition.is_old_wireless&&!DataRecognition.is_normal)
					;
				else
					new ExportToFile(topFrame,analyser);
			}
			if(e.getActionCommand()=="Excute SQLscript"){
				mainView.setDividerLocation(0);
				executeSQL();
			}
			if(e.getActionCommand()=="About")
				new Help(topFrame);
		}
	}	
}
