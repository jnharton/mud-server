package mud;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class Feat {
	public static final Feat ap_chainmail = new Feat("proficiency armor chain_mail");
	
	String data;
	
	String type;    // feat type ex. 'proficiency' which would a feat granting the proficient use of certain items
	String subtype; // a sub category of type
	String other;   // information about the feat ex. 'chain_mail' which is a kind of armor, possibly an AP feat
	String name;    // name of the feat ex. 'ARMOR PROFICIENCY: chain_mail'
	
	// types - proficiency, skill boost
	// ex. proficiency armor chain_mail
	Feat(String featString) {
		this.data = featString;
		
		String[] temp = this.data.split(" ");
		
		this.type = temp[0];
		this.subtype = temp[1];
		this.other = temp[2];
		
		this.name = temp[1].toUpperCase() + " " + temp[0].toUpperCase() + ": " + temp[2];
	}

}
