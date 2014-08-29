package mud;

import java.util.HashMap;

import mud.game.Ability;
import mud.game.Skill;
import mud.interfaces.Ruleset;

public final class D20 implements Ruleset {
	// Abilities/Primary Statistics
	private static final Ability STRENGTH = new Ability("strength", "str", 0);
	private static final Ability DEXTERITY = new Ability("dexterity", "dex", 1);
	private static final Ability CONSTITUTION = new Ability("constitution", "con", 2);
	private static final Ability INTELLIGENCE= new Ability("intelligence", "int", 3);
	private static final Ability CHARISMA = new Ability("charisma", "cha", 4);
	private static final Ability WISDOM = new Ability("wisdom", "wis", 5);
	
	private static final Ability[] abilities = new Ability[] { 
		STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, CHARISMA, WISDOM
	};
	
	private static final HashMap<String, Ability> abilityMap = new HashMap<String, Ability>() {
		{
			put("STR", STRENGTH);
			put("DEX", DEXTERITY);
			put("CON", CONSTITUTION);
			put("INT", INTELLIGENCE);
			put("CHA", CHARISMA);
			put("WIS", WISDOM);
		}
	};
	
	private static D20 instance;
    
    private D20() {
    }
    
    public static D20 getInstance() {
        if( instance == null ) {
            instance = new D20();
        }
        
        return instance;
    }

	@Override
	public String getName() {
		return "D20";
	}

	@Override
	public Ability getAbility(int id) {
		for(final Ability a : abilityMap.values()) {
			if( a.getId() == id ) {
				return a;
			}
		}
		
		return null;
	}

	@Override
	public Ability getAbility(String name) {
		for(final String s : abilityMap.keySet()) {
			if( s.equals(name) ) {
				return abilityMap.get(s);
			}
		}
		
		return null;
	}

	@Override
	public Ability[] getAbilities() {
		return D20.abilities;
	}
	
	@Override
	public Skill[] getSkills() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Skill getSkill(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Skill getSkill(String name) {
		// TODO Auto-generated method stub
		return null;
	}
}