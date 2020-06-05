package src.operation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.Timer;


public class DataRecognition implements ActionListener{
	public String tablePrefix;
	public Connection con;
	private JDialog progress;
	private JProgressBar progressBar;
	private JLabel label;
	private Timer timer;
	private JButton cancel;
	public  File file;
	private int rowNumber;
	private JTextField statusField;
	private int handledRowNum=0;
	public static boolean is_normal=false;
	public static boolean is_old_wireless=false;
	public static boolean is_new_wireless=false;
	public static boolean hasAGT=false;
	public static boolean hasRTR=false;
	public static boolean hasMAC=false;
	public boolean is_right_format=true;
	Thread writeThread1;
	Thread writeThread2;
	Thread writeThread3;
	Thread writeThread4;
	Thread writeThread5;
	
	public DataRecognition(Connection con,JFrame frame,File file,String tablePrefix,JTextField statusField){
		this.file=file;
		this.tablePrefix=tablePrefix;
		this.con=con;
		this.statusField=statusField;
		progress=new JDialog(frame,"Data Recognition",true);
		Container contentPane=progress.getContentPane();
		label=new JLabel("Reading data from "+file.getName()+"...");
		label.setForeground(Color.BLUE);
		cancel=new JButton("Cancel");
		cancel.addActionListener(this);
		JPanel panel=new JPanel();
		panel.add(cancel);
		progressBar=new JProgressBar();
		progressBar.setOrientation(JProgressBar.HORIZONTAL);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setSize(new Dimension(200,30));
		
		timer=new Timer(100,this);
		timer.start();
		
		getTraceInfo();
		writeThread1=new Thread(new WriteDatabase(1,rowNumber/5));
		writeThread2=new Thread(new WriteDatabase(rowNumber/5+1,rowNumber*2/5));
		writeThread3=new Thread(new WriteDatabase(rowNumber*2/5+1,rowNumber*3/5));
		writeThread4=new Thread(new WriteDatabase(rowNumber*3/5+1,rowNumber*4/5));
		writeThread5=new Thread(new WriteDatabase(rowNumber*4/5+1,rowNumber));
		writeThread1.start();
		writeThread2.start();
		writeThread3.start();
		writeThread4.start();
		writeThread5.start();
		
		contentPane.add(label,BorderLayout.NORTH);
		contentPane.add(progressBar,BorderLayout.CENTER);
		contentPane.add(panel,BorderLayout.SOUTH);
		
		progress.setSize(400,130);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		Dimension framesize=progress.getSize();
		if(framesize.width>width)
			framesize.width=width;
		if(framesize.height>height)
			framesize.height=height;
		progress.setLocation((width-framesize.width)/2,(height-framesize.height)/2);
		progress.setVisible(true);		
	}
	
