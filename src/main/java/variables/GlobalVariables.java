//This file consists of variables used throughout the project

package variables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class GlobalVariables {
	public Statement stmt;
	public Connection conn;
	public ResultSet rs;
	public String strSQLite = "org.sqlite.JDBC";
	public String strDBPath = "jdbc:sqlite:C:\\Users\\Nilesh\\eclipse-workspace\\IMDB_Top250-master\\IMDB_Top250-master\\src\\test\\resources\\IMDB.sqlite";
	public String strQuery;
	public String strFilePath = "C:\\Users\\Nilesh\\eclipse-workspace\\IMDB_Top250-master\\IMDB_Top250-master\\src\\test\\resources\\IMDB_Top_250_Movie_List.csv";
}
