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
import java.util.HashMap;
import java.util.List;

public class SpellBook {
	public ArrayList<Spell> L1, L2, L3, L4, L5, L6, L7, L8, L9;
	
	public HashMap<Integer, ArrayList<Spell>> spellTable = new HashMap<Integer, ArrayList<Spell>>(1, 0.75f){
		{
			put(0, null); put(1, L1); put(2, L2);
			put(3, L3);   put(4, L4); put(5, L5);
			put(6, L6);   put(7, L7); put(8, L8);
			put(9, L9);
		}
	};
	
	public SpellBook () {
		L1 = new ArrayList<Spell>();
		L2 = new ArrayList<Spell>();
		L3 = new ArrayList<Spell>();
		L4 = new ArrayList<Spell>();
		L5 = new ArrayList<Spell>();
		L6 = new ArrayList<Spell>();
		L7 = new ArrayList<Spell>();
		L8 = new ArrayList<Spell>();
		L9 = new ArrayList<Spell>();
	}
	
	public SpellBook(SpellBook toCopy) {
	}
	
	public void addSpell(Spell newSpell) {
		spellTable.get(newSpell.getLevel()).add(newSpell);
	}
	
	public void addSpells(Spell... newSpells) {
		for(Spell spell : newSpells) {
			spellTable.get(spell.getLevel()).add(spell);
		}
	}
	
	public Spell getSpell(String name) {
		return null;
	}
	
	/**
	 * Return a list of the spells of that level that this spellbook contains 
	 * 
	 * @param level
	 * @return
	 */
	public List<Spell> getSpells(Integer level) {
		return spellTable.get(level);
	}
	
	public void removeSpell(Spell spell) {
		spellTable.get(spell.getLevel()).remove(spell);
	}
	
	public void removeSpells(Spell... spells) {
		for(Spell spell : spells) {
			spellTable.get(spell.getLevel()).remove(spell);
		}
	}
}