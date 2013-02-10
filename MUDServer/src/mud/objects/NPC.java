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
	
	//@Override
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

}
