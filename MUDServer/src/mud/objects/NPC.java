package mud.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.Abilities;
import mud.Classes;
import mud.Editor;
import mud.Races;
import mud.Skill;
import mud.Skills;
import mud.Slot;
import mud.SlotType;

import mud.interfaces.Interactive;
import mud.interfaces.Vendor;

import mud.net.Client;
import mud.objects.items.ClothingType;

import mud.quest.Quest;
import mud.quest.Task;
import mud.quest.TaskType;

import mud.utils.MailBox;
import mud.utils.Message;
import mud.utils.Utils;

/*
 * problems are calls to debugP() and send()
 */

/**
 * NPC Class
 * 
 * can be interacted with
 * I have a problem in that vendor is a interface
 * that extends the interactive interface
 * 
 * @author Jeremy N. Harton
 * 
 */
public class NPC extends Player
{
	/**
	 * 
	 */
	protected String greeting = "Lovely weather we're having around these parts.";
	private ArrayList<String> messages;
	private ArrayList<Quest> questList = new ArrayList<Quest>(); // a list of quests

	// blank constructor for sub classes
	public NPC() {
	}

	// "normal", but not default, constructor
	/*public NPC(int tempDBRef, String tempName, String tempDesc, int tempLoc, String tempTitle) {
		super(tempDBRef);
		this.name = tempName;
		this.flags = "N";
		this.locks = ""; // should take tempLocks argument
		this.desc = tempDesc;
		this.status = "NPC";
		this.title = tempTitle;
		this.location = tempLoc;
		this.money = new Integer[]{ 0, 0, 0 ,0 };
	}*/

	public NPC(final int tempDBRef, final String tempName, final String tempPass, final EnumSet<ObjectFlag> tempFlags, 
            final String tempDesc, final String tempTitle, final String tempPStatus, final int tempLoc, final String[] tempMoney)
	{
		super(tempDBRef);
		this.name = tempName;
		this.flags = tempFlags;
		this.locks = ""; // should take tempLocks argument    
		this.desc = tempDesc;
		this.status = tempPStatus;
		this.title = tempTitle;
		this.location = tempLoc;
		this.money = Utils.stringsToIntegers(tempMoney);
	}
	
