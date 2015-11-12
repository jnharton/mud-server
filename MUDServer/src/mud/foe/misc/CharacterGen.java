package mud.foe.misc;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mud.utils.Utils;

/**
 * Fallout Equestria -- Character Generator
 * 
 * Ported from my modified Python version of ?@reddit.com's code
 * 
 * @author Jeremy
 *
 */
public final class CharacterGen {
	private static final int STRENGTH = 0,
			                 PERCEPTION = 1,
			                 ENDURANCE = 2,
			                 CHARISMA = 3,
			                 INTELLIGENCE = 4,
			                 AGILITY = 5,
			                 LUCK = 6;
	
	// Races
	private static final List<String> races = Utils.mkList(
			"Earth Pony", "Pegasus", "Unicorn", "Griffin", "Zebra");

	// Traits
	private static final List<String> traits = Utils.mkList(
			"",
			"Blank Flank",          "Blueblood",       "Built to Destroy",
			"Bruiser",              "Chem Reliant",    "Chem Resistant",
			"Dashite",              "Deep Sleeper",    "Fast Shot",
			"Four Eyes",            "Ghoul",           "Good Natured",
			"Heavy Handed",         "Jinxed",          "Kamikaze",
			"Large Frame",          "Loose Cannon",    "Magic Knack",
			"One Trick Pony",       "Random",          "Radiation Child",
			"Sex Appeal",           "Shaman",          "Small Frame",
			"Spiritually Awakened", "Spread Thin",     "Stable Dweller",
			"Tribal Shaman",        "Touched by Luna", "Trigger Discipline",
			"Wild Wasteland",       "nothing",         "nothing",
			"nothing",              "nothing",         "nothing",
			"nothing",              "nothing",         "nothing",
			"nothing"
			);

	// Skills
	private static final List<String> skills = Utils.mkList(
			"",
			"Barter",   "Battle Saddles",         "Explosives", "Firearms",
			"Lockpick", "Magical Energy Weapons", "Mechanics",  "Medicine",
			"Melee",    "Science",                "Sneak",      "Speech",
			"Survival", "Unarmed"
			);

	// Misc Weapon Info (derived from equipment selection)
	// NOTE: does not include melee weapons (4-2-2015)

	private static final List<String> weapons = Utils.mkList(
			".22 Revolver",        "Silenced .22 Revolver", "9mm Pistol",              ".32 Revolver",          "Zebra Pistol",
			".32 SMG",             "BB gun",                ".22 Repeater",            ".32 Rifle",             "Service Carbine",
			"Single Shotgun",      "Flare Gun",             "Pump Charge Rifle",       "Light Grenade Rifle",   ".32 Auto Pistol",
			".357 Revolver",       "10mm Pistol",           "Needler Pistol",          "Silenced .22 SMG",      "9mm SMG",
			"9mm Repeater",        "Varmint Rifle",         "Service Rifle",           "Crossbow",              "Caravan Shotgun",
			"Pump-Action Shotgun", "Lever-Action Shotgun",  "Pulse Pistol",            "Magical Energy Pistol", "Recharger Rifle",
			"Light Flamer",        "Zebra Grenade Rifle",   "Anti-Pony Grenade Rifle");

	private static final List<String> variants = Utils.mkList(
			"Silenced",  "Service",     "Single",       "Auto",           "Needler", 
			"Caravan",   "Pump-Action", "Lever-Action", "Varmint",        "Pump Charge", 
			"Light",     "BB",          "Pulse",        "Magical Energy", "Recharger",
			"Zebra",     "Anti-Pony");

	// Weapon Generator uses tables below this line above the function def

	private static final List<String> types = Utils.mkList(
			"Carbine", "Crossbow", "Flamer", "Flare Gun", "Grenade Rifle", "Pistol", "Repeater", "Revolver", "Rifle", "Shotgun", "SMG");

	// [modifier] [variant] {caliber} <weapon type>
	// [] - optional, {} - where appropriate, <> - required

	private static final Map<String, List<String>> modifier_table = new Hashtable<String, List<String>>() {
		{
			put("Revolver", Utils.mkList("Silenced"));
			put("SMG",      Utils.mkList("Silenced"));
		}
	};

	private static final Map<String, List<String>> variant_table = new Hashtable<String, List<String>>() {
		{
			put("Grenade Rifle", Utils.mkList("", "Anti-pony",   "Light",          "Zebra"));
			put("Pistol",        Utils.mkList("", "Auto",        "Magical energy", "Needler", "Pulse",   "Service"));
			put("Rifle",         Utils.mkList("", "Pump Charge", "Recharger",      "Service", "Varmint"));
		}
	};

	private static final Map<String, List<String>> caliber_table = new Hashtable<String, List<String>>() {
		{
			put("Pistol",   Utils.mkList("9mm", "10mm"));
			put("Repeater", Utils.mkList(".22", "9mm"));
			put("Revolver", Utils.mkList(".22", ".32", ".357"));
			put("Rifle",    Utils.mkList(".32"));
			put("SMG",      Utils.mkList(".22", ".32", "9mm"));	
		}
	};
	
	public CharacterGen() {
	}

	public CharacterData generate_character() {
		final CharacterData charData = new CharacterData();
		
		final Random rand = new Random();
		
		/* Race */
		charData.setRace( races.get( rand.nextInt(5) ) );
		
		/* Stats */
		int P = 5;
		
		// Generate SPECIAL stats
		final Integer[] stats = new Integer[] { 0, 5, 5, 5, 5, 5, 5, 5 };
		
		while( P > 0 && sum(stats) != 40 ) {
			int x = rand.nextInt(14) + 1;                      // net result is a number between 1-7 while having randomness of 1 in 14?
			int y = ((Double) Math.floor( x / 8 )).intValue(); // get a 0 when x < 8 and a 1 otherwise
			
			if( x >= 8 ) {
				x = x + 1; // fix upper half (7-14) being off by 1 b/c of doing modulus by 8 instead of 7
			}
			
			if( y == 0 ) {
				stats[x] += 1;
				P -= 1;
				
				if( stats[x] >= 10 ) {
					stats[x] -= 1;
					P += 1;
				}
			}
			else if( y == 1 ) {
				stats[x] -= 1;
				P += 1;
				
				if( stats[x] <= 3 ) {
					stats[x] += 1;
					P -= 1;
				}
			}
		}
		
		// store generated stats
		charData.stats[0] = stats[STRENGTH];
		charData.stats[1] = stats[PERCEPTION];
		charData.stats[2] = stats[ENDURANCE];
		charData.stats[3] = stats[CHARISMA];
		charData.stats[4] = stats[INTELLIGENCE];
		charData.stats[5] = stats[AGILITY];
		charData.stats[6] = stats[LUCK];
		
		
		return charData;
	}
	
	private static Integer sum(final Integer[] values) {
		Integer result = 0;
		
		for(final Integer value : values) {
			result += value;
		}
		
		return result;
	}
	
	private class CharacterData {
		private String name;
		private String race;
		private Integer age;
		
		public Integer[] stats;
		
		public CharacterData() {
			this.name = "Joe";
			this.race = "None";
			this.age = 0;
			
			stats = new Integer[7];
		}
		
		public void setName(final String name) {
			this.name = name;
		}
		
		public void setRace(final String race) {
			this.race = race;
		}
		
		public void setAge(final int age) {
			this.age = age;
		}
	}
}