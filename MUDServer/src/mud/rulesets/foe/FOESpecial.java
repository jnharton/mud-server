package mud.rulesets.foe;

import mud.game.Ability;
import mud.game.PClass;
import mud.game.Skill;
import mud.rulesets.special.Perk;
import mud.rulesets.special.SpecialRuleset;
import mud.rulesets.special.Trait;

public final class FOESpecial implements SpecialRuleset {
	private static final Ability[] abilities = new Ability[] { 
		Abilities.STRENGTH,
		Abilities.PERCEPTION,
		Abilities.ENDURANCE,
		Abilities.CHARISMA,
		Abilities.INTELLIGENCE,
		Abilities.AGILITY,
		Abilities.LUCK
	};
	
	// Abilities2/Derived Statistics?
	
    private static FOESpecial instance;
    
    private FOESpecial() {
    }
    
    public static FOESpecial getInstance() {
        if( instance == null ) {
            instance = new FOESpecial();
        }
        
        return instance;
    }
    
    public String getName() {
    	return "FOE-SPECIAL";
    }
    
	@Override
	public Ability getAbility(int id) {
		return FOESpecial.abilities[id];
	}
	
	@Override
	public Ability getAbility(String name) {
		for(final String key : Abilities.abilityMap.keySet()) {
			if( key.equals(name) ) {
				return Abilities.abilityMap.get(key);
			}
		}
		
		return null;
	}
	
	public Ability[] getAbilities() {
    	return FOESpecial.abilities;
	}
	
	@Override
	public Skill getSkill(int id) {
		for(final Skill s : Skills.skillMap.values()) {
			if( s.getId() == id ) return s;
		}

		return null;
	}

	@Override
	public Skill getSkill(String name) {
		return Skills.skillMap.get(name);
	}
	
	@Override
	public Skill[] getSkills() {
		Skill[] skills = new Skill[0];

		skills = Skills.skillMap.values().toArray(skills);

		return skills; 
	}
	
	// although part of the module spec, we won't be providing classes here for now
	
	@Override
	public PClass getClass(int id) {
		return null;
	}

	@Override
	public PClass getClass(String className) {
		return null;
	}

	@Override
	public PClass[] getClasses() {
		return null;
	}
	
	@Override
	public Perk getPerk(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Perk getPerk(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Trait getTrait(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Trait getTrait(String name) {
		// TODO Auto-generated method stub
		return null;
	}
}