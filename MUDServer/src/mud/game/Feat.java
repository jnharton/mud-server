package mud.game;

import java.util.Arrays;
import java.util.List;

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
	public static final Feat ap_light = new Feat("proficiency armor light");
	public static final Feat ap_medium = new Feat("proficiency armor medium");
	public static final Feat ap_heavy = new Feat("proficiency armor heavy", new Feat[] { ap_light, ap_medium });
	
	private String name;       // name of the feat ex. 'ARMOR PROFICIENCY: chain_mail'
	private List<Feat> prereq; //
	
	/*
	 * prequisite type:
	 * - ability scores
	 * - class levels
	 * - levels
	 * - skill ranks
	 * - feats
	 */
	
	// types - proficiency, skill boost
	// ex. proficiency armor chain_mail
	Feat(String featName) {
		this.name = featName;
	}
	
	Feat(String featName, Feat...requiredFeats) {
		this(featName);
		
		prereq = Arrays.asList( requiredFeats );
	}
	
	public String getName() {
		return this.name;
	}
}