package src.operation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import javax.swing.JOptionPane;



public class Query {
	private String SQLString;
	public Connection con;
	public Query(Connection con,String SQLString){
		this.con=con;
		this.SQLString=SQLString;		
	}
    public ResultSet doQuery(){
    	if(SQLString==null)
    		return null;
    	try {
			Statement stmt1=con.createStatement();
			boolean isQuery=stmt1.execute(SQLString);
			if(isQuery){
				return stmt1.getResultSet();
			}
			else 
				System.out.println("update or insert "+stmt1.getUpdateCount()+" rows");			
		} catch (SQLException e) {
			// TODO 自动生成 catch 块
			JOptionPane.showMessageDialog(null,"SQL statement has errors! \nPlease check it!","Error Message",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		return null;
    }    
}
