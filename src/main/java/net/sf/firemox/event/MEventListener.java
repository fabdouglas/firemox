/*
 * Created on 19 octobre 2001, 21:31
 * 
 *   Firemox is a turn based strategy simulator
 *   Copyright (C) 2003-2007 Fabrice Daugan
 *
 *   This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 *   This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 *
 *   You should have received a copy of the GNU General Public License along  
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sf.firemox.event;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.Priority;
import net.sf.firemox.clickable.ability.ReplacementAbility;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.TriggeredCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.EventManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.tools.RevertedArrayList;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.53 support additional check registers
 * @see Event
 */
public abstract class MEventListener implements RegisterableEvent {

	/**
	 * Create an instance of MEventListener by reading a file Offset's file must
	 * pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idEvent [1]</li>
	 * <li>idZone [1]</li>
	 * <li>test [...]</li>
	 * <li>event's fields [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @param card
	 *          is the card owning this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 * @see Event
	 * @see net.sf.firemox.token.IdZones
	 */
	protected MEventListener(InputStream inputFile, MCard card)
			throws IOException {
		// Log.debug("EVENT " + this.getClass());
		this.idZone = inputFile.read();
		this.card = card;
		this.test = TestFactory.readNextTest(inputFile);
	}

	/**
	 * Creates a new instance of CanICast specifying all attributes of this class.
	 * All parameters are copied, not cloned. So this new object shares the card
	 * and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param test
	 *          the test of this event
	 * @param card
	 *          is the card owning this card
	 */
	protected MEventListener(int idZone, Test test, MCard card) {
		this.idZone = idZone;
		this.card = card;
		this.test = test;
	}

	/**
	 * Return the HTML code representing this ability.
	 * 
	 * @param ability
	 *          is the attached ability.
	 * @param context
	 *          the context needed by event activated
	 * @return the HTML code representing this ability.
	 * @since 0.85 Event is displayed
	 */
	public String toHtmlString(Ability ability, ContextEventListener context) {
		return "event-" + getClass().getSimpleName();
	}

	/**
	 * Return the idEvent of this event
	 * 
	 * @return the idEvent of this event
	 */
	public abstract Event getIdEvent();

	public abstract void registerToManager(Ability ability);

	public abstract void removeFromManager(Ability ability);

	/**
	 * Return a copy of this with the specified owner
	 * 
	 * @param card
	 *          is the card of the ability of this event
	 * @return copy of this event
	 */
	public abstract MEventListener clone(MCard card);

	/**
	 * return idZone of this card, whitout any code tap, untapped..)
	 * 
	 * @return idZone of this card, whitout any code tap, untapped..)
	 */
	public int getIdPlace() {
		return MCard.getIdZone(idZone, null);
	}

	/**
	 * Tell if the card is well placed for this event to be playable
	 * 
	 * @return true if the card is well placed for this event to be playable
	 */
	public boolean isWellPlaced() {
		return card.isSameIdZone(idZone);
	}

	/**
	 * Tell if the card is well placed for this event to be playable
	 * 
	 * @param idZone
	 *          the supposed zone where card is.
	 * @return true if the card is well placed for this event to be playable
	 */
	public boolean isWellPlaced(int idZone) {
		return this.idZone == idZone;
	}

	/**
	 * Tell if the event still activated before to be added to the stack
	 * 
	 * @param previousContext
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @return true if the current event match with this event
	 */
	public boolean reCheck(ContextEventListener previousContext, Ability ability) {
		return true;
	}

	/**
	 * Tell if the event still activated before to be added to the stack
	 * 
	 * @param triggered
	 *          the triggered card.
	 * @return true if the current event match with this event
	 */
	public boolean reCheck(TriggeredCard triggered) {
		return reCheck(triggered.getAbilityContext(), triggered.triggeredAbility);
	}

	/**
	 * Tell if the current event matches with this event. If there is an
	 * additional code to check, it'would be checked if the main event matches
	 * with the main event
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested component
	 * @return true if the current event match with this event
	 */
	protected boolean test(Ability ability, Target tested) {
		return test == null || test.test(ability, tested);
	}

