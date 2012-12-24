package mud.magic;

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