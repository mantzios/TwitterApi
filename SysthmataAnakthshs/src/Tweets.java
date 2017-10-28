
public class Tweets {
	long id;
	String name;
	String tweet;
	String date;
	boolean retweet;
	String hastag;
	
	public Tweets(long id,String name,String tweet,String date,boolean retweet,String hastag){
		this.id=id;
		this.name=name;
		this.tweet=tweet;
		this.date=date;
		this.retweet=retweet;
		this.hastag=hastag;
	}
	
	public Tweets(){
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTweet() {
		return tweet;
	}

	public void setTweet(String tweet) {
		this.tweet = tweet;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public boolean isRetweet() {
		return retweet;
	}

	public void setRetweet(boolean retweet) {
		this.retweet = retweet;
	}

	public String getHastag() {
		return hastag;
	}

	public void setHastag(String hastag) {
		this.hastag = hastag;
	}
	
	
}
