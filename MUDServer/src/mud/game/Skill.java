package mud.game;

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
import java.util.Arrays;
import java.util.HashMap;

import mud.d20.Abilities;

/* Class Array hold classes for whom the skill is a class skill */
public class Skill
{
	private String name;       // name of the skill (string)
	private int id;            // id of the skills (unique integer)
	private Ability ability;   // ability that modifies the skill (string)
	private PClass[] classes;  // array of classes for which this is a class skill
	
	private static final HashMap<String, Ability> abilityMap = new HashMap<String, Ability>() {
		{
			put("STR", Abilities.STRENGTH);
			put("DEX", Abilities.DEXTERITY);
			put("CON", Abilities.CONSTITUTION);
			put("INT", Abilities.INTELLIGENCE);
			put("WIS", Abilities.WISDOM);
			put("CHA", Abilities.CHARISMA);
		}
	};
	
	public Skill() {
		this.name = "new_skill";
		this.id = -1;
		this.ability = null;
		this.classes = new PClass[0];
	}
	
	/**
	 * Class Constructor for Skills
	 * 
	 * @param name
	 * @param id
	 * @param ability
	 * @param classes
	 */
	public Skill(final String name, final int id, final String ability, final PClass[] classes) {
		this(name, id, abilityMap.get(ability), classes);
	}
	
	/**
	 * Class Constructor2 for Skills
	 * 
	 * @param name
	 * @param id
	 * @param ability
	 * @param classes
	 */
	public Skill(final String name, final int id, final Ability ability, final PClass[] classes) {
		this.name = name;
		this.id = id;
		this.ability = ability;
		this.classes = classes;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Ability getAbility() {
		return this.ability;
	}
	
	public int getId() {
		return this.id;
	}
		
	public ArrayList<PClass> getClasses() {
		return new ArrayList<PClass>(Arrays.asList(this.classes)); 
	}
	
	public String toString() {
		//return this.name + " " + Arrays.asList(this.classes);
		return this.name;
	}
}