	public void actionPerformed(ActionEvent e) {
		// TODO 自动生成方法存根
		if(e.getSource()==cancel){
			//should delete data from all tables and stop all the reading threads
			is_normal=false;
			is_old_wireless=false;
			is_new_wireless=false;
			hasAGT=false;
			hasRTR=false;
			hasMAC=false;
//			statusField.setText("Not open any trace file");
//			statusField.revalidate();
			is_right_format=false;
//			writeThread1.stop();
//			writeThread2.stop();
//			writeThread3.stop();
//			writeThread4.stop();
//			writeThread5.stop();
			deleteData();
			progress.dispose();
		}
		if(e.getSource()==timer){
//			System.out.println("ai");
			int value=(handledRowNum*100)/rowNumber;
			if(value<100){
//				second++;
//			    label.setText("Reading data from "+file.getName()+"...   "+second/10+" seconds passed");
				progressBar.setValue(value);
			}
			else{
				timer.stop();
				progress.dispose();
				JOptionPane.showMessageDialog(null,"Reading completed!","Information",JOptionPane.INFORMATION_MESSAGE);
				String getAGT=null;
				String getRTR=null;
				String getMAC=null;
				Query query;
				ResultSet dataset;
				if(DataRecognition.is_old_wireless){
					getAGT="select count(distinct TN) from "+tablePrefix+"old_wireless_tr where TN='AGT'";
					getRTR="select count(distinct TN) from "+tablePrefix+"old_wireless_tr where TN='RTR'";
					getMAC="select count(distinct TN) from "+tablePrefix+"old_wireless_tr where TN='MAC'";
					query=new Query(con,getAGT);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							while(dataset.next()){
								if(Integer.parseInt(dataset.getString(1))>0)
									hasAGT=true;
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					query=new Query(con,getRTR);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							while(dataset.next()){
								if(Integer.parseInt(dataset.getString(1))>0)
									hasRTR=true;
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					query=new Query(con,getMAC);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							while(dataset.next()){
								if(Integer.parseInt(dataset.getString(1))>0)
									hasMAC=true;
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				}
				if(DataRecognition.is_new_wireless){
					getAGT="select count(distinct Nl) from "+tablePrefix+"new_wireless_tr where Nl='AGT'";
					getRTR="select count(distinct Nl) from "+tablePrefix+"new_wireless_tr where Nl='RTR'";
					getMAC="select count(distinct Nl) from "+tablePrefix+"new_wireless_tr where Nl='MAC'";
					query=new Query(con,getAGT);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							while(dataset.next()){
								if(Integer.parseInt(dataset.getString(1))>0)
									hasAGT=true;
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					query=new Query(con,getRTR);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							while(dataset.next()){
								if(Integer.parseInt(dataset.getString(1))>0)
									hasRTR=true;
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
					query=new Query(con,getMAC);
					dataset=query.doQuery();
					if(dataset!=null){
						try{
							while(dataset.next()){
								if(Integer.parseInt(dataset.getString(1))>0)
									hasMAC=true;
							}
						}catch(SQLException e1){
							e1.printStackTrace();
						}
					}
				}
			}
			if(!is_right_format){
				DataRecognition.is_new_wireless=false;
				DataRecognition.is_normal=false;
				DataRecognition.is_old_wireless=false;
				hasAGT=false;
				hasRTR=false;
				hasMAC=false;
				statusField.setText("Not open any trace file");
				statusField.revalidate();
				deleteData();
				timer.stop();
			}
		}
	}
	

	/*get the row number of the trace file and recognize the format of the trace file 
	 */
	 
	public void getTraceInfo(){
		rowNumber=0;
		String str1;
		String substr;
		try {
			FileReader filereader=new FileReader(file);
			BufferedReader br=new BufferedReader(filereader);
			try {
				while((str1=br.readLine())!=null){
					rowNumber++;
					if(is_normal||is_old_wireless||is_new_wireless)
						continue;
					str1=str1.trim();
					int i=str1.indexOf(' ');
					if(i==-1)
						continue;
					substr=str1.substring(0,i);
					if(substr.equals("M"))
						continue;
					else if(substr.equals("N"))
						continue;
					else if(substr.equals("+"))
						is_normal=true;
					else if(substr.equals("s")){
						substr=str1.substring(i+1);
						i=substr.indexOf(' ');
						if (i==-1)
							continue;
						substr=substr.substring(0,i);
						if(substr.equals("-t"))
							is_new_wireless=true;
						else 
							is_old_wireless=true;
					}
						
				}
				if(!is_normal&&!is_old_wireless&&!is_new_wireless)
					is_new_wireless=true;	
				
				br.close();
				filereader.close();
				
				System.out.println("total row number: "+rowNumber+" is_normal: "+is_normal+" is_old_wireless: "+is_old_wireless+" is_new_wireless: "+is_new_wireless);
			} catch (IOException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,file.getName()+" not found!","Error Message",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	
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
	
	
    public class WriteDatabase implements Runnable{
    	public int start;
    	public int end;
    	public FileReader filereader;
		public BufferedReader br;
		public String[] allData;
		public PreparedStatement ps_norm;
		public PreparedStatement ps_norm_tcp;
		public PreparedStatement ps_norm_sate;
		public PreparedStatement ps_old;
		public PreparedStatement ps_old_arp;
		public PreparedStatement ps_old_dsr;
		public PreparedStatement ps_old_aodvreq;
		public PreparedStatement ps_old_aodvrep;
		public PreparedStatement ps_old_toraque;
		public PreparedStatement ps_old_toraupd;
		public PreparedStatement ps_old_toracle;
		public PreparedStatement ps_old_ip;
		public PreparedStatement ps_old_tcp;
		public PreparedStatement ps_old_cbr;
		public PreparedStatement ps_old_imep;
		public PreparedStatement ps_new;
		public PreparedStatement ps_new_arp;
		public PreparedStatement ps_new_dsr;
		public PreparedStatement ps_new_aodvreq;
		public PreparedStatement ps_new_aodvrep;
		public PreparedStatement ps_new_toraque;
		public PreparedStatement ps_new_toraupd;
		public PreparedStatement ps_new_toracle;
		public PreparedStatement ps_new_ip;
		public PreparedStatement ps_new_tcp;
		public PreparedStatement ps_new_cbr;
		public PreparedStatement ps_new_imep;
		public PreparedStatement ps_movement;
		public PreparedStatement ps_old_energy;
		public PreparedStatement ps_new_energy;
		
    	public WriteDatabase(int start,int end){
    		this.start=start;
    		this.end=end;
			if(is_normal){
				String insert_norm="insert into "+tablePrefix+"normal_tr (Event,Time,SN,DN,PN,PS,Flags,FI,SAN,SAP,DAN,DAP,SeqN,UPI,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_norm_tcp="insert into "+tablePrefix+"normal_tr (Event,Time,SN,DN,PN,PS,Flags,FI,SAN,SAP,DAN,DAP,SeqN,UPI,TCP_AN,TCP_Flags,TCP_HL,TCP_SAL,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_norm_sate="insert into "+tablePrefix+"normal_tr (Event,Time,SN,DN,PN,PS,Flags,FI,SAN,SAP,DAN,DAP,SeqN,UPI,Satellite_SLat,Satellite_SLon,Satellite_DLat,Satellite_DLon,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try {
					ps_norm=con.prepareStatement(insert_norm);
					ps_norm_tcp=con.prepareStatement(insert_norm_tcp);
					ps_norm_sate=con.prepareStatement(insert_norm_sate);
				} catch (SQLException e) {
					// TODO 自动生成 catch 块
					e.printStackTrace();
				} 
				allData=new String[16];
			}
			if(is_old_wireless){
				String insert_old="insert into "+tablePrefix+"old_wireless_tr (Event,Time,NI,TN,Reason,EI,PT,PS,TTSD,DMA,SMA,Type,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_old_arp="insert into "+tablePrefix+"old_wireless_tr (Event,Time,NI,TN,Reason,EI,PT,PS,TTSD,DMA,SMA,Type,ARP_ROR,ARP_SMA,ARP_SA,ARP_DMA,ARP_DA,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_old_ip="insert into "+tablePrefix+"old_wireless_tr (Event,Time,NI,TN,Reason,EI,PT,PS,TTSD,DMA,SMA,Type,IP_SIA,IP_SPN,IP_DIA,IP_DPN,IP_TTL,IP_NHA,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_old_dsr="insert into "+tablePrefix+"old_wireless_tr (Event,Time,NI,TN,Reason,EI,PT,PS,TTSD,DMA,SMA,Type,IP_SIA,IP_SPN,IP_DIA,IP_DPN,IP_TTL,IP_NHA,DSR_NONT,DSR_RReqF,DSR_RReqSN,DSR_RRepF,DSR_RRepSN,DSR_RL,DSR_SOSR,DSR_DOSR,DSR_ERF,DSR_NOE,DSR_RTW,DSR_LEF,DSR_LET,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_old_aodvreq="insert into "+tablePrefix+"old_wireless_tr (Event,Time,NI,TN,Reason,EI,PT,PS,TTSD,DMA,SMA,Type,IP_SIA,IP_SPN,IP_DIA,IP_DPN,IP_TTL,IP_NHA,AODV_ReqType,AODV_ReqHC,AODV_ReqBI,AODV_ReqDestination,AODV_ReqDSN,AODV_ReqSource,AODV_ReqSSN,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_old_aodvrep="insert into "+tablePrefix+"old_wireless_tr (Event,Time,NI,TN,Reason,EI,PT,PS,TTSD,DMA,SMA,Type,IP_SIA,IP_SPN,IP_DIA,IP_DPN,IP_TTL,IP_NHA,AODV_Type,AODV_HC,AODV_Destination,AODV_DSN,AODV_Lifetime,AODV_Operation,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_old_toraque="insert into "+tablePrefix+"old_wireless_tr (Event,Time,NI,TN,Reason,EI,PT,PS,TTSD,DMA,SMA,Type,IP_SIA,IP_SPN,IP_DIA,IP_DPN,IP_TTL,IP_NHA,TORA_QueType,TORA_QueDestination,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_old_toraupd="insert into "+tablePrefix+"old_wireless_tr (Event,Time,NI,TN,Reason,EI,PT,PS,TTSD,DMA,SMA,Type,IP_SIA,IP_SPN,IP_DIA,IP_DPN,IP_TTL,IP_NHA,TORA_UpdType,TORA_UpdDestination,TORA_UpdTau,TORA_UpdOid,TORA_UpdR,TORA_UpdDelta,TORA_UpdID,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_old_toracle="insert into "+tablePrefix+"old_wireless_tr (Event,Time,NI,TN,Reason,EI,PT,PS,TTSD,DMA,SMA,Type,IP_SIA,IP_SPN,IP_DIA,IP_DPN,IP_TTL,IP_NHA,TORA_CleType,TORA_CleDestination,TORA_CleTau,TORA_CleOid,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_old_tcp="insert into "+tablePrefix+"old_wireless_tr (Event,Time,NI,TN,Reason,EI,PT,PS,TTSD,DMA,SMA,Type,IP_SIA,IP_SPN,IP_DIA,IP_DPN,IP_TTL,IP_NHA,TCP_SN,TCP_AN,TCP_NOTPWF,TCP_ONOF,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_old_cbr="insert into "+tablePrefix+"old_wireless_tr (Event,Time,NI,TN,Reason,EI,PT,PS,TTSD,DMA,SMA,Type,IP_SIA,IP_SPN,IP_DIA,IP_DPN,IP_TTL,IP_NHA,CBR_SN,CBR_NOTPWF,CBR_ONOF,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_old_imep="insert into "+tablePrefix+"old_wireless_tr (Event,Time,NI,TN,Reason,EI,PT,PS,TTSD,DMA,SMA,Type,IP_SIA,IP_SPN,IP_DIA,IP_DPN,IP_TTL,IP_NHA,IMEP_AF,IMEP_HF,IMEP_OF,IMEP_Length,Name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_movement="insert into "+tablePrefix+"movement_tr (Event,Time,NodeID,X,Y,Z,DestinationX,DestinationY,Speed) values (?,?,?,?,?,?,?,?,?)";	
				String insert_old_energy="insert into "+tablePrefix+"energy_tr (Event,t,n,e) values(?,?,?,?)";
				try {
					ps_old=con.prepareStatement(insert_old);
					ps_old_arp=con.prepareStatement(insert_old_arp);
					ps_old_ip=con.prepareStatement(insert_old_ip);
					ps_old_dsr=con.prepareStatement(insert_old_dsr);
					ps_old_aodvreq=con.prepareStatement(insert_old_aodvreq);
					ps_old_aodvrep=con.prepareStatement(insert_old_aodvrep);
					ps_old_toraque=con.prepareStatement(insert_old_toraque);
					ps_old_toraupd=con.prepareStatement(insert_old_toraupd);
					ps_old_toracle=con.prepareStatement(insert_old_toracle);
					ps_old_tcp=con.prepareStatement(insert_old_tcp);
					ps_old_cbr=con.prepareStatement(insert_old_cbr);
					ps_old_imep=con.prepareStatement(insert_old_imep);
					ps_movement=con.prepareStatement(insert_movement);
					ps_old_energy=con.prepareStatement(insert_old_energy);
				} catch (SQLException e) {
					// TODO 自动生成 catch 块
					e.printStackTrace();
				}
				allData=new String[28];
			}
			if(is_new_wireless){
				String insert_new="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Nx,Ny,Nz,Ne,Nl,Nw,Hs,Hd,Ma,Ms,Md,Mt,Name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_new_arp="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Nx,Ny,Nz,Ne,Nl,Nw,Hs,Hd,Ma,Ms,Md,Mt,P,ARP_Po,ARP_Pms,ARP_Ps,ARP_Pmd,ARP_Pd,Name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_new_ip="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Nx,Ny,Nz,Ne,Nl,Nw,Hs,Hd,Ma,Ms,Md,Mt,IP_Isn,IP_Isp,IP_Idn,IP_Idp,IP_It,IP_Il,IP_If,IP_Ii,IP_Iv,Name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_new_dsr="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Nx,Ny,Nz,Ne,Nl,Nw,Hs,Hd,Ma,Ms,Md,Mt,IP_Isn,IP_Isp,IP_Idn,IP_Idp,IP_It,IP_Il,IP_If,IP_Ii,IP_Iv,P,DSR_Ph,DSR_Pq,DSR_Ps,DSR_Pp,DSR_Pn,DSR_Pl,DSR_Pes,DSR_Ped,DSR_Pw,DSR_Pm,DSR_Pc,DSR_Pba,DSR_Pbb,Name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_new_aodvreq="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Nx,Ny,Nz,Ne,Nl,Nw,Hs,Hd,Ma,Ms,Md,Mt,IP_Isn,IP_Isp,IP_Idn,IP_Idp,IP_It,IP_Il,IP_If,IP_Ii,IP_Iv,P,AODV_Pt,AODV_Ph,AODV_Pb,AODV_Pd,AODV_Pds,AODV_Ps,AODV_Pss,AODV_Pc,Name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_new_aodvrep="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Nx,Ny,Nz,Ne,Nl,Nw,Hs,Hd,Ma,Ms,Md,Mt,IP_Isn,IP_Isp,IP_Idn,IP_Idp,IP_It,IP_Il,IP_If,IP_Ii,IP_Iv,P,AODV_Pt,AODV_Ph,AODV_Pd,AODV_Pds,AODV_Pl,AODV_Pc,Name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";				
				String insert_new_toraupd="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Nx,Ny,Nz,Ne,Nl,Nw,Hs,Hd,Ma,Ms,Md,Mt,IP_Isn,IP_Isp,IP_Idn,IP_Idp,IP_It,IP_Il,IP_If,IP_Ii,IP_Iv,P,TORA_Pt,TORA_Pd,TORA_Pa,TORA_Po,TORA_Pr,TORA_Pe,TORA_Pi,TORA_Pc,Name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_new_toraque="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Nx,Ny,Nz,Ne,Nl,Nw,Hs,Hd,Ma,Ms,Md,Mt,IP_Isn,IP_Isp,IP_Idn,IP_Idp,IP_It,IP_Il,IP_If,IP_Ii,IP_Iv,P,TORA_Pt,TORA_Pd,TORA_Pc,Name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_new_toracle="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Nx,Ny,Nz,Ne,Nl,Nw,Hs,Hd,Ma,Ms,Md,Mt,IP_Isn,IP_Isp,IP_Idn,IP_Idp,IP_It,IP_Il,IP_If,IP_Ii,IP_Iv,P,TORA_Pt,TORA_Pd,TORA_Pa,TORA_Po,TORA_Pc,Name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";				
				String insert_new_tcp="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Nx,Ny,Nz,Ne,Nl,Nw,Hs,Hd,Ma,Ms,Md,Mt,IP_Isn,IP_Isp,IP_Idn,IP_Idp,IP_It,IP_Il,IP_If,IP_Ii,IP_Iv,Pn,TCP_Ps,TCP_Pa,TCP_Pf,TCP_Po,Name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_new_cbr="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Nx,Ny,Nz,Ne,Nl,Nw,Hs,Hd,Ma,Ms,Md,Mt,IP_Isn,IP_Isp,IP_Idn,IP_Idp,IP_It,IP_Il,IP_If,IP_Ii,IP_Iv,Pn,CBR_Pi,CBR_Pf,CBR_Po,Name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_new_imep="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Nx,Ny,Nz,Ne,Nl,Nw,Hs,Hd,Ma,Ms,Md,Mt,IP_Isn,IP_Isp,IP_Idn,IP_Idp,IP_It,IP_Il,IP_If,IP_Ii,IP_Iv,P,IMEP_Pa,IMEP_Ph,IMEP_Po,IMEP_Pl,Name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				String insert_movement="insert into "+tablePrefix+"movement_tr (Event,Time,NodeID,X,Y,Z,DestinationX,DestinationY,Speed) values (?,?,?,?,?,?,?,?,?)";	
				String insert_new_energy="insert into "+tablePrefix+"new_wireless_tr (Event,Time,Ni,Ne,Name) values (?,?,?,?,?)";
				try {
					ps_new=con.prepareStatement(insert_new);
					ps_new_arp=con.prepareStatement(insert_new_arp);
					ps_new_ip=con.prepareStatement(insert_new_ip);
					ps_new_dsr=con.prepareStatement(insert_new_dsr);
					ps_new_aodvreq=con.prepareStatement(insert_new_aodvreq);
					ps_new_aodvrep=con.prepareStatement(insert_new_aodvrep);
					ps_new_toraupd=con.prepareStatement(insert_new_toraupd);
					ps_new_toraque=con.prepareStatement(insert_new_toraque);
					ps_new_toracle=con.prepareStatement(insert_new_toracle);
					ps_new_tcp=con.prepareStatement(insert_new_tcp);
					ps_new_cbr=con.prepareStatement(insert_new_cbr);
					ps_new_imep=con.prepareStatement(insert_new_imep);
					ps_movement=con.prepareStatement(insert_movement);
					ps_new_energy=con.prepareStatement(insert_new_energy);
				} catch (SQLException e) {
					// TODO 自动生成 catch 块
					e.printStackTrace();
				}
				allData=new String[67];
			}
    	}
    	public void run(){    		
    		String data;
    		int i=1;
    		int k;
    		int j;
			try {
				filereader = new FileReader(file);
				br=new BufferedReader(filereader);
			} catch (FileNotFoundException e) {
				// TODO 自动生成 catch 块
				e.printStackTrace();
			}
			while(i<start){
				try {
					data=br.readLine();
				} catch (IOException e) {
					// TODO 自动生成 catch 块
					e.printStackTrace();
				}
				i++;
			}
			while(is_right_format){
				
				if(i<=end){
					IncHandledRowNum();
					/*
					 * deal with trace that has normal format
					 */
					if(is_normal){
						try {
							data=br.readLine();
							data=data.trim();
							j=0;
							while((k=data.indexOf(' '))!=-1){
								String substr=data.substring(0,k);
								allData[j++]=substr;
								data=data.substring(k+1).trim();
							}
							allData[j++]=data;
//							for(int n=0;n<=j;n++)
//								System.out.println(allData[n]);
//							System.out.println("j="+j);
							/*
							 * file of normal trace format has 12 or 16 datas at every row
							 * otherwise, the trace format is not right
							 */

							
                            if((allData[0].equals("r"))||(allData[0].equals("d"))|(allData[0].equals("e"))||(allData[0].equals("+"))||(allData[0].equals("-"))){
    							if(j!=12&&j!=16){
    								is_right_format=false;
    								progress.dispose();
    								JOptionPane.showMessageDialog(null,"Error detected at row "+i+" of "+file.getName()+" !","Error Message",JOptionPane.ERROR_MESSAGE);
    								return;
    							}
    							if(j==12){
    								try{
    									ps_norm.setString(1,allData[0]);
    									ps_norm.setDouble(2,Double.parseDouble(allData[1]));
    									ps_norm.setInt(3,Integer.parseInt(allData[2]));
    									ps_norm.setInt(4,Integer.parseInt(allData[3]));
    									ps_norm.setString(5,allData[4]);
    									ps_norm.setInt(6,Integer.parseInt(allData[5]));
    									ps_norm.setString(7,allData[6]);
    									ps_norm.setInt(8,Integer.parseInt(allData[7]));
    									
    									int loc1=allData[8].indexOf('.');
										String snode=allData[8].substring(0,loc1);
										String sport=allData[8].substring(loc1+1);
										ps_norm.setInt(9,Integer.parseInt(snode));
										ps_norm.setInt(10,Integer.parseInt(sport));
										
										int loc2=allData[9].indexOf('.');
										String dnode=allData[9].substring(0,loc2);
										String dport=allData[9].substring(loc2+1);
										ps_norm.setInt(11,Integer.parseInt(dnode));
										ps_norm.setInt(12,Integer.parseInt(dport));
										
										ps_norm.setInt(13,Integer.parseInt(allData[10]));
										ps_norm.setInt(14,Integer.parseInt(allData[11]));
										
    									ps_norm.setString(15,"norm");
    									ps_norm.executeUpdate();
    								}
    								catch(Exception e1){
    									e1.printStackTrace();
    									is_right_format=false;
    									progress.dispose();
    									JOptionPane.showMessageDialog(null,"Error detected at row "+i+" of "+file.getName()+" !","Error Message",JOptionPane.ERROR_MESSAGE);
    									return;
    								}
    							}
    							if(j==16){
    								boolean is_tcp=false;
    								if((allData[12].indexOf('.')==-1))
    									is_tcp=true;
    								/*
    								 * if the record is tcp format
    								 * then write it to database by PreparedStatement of ps_norm_tcp
    								 * otherwise write it to database by PreparedStatement of ps_norm_sate
    								 */
    								try{
    									if(is_tcp){
    										 ps_norm_tcp.setString(1,allData[0]);
    										 ps_norm_tcp.setDouble(2,Double.parseDouble(allData[1]));
    										 ps_norm_tcp.setInt(3,Integer.parseInt(allData[2]));
    										 ps_norm_tcp.setInt(4,Integer.parseInt(allData[3]));
    										 ps_norm_tcp.setString(5,allData[4]);
    										 ps_norm_tcp.setInt(6,Integer.parseInt(allData[5]));
    										 ps_norm_tcp.setString(7,allData[6]);
    										 ps_norm_tcp.setInt(8,Integer.parseInt(allData[7]));
    										 
    										 int loc1=allData[8].indexOf('.');
 											 String snode=allData[8].substring(0,loc1);
 											 String sport=allData[8].substring(loc1+1); 											
 										     ps_norm_tcp.setInt(9,Integer.parseInt(snode));
 											 ps_norm_tcp.setInt(10,Integer.parseInt(sport));
 											 
 											 int loc2=allData[9].indexOf('.');
											 String dnode=allData[9].substring(0,loc2);
											 String dport=allData[9].substring(loc2+1); 											
										     ps_norm_tcp.setInt(11,Integer.parseInt(dnode));
											 ps_norm_tcp.setInt(12,Integer.parseInt(dport));
    										 
											 ps_norm_tcp.setInt(13,Integer.parseInt(allData[10]));
											 ps_norm_tcp.setInt(14,Integer.parseInt(allData[11]));
											 ps_norm_tcp.setInt(15,Integer.parseInt(allData[12]));
											 ps_norm_tcp.setInt(16,Integer.parseInt(allData[13],16));
											 ps_norm_tcp.setInt(17,Integer.parseInt(allData[14]));
											 ps_norm_tcp.setInt(18,Integer.parseInt(allData[15]));
											 ps_norm_tcp.setString(19,"tcp");
											 ps_norm_tcp.executeUpdate();
    									}
    									else{
    									  	 ps_norm_sate.setString(1,allData[0]);
   										     ps_norm_sate.setDouble(2,Double.parseDouble(allData[1]));
   										     ps_norm_sate.setInt(3,Integer.parseInt(allData[2]));
   										     ps_norm_sate.setInt(4,Integer.parseInt(allData[3]));
   										     ps_norm_sate.setString(5,allData[4]);
   										     ps_norm_sate.setInt(6,Integer.parseInt(allData[5]));
   										     ps_norm_sate.setString(7,allData[6]);
   										     ps_norm_sate.setInt(8,Integer.parseInt(allData[7]));
   										 
   										     int loc1=allData[8].indexOf('.');
											 String snode=allData[8].substring(0,loc1);
											 String sport=allData[8].substring(loc1+1); 											
										     ps_norm_sate.setInt(9,Integer.parseInt(snode));
											 ps_norm_sate.setInt(10,Integer.parseInt(sport));
											 
											 int loc2=allData[9].indexOf('.');
											 String dnode=allData[9].substring(0,loc2);
											 String dport=allData[9].substring(loc2+1); 											
										     ps_norm_sate.setInt(11,Integer.parseInt(dnode));
											 ps_norm_sate.setInt(12,Integer.parseInt(dport));
   										 
											 ps_norm_sate.setInt(13,Integer.parseInt(allData[10]));
											 ps_norm_sate.setInt(14,Integer.parseInt(allData[11]));
											 ps_norm_sate.setDouble(15,Double.parseDouble(allData[12]));
											 ps_norm_sate.setDouble(16,Double.parseDouble(allData[13]));
											 ps_norm_sate.setDouble(17,Double.parseDouble(allData[14]));
											 ps_norm_sate.setDouble(18,Double.parseDouble(allData[15]));
											 ps_norm_sate.setString(19,"sate");
											 ps_norm_sate.executeUpdate();
    									}
    								}
    								catch(Exception e1){
    									e1.printStackTrace();
    									is_right_format=false;
    									progress.dispose();
    									JOptionPane.showMessageDialog(null,"Error detected at row "+i+" of "+file.getName()+" !","Error Message",JOptionPane.ERROR_MESSAGE);
    									return;
    								}
    							}
                            }
                            /*
                             * if (allData[0]!="r")&&(allData[0]!="d")&&(allData[0]!="e")&&(allData[0]!="+")&&(allData[0]!="-")
                             * we just print the line number and think that this record in the trace is added by NS user
                             */
                            else
                            	/*
                            	 * print the line number to make it convenient to check the program does right or wrong
                            	 * so as follows
                            	 */
                            	System.out.println(i);
						} 
						catch (IOException e) {
							is_right_format=false;
							progress.dispose();
							e.printStackTrace();
							return;
						}
						catch(Exception e){
							e.printStackTrace();
							is_right_format=false;
							progress.dispose();
							JOptionPane.showMessageDialog(null,"Error detected at row "+i+" of "+file.getName()+" !","Error Message",JOptionPane.ERROR_MESSAGE);
							return;
						}
						
					}
					/*
					 * deal with trace that has old wireless format
					 */
					else if(is_old_wireless){
						try{
							data=br.readLine();
							data=data.trim();
							j=0;
							while((k=data.indexOf(' '))!=-1){
								String substr=data.substring(0,k);
								allData[j++]=substr;
								data=data.substring(k+1).trim();
							}
							allData[j++]=data;
//							for(int n=0;n<j;n++)
//							System.out.println(allData[n]);
//						    System.out.println("j="+j);
							if((allData[0].equals("s"))||(allData[0].equals("r"))||(allData[0].equals("D"))||(allData[0].equals("f"))){
								/*
								 * j==12 indicates that the record is old_wireless only(has no other information such as arp,ip...)
								 */
								if(j==12){
									/*
									 * if covert "ffffffff"(has eight 'f's) to int,error will occur
									 * so we change "ffffffff" to "fffffff"(has seven 'f's)
									 * this is needed and it will affected our query when analyse the data
									 */
									if(allData[9].equals("ffffffff"))
										allData[9]="fffffff";
									ps_old.setString(1,allData[0]);
									ps_old.setDouble(2,Double.parseDouble(allData[1]));
									ps_old.setInt(3,Integer.parseInt(allData[2].substring(1,allData[2].length()-1)));
									ps_old.setString(4,allData[3]);
									ps_old.setString(5,allData[4]);
									ps_old.setInt(6,Integer.parseInt(allData[5]));
									ps_old.setString(7,allData[6]);
									ps_old.setInt(8,Integer.parseInt(allData[7]));
									ps_old.setInt(9,Integer.parseInt(allData[8].substring(1),16));
									ps_old.setInt(10,Integer.parseInt(allData[9],16));
									ps_old.setInt(11,Integer.parseInt(allData[10],16));
									ps_old.setInt(12,Integer.parseInt(allData[11].substring(0,allData[11].length()-1),16));
									ps_old.setString(13,"old");
									ps_old.executeUpdate();
								}
								/*
								 * j==16 indicates that the record is old_wireless_arp format
								 */
								else if(j==16){
									/*
									 * if covert "ffffffff"(has eight 'f's) to int,error will occur
									 * so we change "ffffffff" to "fffffff"(has seven 'f's)
									 * this is needed and it will affected our query when analyse the data
									 */
									if(allData[9].equals("ffffffff"))
										allData[9]="fffffff";
									ps_old_arp.setString(1,allData[0]);
									ps_old_arp.setDouble(2,Double.parseDouble(allData[1]));
									ps_old_arp.setInt(3,Integer.parseInt(allData[2].substring(1,allData[2].length()-1)));
									ps_old_arp.setString(4,allData[3]);
									ps_old_arp.setString(5,allData[4]);
									ps_old_arp.setInt(6,Integer.parseInt(allData[5]));
									ps_old_arp.setString(7,allData[6]);
									ps_old_arp.setInt(8,Integer.parseInt(allData[7]));
									ps_old_arp.setInt(9,Integer.parseInt(allData[8].substring(1),16));
									ps_old_arp.setInt(10,Integer.parseInt(allData[9],16));
									ps_old_arp.setInt(11,Integer.parseInt(allData[10],16));
									ps_old_arp.setInt(12,Integer.parseInt(allData[11].substring(0,allData[11].length()-1),16));
									ps_old_arp.setString(13,allData[13].substring(1));
									ps_old_arp.setInt(14,Integer.parseInt(allData[14].substring(0,allData[14].indexOf('/'))));
									ps_old_arp.setInt(15,Integer.parseInt(allData[14].substring(allData[14].indexOf('/')+1)));
									ps_old_arp.setInt(16,Integer.parseInt(allData[15].substring(0,allData[15].indexOf('/'))));
									ps_old_arp.setInt(17,Integer.parseInt(allData[15].substring(allData[15].indexOf('/')+1,allData[15].length()-1)));
									ps_old_arp.setString(18,"arp");
									ps_old_arp.executeUpdate();
								}
								/*
								 * j==17 indicates that the record is old_wireless_ip format 
								 * and has no other information such as dsr,aodv... 
								 */
								else if(j==17){
									/*
									 * if covert "ffffffff"(has eight 'f's) to int,error will occur
									 * so we change "ffffffff" to "fffffff"(has seven 'f's)
									 * this is needed and it will affected our query when analyse the data
									 */
									if(allData[9].equals("ffffffff"))
										allData[9]="fffffff";
									ps_old_ip.setString(1,allData[0]);
									ps_old_ip.setDouble(2,Double.parseDouble(allData[1]));
									ps_old_ip.setInt(3,Integer.parseInt(allData[2].substring(1,allData[2].length()-1)));
									ps_old_ip.setString(4,allData[3]);
									ps_old_ip.setString(5,allData[4]);
									ps_old_ip.setInt(6,Integer.parseInt(allData[5]));
									ps_old_ip.setString(7,allData[6]);
									ps_old_ip.setInt(8,Integer.parseInt(allData[7]));
									ps_old_ip.setInt(9,Integer.parseInt(allData[8].substring(1),16));
									ps_old_ip.setInt(10,Integer.parseInt(allData[9],16));
									ps_old_ip.setInt(11,Integer.parseInt(allData[10],16));
									ps_old_ip.setInt(12,Integer.parseInt(allData[11].substring(0,allData[11].length()-1),16));
									ps_old_ip.setInt(13,Integer.parseInt(allData[13].substring(1,allData[13].indexOf(':'))));
									ps_old_ip.setInt(14,Integer.parseInt(allData[13].substring(allData[13].indexOf(':')+1)));
									ps_old_ip.setInt(15,Integer.parseInt(allData[14].substring(0,allData[14].indexOf(':'))));
									ps_old_ip.setInt(16,Integer.parseInt(allData[14].substring(allData[14].indexOf(':')+1)));
									ps_old_ip.setInt(17,Integer.parseInt(allData[15]));
									ps_old_ip.setInt(18,Integer.parseInt(allData[16].substring(0,allData[16].length()-1)));
									ps_old_ip.setString(19,"ip");
									ps_old_ip.executeUpdate();
								}
								/*
								 * j==20 indicates that the record is old_wireless_tora format 
								 * or old_wireless_cbr format
								 */
								else if(j==20){
									/*
									 * if covert "ffffffff"(has eight 'f's) to int,error will occur
									 * so we change "ffffffff" to "fffffff"(has seven 'f's)
									 * this is needed and it will affected our query when analyse the data
									 */
									if(allData[9].equals("ffffffff"))
										allData[9]="fffffff";
									boolean is_cbr=false;
									if(allData[17].indexOf(']')==(allData[17].length()-1))
										is_cbr=true;
									if(is_cbr){
										ps_old_cbr.setString(1,allData[0]);
										ps_old_cbr.setDouble(2,Double.parseDouble(allData[1]));
										ps_old_cbr.setInt(3,Integer.parseInt(allData[2].substring(1,allData[2].length()-1)));
										ps_old_cbr.setString(4,allData[3]);
										ps_old_cbr.setString(5,allData[4]);
										ps_old_cbr.setInt(6,Integer.parseInt(allData[5]));
										ps_old_cbr.setString(7,allData[6]);
										ps_old_cbr.setInt(8,Integer.parseInt(allData[7]));
										ps_old_cbr.setInt(9,Integer.parseInt(allData[8].substring(1),16));
										ps_old_cbr.setInt(10,Integer.parseInt(allData[9],16));
										ps_old_cbr.setInt(11,Integer.parseInt(allData[10],16));
										ps_old_cbr.setInt(12,Integer.parseInt(allData[11].substring(0,allData[11].length()-1),16));
										ps_old_cbr.setInt(13,Integer.parseInt(allData[13].substring(1,allData[13].indexOf(':'))));
										ps_old_cbr.setInt(14,Integer.parseInt(allData[13].substring(allData[13].indexOf(':')+1)));
										ps_old_cbr.setInt(15,Integer.parseInt(allData[14].substring(0,allData[14].indexOf(':'))));
										ps_old_cbr.setInt(16,Integer.parseInt(allData[14].substring(allData[14].indexOf(':')+1)));
										ps_old_cbr.setInt(17,Integer.parseInt(allData[15]));
										ps_old_cbr.setInt(18,Integer.parseInt(allData[16].substring(0,allData[16].length()-1)));							
										ps_old_cbr.setInt(19,Integer.parseInt(allData[17].substring(1,allData[17].length()-1)));
										ps_old_cbr.setInt(20,Integer.parseInt(allData[18]));
										ps_old_cbr.setInt(21,Integer.parseInt(allData[19]));
										ps_old_cbr.setString(22,"cbr");
										ps_old_cbr.executeUpdate();
									}
									else{
										if(!allData[19].equals("(QUERY)")){
											is_right_format=false;
											progress.dispose();
											JOptionPane.showMessageDialog(null,"Error detected at row "+i+" of "+file.getName()+" !","Error Message",JOptionPane.ERROR_MESSAGE);
											return;
										}
										ps_old_toraque.setString(1,allData[0]);
										ps_old_toraque.setDouble(2,Double.parseDouble(allData[1]));
										ps_old_toraque.setInt(3,Integer.parseInt(allData[2].substring(1,allData[2].length()-1)));
										ps_old_toraque.setString(4,allData[3]);
										ps_old_toraque.setString(5,allData[4]);
										ps_old_toraque.setInt(6,Integer.parseInt(allData[5]));
										ps_old_toraque.setString(7,allData[6]);
										ps_old_toraque.setInt(8,Integer.parseInt(allData[7]));
										ps_old_toraque.setInt(9,Integer.parseInt(allData[8].substring(1),16));
										ps_old_toraque.setInt(10,Integer.parseInt(allData[9],16));
										ps_old_toraque.setInt(11,Integer.parseInt(allData[10],16));
										ps_old_toraque.setInt(12,Integer.parseInt(allData[11].substring(0,allData[11].length()-1),16));
										ps_old_toraque.setInt(13,Integer.parseInt(allData[13].substring(1,allData[13].indexOf(':'))));
										ps_old_toraque.setInt(14,Integer.parseInt(allData[13].substring(allData[13].indexOf(':')+1)));
										ps_old_toraque.setInt(15,Integer.parseInt(allData[14].substring(0,allData[14].indexOf(':'))));
										ps_old_toraque.setInt(16,Integer.parseInt(allData[14].substring(allData[14].indexOf(':')+1)));
										ps_old_toraque.setInt(17,Integer.parseInt(allData[15]));
										ps_old_toraque.setInt(18,Integer.parseInt(allData[16].substring(0,allData[16].length()-1)));
										ps_old_toraque.setInt(19,Integer.parseInt(allData[17].substring(3),16));
										ps_old_toraque.setInt(20,Integer.parseInt(allData[18].substring(0,allData[18].length()-1)));
										ps_old_toraque.setString(21,"tora");
										ps_old_toraque.executeUpdate();
									}
								}
								/*
								 * j==21 indicates that the record is old_wireless_tcp format
								 * or old_wireless_imep format
								 */
								else if(j==21){
									/*
									 * if covert "ffffffff"(has eight 'f's) to int,error will occur
									 * so we change "ffffffff" to "fffffff"(has seven 'f's)
									 * this is needed and it will affected our query when analyse the data
									 */
									if(allData[9].equals("ffffffff"))
										allData[9]="fffffff";
									boolean is_tcp=false;
									if(allData[20].indexOf(']')==-1)
										is_tcp=true;
									if(is_tcp){
										ps_old_tcp.setString(1,allData[0]);
										ps_old_tcp.setDouble(2,Double.parseDouble(allData[1]));
										ps_old_tcp.setInt(3,Integer.parseInt(allData[2].substring(1,allData[2].length()-1)));
										ps_old_tcp.setString(4,allData[3]);
										ps_old_tcp.setString(5,allData[4]);
			//							System.out.println(allData[5]);
										ps_old_tcp.setInt(6,Integer.parseInt(allData[5]));
										ps_old_tcp.setString(7,allData[6]);
										ps_old_tcp.setInt(8,Integer.parseInt(allData[7]));
										ps_old_tcp.setInt(9,Integer.parseInt(allData[8].substring(1),16));
										ps_old_tcp.setInt(10,Integer.parseInt(allData[9],16));
										ps_old_tcp.setInt(11,Integer.parseInt(allData[10],16));
										ps_old_tcp.setInt(12,Integer.parseInt(allData[11].substring(0,allData[11].length()-1),16));
										ps_old_tcp.setInt(13,Integer.parseInt(allData[13].substring(1,allData[13].indexOf(':'))));
										ps_old_tcp.setInt(14,Integer.parseInt(allData[13].substring(allData[13].indexOf(':')+1)));
										ps_old_tcp.setInt(15,Integer.parseInt(allData[14].substring(0,allData[14].indexOf(':'))));
										ps_old_tcp.setInt(16,Integer.parseInt(allData[14].substring(allData[14].indexOf(':')+1)));
										ps_old_tcp.setInt(17,Integer.parseInt(allData[15]));
										ps_old_tcp.setInt(18,Integer.parseInt(allData[16].substring(0,allData[16].length()-1)));
									    ps_old_tcp.setInt(19,Integer.parseInt(allData[17].substring(1)));
									    ps_old_tcp.setInt(20,Integer.parseInt(allData[18].substring(0,allData[18].length()-1)));
									    ps_old_tcp.setInt(21,Integer.parseInt(allData[19]));
									    ps_old_tcp.setInt(22,Integer.parseInt(allData[20]));
									    ps_old_tcp.setString(23,"tcp");
									    ps_old_tcp.executeUpdate();									    
									}
									else{
										ps_old_imep.setString(1,allData[0]);
										ps_old_imep.setDouble(2,Double.parseDouble(allData[1]));
										ps_old_imep.setInt(3,Integer.parseInt(allData[2].substring(1,allData[2].length()-1)));
										ps_old_imep.setString(4,allData[3]);
										ps_old_imep.setString(5,allData[4]);
										ps_old_imep.setInt(6,Integer.parseInt(allData[5]));
										ps_old_imep.setString(7,allData[6]);
										ps_old_imep.setInt(8,Integer.parseInt(allData[7]));
										ps_old_imep.setInt(9,Integer.parseInt(allData[8].substring(1),16));
										ps_old_imep.setInt(10,Integer.parseInt(allData[9],16));
										ps_old_imep.setInt(11,Integer.parseInt(allData[10],16));
										ps_old_imep.setInt(12,Integer.parseInt(allData[11].substring(0,allData[11].length()-1),16));
										ps_old_imep.setInt(13,Integer.parseInt(allData[13].substring(1,allData[13].indexOf(':'))));
										ps_old_imep.setInt(14,Integer.parseInt(allData[13].substring(allData[13].indexOf(':')+1)));
										ps_old_imep.setInt(15,Integer.parseInt(allData[14].substring(0,allData[14].indexOf(':'))));
										ps_old_imep.setInt(16,Integer.parseInt(allData[14].substring(allData[14].indexOf(':')+1)));
										ps_old_imep.setInt(17,Integer.parseInt(allData[15]));
										ps_old_imep.setInt(18,Integer.parseInt(allData[16].substring(0,allData[16].length()-1)));
									    ps_old_imep.setString(19,allData[17].substring(1));
									    ps_old_imep.setString(20,allData[18]);
									    ps_old_imep.setString(21,allData[19]);
									    ps_old_imep.setInt(22,Integer.parseInt(allData[20].substring(2,allData[20].length()-1),16));
									    ps_old_imep.setString(23,"imep");
									    ps_old_imep.executeUpdate();				
									}
								}
								/*
								 * j==22 indicates that the record is old_wireless_tora format
								 */
								else if(j==22){
									if(allData[9].equals("ffffffff"))
										allData[9]="fffffff";
									if(!allData[21].equals("(CLEAR)")){
										is_right_format=false;
										progress.dispose();
										JOptionPane.showMessageDialog(null,"Error detected at row "+i+" of "+file.getName()+" !","Error Message",JOptionPane.ERROR_MESSAGE);
										return;
									}
									ps_old_toracle.setString(1,allData[0]);
									ps_old_toracle.setDouble(2,Double.parseDouble(allData[1]));
									ps_old_toracle.setInt(3,Integer.parseInt(allData[2].substring(1,allData[2].length()-1)));
									ps_old_toracle.setString(4,allData[3]);
									ps_old_toracle.setString(5,allData[4]);
									ps_old_toracle.setInt(6,Integer.parseInt(allData[5]));
									ps_old_toracle.setString(7,allData[6]);
									ps_old_toracle.setInt(8,Integer.parseInt(allData[7]));
									ps_old_toracle.setInt(9,Integer.parseInt(allData[8].substring(1),16));
									ps_old_toracle.setInt(10,Integer.parseInt(allData[9],16));
									ps_old_toracle.setInt(11,Integer.parseInt(allData[10],16));
									ps_old_toracle.setInt(12,Integer.parseInt(allData[11].substring(0,allData[11].length()-1),16));
									ps_old_toracle.setInt(13,Integer.parseInt(allData[13].substring(1,allData[13].indexOf(':'))));
									ps_old_toracle.setInt(14,Integer.parseInt(allData[13].substring(allData[13].indexOf(':')+1)));
									ps_old_toracle.setInt(15,Integer.parseInt(allData[14].substring(0,allData[14].indexOf(':'))));
									ps_old_toracle.setInt(16,Integer.parseInt(allData[14].substring(allData[14].indexOf(':')+1)));
									ps_old_toracle.setInt(17,Integer.parseInt(allData[15]));
									ps_old_toracle.setInt(18,Integer.parseInt(allData[16].substring(0,allData[16].length()-1)));
								    ps_old_toracle.setInt(19,Integer.parseInt(allData[17].substring(3),16));
								    ps_old_toracle.setInt(20,Integer.parseInt(allData[18]));
								    ps_old_toracle.setDouble(21,Double.parseDouble(allData[19]));
								    ps_old_toracle.setInt(22,Integer.parseInt(allData[20].substring(0,allData[20].length()-1)));
								    ps_old_toracle.setString(23,"tora");
								    ps_old_toracle.executeUpdate();
								}
								/*
								 * j==23 indicates that the record is old_wireless_aodv format
								 */
								else if(j==23){
									if(allData[9].equals("ffffffff"))
										allData[9]="fffffff";
									ps_old_aodvrep.setString(1,allData[0]);
									ps_old_aodvrep.setDouble(2,Double.parseDouble(allData[1]));
									ps_old_aodvrep.setInt(3,Integer.parseInt(allData[2].substring(1,allData[2].length()-1)));
									ps_old_aodvrep.setString(4,allData[3]);
									ps_old_aodvrep.setString(5,allData[4]);
									ps_old_aodvrep.setInt(6,Integer.parseInt(allData[5]));
									ps_old_aodvrep.setString(7,allData[6]);
									ps_old_aodvrep.setInt(8,Integer.parseInt(allData[7]));
									ps_old_aodvrep.setInt(9,Integer.parseInt(allData[8].substring(1),16));
									ps_old_aodvrep.setInt(10,Integer.parseInt(allData[9],16));
									ps_old_aodvrep.setInt(11,Integer.parseInt(allData[10],16));
									ps_old_aodvrep.setInt(12,Integer.parseInt(allData[11].substring(0,allData[11].length()-1),16));
									ps_old_aodvrep.setInt(13,Integer.parseInt(allData[13].substring(1,allData[13].indexOf(':'))));
									ps_old_aodvrep.setInt(14,Integer.parseInt(allData[13].substring(allData[13].indexOf(':')+1)));
									ps_old_aodvrep.setInt(15,Integer.parseInt(allData[14].substring(0,allData[14].indexOf(':'))));
									ps_old_aodvrep.setInt(16,Integer.parseInt(allData[14].substring(allData[14].indexOf(':')+1)));
									ps_old_aodvrep.setInt(17,Integer.parseInt(allData[15]));
									ps_old_aodvrep.setInt(18,Integer.parseInt(allData[16].substring(0,allData[16].length()-1)));
								    ps_old_aodvrep.setInt(19,Integer.parseInt(allData[17].substring(3),16));
								    ps_old_aodvrep.setInt(20,Integer.parseInt(allData[18]));
								    ps_old_aodvrep.setInt(21,Integer.parseInt(allData[19].substring(1)));
								    ps_old_aodvrep.setInt(22,Integer.parseInt(allData[20].substring(0,allData[20].length()-1)));
								    ps_old_aodvrep.setDouble(23,Double.parseDouble(allData[21].substring(0,allData[21].length()-1)));								    
								    ps_old_aodvrep.setString(24,allData[22].substring(1,allData[22].length()-1));
								    ps_old_aodvrep.setString(25,"aodv");
								    ps_old_aodvrep.executeUpdate();
								}
								/*
								 * j==25 indicates that the record is old_wireless_aodv format
								 * or old_wireless_tora format
								 */
								else if(j==25){
									if(allData[9].equals("ffffffff"))
										allData[9]="fffffff";
									boolean is_aodv=false;
									if(allData[17].indexOf('[')==0)
										is_aodv=true;
									if(is_aodv){
										if(!allData[24].equals("(REQUEST)")){									
											is_right_format=false;
											progress.dispose();
											JOptionPane.showMessageDialog(null,"Error detected at row "+i+" of "+file.getName()+" !","Error Message",JOptionPane.ERROR_MESSAGE);
											return;
										}
										ps_old_aodvreq.setString(1,allData[0]);
										ps_old_aodvreq.setDouble(2,Double.parseDouble(allData[1]));
										ps_old_aodvreq.setInt(3,Integer.parseInt(allData[2].substring(1,allData[2].length()-1)));
										ps_old_aodvreq.setString(4,allData[3]);
										ps_old_aodvreq.setString(5,allData[4]);
										ps_old_aodvreq.setInt(6,Integer.parseInt(allData[5]));
										ps_old_aodvreq.setString(7,allData[6]);
										ps_old_aodvreq.setInt(8,Integer.parseInt(allData[7]));
										ps_old_aodvreq.setInt(9,Integer.parseInt(allData[8].substring(1),16));
										ps_old_aodvreq.setInt(10,Integer.parseInt(allData[9],16));
										ps_old_aodvreq.setInt(11,Integer.parseInt(allData[10],16));
										ps_old_aodvreq.setInt(12,Integer.parseInt(allData[11].substring(0,allData[11].length()-1),16));
										ps_old_aodvreq.setInt(13,Integer.parseInt(allData[13].substring(1,allData[13].indexOf(':'))));
										ps_old_aodvreq.setInt(14,Integer.parseInt(allData[13].substring(allData[13].indexOf(':')+1)));
										ps_old_aodvreq.setInt(15,Integer.parseInt(allData[14].substring(0,allData[14].indexOf(':'))));
										ps_old_aodvreq.setInt(16,Integer.parseInt(allData[14].substring(allData[14].indexOf(':')+1)));
										ps_old_aodvreq.setInt(17,Integer.parseInt(allData[15]));
										ps_old_aodvreq.setInt(18,Integer.parseInt(allData[16].substring(0,allData[16].length()-1)));
										ps_old_aodvreq.setInt(19,Integer.parseInt(allData[17].substring(3),16));
										ps_old_aodvreq.setInt(20,Integer.parseInt(allData[18]));
										ps_old_aodvreq.setInt(21,Integer.parseInt(allData[19]));
										ps_old_aodvreq.setInt(22,Integer.parseInt(allData[20].substring(1)));
										ps_old_aodvreq.setInt(23,Integer.parseInt(allData[21].substring(0,allData[21].length()-1)));
										ps_old_aodvreq.setInt(24,Integer.parseInt(allData[22].substring(1)));
										ps_old_aodvreq.setInt(25,Integer.parseInt(allData[23].substring(0,allData[23].length()-2)));
										ps_old_aodvreq.setString(26,"aodv");
										ps_old_aodvreq.executeUpdate();
									}
									else{
										if(!allData[24].equals("(UPDATE)")){
											is_right_format=false;
											progress.dispose();
											JOptionPane.showMessageDialog(null,"Error detected at row "+i+" !","Error Message",JOptionPane.ERROR_MESSAGE);
											return;
										}
										ps_old_toraupd.setString(1,allData[0]);
										ps_old_toraupd.setDouble(2,Double.parseDouble(allData[1]));
										ps_old_toraupd.setInt(3,Integer.parseInt(allData[2].substring(1,allData[2].length()-1)));
										ps_old_toraupd.setString(4,allData[3]);
										ps_old_toraupd.setString(5,allData[4]);
										ps_old_toraupd.setInt(6,Integer.parseInt(allData[5]));
										ps_old_toraupd.setString(7,allData[6]);
										ps_old_toraupd.setInt(8,Integer.parseInt(allData[7]));
										ps_old_toraupd.setInt(9,Integer.parseInt(allData[8].substring(1),16));
										ps_old_toraupd.setInt(10,Integer.parseInt(allData[9],16));
										ps_old_toraupd.setInt(11,Integer.parseInt(allData[10],16));
										ps_old_toraupd.setInt(12,Integer.parseInt(allData[11].substring(0,allData[11].length()-1),16));
										ps_old_toraupd.setInt(13,Integer.parseInt(allData[13].substring(1,allData[13].indexOf(':'))));
										ps_old_toraupd.setInt(14,Integer.parseInt(allData[13].substring(allData[13].indexOf(':')+1)));
										ps_old_toraupd.setInt(15,Integer.parseInt(allData[14].substring(0,allData[14].indexOf(':'))));
										ps_old_toraupd.setInt(16,Integer.parseInt(allData[14].substring(allData[14].indexOf(':')+1)));
										ps_old_toraupd.setInt(17,Integer.parseInt(allData[15]));
										ps_old_toraupd.setInt(18,Integer.parseInt(allData[16].substring(0,allData[16].length()-1)));
										ps_old_toraupd.setInt(19,Integer.parseInt(allData[17].substring(2),16));
										ps_old_toraupd.setInt(20,Integer.parseInt(allData[18]));
										ps_old_toraupd.setDouble(21,Double.parseDouble(allData[19].substring(1)));
										ps_old_toraupd.setInt(22,Integer.parseInt(allData[20]));
										ps_old_toraupd.setInt(23,Integer.parseInt(allData[21]));
										ps_old_toraupd.setInt(24,Integer.parseInt(allData[22]));
										ps_old_toraupd.setInt(25,Integer.parseInt(allData[23].substring(0,allData[23].length()-1)));
										ps_old_toraupd.setString(26,"tora");
										ps_old_toraupd.executeUpdate();
									}
								}
								/*
								 * j==28 indicates that the record is old_wireless_dsr format
								 */
								else if(j==28){
									if(allData[9].equals("ffffffff"))
										allData[9]="fffffff";
									ps_old_dsr.setString(1,allData[0]);
									ps_old_dsr.setDouble(2,Double.parseDouble(allData[1]));
									ps_old_dsr.setInt(3,Integer.parseInt(allData[2].substring(1,allData[2].length()-1)));
									ps_old_dsr.setString(4,allData[3]);
									ps_old_dsr.setString(5,allData[4]);
									ps_old_dsr.setInt(6,Integer.parseInt(allData[5]));
									ps_old_dsr.setString(7,allData[6]);
									ps_old_dsr.setInt(8,Integer.parseInt(allData[7]));
									ps_old_dsr.setInt(9,Integer.parseInt(allData[8].substring(1),16));
									ps_old_dsr.setInt(10,Integer.parseInt(allData[9],16));
									ps_old_dsr.setInt(11,Integer.parseInt(allData[10],16));
									ps_old_dsr.setInt(12,Integer.parseInt(allData[11].substring(0,allData[11].length()-1),16));
									ps_old_dsr.setInt(13,Integer.parseInt(allData[13].substring(1,allData[13].indexOf(':'))));
									ps_old_dsr.setInt(14,Integer.parseInt(allData[13].substring(allData[13].indexOf(':')+1)));
									ps_old_dsr.setInt(15,Integer.parseInt(allData[14].substring(0,allData[14].indexOf(':'))));
									ps_old_dsr.setInt(16,Integer.parseInt(allData[14].substring(allData[14].indexOf(':')+1)));
									ps_old_dsr.setInt(17,Integer.parseInt(allData[15]));
									ps_old_dsr.setInt(18,Integer.parseInt(allData[16].substring(0,allData[16].length()-1)));
									ps_old_dsr.setInt(19,Integer.parseInt(allData[17]));
									ps_old_dsr.setInt(20,Integer.parseInt(allData[18].substring(1)));
									ps_old_dsr.setInt(21,Integer.parseInt(allData[19].substring(0,allData[19].length()-1)));
									ps_old_dsr.setInt(22,Integer.parseInt(allData[20].substring(1)));
									ps_old_dsr.setInt(23,Integer.parseInt(allData[21]));
									ps_old_dsr.setInt(24,Integer.parseInt(allData[22]));
									ps_old_dsr.setInt(25,Integer.parseInt(allData[23].substring(0,allData[23].indexOf('-'))));
									ps_old_dsr.setInt(26,Integer.parseInt(allData[23].substring(allData[23].indexOf('-')+2,allData[23].length()-1)));
									ps_old_dsr.setInt(27,Integer.parseInt(allData[24].substring(1)));
									ps_old_dsr.setInt(28,Integer.parseInt(allData[25]));
									ps_old_dsr.setInt(29,Integer.parseInt(allData[26]));
									ps_old_dsr.setInt(30,Integer.parseInt(allData[27].substring(0,allData[27].indexOf('-'))));
									ps_old_dsr.setInt(31,Integer.parseInt(allData[27].substring(allData[27].indexOf('-')+2,allData[27].length()-1)));
									ps_old_dsr.setString(32,"dsr");
								    ps_old_dsr.executeUpdate();
								}
							}
							/*
							 * if the record begins with M
							 * if it has movement format then handle it
							 * ortherwise do nothing and think that it is added by NS user
							 */
							else if(allData[0].equals("M")){
								if(j==9){
									try{
										double time=Double.parseDouble(allData[1]);
										int id=Integer.parseInt(allData[2]);
										double x=Double.parseDouble(allData[3].substring(1,allData[4].length()-1));
										double y=Double.parseDouble(allData[4].substring(0,allData[4].length()-1));
										double z=Double.parseDouble(allData[5].substring(0,allData[5].length()-2));
										double dx=Double.parseDouble(allData[6].substring(1,allData[6].length()-1));
										double dy=Double.parseDouble(allData[7].substring(0,allData[7].length()-2));
										double speed=Double.parseDouble(allData[8]);
										ps_movement.setString(1,allData[0]);
										ps_movement.setDouble(2,time);
										ps_movement.setInt(3,id);
										ps_movement.setDouble(4,x);
										ps_movement.setDouble(5,y);
										ps_movement.setDouble(6,z);
										ps_movement.setDouble(7,dx);
										ps_movement.setDouble(8,dy);
										ps_movement.setDouble(9,speed);
										ps_movement.executeUpdate();
									}
									catch(Exception e1){
										System.out.println(i);
									}
								}																
							}
							/*
							 *if the record begins with N
							 * if it has energy format then handle it
							 * ortherwise do nothing and think that it is added by NS user
							 */
							else if(allData[0].equals("N")){
								if(j==7){
									try{
										double time=Double.parseDouble(allData[2]);
										int id=Integer.parseInt(allData[4]);
										double energy=Double.parseDouble(allData[6]);
										ps_old_energy.setString(1,allData[0]);
										ps_old_energy.setDouble(2,time);
										ps_old_energy.setInt(3,id);
										ps_old_energy.setDouble(4,energy);
										ps_old_energy.executeUpdate();
									}
									catch(Exception e1){
										System.out.println(i);
									}
								}
							}
							else
								System.out.println(i);
						}
						catch (IOException e) {
							is_right_format=false;
							progress.dispose();
							e.printStackTrace();
							return;
						}
						catch(Exception e){
							e.printStackTrace();
							is_right_format=false;
							progress.dispose();
							JOptionPane.showMessageDialog(null,"Error detected at row "+i+" !","Error Message",JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					/*
					 * deal with trace that has new wireless format
					 */
					else if(is_new_wireless){
						try{
							data=br.readLine();
							data=data.trim();
							j=0;
							while((k=data.indexOf(' '))!=-1){
								String substr=data.substring(0,k);
								allData[j++]=substr;
								data=data.substring(k+1).trim();
							}
							allData[j++]=data;

//							for(int n=0;n<j;n++)
//							System.out.println(allData[n]);
//						    System.out.println("j="+j);
							if((allData[0].equals("s"))||(allData[0].equals("r"))||(allData[0].equals("d"))||(allData[0].equals("f"))){
								if(j>=29){
									/*
									 * if covert "ffffffff"(has eight 'f's) to int,error will occur
									 * so we change "ffffffff" to "fffffff"(has seven 'f's)
									 * this is needed and it will affected our query when analyse the data
									 */
									if(allData[24].equals("ffffffff"))
										allData[24]="fffffff";
								}
								/*
								 * j==29 indicates that the record is new_wireless format(has no other information such as arp,ip...)
								 */
								if(j==29){
									ps_new.setString(1,allData[0]);
									ps_new.setDouble(2,Double.parseDouble(allData[2]));
									ps_new.setInt(10,Integer.parseInt(allData[4]));
									ps_new.setInt(11,Integer.parseInt(allData[6]));
									ps_new.setInt(3,Integer.parseInt(allData[8]));
									ps_new.setDouble(4,Double.parseDouble(allData[10]));
									ps_new.setDouble(5,Double.parseDouble(allData[12]));
									ps_new.setDouble(6,Double.parseDouble(allData[14]));
									ps_new.setDouble(7,Double.parseDouble(allData[16]));
									ps_new.setString(8,allData[18]);
									ps_new.setString(9,allData[20]);
									ps_new.setInt(12,Integer.parseInt(allData[22],16));
									ps_new.setInt(14,Integer.parseInt(allData[24],16));
									ps_new.setInt(13,Integer.parseInt(allData[26],16));
									ps_new.setInt(15,Integer.parseInt(allData[28],16));									
									ps_new.setString(16,"new");
									ps_new.executeUpdate();
								}
								/*
								 * j==41 indicates that the record is new_wireless_arp format
								 */
								else if(j==41){
									ps_new_arp.setString(1,allData[0]);
									ps_new_arp.setDouble(2,Double.parseDouble(allData[2]));
									ps_new_arp.setInt(10,Integer.parseInt(allData[4]));
									ps_new_arp.setInt(11,Integer.parseInt(allData[6]));
									ps_new_arp.setInt(3,Integer.parseInt(allData[8]));
									ps_new_arp.setDouble(4,Double.parseDouble(allData[10]));
									ps_new_arp.setDouble(5,Double.parseDouble(allData[12]));
									ps_new_arp.setDouble(6,Double.parseDouble(allData[14]));
									ps_new_arp.setDouble(7,Double.parseDouble(allData[16]));
									ps_new_arp.setString(8,allData[18]);
									ps_new_arp.setString(9,allData[20]);
									ps_new_arp.setInt(12,Integer.parseInt(allData[22],16));
									ps_new_arp.setInt(14,Integer.parseInt(allData[24],16));
									ps_new_arp.setInt(13,Integer.parseInt(allData[26],16));
									ps_new_arp.setInt(15,Integer.parseInt(allData[28],16));
									ps_new_arp.setString(16,allData[30]);
									ps_new_arp.setString(17,allData[32]);
									ps_new_arp.setInt(18,Integer.parseInt(allData[34]));
									ps_new_arp.setInt(19,Integer.parseInt(allData[36]));
									ps_new_arp.setInt(20,Integer.parseInt(allData[38]));
									ps_new_arp.setInt(21,Integer.parseInt(allData[40]));
									ps_new_arp.setString(22,"arp");
									ps_new_arp.executeUpdate();
								}
								/*
								 * j==43 indicates that the record is new_wireless_ip format(has no other information such as dsr,aodv...)
								 */
								else if(j==43){
									ps_new_ip.setString(1,allData[0]);
									ps_new_ip.setDouble(2,Double.parseDouble(allData[2]));
									ps_new_ip.setInt(10,Integer.parseInt(allData[4]));
									ps_new_ip.setInt(11,Integer.parseInt(allData[6]));
									ps_new_ip.setInt(3,Integer.parseInt(allData[8]));
									ps_new_ip.setDouble(4,Double.parseDouble(allData[10]));
									ps_new_ip.setDouble(5,Double.parseDouble(allData[12]));
									ps_new_ip.setDouble(6,Double.parseDouble(allData[14]));
									ps_new_ip.setDouble(7,Double.parseDouble(allData[16]));
									ps_new_ip.setString(8,allData[18]);
									ps_new_ip.setString(9,allData[20]);
									ps_new_ip.setInt(12,Integer.parseInt(allData[22],16));
									ps_new_ip.setInt(14,Integer.parseInt(allData[24],16));
									ps_new_ip.setInt(13,Integer.parseInt(allData[26],16));
									ps_new_ip.setInt(15,Integer.parseInt(allData[28],16));	
									ps_new_ip.setInt(16,Integer.parseInt(allData[30].substring(0,allData[30].indexOf('.'))));
									ps_new_ip.setInt(17,Integer.parseInt(allData[30].substring(allData[30].indexOf('.')+1)));
									ps_new_ip.setInt(18,Integer.parseInt(allData[32].substring(0,allData[32].indexOf('.'))));
									ps_new_ip.setInt(19,Integer.parseInt(allData[32].substring(allData[32].indexOf('.')+1)));
									ps_new_ip.setString(20,allData[34]);
									ps_new_ip.setInt(21,Integer.parseInt(allData[36]));
									ps_new_ip.setInt(22,Integer.parseInt(allData[38]));
									ps_new_ip.setInt(23,Integer.parseInt(allData[40]));
									ps_new_ip.setInt(24,Integer.parseInt(allData[42]));
									ps_new_ip.setString(25,"ip");
									ps_new_ip.executeUpdate();
								}
								/*
								 * j==51 indicates that the record is new_wireless_cbr format
								 * or new_wireless_toraque  format
								 */
								else if(j==51){
									boolean is_cbr=false;
									if(!allData[50].equals("QUERY"))
										is_cbr=true;
									if(is_cbr){
										ps_new_cbr.setString(1,allData[0]);
										ps_new_cbr.setDouble(2,Double.parseDouble(allData[2]));
										ps_new_cbr.setInt(10,Integer.parseInt(allData[4]));
										ps_new_cbr.setInt(11,Integer.parseInt(allData[6]));
										ps_new_cbr.setInt(3,Integer.parseInt(allData[8]));
										ps_new_cbr.setDouble(4,Double.parseDouble(allData[10]));
										ps_new_cbr.setDouble(5,Double.parseDouble(allData[12]));
										ps_new_cbr.setDouble(6,Double.parseDouble(allData[14]));
										ps_new_cbr.setDouble(7,Double.parseDouble(allData[16]));
										ps_new_cbr.setString(8,allData[18]);
										ps_new_cbr.setString(9,allData[20]);
										ps_new_cbr.setInt(12,Integer.parseInt(allData[22],16));
										ps_new_cbr.setInt(14,Integer.parseInt(allData[24],16));
										ps_new_cbr.setInt(13,Integer.parseInt(allData[26],16));
										ps_new_cbr.setInt(15,Integer.parseInt(allData[28],16));	
										ps_new_cbr.setInt(16,Integer.parseInt(allData[30].substring(0,allData[30].indexOf('.'))));
										ps_new_cbr.setInt(17,Integer.parseInt(allData[30].substring(allData[30].indexOf('.')+1)));
										ps_new_cbr.setInt(18,Integer.parseInt(allData[32].substring(0,allData[32].indexOf('.'))));
										ps_new_cbr.setInt(19,Integer.parseInt(allData[32].substring(allData[32].indexOf('.')+1)));
										ps_new_cbr.setString(20,allData[34]);
										ps_new_cbr.setInt(21,Integer.parseInt(allData[36]));
										ps_new_cbr.setInt(22,Integer.parseInt(allData[38]));
										ps_new_cbr.setInt(23,Integer.parseInt(allData[40]));
										ps_new_cbr.setInt(24,Integer.parseInt(allData[42]));
										ps_new_cbr.setString(25,allData[44]);
										ps_new_cbr.setInt(26,Integer.parseInt(allData[46]));
										ps_new_cbr.setInt(27,Integer.parseInt(allData[48]));
										ps_new_cbr.setInt(28,Integer.parseInt(allData[50]));
										ps_new_cbr.setString(29,"cbr");
										ps_new_cbr.executeUpdate();
									}
									else{
										ps_new_toraque.setString(1,allData[0]);
										ps_new_toraque.setDouble(2,Double.parseDouble(allData[2]));
										ps_new_toraque.setInt(10,Integer.parseInt(allData[4]));
										ps_new_toraque.setInt(11,Integer.parseInt(allData[6]));
										ps_new_toraque.setInt(3,Integer.parseInt(allData[8]));
										ps_new_toraque.setDouble(4,Double.parseDouble(allData[10]));
										ps_new_toraque.setDouble(5,Double.parseDouble(allData[12]));
										ps_new_toraque.setDouble(6,Double.parseDouble(allData[14]));
										ps_new_toraque.setDouble(7,Double.parseDouble(allData[16]));
										ps_new_toraque.setString(8,allData[18]);
										ps_new_toraque.setString(9,allData[20]);
										ps_new_toraque.setInt(12,Integer.parseInt(allData[22],16));
										ps_new_toraque.setInt(14,Integer.parseInt(allData[24],16));
										ps_new_toraque.setInt(13,Integer.parseInt(allData[26],16));
										ps_new_toraque.setInt(15,Integer.parseInt(allData[28],16));	
										ps_new_toraque.setInt(16,Integer.parseInt(allData[30].substring(0,allData[30].indexOf('.'))));
										ps_new_toraque.setInt(17,Integer.parseInt(allData[30].substring(allData[30].indexOf('.')+1)));
										ps_new_toraque.setInt(18,Integer.parseInt(allData[32].substring(0,allData[32].indexOf('.'))));
										ps_new_toraque.setInt(19,Integer.parseInt(allData[32].substring(allData[32].indexOf('.')+1)));
										ps_new_toraque.setString(20,allData[34]);
										ps_new_toraque.setInt(21,Integer.parseInt(allData[36]));
										ps_new_toraque.setInt(22,Integer.parseInt(allData[38]));
										ps_new_toraque.setInt(23,Integer.parseInt(allData[40]));
										ps_new_toraque.setInt(24,Integer.parseInt(allData[42]));
										ps_new_toraque.setString(25,allData[44]);
										ps_new_toraque.setInt(26,Integer.parseInt(allData[46].substring(2),16));
										ps_new_toraque.setInt(27,Integer.parseInt(allData[48]));
										ps_new_toraque.setString(28,allData[50]);
										ps_new_toraque.setString(29,"tora");
										ps_new_toraque.executeUpdate();
									}
								}
								/*
								 * j==53 indicates that the record is new_wireless_tcp format
								 */
								else if(j==53){
									ps_new_tcp.setString(1,allData[0]);
									ps_new_tcp.setDouble(2,Double.parseDouble(allData[2]));
									ps_new_tcp.setInt(10,Integer.parseInt(allData[4]));
									ps_new_tcp.setInt(11,Integer.parseInt(allData[6]));
									ps_new_tcp.setInt(3,Integer.parseInt(allData[8]));
									ps_new_tcp.setDouble(4,Double.parseDouble(allData[10]));
									ps_new_tcp.setDouble(5,Double.parseDouble(allData[12]));
									ps_new_tcp.setDouble(6,Double.parseDouble(allData[14]));
									ps_new_tcp.setDouble(7,Double.parseDouble(allData[16]));
									ps_new_tcp.setString(8,allData[18]);
									ps_new_tcp.setString(9,allData[20]);
									ps_new_tcp.setInt(12,Integer.parseInt(allData[22],16));
									ps_new_tcp.setInt(14,Integer.parseInt(allData[24],16));
									ps_new_tcp.setInt(13,Integer.parseInt(allData[26],16));
									ps_new_tcp.setInt(15,Integer.parseInt(allData[28],16));	
									ps_new_tcp.setInt(16,Integer.parseInt(allData[30].substring(0,allData[30].indexOf('.'))));
									ps_new_tcp.setInt(17,Integer.parseInt(allData[30].substring(allData[30].indexOf('.')+1)));
									ps_new_tcp.setInt(18,Integer.parseInt(allData[32].substring(0,allData[32].indexOf('.'))));
									ps_new_tcp.setInt(19,Integer.parseInt(allData[32].substring(allData[32].indexOf('.')+1)));
									ps_new_tcp.setString(20,allData[34]);
									ps_new_tcp.setInt(21,Integer.parseInt(allData[36]));
									ps_new_tcp.setInt(22,Integer.parseInt(allData[38]));
									ps_new_tcp.setInt(23,Integer.parseInt(allData[40]));
									ps_new_tcp.setInt(24,Integer.parseInt(allData[42]));
									ps_new_tcp.setString(25,allData[44]);
									ps_new_tcp.setInt(26,Integer.parseInt(allData[46]));
									ps_new_tcp.setInt(27,Integer.parseInt(allData[48]));
									ps_new_tcp.setInt(28,Integer.parseInt(allData[50]));
									ps_new_tcp.setInt(29,Integer.parseInt(allData[52]));
									ps_new_tcp.setString(30,"tcp");
									ps_new_tcp.executeUpdate();
								}
								/*
								 * j==54 indicates that the record is new_wireless_imep format
								 */
								else if(j==54){
									ps_new_imep.setString(1,allData[0]);
									ps_new_imep.setDouble(2,Double.parseDouble(allData[2]));
									ps_new_imep.setInt(10,Integer.parseInt(allData[4]));
									ps_new_imep.setInt(11,Integer.parseInt(allData[6]));
									ps_new_imep.setInt(3,Integer.parseInt(allData[8]));
									ps_new_imep.setDouble(4,Double.parseDouble(allData[10]));
									ps_new_imep.setDouble(5,Double.parseDouble(allData[12]));
									ps_new_imep.setDouble(6,Double.parseDouble(allData[14]));
									ps_new_imep.setDouble(7,Double.parseDouble(allData[16]));
									ps_new_imep.setString(8,allData[18]);
									ps_new_imep.setString(9,allData[20]);
									ps_new_imep.setInt(12,Integer.parseInt(allData[22],16));
									ps_new_imep.setInt(14,Integer.parseInt(allData[24],16));
									ps_new_imep.setInt(13,Integer.parseInt(allData[26],16));
									ps_new_imep.setInt(15,Integer.parseInt(allData[28],16));	
									ps_new_imep.setInt(16,Integer.parseInt(allData[30].substring(0,allData[30].indexOf('.'))));
									ps_new_imep.setInt(17,Integer.parseInt(allData[30].substring(allData[30].indexOf('.')+1)));
									ps_new_imep.setInt(18,Integer.parseInt(allData[32].substring(0,allData[32].indexOf('.'))));
									ps_new_imep.setInt(19,Integer.parseInt(allData[32].substring(allData[32].indexOf('.')+1)));
									ps_new_imep.setString(20,allData[34]);
									ps_new_imep.setInt(21,Integer.parseInt(allData[36]));
									ps_new_imep.setInt(22,Integer.parseInt(allData[38]));
									ps_new_imep.setInt(23,Integer.parseInt(allData[40]));
									ps_new_imep.setInt(24,Integer.parseInt(allData[42]));
									ps_new_imep.setString(25,allData[44]);
									ps_new_imep.setString(26,allData[46]);
									ps_new_imep.setString(27,allData[48]);
									ps_new_imep.setString(28,allData[50]);
									ps_new_imep.setInt(29,Integer.parseInt(allData[52].substring(2),16));
									ps_new_imep.setString(30,"imep");
									ps_new_imep.executeUpdate();
								}
								/*
								 * j==55 indicates that the record is new_wireless_toracle format
								 */
								else if(j==55){
									ps_new_toracle.setString(1,allData[0]);
									ps_new_toracle.setDouble(2,Double.parseDouble(allData[2]));
									ps_new_toracle.setInt(10,Integer.parseInt(allData[4]));
									ps_new_toracle.setInt(11,Integer.parseInt(allData[6]));
									ps_new_toracle.setInt(3,Integer.parseInt(allData[8]));
									ps_new_toracle.setDouble(4,Double.parseDouble(allData[10]));
									ps_new_toracle.setDouble(5,Double.parseDouble(allData[12]));
									ps_new_toracle.setDouble(6,Double.parseDouble(allData[14]));
									ps_new_toracle.setDouble(7,Double.parseDouble(allData[16]));
									ps_new_toracle.setString(8,allData[18]);
									ps_new_toracle.setString(9,allData[20]);
									ps_new_toracle.setInt(12,Integer.parseInt(allData[22],16));
									ps_new_toracle.setInt(14,Integer.parseInt(allData[24],16));
									ps_new_toracle.setInt(13,Integer.parseInt(allData[26],16));
									ps_new_toracle.setInt(15,Integer.parseInt(allData[28],16));	
									ps_new_toracle.setInt(16,Integer.parseInt(allData[30].substring(0,allData[30].indexOf('.'))));
									ps_new_toracle.setInt(17,Integer.parseInt(allData[30].substring(allData[30].indexOf('.')+1)));
									ps_new_toracle.setInt(18,Integer.parseInt(allData[32].substring(0,allData[32].indexOf('.'))));
									ps_new_toracle.setInt(19,Integer.parseInt(allData[32].substring(allData[32].indexOf('.')+1)));
									ps_new_toracle.setString(20,allData[34]);
									ps_new_toracle.setInt(21,Integer.parseInt(allData[36]));
									ps_new_toracle.setInt(22,Integer.parseInt(allData[38]));
									ps_new_toracle.setInt(23,Integer.parseInt(allData[40]));
									ps_new_toracle.setInt(24,Integer.parseInt(allData[42]));
									ps_new_toracle.setString(25,allData[44]);
									ps_new_toracle.setInt(26,Integer.parseInt(allData[46].substring(2),16));
									ps_new_toracle.setInt(27,Integer.parseInt(allData[48]));
									ps_new_toracle.setDouble(28,Double.parseDouble(allData[50]));
									ps_new_toracle.setInt(29,Integer.parseInt(allData[52]));
									ps_new_toracle.setString(30,allData[54]);
									ps_new_toracle.setString(31,"tora");
									ps_new_toracle.executeUpdate();
								}
								/*
								 * j==57 indicates that the record is new_wireless_aodvrep format
								 */
								else if(j==57){
									ps_new_aodvrep.setString(1,allData[0]);
									ps_new_aodvrep.setDouble(2,Double.parseDouble(allData[2]));
									ps_new_aodvrep.setInt(10,Integer.parseInt(allData[4]));
									ps_new_aodvrep.setInt(11,Integer.parseInt(allData[6]));
									ps_new_aodvrep.setInt(3,Integer.parseInt(allData[8]));
									ps_new_aodvrep.setDouble(4,Double.parseDouble(allData[10]));
									ps_new_aodvrep.setDouble(5,Double.parseDouble(allData[12]));
									ps_new_aodvrep.setDouble(6,Double.parseDouble(allData[14]));
									ps_new_aodvrep.setDouble(7,Double.parseDouble(allData[16]));
									ps_new_aodvrep.setString(8,allData[18]);
									ps_new_aodvrep.setString(9,allData[20]);
									ps_new_aodvrep.setInt(12,Integer.parseInt(allData[22],16));
									ps_new_aodvrep.setInt(14,Integer.parseInt(allData[24],16));
									ps_new_aodvrep.setInt(13,Integer.parseInt(allData[26],16));
									ps_new_aodvrep.setInt(15,Integer.parseInt(allData[28],16));	
									ps_new_aodvrep.setInt(16,Integer.parseInt(allData[30].substring(0,allData[30].indexOf('.'))));
									ps_new_aodvrep.setInt(17,Integer.parseInt(allData[30].substring(allData[30].indexOf('.')+1)));
									ps_new_aodvrep.setInt(18,Integer.parseInt(allData[32].substring(0,allData[32].indexOf('.'))));
									ps_new_aodvrep.setInt(19,Integer.parseInt(allData[32].substring(allData[32].indexOf('.')+1)));
									ps_new_aodvrep.setString(20,allData[34]);
									ps_new_aodvrep.setInt(21,Integer.parseInt(allData[36]));
									ps_new_aodvrep.setInt(22,Integer.parseInt(allData[38]));
									ps_new_aodvrep.setInt(23,Integer.parseInt(allData[40]));
									ps_new_aodvrep.setInt(24,Integer.parseInt(allData[42]));
									ps_new_aodvrep.setString(25,allData[44]);
									ps_new_aodvrep.setInt(26,Integer.parseInt(allData[46].substring(2),16));
									ps_new_aodvrep.setInt(27,Integer.parseInt(allData[48]));
									ps_new_aodvrep.setInt(28,Integer.parseInt(allData[50]));
									ps_new_aodvrep.setInt(29,Integer.parseInt(allData[52]));
									ps_new_aodvrep.setDouble(30,Double.parseDouble(allData[54]));
									ps_new_aodvrep.setString(31,allData[56]);
									ps_new_aodvrep.setString(32,"aodv");
									ps_new_aodvrep.executeUpdate();
								}
								/*
								 * j==61 indicates that the record is new_wireless_aodvreq format or new_wireless_tora format
								 */
								else if(j==61){
									boolean is_aodv=false;
									if(allData[47].equals("-Ph"))
										is_aodv=true;
									if(is_aodv){
										ps_new_aodvreq.setString(1,allData[0]);
										ps_new_aodvreq.setDouble(2,Double.parseDouble(allData[2]));
										ps_new_aodvreq.setInt(10,Integer.parseInt(allData[4]));
										ps_new_aodvreq.setInt(11,Integer.parseInt(allData[6]));
										ps_new_aodvreq.setInt(3,Integer.parseInt(allData[8]));
										ps_new_aodvreq.setDouble(4,Double.parseDouble(allData[10]));
										ps_new_aodvreq.setDouble(5,Double.parseDouble(allData[12]));
										ps_new_aodvreq.setDouble(6,Double.parseDouble(allData[14]));
										ps_new_aodvreq.setDouble(7,Double.parseDouble(allData[16]));
										ps_new_aodvreq.setString(8,allData[18]);
										ps_new_aodvreq.setString(9,allData[20]);
										ps_new_aodvreq.setInt(12,Integer.parseInt(allData[22],16));
										ps_new_aodvreq.setInt(14,Integer.parseInt(allData[24],16));
										ps_new_aodvreq.setInt(13,Integer.parseInt(allData[26],16));
										ps_new_aodvreq.setInt(15,Integer.parseInt(allData[28],16));	
										ps_new_aodvreq.setInt(16,Integer.parseInt(allData[30].substring(0,allData[30].indexOf('.'))));
										ps_new_aodvreq.setInt(17,Integer.parseInt(allData[30].substring(allData[30].indexOf('.')+1)));
										ps_new_aodvreq.setInt(18,Integer.parseInt(allData[32].substring(0,allData[32].indexOf('.'))));
										ps_new_aodvreq.setInt(19,Integer.parseInt(allData[32].substring(allData[32].indexOf('.')+1)));
										ps_new_aodvreq.setString(20,allData[34]);
										ps_new_aodvreq.setInt(21,Integer.parseInt(allData[36]));
										ps_new_aodvreq.setInt(22,Integer.parseInt(allData[38]));
										ps_new_aodvreq.setInt(23,Integer.parseInt(allData[40]));
										ps_new_aodvreq.setInt(24,Integer.parseInt(allData[42]));
										ps_new_aodvreq.setString(25,allData[44]);
										ps_new_aodvreq.setInt(26,Integer.parseInt(allData[46].substring(2),16));
										ps_new_aodvreq.setInt(27,Integer.parseInt(allData[48]));
										ps_new_aodvreq.setInt(28,Integer.parseInt(allData[50]));
										ps_new_aodvreq.setInt(29,Integer.parseInt(allData[52]));
										ps_new_aodvreq.setInt(30,Integer.parseInt(allData[54]));
										ps_new_aodvreq.setInt(31,Integer.parseInt(allData[56]));
										ps_new_aodvreq.setInt(32,Integer.parseInt(allData[58]));
										ps_new_aodvreq.setString(33,allData[60]);
										ps_new_aodvreq.setString(34,"aodv");
										ps_new_aodvreq.executeUpdate();
									}
									else{
										ps_new_toraupd.setString(1,allData[0]);
										ps_new_toraupd.setDouble(2,Double.parseDouble(allData[2]));
										ps_new_toraupd.setInt(10,Integer.parseInt(allData[4]));
										ps_new_toraupd.setInt(11,Integer.parseInt(allData[6]));
										ps_new_toraupd.setInt(3,Integer.parseInt(allData[8]));
										ps_new_toraupd.setDouble(4,Double.parseDouble(allData[10]));
										ps_new_toraupd.setDouble(5,Double.parseDouble(allData[12]));
										ps_new_toraupd.setDouble(6,Double.parseDouble(allData[14]));
										ps_new_toraupd.setDouble(7,Double.parseDouble(allData[16]));
										ps_new_toraupd.setString(8,allData[18]);
										ps_new_toraupd.setString(9,allData[20]);
										ps_new_toraupd.setInt(12,Integer.parseInt(allData[22],16));
										ps_new_toraupd.setInt(14,Integer.parseInt(allData[24],16));
										ps_new_toraupd.setInt(13,Integer.parseInt(allData[26],16));
										ps_new_toraupd.setInt(15,Integer.parseInt(allData[28],16));	
										ps_new_toraupd.setInt(16,Integer.parseInt(allData[30].substring(0,allData[30].indexOf('.'))));
										ps_new_toraupd.setInt(17,Integer.parseInt(allData[30].substring(allData[30].indexOf('.')+1)));
										ps_new_toraupd.setInt(18,Integer.parseInt(allData[32].substring(0,allData[32].indexOf('.'))));
										ps_new_toraupd.setInt(19,Integer.parseInt(allData[32].substring(allData[32].indexOf('.')+1)));
										ps_new_toraupd.setString(20,allData[34]);
										ps_new_toraupd.setInt(21,Integer.parseInt(allData[36]));
										ps_new_toraupd.setInt(22,Integer.parseInt(allData[38]));
										ps_new_toraupd.setInt(23,Integer.parseInt(allData[40]));
										ps_new_toraupd.setInt(24,Integer.parseInt(allData[42]));
										ps_new_toraupd.setString(25,allData[44]);
										ps_new_toraupd.setInt(26,Integer.parseInt(allData[46].substring(2),16));
										ps_new_toraupd.setInt(27,Integer.parseInt(allData[48]));
										ps_new_toraupd.setDouble(28,Double.parseDouble(allData[50]));
										ps_new_toraupd.setInt(29,Integer.parseInt(allData[52]));
										ps_new_toraupd.setInt(30,Integer.parseInt(allData[54]));
										ps_new_toraupd.setInt(31,Integer.parseInt(allData[56]));
										ps_new_toraupd.setInt(32,Integer.parseInt(allData[58]));
										ps_new_toraupd.setString(33,allData[60]);
										ps_new_toraupd.setString(34,"tora");
										ps_new_toraupd.executeUpdate();
									}
								}
								else if(j==67){
									ps_new_dsr.setString(1,allData[0]);
									ps_new_dsr.setDouble(2,Double.parseDouble(allData[2]));
									ps_new_dsr.setInt(10,Integer.parseInt(allData[4]));
									ps_new_dsr.setInt(11,Integer.parseInt(allData[6]));
									ps_new_dsr.setInt(3,Integer.parseInt(allData[8]));
									ps_new_dsr.setDouble(4,Double.parseDouble(allData[10]));
									ps_new_dsr.setDouble(5,Double.parseDouble(allData[12]));
									ps_new_dsr.setDouble(6,Double.parseDouble(allData[14]));
									ps_new_dsr.setDouble(7,Double.parseDouble(allData[16]));
									ps_new_dsr.setString(8,allData[18]);
									ps_new_dsr.setString(9,allData[20]);
									ps_new_dsr.setInt(12,Integer.parseInt(allData[22],16));
									ps_new_dsr.setInt(14,Integer.parseInt(allData[24],16));
									ps_new_dsr.setInt(13,Integer.parseInt(allData[26],16));
									ps_new_dsr.setInt(15,Integer.parseInt(allData[28],16));	
									ps_new_dsr.setInt(16,Integer.parseInt(allData[30].substring(0,allData[30].indexOf('.'))));
									ps_new_dsr.setInt(17,Integer.parseInt(allData[30].substring(allData[30].indexOf('.')+1)));
									ps_new_dsr.setInt(18,Integer.parseInt(allData[32].substring(0,allData[32].indexOf('.'))));
									ps_new_dsr.setInt(19,Integer.parseInt(allData[32].substring(allData[32].indexOf('.')+1)));
									ps_new_dsr.setString(20,allData[34]);
									ps_new_dsr.setInt(21,Integer.parseInt(allData[36]));
									ps_new_dsr.setInt(22,Integer.parseInt(allData[38]));
									ps_new_dsr.setInt(23,Integer.parseInt(allData[40]));
									ps_new_dsr.setInt(24,Integer.parseInt(allData[42]));
									ps_new_dsr.setString(25,allData[44]);
									ps_new_dsr.setInt(26,Integer.parseInt(allData[46]));
									ps_new_dsr.setInt(27,Integer.parseInt(allData[48]));
									ps_new_dsr.setInt(28,Integer.parseInt(allData[50]));
									ps_new_dsr.setInt(29,Integer.parseInt(allData[52]));
									ps_new_dsr.setInt(30,Integer.parseInt(allData[54]));
									ps_new_dsr.setInt(31,Integer.parseInt(allData[56]));
									ps_new_dsr.setInt(32,Integer.parseInt(allData[58].substring(0,allData[58].indexOf('-'))));
									ps_new_dsr.setInt(33,Integer.parseInt(allData[58].substring(allData[58].indexOf('-')+2)));
									ps_new_dsr.setInt(34,Integer.parseInt(allData[60]));
									ps_new_dsr.setInt(35,Integer.parseInt(allData[62]));
									ps_new_dsr.setInt(36,Integer.parseInt(allData[64]));
									ps_new_dsr.setInt(37,Integer.parseInt(allData[66].substring(0,allData[66].indexOf('-'))));
									ps_new_dsr.setInt(38,Integer.parseInt(allData[66].substring(allData[66].indexOf('-')+2)));
									ps_new_dsr.setString(39,"dsr");
									ps_new_dsr.executeUpdate();
								}
							}
							else if(allData[0].equals("M")){
								if(j==9){
									try{
										double time=Double.parseDouble(allData[1]);
										int id=Integer.parseInt(allData[2]);
										double x=Double.parseDouble(allData[3].substring(1,allData[4].length()-1));
										double y=Double.parseDouble(allData[4].substring(0,allData[4].length()-1));
										double z=Double.parseDouble(allData[5].substring(0,allData[5].length()-2));
										double dx=Double.parseDouble(allData[6].substring(1,allData[6].length()-1));
										double dy=Double.parseDouble(allData[7].substring(0,allData[7].length()-2));
										double speed=Double.parseDouble(allData[8]);
										ps_movement.setString(1,allData[0]);
										ps_movement.setDouble(2,time);
										ps_movement.setInt(3,id);
										ps_movement.setDouble(4,x);
										ps_movement.setDouble(5,y);
										ps_movement.setDouble(6,z);
										ps_movement.setDouble(7,dx);
										ps_movement.setDouble(8,dy);
										ps_movement.setDouble(9,speed);
										ps_movement.executeUpdate();
									}
									catch(Exception e1){
										System.out.println(i);
									}
								}											
							}
							else if(allData[0].equals("N")){
								if(j==7){
									try{
										double time=Double.parseDouble(allData[2]);
										int id=Integer.parseInt(allData[4]);
										double energy=Double.parseDouble(allData[6]);
										ps_new_energy.setString(1,allData[0]);
										ps_new_energy.setDouble(2,time);
										ps_new_energy.setInt(3,id);
										ps_new_energy.setDouble(4,energy);
										ps_new_energy.setString(5,"ener");
										ps_new_energy.executeUpdate();
									}
									catch(Exception e1){
										System.out.println(i);
									}
								}
							}
							else
								System.out.println(i);
						}
						catch (IOException e) {
							is_right_format=false;
							progress.dispose();
							e.printStackTrace();
							return;
						}
						catch(Exception e){
							e.printStackTrace();
							is_right_format=false;
							progress.dispose();
							JOptionPane.showMessageDialog(null,"Error detected at row "+i+" of "+file.getName()+" !","Error Message",JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					i++;
				}
				else
					break;
				
			}
			try {
				/*
				 * release the sources
				 */
				allData=null;
				br.close();
				filereader.close();
			}  catch (IOException e){
				e.printStackTrace();
			}
    	}
    	
    	/*all the threads should increase the handledRowNum synchronized
    	 *otherwise, we can not get the right number of handled rows
    	 * 
    	 */
    	public synchronized void IncHandledRowNum(){
    		handledRowNum++;
    	}
    }
}
