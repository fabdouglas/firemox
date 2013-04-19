/*
 * WaitTriggeredBufferChoice.java
 * Created on 26 févr. 2004
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
 * 
 */
package net.sf.firemox.action;

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.action.listener.WaitingTriggeredCard;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.TriggeredCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.EventManager;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.Log;
import net.sf.firemox.zone.TriggeredBuffer;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 */
public final class WaitTriggeredBufferChoice extends MAction implements
		StandardAction, WaitingTriggeredCard {

	/**
	 * Create a new instance of WaitTriggeredBufferChoice
	 */
	private WaitTriggeredBufferChoice() {
		super();
	}

	public void finished() {
		StackManager.activePlayer().zoneManager.triggeredBuffer.disHighLightAll();
		if (EventManager.parsinfBeforeEnd() || EventManager.parsingEndPhaseEvent) {
			/*
			 * we are parsing the BEFORE_PHASE_... or END_PHASE_... event : no player
			 * gets priority.
			 */
			/* if (EventManager.isEndOfPhaseEventBroken()) { */
			if (StackManager.isEmpty()) {
				// the stack is empty, so neither new spell, neither waiting triggered
				EventManager.gotoNextPhase();
			} else {
				// we start resolving the stack
				StackManager.resolveStack();
			}
			// }
		} else {
			// we set the active action as the WaitActivatedChoice one
			StackManager.actionManager.currentAction = WaitActivatedChoice
					.getInstance();
			// The active player gets priority
			if (WaitActivatedChoice.getInstance().play(null, null)) {
				StackManager.resolveStack();
			}
		}
	}

	public boolean play(ContextEventListener context, Ability ability) {
		throw new InternalError("should not be called");
	}

	public boolean clickOn(TriggeredCard triggeredCard) {
		if (StackManager.idHandedPlayer == 0) {
			TriggeredBuffer cont = StackManager.PLAYERS[0].zoneManager.triggeredBuffer;
			for (int i = cont.getCardCount(); i-- > 0;) {
				// this card has to be found in the choice of handed player
				if (cont.getTriggeredAbility(i) == triggeredCard) {
					// card has been found, we can play it
					return true;
				}
			}
			/*
			 * TriggeredCard has not been found into the Triggered Buffer zone, we
			 * suppose this triggeredcard has been played automatically from the
			 * abstractZone of Triggered Buffer. So, we add to the stack this hidden
			 * triggered ability
			 */
			if (!triggeredCard.triggeredAbility.isHidden()) {
				// force the resolving stack process
				throw new InternalError("Mysterious unhidden triggered ability "
						+ triggeredCard.getCardName()
						+ ", card="
						+ triggeredCard.triggeredAbility.getCard()
						+ (triggeredCard.triggeredAbility.getCard() != null ? "@"
								+ Integer.toHexString(triggeredCard.triggeredAbility.getCard()
										.hashCode()) : ""));
			}
			throw new InternalError("clicked on abstract triggered ability");
		}
		return false;
	}

	public boolean succeedClickOn(TriggeredCard triggeredCard) {
		try {
			StackManager.PLAYERS[triggeredCard.triggeredAbility.getCard()
					.getController().idPlayer].zoneManager.triggeredBuffer
					.disHighLightAll();
		} catch (Exception e) {
			Log.debug("error : " + e);
		}
		return triggeredCard.newSpell();
	}

	public boolean manualSkip() {
		/*
		 * we stack automatically the first waiting triggered buffer choice to the
		 * stack in the reverse order they have been added into the triggered buffer
		 * zone. Note: the abstract triggered buffer zone is empty when we are here
		 * since these hidden abilities have been already played automatically
		 * before the played has received the hand.
		 */
		StackManager.actionManager
				.succeedClickOn((TriggeredCard) StackManager.PLAYERS[StackManager.oldIdHandedPlayer].zoneManager.triggeredBuffer
						.getComponent(0));
		return false;
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.WAIT_TRIGGERED_BUFFER_CHOICE;
	}

	@Override
	public String toString(Ability ability) {
		return "wait triggered ability buffer choice";
	}

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class.
	 */
	public static WaitTriggeredBufferChoice getInstance() {
		if (instance == null)
			instance = new WaitTriggeredBufferChoice();
		return instance;
	}

	/**
	 * The unique instance of this class
	 */
	private static WaitTriggeredBufferChoice instance;

}