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
 */
package net.sf.firemox.action;

import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.Magic;
import net.sf.firemox.action.listener.WaitingAbility;
import net.sf.firemox.action.listener.WaitingCard;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.CanICast;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.ActivatedChoiceList;
import net.sf.firemox.stack.EventManager;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.component.TableTop;
import net.sf.firemox.zone.MZone;
import net.sf.firemox.zone.ZoneManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 */
public final class WaitActivatedChoice extends MAction implements WaitingCard,
		WaitingAbility {

	/**
	 * Create a new instance of WaitTriggeredBufferChoice
	 */
	private WaitActivatedChoice() {
		super();
	}

	/**
	 * Generate event associated to this action. Only one or several events are
	 * generated and may be collected by event listeners. Then play this action
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          is the context attached to this action.
	 * @return true if the stack resolution can continue. False if the stack
	 *         resolution is broken.
	 */
	public boolean play(ContextEventListener context, Ability ability) {
		hasDesclined[0] = false;
		hasDesclined[1] = false;
		if (!opponentResponse) {
			hasDesclined[1 - StackManager.idActivePlayer] = true;
		}
		zoneList = null;
		finished();
		if (waitAbilityChoice(StackManager.activePlayer())) {
			return finishedActivatedChoice();
		}
		return false;
	}

	public boolean clickOn(MCard card) {
		return StackManager.idHandedPlayer != 0;
	}

	public boolean clickOn(Ability ability) {
		return StackManager.idHandedPlayer == 0;
	}

	public boolean succeedClickOn(MCard card) {
		throw new InternalError("Card selection was not expected");
	}

	public boolean succeedClickOn(Ability ability) {
		finished();
		StackManager.newSpell(ability, ability.isMatching());
		return true;
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.WAIT_ACTIVATED_CHOICE;
	}

	public boolean manualSkip() {
		finished();
		hasDesclined[StackManager.oldIdHandedPlayer] = true;
		if (waitAbilityChoice(StackManager.nonActivePlayer())) {
			return finishedActivatedChoice();
		}
		return false;
	}

	public void finished() {
		if (activChoiceList != null) {
			activChoiceList.disHighLight();
			activChoiceList.clear();
			activChoiceList = null;
		}
		if (zoneList != null) {
			for (MZone zone : zoneList) {
				zone.disHighLight();
			}
			zoneList.clear();
			zoneList = null;
			StackManager.PLAYERS[0].disHighLight();
			StackManager.PLAYERS[1].disHighLight();
		}
	}

	/**
	 * Called when active player and non-active player have finished their choice
	 * order of their triggered abilities in the stack
	 * 
	 * @return true if the stack resolution can continue. False if the stack
	 *         resolution is broken.
	 */
	private boolean finishedActivatedChoice() {
		StackManager.disableAbort();
		WaitActivatedChoice.getInstance().finished();
		if (StackManager.isEmpty()) {
			// the stack is empty, so neither new spell, neither waiting triggered
			EventManager.gotoNextPhase();
			return false;
		}
		// we start resolving the stack
		return true;
	}

	/**
	 * @param firstPlayer
	 * @return true if the stack resolution can continue. False if the stack
	 *         resolution is broken.
	 */
	private boolean waitAbilityChoice(Player firstPlayer) {
		if (hasDesclined[firstPlayer.idPlayer]) {
			if (hasDesclined[1 - firstPlayer.idPlayer]) {
				// both players have declined to response, so we resolve stack
				return true;
			}
			return waitAbilityChoice(firstPlayer.getOpponent());
		}
		final List<Ability> res = new ArrayList<Ability>();
		final List<Ability> advRes = new ArrayList<Ability>();
		CanICast.dispatchEvent(firstPlayer.idPlayer, res, advRes);
		finished();
		if (!res.isEmpty()) {
			/*
			 * at least one playable ability, so this player becomes the new active
			 */
			activChoiceList = new ActivatedChoiceList(res, advRes);
			res.clear();
			advRes.clear();
			if (firstPlayer.isYou() && StackManager.isEmpty()
					&& firstPlayer.declinePlay() || firstPlayer.isYou()
					&& StackManager.currentIsYou() && !StackManager.isEmpty()
					&& firstPlayer.declineResponseMe() || firstPlayer.isYou()
					&& !StackManager.currentIsYou() && !StackManager.isEmpty()
					&& firstPlayer.declineResponseOpponent()) {

				StackManager.idActivePlayer = 0;
				StackManager.idHandedPlayer = 0;
				Log.debug(new StringBuilder("You play. Dump [ap :").append(
						StackManager.idActivePlayer).append(", cp :").append(
						StackManager.idCurrentPlayer).append(", s# :").append(
						StackManager.CONTEXTES.size()).append(", action :")
						.append(
								StackManager.actionManager.currentAction.getClass()
										.getSimpleName()).append(", li :").append(
								StackManager.actionManager.loopingIndex).append(", hop :")
						.append(StackManager.actionManager.hop).append(
								"] - (AUTO SKIP IS COMING)"));
				StackManager.oldIdHandedPlayer = 0;
				Magic.sendManualSkip();
				return manualSkip();
			}
			if (activChoiceList != null) {
				zoneList = activChoiceList.highLight();
				if (zoneList != null && !StackManager.isEmpty()
						&& !zoneList.contains(ZoneManager.stack)) {
					TableTop.getInstance().tabbedPane
							.setSelectedComponent(ZoneManager.stack.superPanel);
				}
			}
			firstPlayer.setActivePlayer();
			return false;
		}
		// this player has no playable ability, we test the opponent
		hasDesclined[firstPlayer.idPlayer] = true;
		return waitAbilityChoice(firstPlayer.getOpponent());
	}

	public List<Ability> abilitiesOf(MCard card) {
		return activChoiceList == null ? null : activChoiceList.abilitiesOf(card);
	}

	public List<Ability> advancedAbilitiesOf(MCard card) {
		return activChoiceList == null ? null : activChoiceList
				.advancedAbilitiesOf(card);
	}

	/**
	 * return the string representation of this action
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @return the string representation of this action
	 * @see Object#toString()
	 */
	@Override
	public String toString(Ability ability) {
		return "Wait activated ability choice";
	}

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class.
	 */
	public static WaitActivatedChoice getInstance() {
		if (instance == null)
			instance = new WaitActivatedChoice();
		return instance;
	}

	/**
	 * The unique instance of this class
	 */
	private static WaitActivatedChoice instance;

	/**
	 * indicates if this player has already declined to response to current spell
	 * When the active players gets priority, it would be tested to know if we
	 * have to resolve the stack
	 */
	private boolean[] hasDesclined = new boolean[2];

	/**
	 * This is the list of zone concerned by highlighted cards
	 */
	private List<MZone> zoneList;

	/**
	 * the current list of choices of active player
	 */
	private ActivatedChoiceList activChoiceList;

	/**
	 * If true, player can response to opponent effects
	 */
	public static boolean opponentResponse;
}