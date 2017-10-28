import twitter4j.*;
import twitter4j.conf.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
import org.jfree.chart.JFreeChart; 
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.ChartUtilities; 
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import org.jblas.DoubleMatrix;
import org.jblas.Singular;

public class sap {
	private db_connect aa=new db_connect();
	private boolean flag=true;
	private ArrayList<Status> syriza=new ArrayList<Status>();
	private ArrayList<Status> nd=new ArrayList<Status>();
	private ArrayList<String> stopWords=new ArrayList<String>();
	private ArrayList<String> NegativeWords=new ArrayList<String>();
	private ArrayList<String> PositiveWords=new ArrayList<String>();
	private ArrayList<String> hastag = new ArrayList<String>();
	private ArrayList<String> status = new ArrayList<String>();
	private ArrayList<String> NewNeq=new ArrayList<String>();
	private ArrayList<String> NewPos=new ArrayList<String>();
	private int [] freqPos;
	private int [] freqNeg;
	private double[][] termXdoc;
	private double[][] U;
	
	public sap(){
		hastag.add("tsipras");
		hastag.add("mhtsotakhs");
		hastag.add("syriza");
		hastag.add("nd");
		status.add("positive");
		status.add("negative");
		status.add("neutral");
		stopWords=readFromTxt("StopWords.txt");
		NegativeWords=readFromXls("NegLex.txt");
		PositiveWords=readFromXls("PosLex.txt");
		freqPos=new int[PositiveWords.size()];
		freqNeg=new int[NegativeWords.size()];
	}
	
	public void DownloadTweets(Query query,int i)
	{	
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("Pk7baE6OObZJOzSa9uNC87FOh")
		  .setOAuthConsumerSecret("JIQIJBKRkVk9EDZ6PRnczq6bEN4zE3W9KbIlzieWnJFgtUEnvI")
		  .setOAuthAccessToken("795968398610022400-ZXYDHqDBSAwRBqqgf9rlxZiPYrixtHY")
		  .setOAuthAccessTokenSecret("Dty61AX0mTcFIXOKwb9LO0FBY95p54J9RtU2u4NdixIFp");
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		
		try{
			while(true){
				query.setCount(100);
				QueryResult result;
				result=twitter.search(query);
				List<Status> tweets =result.getTweets();
				
				insert(tweets,i,flag);
				if(tweets.size()!=0)
					System.out.println("Download "+tweets.size()+" tweets");
				
				if (result.hasNext()){
					query=result.nextQuery();
				}else
					break;
				
			}
		}catch(TwitterException e){
			e.printStackTrace();
		}
		
	}
	
	public void analyzeTweets(boolean flag) {
		ArrayList<Tweets> tweet=new ArrayList<Tweets>();
		String status;
		HashMap<String,Long> detect=new HashMap<String,Long>();
		for(int i=0; i<hastag.size(); i++){
			tweet=aa.select(hastag.get(i));
			for(int j=0; j<tweet.size(); j++){
				if(!tweet.get(j).isRetweet()){
					String temp="";
					if(tweet.get(j).getTweet()!=null){
						detect.put(tweet.get(j).getTweet(),tweet.get(j).getId());
						temp=removeTonous(tweet.get(j).getTweet().toUpperCase()).replaceAll("[^á-ùÁ-Ù\\\\s]", " ").replaceAll("\\s+", " ");
						temp=RemoveMetaData(temp);
						if(detect.containsKey(temp) && flag){
							DeleteFromDb(tweet.get(j).getId());
						}else{	
							detect.put(temp,tweet.get(j).getId());
							temp=RemoveStopWords(temp);
							temp=Stemming(temp);
							status=AnalyzeTweet(temp);
							UpdateStatusDb(tweet.get(j).getId(),status);
							if(!temp.equals("")){
								//DeleteFromDb(tweet.get(j).getId());
								UpdateTweet(temp,tweet.get(j).getId());
							}else{
								DeleteFromDb(tweet.get(j).getId());
							}
							
						}
					}	
					
				}	
			}
		}
		WriteToFile(0);
		WriteToFile(1);		
	}
	
	private void UpdateTweet(String tweet,Long id){
		aa.UpdateTweetDb(id, tweet);
	}
	