	public NPC(final int tempDBREF, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, 
            final int tempLoc, final String tempTitle, final String tempPStatus, final Integer[] tempStats, final Integer[] tempMoney)
	{
		super(tempDBREF);
		
		this.race = Races.NONE;
		this.gender = 'N';
		this.pclass = Classes.NONE;
		this.pclass = Classes.NONE;

		this.hp = 10;
		this.totalhp = 10;
		this.mana = 40;
		this.totalmana = 40;
		this.speed = 0;
		this.capacity = 200;
		this.level = 0;
		this.xp = 0;
		this.money = tempMoney; // use default money criteria from server config or stored player money in future

		this.name = tempName;
		this.flags = tempFlags;
		this.locks = ""; // should take tempLocks argument
		this.desc = tempDesc;
		this.status = tempPStatus;
		this.title = tempTitle;
		this.location = tempLoc;
		this.target = null;
		
		// instantiate slots
		this.slots = new LinkedHashMap<String, Slot>(11, 0.75f);
		
		// initialize slots
		this.slots.put("helmet", new Slot(SlotType.HEAD, ItemType.HELMET));
		this.slots.put("necklace", new Slot(SlotType.NECK, ItemType.NECKLACE));
		this.slots.put("armor", new Slot(SlotType.BODY, ItemType.ARMOR));
		this.slots.put("cloak", new Slot(SlotType.BODY, ClothingType.CLOAK));
		this.slots.put("ring1", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring2", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring3", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring4", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring5", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring6", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring7", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring8", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring9", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring10", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("gloves", new Slot(SlotType.HANDS, ClothingType.GLOVES));
		this.slots.put("weapon", new Slot(SlotType.RHAND, ItemType.WEAPON));
		this.slots.put("weapon1", new Slot(SlotType.LHAND, ItemType.WEAPON));
		this.slots.put("belt", new Slot(SlotType.WAIST, ClothingType.BELT));;
		this.slots.put("boots", new Slot(SlotType.FEET, ClothingType.BOOTS));
		
		// instantiate stats
		stats = new LinkedHashMap<Abilities, Integer>(6, 0.75f);
		
		// initialize stats
		this.stats.put(Abilities.STRENGTH, tempStats[0]);     // Strength
		this.stats.put(Abilities.DEXTERITY, tempStats[1]);    // Dexterity
		this.stats.put(Abilities.CONSTITUTION, tempStats[2]); // Constitution
		this.stats.put(Abilities.INTELLIGENCE, tempStats[3]); // Intelligence
		this.stats.put(Abilities.WISDOM, tempStats[4]);       // Wisdom
		this.stats.put(Abilities.CHARISMA, tempStats[5]);     // Charisma
		
		// instantiate skills
		skills = new LinkedHashMap<Skill, Integer>(36, 0.75f);
		
		// initialize skills
		// these should be all -1, since no class is specified initially
		this.skills.put(Skills.APPRAISE, -1);            this.skills.put(Skills.BALANCE, -1);            this.skills.put(Skills.BLUFF, -1);
		this.skills.put(Skills.CLIMB, -1);               this.skills.put(Skills.CONCENTRATION, -1);      this.skills.put(Skills.CRAFT, -1);
		this.skills.put(Skills.DECIPHER_SCRIPT, -1);     this.skills.put(Skills.DIPLOMACY, -1);          this.skills.put(Skills.DISGUISE, -1);
		this.skills.put(Skills.ESCAPE_ARTIST, -1);       this.skills.put(Skills.GATHER_INFORMATION, -1); this.skills.put(Skills.HANDLE_ANIMAL, -1);
		this.skills.put(Skills.HEAL, -1);                this.skills.put(Skills.HIDE, -1);               this.skills.put(Skills.INTIMIDATE, -1);
		this.skills.put(Skills.JUMP, -1);
		
		this.skills.put(Skills.KNOWLEDGE, -1);           this.skills.put(Skills.KNOWLEDGE_ARCANA, -1);   this.skills.put(Skills.KNOWLEDGE_DUNGEONEERING, -1); 
		this.skills.put(Skills.KNOWLEDGE_GEOGRAPHY, -1); this.skills.put(Skills.KNOWLEDGE_HISTORY, -1);  this.skills.put(Skills.KNOWLEDGE_LOCAL, -1);         
		this.skills.put(Skills.KNOWLEDGE_NATURE, -1);    this.skills.put(Skills.KNOWLEDGE_NOBILITY, -1); this.skills.put(Skills.KNOWLEDGE_PLANAR, -1);        
		this.skills.put(Skills.KNOWLEDGE_RELIGION, -1);
		
		this.skills.put(Skills.LISTEN, -1);              this.skills.put(Skills.MOVE_SILENTLY, -1);      this.skills.put(Skills.NAVIGATION, -1);
		this.skills.put(Skills.PERFORM, -1);             this.skills.put(Skills.PROFESSION, -1);         this.skills.put(Skills.RIDE, -1);
		this.skills.put(Skills.SEARCH, -1);              this.skills.put(Skills.SENSE_MOTIVE, -1);       this.skills.put(Skills.SLEIGHT_OF_HAND, -1);
		this.skills.put(Skills.SPEAK_LANGUAGE, -1);      this.skills.put(Skills.SPELLCRAFT, -1);         this.skills.put(Skills.SPOT, -1);
		this.skills.put(Skills.SURVIVAL, -1);            this.skills.put(Skills.SWIM, -1);               this.skills.put(Skills.TRACKING, -1);
		this.skills.put(Skills.TUMBLE, -1);              this.skills.put(Skills.USE_MAGIC_DEVICE, -1);   this.skills.put(Skills.USE_ROPE, -1);
		
		// we get a new blank list this way, not a loaded state
		this.names = new ArrayList<String>();
	}
	
	//@Override
	public void say(String message) {
		Message msg = new Message(this, message);
		parent.addMessage(msg);
	}
	
	// THIS IS A HACKED-UP SOLUTION THAT NEEDS FIXING
	public ArrayList<Message> interact(int n) {
		ArrayList<Message> ret = new ArrayList<Message>(2);
		
		try {
			// send some kind of generic response (need an ai of a kind to generate/determine responses)
			Message msg = new Message(this, greeting);
			ret.add(msg);
			if (questList.size() > 0) {
				//
				String test = "I have two quests available:\n" +
						questList.get(0).getName() +
						" - " +
						questList.get(0).getDescription() +
						"\n" +
						questList.get(1).getName() +
						questList.get(1).getDescription() +
						"\n";
				Message m2 = new Message(this, test);
				ret.add(m2);
				return ret;
			}
			else {
				return ret;
			}
		}
		catch(NullPointerException npe) {
			System.out.println("Exception(INTERACT): " + npe.getMessage());
		}
		catch(Exception e) {
			System.out.println("Exception(INTERACT): " + e.getMessage());
		}
		
		return null;
	}

	/*@Override
	public ArrayList<Item> list() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item buy(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sell(String name) {
		// TODO Auto-generated method stub

	}*/

	public void setGreeting(String newGreeting) {
		this.greeting = newGreeting;
	}
	
	public Quest getQuestFor(final Player player, final int questNum) {
        if (questNum < 0) {
            return null;
        }
        int foundQuests = -1;

        for (final Quest q : questList) {
            if ( q.isSuitable(player) ) {
                foundQuests += 1;
                if (foundQuests == questNum) {
                    return q;
                }
            }
        }
        return null;
	}

	public List<Quest> getQuestsFor(final Player player) {
        final ArrayList<Quest> suitable = new ArrayList<Quest>();

        for (final Quest quest : questList) {
            if ( quest.isSuitable(player) ) {
                suitable.add( new Quest( quest ) );	
            }
        }
        return suitable;
	}

	public void addQuest(Quest newQuest) {
		this.questList.add(newQuest);
	}
	
	public void setQuestList() {
		this.questList = new ArrayList<Quest>();
	}
}
