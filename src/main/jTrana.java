package src.main;

import java.util.Calendar;

import javax.swing.JOptionPane;

import src.gui.MainFrame;

public class jTrana {
	
	  public static void main(String[] args){
		Thread.currentThread().setName("jTrana");
	   com.incors.plaf.alloy.AlloyLookAndFeel.setProperty("alloy.licenseCode", "C#iamqianhh@gmail.com#1ulp8z6#7muhkk");
	try {
	  javax.swing.LookAndFeel alloyLnF = new com.incors.plaf.alloy.AlloyLookAndFeel();
	  javax.swing.UIManager.setLookAndFeel(alloyLnF);
	}  
	    catch ( Exception e ) {
	    	JOptionPane.showMessageDialog(null,"Unexpected error.\nProgram Terminated!","Error Message",JOptionPane.ERROR_MESSAGE);
		    System.out.println ("Unexpected error. \nProgram Terminated");
		    e.printStackTrace();
		    System.exit(0);
	    }
	    Calendar dateTime=Calendar.getInstance();
    	String day=String.valueOf(dateTime.get(Calendar.DAY_OF_MONTH));
    	String hour=String.valueOf(dateTime.get(Calendar.HOUR_OF_DAY));
    	String minute=String.valueOf(dateTime.get(Calendar.MINUTE));
    	String second=String.valueOf(dateTime.get(Calendar.SECOND));
    	String tablePrefix=System.getProperty("user.name")+day+hour+minute+second;
    	System.out.println("tablePrefix: "+tablePrefix);
		try {
	      Class.forName("com.mysql.jdbc.Driver");
//	      System.out.println("Success loading MySQL Driver...");
	    }
	    catch (Exception e) {
	    	JOptionPane.showMessageDialog(null,"Error loading MySQL Driver...","Error Message",JOptionPane.ERROR_MESSAGE);
	        System.out.println("Error loading MySQL Driver...");
	        e.printStackTrace();
	        System.exit(1);
	    }
	    new MainFrame(tablePrefix);  	    
	  } // end of main
	}//end of class LookAndFeel