	private void SVD(){
		ArrayList<Tweets> tweet=new ArrayList<Tweets>();
		for(int i=0; i<hastag.size(); i++){
			tweet.addAll(aa.select(hastag.get(i)));
		}	
		int doc=tweet.size();
		HashMap<String,Integer> term=new HashMap<String,Integer>();
		for(int i=0; i<tweet.size(); i++){
			String temp=tweet.get(i).getTweet();
			if(temp!=null){
				StringTokenizer st=new StringTokenizer(temp);
				while(st.hasMoreTokens()){
					String s=st.nextToken();
					if(term.containsKey(s)){
						int k=term.get(s);
						k++;
						term.put(s,k);
					}else{
						term.put(s,0);
					}
				}
			}
		}	
		ArrayList<String> words=new ArrayList<String>();
		
		Iterator it=term.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String,Integer> pair=(Map.Entry<String,Integer>)it.next();
			if(pair.getValue()>2){
				words.add(pair.getKey());
				
			}
			
		}
		WriteWords(words);
		termXdoc=new double[words.size()][doc];
		for(int i=0; i<words.size(); i++){
			for(int j=0; j<tweet.size(); j++){
				if(tweet.get(j).getTweet().contains(words.get(i))){
					termXdoc[i][j]++;
				}
			}
		}
		