	/**
	 * ONLY for ID__CAN_CAST_CARD event evaluates the code condition, and tell if
	 * we can cast a spell
	 * 
	 * @param idActivePlayer
	 *          the active player identifier
	 * @param idCard
	 *          is the idCard we would cast
	 * @return true if we can cast a spell
	 */
	protected boolean canIcastCondition(int idActivePlayer, int idCard) {
		if (idCard == IdTokens.MYSELF) {
			return EventManager.currentPhaseType().canICast(idActivePlayer,
					card.getIdCard());
		}
		return EventManager.currentPhaseType().canICast(idActivePlayer, idCard);
	}

	/**
	 * Reset all instant, empty-stack and triggered abilities. After the call to
	 * this method, there is no event listener able to be activated
	 */
	public static void reset() {
		TRIGGRED_ABILITIES.clear();
		REPLACEMENT_ABILITIES.clear();
		for (Event event : Event.values()) {
			TRIGGRED_ABILITIES.put(event, new RevertedArrayList<Ability>(20));
			Map<Priority, List<ReplacementAbility>> map = new EnumMap<Priority, List<ReplacementAbility>>(
					Priority.class);
			for (Priority priority : Priority.values()) {
				map.put(priority, new RevertedArrayList<ReplacementAbility>(20));
			}
			REPLACEMENT_ABILITIES.put(event, map);
		}
		CAN_I_CAST_ABILITIES[0].clear();
		CAN_I_CAST_ABILITIES[1].clear();
	}

	/**
	 * Create and returns an union of this event and the specified one. Both event
	 * must have the same type. Test(s) and events attributes may be groupped
	 * depending instance of this event. If no possible append is possible
	 * <code>null</code> is returned.
	 * 
	 * @param other
	 *          the event to append with 'or' operator.
	 * @return a new event reprensenting 'this' or 'other'
	 */
	public MEventListener appendOr(MEventListener other) {
		// TODO implements the OR appender to all child classes
		return null;
	}

	/**
	 * Indicates if this event corresponds to an activated ability
	 * 
	 * @return true if this event corresponds to an activated ability
	 */
	public abstract boolean isActivated();

	/**
	 * Indicates if this event corresponds to a triggered ability
	 * 
	 * @return true if this event corresponds to a triggered ability
	 */
	public abstract boolean isTriggered();

	/**
	 * additional code to check.
	 */
	public Test test = null;

	/**
	 * card owning this event
	 */
	public MCard card;

	/**
	 * idZone where the card have to be to be activated
	 */
	protected int idZone;

	/**
	 * Represent all active triggered abilities of games for each event
	 */
	public static final Map<Event, List<Ability>> TRIGGRED_ABILITIES = new EnumMap<Event, List<Ability>>(
			Event.class);

	/**
	 * Represent all active replacement abilities of games for each event and each
	 * priority.
	 */
	public static final Map<Event, Map<Priority, List<ReplacementAbility>>> REPLACEMENT_ABILITIES = new EnumMap<Event, Map<Priority, List<ReplacementAbility>>>(
			Event.class);

	/**
	 * Returns the replacement abilities associated to this event.
	 * 
	 * @param event
	 *          the event to use to retrieve the associated replacement abilities.
	 * @return the replacement abilities associated to this event.
	 */
	protected static final Map<Priority, List<ReplacementAbility>> getReplacementAbilities(
			Event event) {
		return REPLACEMENT_ABILITIES.get(event);
	}

	/**
	 * Represent all activated abilities/spell playable. One index per player.
	 */
	@SuppressWarnings("unchecked")
	public static final List<Ability>[] CAN_I_CAST_ABILITIES = new List[2];

	static {
		CAN_I_CAST_ABILITIES[0] = new ArrayList<Ability>();
		CAN_I_CAST_ABILITIES[1] = new ArrayList<Ability>();
	}

}