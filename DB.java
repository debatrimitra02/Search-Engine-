import java.sql.*;

public class DB{

	static final String driverJDBC = "com.mysql.jdbc.Driver";
	static final String urlDB = "jdbc:mysql://localhost:3306/index_db";
	static final String userNmae = "root";
	static final String password = "root";
	static Connection connection = null;
	static Statement statement = null;
	
	public static void setup(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(urlDB, userNmae, password);
			
			statement = connection.createStatement();
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static Statement getStatement(){
		return statement;
	}
	
	public static void close(){
		try{
			if(connection != null){
				connection.close();
			}
			if(statement != null){
				statement.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
