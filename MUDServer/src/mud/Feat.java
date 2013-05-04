package mud;

public class Feat {
	public static final Feat ap_chainmail = new Feat("proficiency armor chain_mail");
	
	String data;
	
	// types - proficiency, skill boost
	// ex. proficiency armor chain_mail
	Feat(String featString) {
		this.data = featString;
	}

}
