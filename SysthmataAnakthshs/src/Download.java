import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import twitter4j.Query;

public class Download {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DateFormat d=new SimpleDateFormat("YYYY-MM-dd");
		Date d1=new Date(System.currentTimeMillis()-7L*24*3600*1000);
		String date=d.format(d1);
		System.out.println(date);
		Query query =new Query("#Syriza OR #Syrizanel OR #Συριζα OR #Συριζανελ OR #syriza_gr AND(-#ND AND -#neadhmokratia AND -#NewDemocrasy AND -#ΝΔ) -filter:retweets");
		ArrayList<Query> queries=new ArrayList<Query>();
		queries.add(query);
		queries.add(new Query("@atsipras -filter:retweets"));//.since(date));
		queries.add(new Query("#nd OR #neadhmokratia OR #NewDemocracy OR #νεα_δημοκρατια AND (-#Syriza AND -#Syrizanel AND -#Συριζα AND -#Συριζανελ) -filter:retweets"));
		queries.add(new Query("@kmitsotakis -#neadimokratia -#νεαδημοκρατια -#syriza -filter:retweets"));
		sap aa=new sap();
		for(int i=0; i<queries.size(); i++){
			aa.DownloadTweets(queries.get(i), i);
		}
	}

}
