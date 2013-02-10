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

	private HashMap<Integer, ArrayList<Spell>> spellTable = new HashMap<Integer, ArrayList<Spell>>();

	public SpellBook () {
        for (int i = 1; i < 10; i++) {
            spellTable.put(i, new ArrayList<Spell>());
        }
	}
	
	public SpellBook(SpellBook toCopy) {
        for (int i = 1; i < 10; i++) {
            spellTable.put(i, new ArrayList<Spell>(toCopy.spellTable.get(i)));
        }
	}
	
	public void addSpell(Spell newSpell) {
		spellTable.get(newSpell.getLevel()).add(newSpell);
	}
	
	public void addSpells(final Spell... newSpells) {
		for (Spell spell : newSpells) {
			spellTable.get(spell.getLevel()).add(spell);
		}
	}
	
	public Spell getSpell(final String name) {
        for (final ArrayList<Spell> spellList : spellTable.values()) {
            for (final Spell spell : spellList) {
                if (spell.getName().equals(name)) {
                    return spell;
                }
            }
        }
		return null;
	}
	
	/**
	 * Return a list of the spells of that level that this spellbook contains 
	 */
	public List<Spell> getSpells(final int level) {
		return spellTable.get(level);
	}
	
	public void removeSpell(final Spell spell) {
		spellTable.get(spell.getLevel()).remove(spell);
	}
	
	public void removeSpells(final Spell... spells) {
		for (Spell spell : spells) {
            removeSpell(spell);
		}
	}
}