/* 
 * Created on 21 févr. 2004
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
package net.sf.firemox.event.phase;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.Priority;
import net.sf.firemox.clickable.ability.ReplacementAbility;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.Event;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.stack.EventManager;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Or;
import net.sf.firemox.test.Test;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * End of phase events. Raised just before to go to the next phase <br>
 * If idPhase value is NO_CARE, this event would be acivated at each end of
 * phase <br>
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public class EndOfPhase extends PhaseEvent {

	/**
	 * Create an instance of MEventEndOfPhase by reading a file Offset's file must
	 * pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone [1]</li>
	 * <li>test [...]</li>
	 * <li>idPhase(phase identifier) [2]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @param card
	 *          is the card owning this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public EndOfPhase(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
	}

	/**
	 * Creates a new instance of MEventEndOfPhase specifying all attributes of
	 * this class. All parameters are copied, not cloned. So this new object
	 * shares the card and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param test
	 *          the test of this event
	 * @param card
	 *          is the card owning this card
	 * @param idPhase
	 *          the looked for phase identifier
	 * @param phaseFilter
	 *          the filter.
	 */
	public EndOfPhase(int idZone, Test test, MCard card, Expression idPhase,
			PhaseFilter phaseFilter) {
		super(idZone, test, card, idPhase, phaseFilter);
	}

	@Override
	public MEventListener clone(MCard card) {
		return new EndOfPhase(idZone, test, card, idPhase, phaseFilter);
	}

	/**
	 * Dispatch this event to all active event listeners able to understand this
	 * event. The listening events able to understand this event are <code>this
	 * </code>
	 * and other multiple event listeners. For each event listeners having
	 * responded they have been activated, the corresponding ability is added to
	 * the triggered buffer zone of player owning this ability. <br>
	 */
	public static void dispatchEvent() {
		dispatchEvent(EVENT);
	}

	/**
	 * Dispatch this event to replacement abilites only. If one or several
	 * abilities have been activated this way, this function will return false.
	 * The return value must be checked. In case of <code>false</code> value,
	 * the caller should not call any stack resolution since activated abilities
	 * are being played. Unusually, when several replacement abilities are found
	 * for this event, ONLY the first replacement found replacement ability is
	 * picked and played.
	 * 
	 * @param currentIdPhase
	 *          the current phase type identifier (not index)
	 * @return true if the event has not been replaced.
	 */
	public static boolean tryAction(int currentIdPhase) {
		final Map<Priority, List<ReplacementAbility>> map = getReplacementAbilities(EVENT);
		for (Priority priority : Priority.values()) {
			for (ReplacementAbility ability : map.get(priority)) {
				if (((EndOfPhase) ability.eventComing()).isMatching(ability)) {
					if (StackManager.newSpell(ability
							.getTriggeredClone(new MContextCardCardIntInt(StackManager
									.currentPlayer(), currentIdPhase)))) {
						StackManager.resolveStack();
					}
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public final Event getIdEvent() {
		return EVENT;
	}

	/**
	 * The event type.
	 */
	public static final Event EVENT = Event.EOP;

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		return LanguageManagerMDB.getString("event-eop", LanguageManagerMDB
				.getString(EventManager.turnStructure[idPhase.getValue(ability, null,
						context)].phaseName));
	}

	@Override
	public MEventListener appendOr(MEventListener other) {
		if (((PhaseEvent) other).idPhase == idPhase) {
			return new EndOfPhase(idZone, Or.append(this.test, other.test), card,
					idPhase, phaseFilter);
		}
		return null;
	}
}