package mud.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.Abilities;
import mud.Classes;
import mud.Coins;
import mud.Editor;
import mud.Races;
import mud.Skill;
import mud.Skills;
import mud.Slot;
import mud.SlotType;

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
public class NPC extends Player implements InteractiveI
{
	/**
	 * 
	 */
	protected String greeting = "Lovely weather we're having around these parts.";
	private ArrayList<Quest> questList = new ArrayList<Quest>();

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
            final String tempDesc, final String tempTitle, final String tempPStatus, final int tempLoc, final Coins tempMoney)
	{
        super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc, tempTitle, tempPass, tempPStatus, new Integer[] {10, 10, 10, 10, 10, 10}, tempMoney);
	}
	
	public NPC(final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, 
            final int tempLoc, final String tempTitle, final String tempPStatus, final Integer[] tempStats, final Coins tempMoney)
	{
		super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc, tempTitle, "", tempPStatus, tempStats, tempMoney);
	}
	
	public void say(String message) {
		parent.addMessage(new Message(this, message));
	}
	
	// THIS IS A HACKED-UP SOLUTION THAT NEEDS FIXING
	public ArrayList<Message> interact(final int n) {
		final ArrayList<Message> ret = new ArrayList<Message>(2);

        ret.add(new Message(this, greeting));       // send some kind of generic response (need an ai of a kind to generate/determine responses)

        if (questList.size() > 0) {
            final Quest q = questList.get(0);
            ret.add(new Message(this, "I have two quests available:\n" 
                            + q.getName() + " - " + q.getDescription() + "\n" 
                            + q.getName() + q.getDescription() + "\n"));
        }
        return ret;
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
            //if ( q.isSuitable(player) ) {
        	if( true ) {
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

	public void addQuest(Quest newQuest) {
		this.questList.add(newQuest);
	}
	
	/**
	 * Translate the persistent aspects of the player into the string
	 * format used by the database
	 */
	public String toDB() {
		String[] output = new String[12];
		output[0] = this.getDBRef() + "";              // player database reference number
		output[1] = this.getName();                    // player name
		output[2] = this.getFlagsAsString();           // player flags
		output[3] = this.getDesc();                    // player description
		output[4] = this.getLocation() + "";           // player location
		output[5] = this.getPass();                    // player password
		output[6] = stats.get(Abilities.STRENGTH) +
				"," + stats.get(Abilities.DEXTERITY) +
				"," + stats.get(Abilities.CONSTITUTION) +
				"," + stats.get(Abilities.INTELLIGENCE) +
				"," + stats.get(Abilities.WISDOM) +
				"," + stats.get(Abilities.CHARISMA);
		output[7] = getMoney().toString(false);        // player money
		output[8] = this.access + "";                  // player permissions level
		output[9] = race.getId() + "";                 // player race
		output[10] = pclass.getId() + "";              // player class
		output[11] = this.getStatus();                 // player status
		return Utils.join(output, "#");
	}

	@Override
	public void interact(Client client) {
		// TODO Auto-generated method stub
		
	}
}