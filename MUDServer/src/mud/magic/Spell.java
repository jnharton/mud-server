package mud.magic;

/*
Copyright (c) 2012 Jeremy N. Harton

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import mud.game.PClass;
import mud.misc.Effect;
import mud.utils.Utils;

/**
 * class to implement Dungeons and Dragons spells
 * 
 * @author Jeremy
 *
 */
public class Spell
{
	protected String name;
	
	protected SpellType sType;
	protected School school;
	
	protected int spellLevel;
	
	protected List<PClass> sc = new LinkedList<PClass>();
	
	protected int castTime; // time taken to cast the spell
	protected int duration; // duration of the spell
	protected int manaCost;
	
	protected RangeData range;
	
	protected String castMsg;
	
	protected List<Effect> effects;   // list of spell effects
	protected List<Reagent> reagents; // list of spell requirements
	
	protected int targets = 0; // type of targets for this spell
	
	public Spell() {
		this("NoName", SpellType.ARCANE, School.OTHER, 0, "You cast &name at &target.", new ArrayList<Effect>(), new ArrayList<Reagent>());
		
		this.castTime = 1;
		this.manaCost = 20;
		this.duration = 5;
	}
	
	public Spell(String tName, SpellType tType, School tSchool, int tLevel, String tCastMsg) {
		this.manaCost = 20;
	}
	
	public Spell(String tName, SpellType tType, School tSchool, int tLevel, String tCastMsg, List<Effect> tEffects) {
		this.manaCost = 20;
	}
	
	public Spell(String tName, SpellType tType, School tSchool, int tLevel, String tCastMsg, List<Effect> tEffects, List<Reagent> tReagents) {
		this.name = tName;
		this.school = tSchool;
		this.sType = tType;
		this.spellLevel = tLevel;
		
		this.range = new RangeData(RangeType.STD, RangeClass.LINEAR);
		
		this.castMsg = tCastMsg;
		
		this.effects = tEffects;
		this.reagents = tReagents;
		
		this.manaCost = 20;
	}
	
	public void setName(String spellName) {
		this.name = spellName;
	}

	public String getName() {
		return this.name;
	}
	
	public School getSchool() {
		return this.school;
	}

	public static School getSchool(final String schoolName) {
		final String sN = schoolName.toLowerCase();
		
		if (sN.equals("abjuration") || sN.equals("abj")) { return School.ABJURATION; }
		else if (sN.equals("conjuration") || sN.equals("con")) { return School.CONJURATION; }
		else if (sN.equals("divination") || sN.equals("div")) { return School.DIVINATION; }
		else if (sN.equals("enchantment") || sN.equals("enc")) { return School.ENCHANTMENT; }
		else if (sN.equals("evocation") || sN.equals("evo")) { return School.EVOCATION; }
		else if (sN.equals("illusion") || sN.equals("ill")) { return School.ILLUSION; }
		else if (sN.equals("invocation") || sN.equals("inv")) { return School.INVOCATION; }
		else if (sN.equals("transmutation") || sN.equals("tra")) { return School.TRANSMUTATION; }
		else if (sN.equals("necromancy") || sN.equals("nec")) { return School.NECROMANCY; }
		else { return School.OTHER; }
	}
	
	public void setSchool(School s) {
		this.school = s;
	}

	public void setSchool(final String schoolName) {
		if (schoolName.toLowerCase().equals("abjuration")) { this.school = School.ABJURATION; }
		else if (schoolName.toLowerCase().equals("conjuration")) { this.school = School.CONJURATION; }
		else if (schoolName.toLowerCase().equals("divination")) { this.school = School.DIVINATION; }
		else if (schoolName.toLowerCase().equals("enchantment")) { this.school = School.ENCHANTMENT; }
		else if (schoolName.toLowerCase().equals("evocation")) { this.school = School.EVOCATION; }
		else if (schoolName.toLowerCase().equals("illusion")) { this.school = School.ILLUSION; }
		else if (schoolName.toLowerCase().equals("invocation")) { this.school = School.INVOCATION; }
		else if (schoolName.toLowerCase().equals("transmutation")) { this.school = School.TRANSMUTATION; }
		else if (schoolName.toLowerCase().equals("necromancy")) { this.school = School.NECROMANCY; }
		else { this.school = School.OTHER; }
	}
	
