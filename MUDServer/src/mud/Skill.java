package mud;
import java.util.ArrayList;
import java.util.Arrays;


/* Class Array hold classes for whom the skill is a class skill */
public class Skill {
	
	private String name;       // name of the skill (string)
	private int id;            // id of the skills (unique integer)
	private String ability;    // ability that modifies the skill (string)
	private String skillMod;  //
	private Classes[] classes; //
	
	/**
	 * Class Constructor for Skills
	 * 
	 * @param name
	 * @param id
	 * @param ability
	 * @param classes
	 */
	public Skill(String name, int id, String ability, Classes[] classes) {
		this.name = name;
		this.ability = ability;
		this.skillMod = null;
		this.classes = classes;
	}
	
	/**
	 * Class Constructor for Skills
	 * 
	 * @param name
	 * @param id
	 * @param ability
	 * @param skill_spec
	 * @param classes
	 */
	public Skill(String name, int id, String ability, String skillMod, Classes[] classes) {
		this.name = name;
		this.ability = ability;
		this.skillMod = skillMod;
		this.classes = classes;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getAbility() {
		return this.ability;
	}
	
	/**
	 * 
	 * @param skillId
	 * @return
	 */
	/*public Skills getSkill(int skillId) {
		for(Classes c : temp) {
			if(temp.id == skillId) {
				return  temp;
			}
			else {
			}
		}
	}*/
	
	public int getSkillId(Skill s) {
		return s.id;
	}
	
	public String getSpec() {
		return this.skillMod;
	}
		
	public ArrayList<Classes> getClasses() {
		return new ArrayList<Classes>(Arrays.asList(this.classes)); 
	}
	
	public String toString() {
		//return this.name + " " + Arrays.asList(this.classes);
		return this.name;
	}
}