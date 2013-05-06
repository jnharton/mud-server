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
	
	// types - proficiency, skill boost
	// ex. proficiency armor chain_mail
	Feat(String featString) {
		this.data = featString;
	}

}
