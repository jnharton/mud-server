package mud.rulesets.special;

import java.util.Hashtable;
import java.util.Map;

import mud.game.Ability;
import mud.game.Skill;

/**
 * References: http://fallout.wikia.com/wiki/Perk
 * 
 * @author Jeremy
 *
 */
public final class Perk {	
	public enum Type { REGULAR, SPECIAL, QUEST, CHALLENGE, UNARMED, COMPANION, UNIQUE_COMPANION, UNIQUE };
	
	private String name;        // perk name
	private String description; // perk description
	private Type type;          // perk type
	private int max_ranks;      // how many ranks in this perk can you take
	
	// perk selection requirements
	private int required_level;                   // required level to select perk
	private Map<Ability, Integer> required_stats; //
	private Map<Skill, Integer> required_skills;  // { climb, 5 } - skills required to select perk
	
	/**
	 * A Perk constructor for Perks that have no required stats or skills.
	 * 
	 * @param pName
	 * @param pDescription
	 * @param pType
	 * @param pRanks
	 * @param pRequiredLevel
	 */
	public Perk(final String pName, final String pDesc, Type pType, final int pRanks, final int pReqLevel) {
		this(pName, pDesc, pType, pRanks, pReqLevel, null, null);
	}
	
	/**
	 * A Perk constructor for Perks that have required stats, but no required skills.
	 *  
	 * @param pName
	 * @param pDesc
	 * @param pType
	 * @param pRanks
	 * @param pReqLevel
	 * @param req_stats
	 */
	public Perk(final String pName, final String pDesc, Type pType, final int pRanks, final int pReqLevel, Map<Ability, Integer> req_stats) {
		this(pName, pDesc, pType, pRanks, pReqLevel, req_stats, null);
	}
	
	/**
	 * Full, standard Perk constructor
	 * 
	 * @param pName
	 * @param pDesc
	 * @param pType
	 * @param pRanks
	 * @param pReqLevel
	 * @param req_stats
	 * @param req_skills
	 */
	public Perk(final String pName, final String pDesc, Type pType, final int pRanks, final int pReqLevel, Map<Ability, Integer> req_stats, Map<Skill, Integer> req_skills){
		this.name = pName;
		this.description = pDesc;
		this.type = pType;
		this.max_ranks = pRanks;
		this.required_level = pReqLevel;
		
		if( required_stats != null ) {
			this.required_stats.putAll(req_stats);
		}
		else this.required_stats = new Hashtable<Ability, Integer>();
		
		if( required_skills != null ) {
			this.required_skills.putAll(req_skills);
		}
		else this.required_skills = new Hashtable<Skill, Integer>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public int getMaxRanks() {
		return this.max_ranks;
	}
	
	public int getRequiredLevel() {
		return this.required_level;
	}
	
	public boolean isType(final Type ofType) {
		return this.type == ofType;
	}
}