package mud.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.game.Ability;
import mud.game.Coins;
import mud.interfaces.Interactive;
import mud.misc.Slot;
import mud.quest.Quest;
import mud.rulesets.d20.Classes;
import mud.rulesets.d20.Races;
import mud.utils.CNode;
import mud.utils.Message;
import mud.utils.Utils;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
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
public class NPC extends Player implements Interactive
{
	public String greeting = "Lovely weather we're having around these parts.";

	private boolean givesQuests = false;

	private transient List<Quest> questList = new ArrayList<Quest>();

	private transient CNode conversation = null;

	//private Direction lastDir = Direction.NONE;

	// TODO make the below work (may need to modify Player)
	public NPC(final String name) {
		this(name, "A generic npc.");
	}
	
	public NPC(final String name, final String description) {
		super(-1);

		this.name = name;
		this.desc = "A generic npc.";
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		this.status = "NPC";
		
		this.type = TypeFlag.NPC;

		this.race = Races.NONE;
		this.pclass = Classes.NONE;

		this.money = Coins.copper(0);

		this.slots = new LinkedHashMap<String, Slot>();

		this.stats = new LinkedHashMap<Ability, Integer>(ruleset.getAbilities().length, 0.75f);
	}

	/**
	 * Loading Constructor
	 * 
	 * @param tempDBRef
	 * @param tempName
	 * @param tempFlags
	 * @param tempDesc
	 * @param tempLoc
	 * @param tempMoney
	 */
	public NPC(final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, final int tempLoc, final Coins tempMoney)
	{
		super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc, null, null, "NPC", new Integer[] {10, 10, 10, 10, 10, 10}, tempMoney);
		
		this.type = TypeFlag.NPC;
	}

	/**
	 * 
	 * @param tempDBRef
	 * @param tempName
	 * @param tempFlags
	 * @param tempDesc
	 * @param tempLoc
	 * @param tempTitle
	 * @param tempPStatus
	 * @param tempStats
	 * @param tempMoney
	 */
	public NPC(final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, 
			final int tempLoc, final String tempTitle, final String tempPStatus, final Integer[] tempStats, final Coins tempMoney)
	{
		super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc, tempTitle, "", tempPStatus, tempStats, tempMoney);
		
		this.type = TypeFlag.NPC;
	}
	
	/* copy constructor */
	protected NPC(final NPC template) {
		super(-1);
		
		this.type = TypeFlag.NPC;
	}

	// TODO perhaps these should just return the message object?
	public Message greet(Player player) {
		return new Message(this, player, greeting);
	}

	public Message say(String message) {
		return new Message(this, null, message);
	}

	public Message tell(Player player, String message) {
		return new Message(this, player, message);
	}

	// pose/do/act

	// THIS IS A HACKED-UP SOLUTION THAT NEEDS FIXING
	/*public ArrayList<Message> interact(final int n) {
		final ArrayList<Message> ret = new ArrayList<Message>(2);

        ret.add(new Message(this, greeting));       // send some kind of generic response (need an ai of a kind to generate/determine responses)

        if (questList.size() > 0) {
            final Quest q = questList.get(0);
            ret.add(new Message(this, "I have two quests available:\n" 
                            + q.getName() + " - " + q.getDescription() + "\n" 
                            + q.getName() + q.getDescription() + "\n"));
        }
        return ret;
	}*/

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

	public void setQuestGiver(boolean givesQuests) {
		this.givesQuests = givesQuests;
	}

	/**
	 * isQuestgiver
	 * 
	 * Does this NPC give quests.
	 * 
	 * @return
	 */
	public boolean isQuestgiver() {
		return this.givesQuests;
	}

	/**
	 * getQuestFor
	 * 
	 * Get the specified Quest for the Player, which would be the one with it's
	 * list index equal to the specified integer.
	 * 
	 * @param player
	 * @param questNum
	 * @return
	 */
	public Quest getQuestFor(final Player player, final int questNum) {
		if (questNum < 0) {
			return null;
		}

		int foundQuests = -1;

		// for each of the npcs quests, decided if it's suitable for the player or not
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
				suitable.add( quest );
			}
		}

		return suitable;
	}
	
	@Override
	public void addQuest(Quest newQuest) {
		this.questList.add(newQuest);
	}
	
	@Override
	public void removeQuest(final Quest oldQuest) {
		this.questList.remove(oldQuest);
	}

	@Override
	public boolean hasQuest(final Quest quest) {
		boolean success = false;

		for(final Quest q : this.questList) {
			if( quest.getId() == q.getId() ) {
				success = true;
				break;
			}
		}
		
		return success;
	}
	
	public void remember(final Player player) {
		//this.names.add( player.getName() );
		addName( player.getName() );
	}
	
	public void forget(final Player player) {
		//this.names.remove( player.getName() );
		removeName( player.getName() );
	}

	/**
	 * Translate the persistent aspects of the player into the string
	 * format used by the database
	 */
	public String toDB() {
		String[] output = new String[12];
		
		output[0] = this.getDBRef() + "";                   // database reference number
		output[1] = this.getName();                         // name
		output[2] = TypeFlag.asLetter(this.type) + "";      // flags
		output[2] = output[2] + this.getFlagsAsString();
		output[3] = this.getDesc();                         // description
		output[4] = this.getLocation() + "";                // location
		output[5] = "";                                     // empty (npcs have no password)

		// convert stats to a string representation
		final StringBuilder sb = new StringBuilder();
		int abilities = ruleset.getAbilities().length;
		int count = 1;

		for(Ability ability : ruleset.getAbilities()) {
			sb.append( this.stats.get(ability) );
			if( count < abilities) sb.append(",");
			count++;
		}

		output[6] = sb.toString();                          // stats 
		output[7] = getMoney().toString(false);             // money
		output[8] = this.access + "";                       // permissions level
		output[9] = this.race.getId() + "";                 // race
		output[10] = this.pclass.getId() + "";              // class
		output[11] = this.getStatus();                      // status

		return Utils.join(output, "#");
	}

	@Override
	public void interact(Player player) {
		if( player != null ) {
			if( this.names.contains(player.getName()) ) {
				tell(player, "Hello, " + player.getName() + ".");
				//say("Hello, " + player.getName() + ".");
			}
			else {
				//say(greeting);
				tell(player, greeting);
			}
		}
	}

	public CNode getConversation() {
		return this.conversation;
	}

	public void setConversation(final CNode conversation) {
		this.conversation = conversation;
	}
}