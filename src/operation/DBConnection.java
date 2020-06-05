package src.operation;

import java.sql.Connection;
import java.sql.DriverManager;

import javax.swing.JOptionPane;

public class DBConnection {
	String url;
	String userName;
	String password;
	public DBConnection(String url,String userName,String password){
		this.url=url;
		this.userName=userName;
		this.password=password;
		try {
		      Class.forName("com.mysql.jdbc.Driver");
//		      System.out.println("Success loading MySQL Driver...");
		    }
		    catch (Exception e) {
		    	JOptionPane.showMessageDialog(null,"Error loading MySQL Driver...","Error Message",JOptionPane.ERROR_MESSAGE);
		        System.out.println("Error loading MySQL Driver...");
		        e.printStackTrace();
		        System.exit(1);
		    }
	}
	public Connection makeConnection(){
		Connection con=null;
		try{
			con=DriverManager.getConnection(url,userName,password);
		}catch(Exception e){
//			JOptionPane.showMessageDialog(null,"Error connecting to the "+url,"Error Message",JOptionPane.ERROR_MESSAGE);
//			e.printStackTrace();
		}
		return con;
	}

}
