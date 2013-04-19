/*
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.action.listener.WaitingAbility;
import net.sf.firemox.action.listener.WaitingCard;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.ActivatedAbility;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.stack.ActivatedChoiceList;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.zone.MZone;

/**
 * Force a player to play ONE or several abilities. The player must be in the
 * first index of current target-list. When the forced ability is played, the
 * corresponding card is add to the target-list. The 'hop' attribute is used to
 * skip actions when no available ability can be played or when the player
 * cancels this action. The 'must-be-played' attribute allow or not the player
 * to cancel this action.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
class ForcePlay extends UserAction implements StandardAction, WaitingCard,
		WaitingAbility {

	/**
	 * Create an instance of CreateCard by reading a file Offset's file must
	 * pointing on the first byte of this action
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>on error hop [Expression]</li>
	 * <li>mustBePlayed [boolean]</li>
	 * <li>cardTest [Test.]</li>
	 * <li>abilityTest [Test]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	ForcePlay(InputStream inputFile) throws IOException {
		super(inputFile);
		hop = ExpressionFactory.readNextExpression(inputFile);
		mustBePlayed = inputFile.read() == 1;
		cardTest = TestFactory.readNextTest(inputFile);
		abilityTest = TestFactory.readNextTest(inputFile);
	}

	/**
	 * return the id of this action. This action has been read from the mdb file.
	 * 
	 * @see Actiontype
	 * @return the id of this action
	 */
	@Override
	public final Actiontype getIdAction() {
		return Actiontype.FORCE_PLAY;
	}

	public List<Ability> abilitiesOf(MCard card) {
		return activChoiceList == null ? null : activChoiceList.abilitiesOf(card);
	}

	public List<Ability> advancedAbilitiesOf(MCard card) {
		return activChoiceList == null ? null : activChoiceList
				.advancedAbilitiesOf(card);
	}

	/**
	 * Called to specify the triggered card chosen for the current action by the
	 * handed player
	 * 
	 * @param card
	 *          the clicked card by the handed player for the current action
	 * @return true if this click has been managed. Return false if this click has
	 *         been ignored
	 */
	public boolean clickOn(MCard card) {
		return StackManager.idHandedPlayer != 0;
	}

	/**
	 * Called to specify the triggered card chosen for the current action by the
	 * handed player
	 * 
	 * @param ability
	 *          the clicked card by the handed player for the current action
	 * @return true if this click has been managed. Return false if this click has
	 *         been ignored
	 */
	public boolean clickOn(Ability ability) {
		return StackManager.idHandedPlayer == 0;
	}

	public boolean succeedClickOn(MCard card) {
		throw new InternalError("doesn't wait card");
	}

	public boolean succeedClickOn(Ability ability) {
		finished();
		StackManager.getInstance().getTargetedList().list.add(ability.getCard());
		StackManager.newSpell(ability, ability.isMatching());
		return true;
	}

	/**
	 * Called by the handed player when he/she wants to skip/abort this action. No
	 * stack operation should be done here.<br>
	 * For this action, the cancel is always accepted. If both players haved
	 * declined to play an activated ability, the stack would be resolved.
	 * 
	 * @return true if this action allow the skip/cancel.
	 */
	public boolean manualSkip() {
		if (mustBePlayed) {
			return false;
		}
		finished();
		return false;
	}

	/**
	 * Called when this action is finished (aborted or completed)
	 */
	public void finished() {
		if (activChoiceList != null) {
			activChoiceList.disHighLight();
			activChoiceList.clear();
			activChoiceList = null;
		}
		if (zoneList != null) {
			for (int i = zoneList.size(); i-- > 0;) {
				zoneList.get(i).disHighLight();
			}
			zoneList.clear();
			zoneList = null;
			StackManager.PLAYERS[0].disHighLight();
			StackManager.PLAYERS[1].disHighLight();
		}
	}

	public boolean play(ContextEventListener context, Ability ability) {
		if (StackManager.getTargetListAccess().isEmpty()
				|| !StackManager.getTargetListAccess().get(0).isPlayer()) {
			throw new RuntimeException(
					"In ForcePlay action, the target-list should contin at least on player");
		}
		final List<Target> validCards = new ArrayList<Target>();
		final Player player = (Player) StackManager.getTargetListAccess().get(0);

		if (restrictionZone != -1) {
			StackManager.PLAYERS[StackManager.idCurrentPlayer].zoneManager
					.getContainer(restrictionZone).checkAllCardsOf(cardTest, validCards,
							ability);
			StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].zoneManager
					.getContainer(restrictionZone).checkAllCardsOf(cardTest, validCards,
							ability);
		} else {
			StackManager.PLAYERS[StackManager.idCurrentPlayer].zoneManager
					.checkAllCardsOf(cardTest, validCards, ability);
			StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].zoneManager
					.checkAllCardsOf(cardTest, validCards, ability);
		}
		final List<Ability> res = new ArrayList<Ability>();
		final List<Ability> advRes = new ArrayList<Ability>();

		for (int i = MEventListener.CAN_I_CAST_ABILITIES[player.idPlayer].size(); i-- > 0;) {
			final Ability abilityI = MEventListener.CAN_I_CAST_ABILITIES[player.idPlayer]
					.get(i);
			if (abilityI instanceof ActivatedAbility
					&& validCards.contains(abilityI.getCard())
					&& abilityTest.test(abilityI, abilityI.getCard())
					&& abilityI.checkTargetActions() && abilityI.checkObjectActions()) {
				if (abilityI.isMatching()) {
					res.add(abilityI);
				} else {
					advRes.add(abilityI);
				}
			}
		}
		if (!res.isEmpty()) {
			// at least on playable/valid ability
			finished();
			activChoiceList = new ActivatedChoiceList(res, advRes);
			res.clear();
			advRes.clear();
			zoneList = activChoiceList.highLight();
			player.setActivePlayer();
			return false;
		}
		StackManager.actionManager.setHop(hop.getValue(ability, null, context));
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "Force to play";
	}

	/**
	 * The card to add
	 */
	private Test abilityTest;

	/**
	 * The test applied on card to determine if one ability can/must be played
	 * from this card.
	 */
	private Test cardTest;

	/**
	 * The zone identifant where the scan is restricted. If is equal to -1, there
	 * would be no restriction zone.
	 * 
	 * @see net.sf.firemox.token.IdZones
	 */
	protected int restrictionZone;

	/**
	 * When is true, the player must play the determinated abilities.
	 */
	private boolean mustBePlayed;

	/**
	 * This is the list of zone concerned by highlighted cards
	 */
	private List<MZone> zoneList;

	/**
	 * The hop to do in case of no plaable ability
	 */
	private Expression hop;

	/**
	 * the current list of choices of active player
	 */
	private ActivatedChoiceList activChoiceList;
}