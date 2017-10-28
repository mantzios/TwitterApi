import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

import twitter4j.Query;
import twitter4j.Status;

public class Test {

	public static void main(String[] args) {
		
		sap a=new sap();
		db_connect db=new db_connect();
		//a.analyzeTweets(false);
		//a.GetResults();
		//a.LineChart();
		a.ResultsSVD(3);
		
	}

}