		DoubleMatrix a=new DoubleMatrix(termXdoc);
		DoubleMatrix USV[]=Singular.fullSVD(a);
		U=USV[0].mmul(USV[0].transpose()).toArray2();
	}

	
	
	private void WriteWords(ArrayList<String> words) {
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			fw = new FileWriter("Words.txt");
			bw = new BufferedWriter(fw);
			for(int i=0; i<words.size(); i++){
				bw.write(words.get(i));
				bw.newLine();
			}	

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}	
	}

	private void WriteWordsFromSVD(ArrayList<String> words,int k) {
		BufferedWriter bw = null;
		FileWriter fw = null;
		String name;
		if (k==0){
			name="NewPos.txt";
		}else{
			name="NewNeg.txt";
		}
		try {
			fw = new FileWriter(name);
			bw = new BufferedWriter(fw);
			for(int i=0; i<words.size(); i++){
				bw.write(words.get(i));
				bw.newLine();
			}	

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}	
	}
	

	public void WriteToFile(int k){
		BufferedWriter bw = null;
		FileWriter fw = null;
		String file=null;
		int number=0;
		if(k==0){
			file="freqPos.txt";
			number=PositiveWords.size();
		}else{
			file="freqNeg.txt";
			number=NegativeWords.size();
		}
		try {
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			for(int i=0; i<number; i++){
				if(k==0){
					bw.write(PositiveWords.get(i)+" "+freqPos[i]);
					bw.newLine();
				}else{
					bw.write(NegativeWords.get(i)+" "+freqNeg[i]);
					bw.newLine();
				}
			}	

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}	
	}
	
	
	public void GetResults(){
		for(int i=0; i<hastag.size(); i++){
			for(int j=0; j<status.size(); j++){
				System.out.println("There are "+aa.GetResult(status.get(j), hastag.get(i))+" "+status.get(j)+" tweets for "+hastag.get(i));
			}
		}
		//mean std gia ka8e kathgoria
		System.out.println();
		ArrayList<Graph> graph=new ArrayList<Graph>();
		int countPositive=0;
		int countNegative=0;
		for(int i=0; i<hastag.size()-1; i++){
			graph=aa.Graph(hastag.get(i));
			for (int j=0; j<graph.size(); j++){
				if(graph.get(j).getStatus().equals("positive")){
					countPositive+=graph.get(j).getCount();
				}
				if(graph.get(j).getStatus().equals("negative")){
					countNegative+=graph.get(j).getCount();
				}
			}
			System.out.println("Average Positive for "+hastag.get(i)+": "+countPositive/(graph.size()/3.0));
			System.out.println("Average Negative for "+hastag.get(i)+": "+countNegative/(graph.size()/3.0));
			int stdPos=0;
			int stdNeg=0;
			for (int j=0; j<graph.size()-1; j++){
				if(graph.get(j).getStatus().equals("positive")){
					stdPos+=Math.pow(graph.get(j).getCount()-countPositive/(graph.size()/3.0),2);
				}
				if(graph.get(j).getStatus().equals("negative")){
					stdNeg+=Math.pow(graph.get(j).getCount()-countNegative/(graph.size()/3.0),2);
				}
			}
			System.out.println("Standard Deviation Positive for "+hastag.get(i)+": "+Math.pow(stdPos/(graph.size()/3.0),0.5));
			System.out.println("Standard Deviation Negative for "+hastag.get(i)+": "+Math.pow(stdNeg/(graph.size()/3.0),0.5));
			System.out.println();
			countPositive=0;
			countNegative=0;
			stdPos=0;
			stdNeg=0;
			graph.clear();
		}
	}
	
	
	private void UpdateStatusDb(long id, String status) {
		aa.UpdateStatusDb(id, status);
		
	}

	private void DeleteFromDb(long id) {
		// TODO Auto-generated method stub
		aa.DeleteFromDb(id);
		
	}

	private String RemoveStopWords(String tweet) {
		String temp="";
		StringTokenizer st=new StringTokenizer(tweet);
		boolean flag=true;
		while(st.hasMoreTokens()){
			String s=st.nextToken();
			for(int i=0; i<stopWords.size(); i++){
				if(s.equals(stopWords.get(i))){
					flag=false;
					break;
				}
			}
			if(flag){
				temp+=" "+s;
				
			}
			flag=true;
		}
		
		return temp;
	}

	private String AnalyzeTweet(String temp) {
		String status=null;
		int positive=0;
		int negative=0;
		for(int i=0; i<PositiveWords.size(); i++){
			if(temp.contains(PositiveWords.get(i))){
				positive++;
				freqPos[i]++;
			}
		}
		for(int i=0; i<NegativeWords.size(); i++){
			if(temp.contains(NegativeWords.get(i))){
				negative++;
				freqNeg[i]++;
			}
		}
		
		if(positive>negative){
			status="positive";
		}else if(positive<negative){
			status="negative";
		}else{
			status="neutral";
		}
		return status;
	}

	private String Stemming(String tweet) {
		GreekStemmer stem=new GreekStemmer();
		StringTokenizer st=new StringTokenizer(tweet);
		String temp="";
		while(st.hasMoreTokens()){
			String s=st.nextToken();
			temp+=" "+stem.stem(s);
		}
		return temp;
	}

	private String RemoveMetaData(String tweet) {
		StringTokenizer st=new StringTokenizer(tweet);
		String temp="";
		while(st.hasMoreTokens()){
			String s=st.nextToken();
			if(!(s.contains("HTTPS") || s.contains("@") || s.contains("#"))){
				temp+=" "+s;
			}
		}
		return temp;
	}

	private ArrayList<String> readFromTxt(String file){
		ArrayList<String> temp= new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			while((line = in.readLine()) != null)
			{
			    temp.add(line);
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return temp;
	}

	
	public void ResultsSVD(int p){
		ArrayList<String> words=new ArrayList<String>();
		words=readFromTxt("Words.txt");
		SVD();
		double[][] temp=U;
		findPMax(p,temp,words);
		int count=0;
		for(int i=0; i<NegativeWords.size(); i++){
			for(int j=0; j<NewNeq.size(); j++){
				if(NegativeWords.get(i).equals(NewNeq.get(j))){
					count++;
				}
			}
		}
		int count1=0;
		for(int i=0; i<PositiveWords.size(); i++){
			for(int j=0; j<NewPos.size(); j++){
				if(PositiveWords.get(i).equals(NewPos.get(j))){
					count1++;
				}
			}
		}
		for(int m=0; m<NewNeq.size(); m++){
			for(int l=0; l<NewNeq.size(); l++){
				if (l!=m) {
					if(NewNeq.get(m).equals(NewNeq.get(l))){
						NewNeq.remove(m);
					}
				}
			}
		}
		
		for(int m=0; m<NewPos.size(); m++){
			for(int l=0; l<NewPos.size(); l++){
				if (l!=m) {
					if(NewPos.get(m).equals(NewPos.get(l))){
						NewPos.remove(m);
					}
				}
			}
		}
		
		WriteWordsFromSVD(NewPos,0);
		WriteWordsFromSVD(NewNeq,1);
		System.out.println("Same Negative Words "+count);
		System.out.println("Same Positive Words "+count1);
		System.out.println("Size of new negative words "+NewNeq.size());
		System.out.println("Size of new positive words "+NewPos.size());
		System.out.println("Ratio of negative: "+((double)count/(double)(NewNeq.size()))*100+"%");
		System.out.println("Ratio of positive: "+((double)count1/(double)(NewPos.size()))*100+"%");
	}
	
	
	
	private void findPMax(int p, double[][] temp2, ArrayList<String> words) {
		double [] temp;
		int count=0;
		for(int i=0; i<temp2[0].length; i++){
			temp=new double[temp2[0].length];
			System.arraycopy(temp2[i], 0, temp, 0, temp2[0].length);
			Arrays.sort(temp);
			int [] pos=new int[p];
			for(int j=0; j<temp2[1].length; j++){
				for(int m=0; m<p; m++){
					if(temp2[i][j]==temp[temp.length-2-m]){
						pos[m]=j;
					}
				}
			}
			//exoume vrei poies einai oi 8eseis 
			for(int m=0; m<PositiveWords.size(); m++){
				if (words.get(i).equals(PositiveWords.get(m))){
					for(int k=0; k<pos.length; k++){
						NewPos.add(words.get(pos[k]));
					}
				}
				
			}	
			
			for(int m=0; m<NegativeWords.size(); m++){
				if (words.get(i).equals(NegativeWords.get(m))){
					count++;
					for(int k=0; k<pos.length; k++){
						NewNeq.add(words.get(pos[k]));
					}
				}
			}	
		}
		System.out.println(count);
		
	}

	private ArrayList<String> readFromXls(String file){
		ArrayList<String> temp= new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			line=in.readLine();
			while((line = in.readLine()) != null)
			{
			    StringTokenizer st=new StringTokenizer(line,"\t");
			    st.nextToken();
			    st.nextToken();
			    temp.add(st.nextToken());
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		    
		
		return temp;
	}
	
	
	private  void insert(List<Status> tweets,int i, boolean flag){
		if (flag){
			insertDB(tweets,i);
		}	
		else{
			insertList(tweets,i);
		}	
	}
	
	private  void insertList(List<Status> tweets, int i){
		if(i!=2)
			syriza.addAll(tweets);
		else
			nd.addAll(tweets);
	}
	
	private  void insertDB(List<Status> tweets,int i){
		String komma;
		if(i==0) 
			komma="syriza";
		else if (i==2)
			komma="nd";
		else if (i==1){
			komma="tsipras";
		}
		else
			komma="mhtsotakhs";
		for(Status tweet:tweets){
			
			aa.insert(tweet.getId(),tweet.getUser().getScreenName(),tweet.getText(),tweet.getCreatedAt(),tweet.isRetweet(),komma);
		}
	}

	public ArrayList<Status> getSyrizaTweets() {
			return syriza;
	}	
	
	public ArrayList<Status> getNDTweets() {
		return nd;
	}
	private String removeTonous(String a){
		ArrayList<Character> letters =new ArrayList<Character>();
		ArrayList<Character> l =new ArrayList<Character>();
		letters.add('¢');
		letters.add('¸');
		letters.add('¹');
		letters.add('º');
		letters.add('¼');
		l.add('Á');
		l.add('Å');
		l.add('Ç');
		l.add('É');
		l.add('Ï');
		boolean flag=false;
		String n="";
	 
		for(int i=0; i<a.length(); i++){
			for(int j=0; j<letters.size(); j++){
				if((a.charAt(i))==(letters.get(j))){
					n+=(l.get(j));
					flag=true;
					break;
				}	 
			}
			if (!flag){
				n+=a.charAt(i);
			}			
			flag=false;
			
			
		}
		return n;
	} 
	
	public void LineChart(){
		ArrayList<Graph> graph=new ArrayList<Graph>();
		for(int j=0; j<hastag.size(); j++){
			graph=aa.Graph(hastag.get(j));
			DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
			for(int i=0; i<graph.size(); i++){  
				line_chart_dataset.addValue(graph.get(i).getCount(),graph.get(i).getStatus(), graph.get(i).getDate() );
			} 
			JFreeChart lineChartObject = ChartFactory.createLineChart(
	         hastag.get(j),"Date",
	         "Count",
	         line_chart_dataset,PlotOrientation.VERTICAL,
	         true,true,false);

			int width = 1024; /* Width of the image */
			int height = 480; /* Height of the image */ 
			File lineChart = new File( hastag.get(j)+".jpeg" ); 
			try {
				ChartUtilities.saveChartAsJPEG(lineChart ,lineChartObject, width ,height);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}  
	}
	
	
}
