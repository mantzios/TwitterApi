import java.sql.Connection;
	import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
	
	
public class db_connect {
	private Connection connect;
	private java.sql.PreparedStatement statement;
	private java.sql.Statement statement1;
	String query;
	private String dburl="jdbc:mysql://127.0.0.1:3306/sap?user=root&password=cho1797&useSSL=false";
	private ResultSet result;
	
	public db_connect(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection(dburl);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void insert(long id,String name,String text,Date date,boolean retweet,String komma){
		java.sql.Date x=new java.sql.Date(date.getTime());
		try{
			String update="insert ignore into tweets(id,name_f,tweet,date,retweet,hastag)"+ "values(?,?,?,?,?,?)";
			statement = connect.prepareStatement(update);
			//query="insert ignore into tweets values('"+id+"','"+name+"','"+text+"','" +date.toString()+"');";
			//statement.executeUpdate(query);
			statement.setLong(1, id);
			statement.setString(2, name);
			statement.setString(3, text);
			statement.setDate(4,x);
			statement.setBoolean(5, retweet);
			statement.setString(6, komma);
			statement.execute();
		}catch(SQLException e){
			System.err.println("Cannot Connect to database");
			e.getLocalizedMessage();
			System.out.println(e);
		}
	}
	
	public ArrayList<Tweets> select(String hastag){
		ArrayList<Tweets> tt=new ArrayList<Tweets>();
		try {
			
			statement1 = connect.createStatement();
			query= "select * from tweets where hastag="+"'"+hastag+"';";
			result = statement1.executeQuery(query);
			tt.clear();
			while(result.next()){
				tt.add(new Tweets(result.getLong(1),result.getString(2),result.getString(3),result.getString(4),result.getBoolean(5),result.getString(6)));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tt;
	}


	public void DeleteFromDb(long id) {
			try {
				statement1 = connect.createStatement();
				query= "delete from tweets where id="+"'"+id+"';";
				statement1.execute(query);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	public void UpdateStatusDb(long id,String status){
		String updateStatement="update tweets set status = ?"+"where id = ?";
		try {
			statement = connect.prepareStatement(updateStatement);
			statement.setString(1,status);
			statement.setLong(2, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void UpdateTweetDb(long id,String tweet){
		String updateStatement="update tweets set tweet = ?"+"where id = ?";
		try {
			statement = connect.prepareStatement(updateStatement);
			statement.setString(1,tweet);
			statement.setLong(2, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Graph> Graph(String hastag){
		ArrayList<Graph> graph=new ArrayList<Graph>();
		try {
			statement1=connect.createStatement();
			query= "select date,status,count(status) from tweets where hastag="+"'"+hastag+"'"+"group by status,date order by date";
			result=statement1.executeQuery(query);
			graph.clear();
			while(result.next()){
				graph.add(new Graph(result.getInt(3),result.getString(2),result.getString(1)));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return graph;
		
	}
	
	public int GetResult(String status,String hastag){
		int temp=0;
		try {
			statement1 = connect.createStatement();
			query= "select count(*) from tweets where hastag="+"'"+hastag+"'"+" and status="+"'"+status+"';";
			result = statement1.executeQuery(query);
			result.next();
			temp=result.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;
	}


	
	
	
}