	public void setLevel(int tLevel) {
		this.spellLevel = tLevel;
	}
	
	/**
	 * compare this to the player's level to figure out whether they can use it
	 */
	public int getLevel() {
		return this.spellLevel;
	}
	
	public String getCastMessage() {
		return this.castMsg;
	}
	
	public void setCastMessage(String newCastMsg) {
		this.castMsg = newCastMsg;
	}
	
	public void setManaCost(int manaCost) {
		this.manaCost = manaCost;
	}

	public int getManaCost() {
		return this.manaCost;
	}
	
	public void setSpellClass(PClass newSpellClass) {
		if( !this.sc.contains(newSpellClass) ) {
			this.sc.add(newSpellClass);
		}
	}
	
	/**
	 * compare this to the player's class to figure out whether they can use it
	 */
	public List<PClass> getSpellClasses() {
		return Collections.unmodifiableList(this.sc);
	}
	
	public RangeData getRangeData() {
		return this.range;
	}
	
	public List<Effect> getEffects() {
		return this.effects;
	}
	
	public List<Reagent> getReagents() {
		return this.reagents;
	}
	
	public void setTargets(final int encodedTargets) {
		this.targets = encodedTargets;
	}
	
	public int getTargets() {
		return this.targets;
	}
	
	/**
	 * Decode an integer to reconstruct data about valid targets
	 * for the spell in question.
	 * 
	 * @param spell
	 * @return
	 */
	public static String decodeTargets(final Spell spell) {
		final int SELF = 4;
		final int FRIEND = 2;
		final int ENEMY = 1;

		final StringBuilder sb = new StringBuilder();

		int temp = spell.targets;

		// decode targets
		// S, F, E
		
		/*switch(temp) {
		case 0: sb.append("none");                break;
		case 1: sb.append("enemy");               break;
		case 2: sb.append("friend");              break;
		case 3: sb.append("friend, enemy");       break;
		case 4: sb.append("self");                break;
		case 5: sb.append("self, enemy");         break;
		case 6: sb.append("self, friend");        break;
		case 7: sb.append("self, friend, enemy"); break;
		default: break;
		}*/
		
		if( temp < 0  || temp > 7 ) {
			return "none";
		}
		
		if( Utils.inRange(temp, 4, 7) ) {
			temp = temp ^ SELF;
			
			if( Utils.inRange(temp, 1, 3) ) {
				sb.append("self, ");
				
				temp = temp ^ FRIEND;
				
				if( Utils.inRange(temp, 1, 1) ) {
					sb.append("friend, ");
					
					temp = temp ^ ENEMY;
					
					if( temp > 0 && temp <= 0 ) sb.append("enemy, ");
					else if( temp == 0 )        sb.append("enemy");
				}
				else if( temp == 0 ) sb.append("friend");
			}
			else if( temp == 0 ) sb.append("self");
		}
		else if( temp == 3 ) sb.append("friend, enemy");
		else if( temp == 2 ) sb.append("friend");
		else if( temp == 1 ) sb.append("enemy");
		else sb.append("none");
		
		return sb.toString();
	}
	
	
	
	
	/**
	 * Convert valid targets for spells into a short form to store it
	 * by encoding it into binary as a single integer.
	 * 
	 * @param tTargets
	 * @return
	 */
	public static int encodeTargets(final String[] tTargets) {
		int targets = 0;

		int SELF = 4;
		int FRIEND = 2;
		int ENEMY = 1;

		for(final String s : tTargets) {
			if( s.equals("none") ) {
				targets = 0;
				break;
			}
			else {
				switch(s) {
				case "self":   targets |= SELF;   break;
				case "enemy":  targets |= ENEMY;  break;
				case "friend": targets |= FRIEND; break;
				default: break;
				}
			}
		}

		return targets;
	}

    @Override
	public boolean equals(Object obj) {
		return obj instanceof Spell && ((Spell) obj).name.equals(name);
	}

    @Override
	public int hashCode() {
		return name.hashCode();
	}

}