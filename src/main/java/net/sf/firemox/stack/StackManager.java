/*
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
package net.sf.firemox.stack;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import net.sf.firemox.action.MAction;
import net.sf.firemox.action.MoveCard;
import net.sf.firemox.action.PayMana;
import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.ManaCost;
import net.sf.firemox.action.listener.Waiting;
import net.sf.firemox.action.target.ChosenTarget;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.ReplacementAbility;
import net.sf.firemox.clickable.ability.TriggeredAbility;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.clickable.target.card.TriggeredCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.network.MMiniPipe;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.token.IdPositions;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.IntegerList;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.PairCardInt;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.zone.ZoneManager;

/**
 * Represents all methods to cast a spell/ability. Manage the succession of
 * actions of a ability.
 * 
 * @since 0.12 enable to abort mana pay of a spell/ability <br>
 *        0.13 ability will be differenced from other spell, copy is highlighted
 *        to green <br>
 *        0.24 save and restore choice list of opponent too <br>
 *        0.30 an option playing automatically single "YOU MUST" ability is
 *        supported <br>
 *        0.30 stack resolution activated when two players declines to response
 *        to a spell<br>
 *        0.31 additional "skipping" options added <br>
 *        0.52 "hop" instruction supported, to enable 'if then else', 'for' like
 *        instructions <br>
 *        0.52 private values added, used as registers for each spell. <br>
 *        0.72 requesting 'refresh' for cards have been implemented. <br>
 *        0.80 looping actions supported <br>
 *        0.85 Game restart after a game lost is supported <br>
 *        0.85 Mana pay can be aborted. <br>
 *        0.86 Cost are initialized and can be canceled/replayed. <br>
 *        0.94 Additional costs are handled and filtered to ActionManager <br>
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public final class StackManager implements StackContext {

	/**
	 * Create a new instance of this class.
	 */
	private StackManager() {
		super();
	}

	/**
	 * Reset completely the stack
	 */
	public static void reset() {
		lostGameStatus = 0;
		gameLostProceed = false;
		CONTEXTES.clear();
		disableAbort();
		SAVED_INT_LISTS.clear();
		SAVED_TARGET_LISTS.clear();
		noReplayToken.release();
		REFRESH_PROPERTIES.clear();
		REFRESH_REGISTERS.clear();
		REFRESH_CONTROLLER.clear();
		REFRESH_TYPES.clear();
		REFRESH_ABILITIES.clear();
		REFRESH_COLORS.clear();
		idHandedPlayer = -1;
	}

	/**
	 * Read from the specified stream the initial registers of players.
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>register of starting player [IdTokens.PLAYER_REGISTER_SIZE]</li>
	 * <li>register of non starting player [IdTokens.PLAYER_REGISTER_SIZE]</li>
	 * <li>abortion idZone [1]</li>
	 * <li>additional-costs [Test,Action[]]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing the stack definition.
	 * @param firstPlayer
	 *          the player's id starting the game.
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public void init(InputStream inputFile, int firstPlayer) throws IOException {

		// load the registers
		PLAYERS[0].registers = new int[IdTokens.PLAYER_REGISTER_SIZE];
		PLAYERS[1].registers = new int[IdTokens.PLAYER_REGISTER_SIZE];
		for (int i = 0; i < IdTokens.PLAYER_REGISTER_SIZE; i++) {
			PLAYERS[firstPlayer].registers[i] = inputFile.read();
		}
		for (int i = 0; i < IdTokens.PLAYER_REGISTER_SIZE; i++) {
			PLAYERS[1 - firstPlayer].registers[i] = inputFile.read();
		}

		// place where aborted spell would be placed
		zoneAbortion = inputFile.read();

		// Load additional costs
		final int count = inputFile.read();
		additionalCosts = new ArrayList<AdditionalCost>();
		for (int i = 0; i < count; i++) {
			additionalCosts.add(new AdditionalCost(inputFile));
		}

		getInstance().abortingAbility = null;
		actionManager = new ActionManager(null, null);
	}

	/**
	 * Add a spell/activated ability to the stack
	 * 
	 * @param ability
	 *          is the ability played
	 * @param advancedMode
	 *          if true, this ability would be added with advanced mode option.
	 * @since 0.13 ability will be differenced from other spell, copy is
	 *        highlighted to green
	 * @since 0.85 advanced mode supported
	 */
	public static void newSpell(Ability ability, boolean advancedMode) {
		// initialize parameter of this spell/ability
		Log.info("NEW SPELL " + MToolKit.getLogCardInfo(ability.getCard())
				+ (ability.isAutoResolve() ? ", autoResolve=true" : ""));
		instance.initNewSpell(ability, ability.getCard().controller, null);
		if (ability.isPlayAsSpell()) {
			tokenCard = ability.getCard();
			// it's a spell, we place the card of this spell into the stack
			previousPlace = ((MCard) tokenCard).getIdZone();
			// TODO previousPosition = ((MCard) tokenCard).getIdZone();
			tokenCard.moveCard(IdZones.STACK, tokenCard.controller, false,
					IdPositions.ON_THE_TOP);
			((MCard) tokenCard).unregisterAbilities();
		} else {
			tokenCard = ability.getCardCopy();
			// we place a copy of the card of the ability on the top of the stack
			ZoneManager.stack.addTop((MCard) tokenCard);
			previousPlace = -1;
		}
		MagicUIComponents.logListing.append(spellController.idPlayer, ability
				.getLog(null));
	}

	/**
	 * Add a triggered ability to the stack. If this ability is an hidden one, the
	 * stack is resolved immediately and then another triggered card can be added.
	 * Otherwise, this triggered ability is added to the stack and the 'wait
	 * triggered buffer choice' process is reloaded until no more triggered
	 * ability can be added to the stack.
	 * 
	 * @param triggered
	 *          is the triggered ability to add to t he stack
	 * @return true if the stack can be resolved after this call.
	 * @since 0.60
	 */
	public static boolean newSpell(TriggeredCard triggered) {
		// initialize parameter of this spell/ability
		Log.info("NEW TRIGGERED ABILITY " + triggered);
		instance.initNewSpell(triggered.triggeredAbility, triggered.controller,
				triggered);
		StackManager.triggered = triggered;
		StackManager.tokenCard = triggered;
		previousPlace = -1;
		if (currentAbility.isSystemAbility()) {
			MagicUIComponents.logListing.append(-1, currentAbility.getLog(instance
					.getAbilityContext()));
		} else {
			MagicUIComponents.logListing.append(spellController.idPlayer,
					currentAbility.getLog(instance.getAbilityContext()));
		}
		if (triggered.triggeredAbility.isHidden()) {
			return true;
		}
		triggered.moveCard(IdZones.STACK, triggered.triggeredAbility.getCard()
				.getController(), false, IdPositions.ON_THE_TOP);
		// check again waiting triggered abilities
		StackManager.activePlayer().waitTriggeredBufferChoice(true);
		return false;
	}

	/**
	 * Save context, initialize variables for this new spell/ability
	 * 
	 * @param ability
	 *          the new spell/ability to add to the stack
	 * @param controller
	 *          the controller of this new spell.
	 */
	private void initNewSpell(Ability ability, Player controller,
			TriggeredCard triggered) {
		CONTEXTES.push(new StackElement());
		spellController = controller;
		registers = new int[IdTokens.STACK_REGISTER_SIZE];
		getInstance().targetedList = new TargetedList(ability);
		intList = new IntegerList();
		targetOptions = new TargetManager();
		StackManager.triggered = triggered;
		canBeAborted = true;
		abilityID++;

		// add this ability in the abilities list
		currentAbility = ability;

		// Built the new action manager
		actionManager = new ActionManager(currentAbility,
				getAdditionalCost(ability));
	}

	/**
	 * Returns the current additional costs.
	 * 
	 * @return the current additional costs.
	 */
	public List<AdditionalCost> getAdditionalCost() {
		return additionalCosts;
	}

	/**
	 * Returns the additional cost can be applied to the given ability.
	 * 
	 * @param ability
	 *          the ability to test.
	 * @return the additional cost can be applied to the given ability.
	 */
	public MAction[] getAdditionalCost(Ability ability) {
		MAction[] addiCosts = null;
		for (AdditionalCost addiCost : additionalCosts) {
			if (addiCost.constraint.test(ability, ability.getCard())) {
				if (addiCosts == null) {
					addiCosts = addiCost.cost;
				} else {
					final MAction[] tmp = addiCosts;
					addiCosts = new MAction[addiCosts.length + addiCost.cost.length];
					System.arraycopy(tmp, 0, addiCosts, 0, tmp.length);
					System.arraycopy(addiCost.cost, 0, addiCosts, tmp.length,
							addiCost.cost.length);
				}
			}
		}
		return addiCosts;
	}

	/**
	 * Resolve the stack <br>
	 * <ul>
	 * An ability is split into 4 parts :
	 * <li>paying part (cost)
	 * <li>raise event "casting", stack waiting triggered abilities and active
	 * player gets priority
	 * <li>effect part (effects)
	 * <li>restore context, stack waiting triggered abilities and active player
	 * gets priority
	 * </ul>
	 */
	public static void resolveStack() {
		do {
			if (gameLostProceed) {
				return;
			}
			if (spellController != null) {
				idActivePlayer = spellController.idPlayer;
			}

			if (isEmpty()) {
				// The active player gets priority (if no triggered have to be stacked)
				StackManager.activePlayer().waitTriggeredBufferChoice(true);
				return;
			}
			if (aborted) {
				// the current spell has been aborted
				getInstance().finishSpell();
				return;
			}
		} while (actionManager.playNextAction());
		// resolveStack();

	}

	/**
	 * Cancel the current spell or ability. A canceled ability is removed from the
	 * stack. A canceled spell is returned from the stack to it's previous zone. A
	 * canceled triggered ability, if hidden, is stopped immediately and the stack
	 * resolution continues. A canceled triggered ability that is not hidden is
	 * simply restarted : it is neither removed from the stack neither replaced in
	 * the TBZ.
	 * 
	 * @since 0.86
	 * @since 0.90 The spell is only canceled if the current ability is in "cost
	 *        part".
	 */
	public static void cancel() {
		if (actionManager.advancedEffectMode) {
			// we are in advanced mode in the "effect part", restart only this part
			Log.info("\t...ability " + triggered
					+ " canceled in effect part, restart it");
			actionManager.rollback();
			actionManager.restart(currentAbility);
			resolveStack();
		} else {

			// Terminate the current 'Waiting' action
			((Waiting) actionManager.currentAction).finished();

			// Is the current ability is a triggered one?
			if (actionManager.advancedMode) {
				actionManager.rollback();
			}
			if (triggered == null) {
				if (tokenCard.isACopy()) {
					/*
					 * it was an activated ability, so since card is a copy in stack panel
					 * we have to remove it from stack to put it into the abyss.
					 */
					Log.info("\t...restore context, " + tokenCard
							+ "'s ability is canceled");
					ZoneManager.stack.remove(tokenCard);
				} else {
					/*
					 * it was a spell. This card has to be placed into it's previous zone.
					 */
					tokenCard.moveCard(previousPlace, spellController, false, 0);

					// restore available abilities
					((MCard) tokenCard).registerAbilities(MCard.getIdZone(previousPlace,
							null));
				}
				Log.info("\t...restore context, " + tokenCard + " spell is canceled");

				// we restore the context of the stack and continue to resolve it
				CONTEXTES.pop().restore();
			} else {
				/*
				 * it was a triggered ability, this card has NOT to be placed into it's
				 * previous zone. This ability is not canceled, but only reinitialized.
				 */
				if (triggered.triggeredAbility.isHidden()) {
					// this ability has never been put in the stack, should not happens
					Log.warn("\t...restore context, hidden triggered canceled "
							+ triggered);
					CONTEXTES.pop().restore();
				} else {
					Log.info("\t...triggered canceled " + triggered + ", restarting it");
					StackManager.actionManager.restart(currentAbility);
					StackManager.resolveStack();
				}
			}
		}
	}

	/**
	 * This method finish the current spell. Remove it from the stack and restore
	 * the last context of stack.
	 */
	public void finishSpell() {
		// Is the game is finished?
		if (processGameLost()) {
			// Ok, stop stack resolution now
			gameLostProceed = true;
			return;
		}

		// Is the current ability is a triggered one?
		if (triggered == null) {
			if (tokenCard.isACopy()) {
				/**
				 * it was an activated ability, so since card is a copy in stack panel
				 * we have to remove it from stack to put it into the abyss.
				 */
				Log.info("\t...restore context, " + tokenCard + "'s ability is done");
				ZoneManager.stack.remove(tokenCard);
			} else {
				/**
				 * it was a spell. This card has at least on action as part of it's
				 * effects that moves itself into another place like graveyard or hand.
				 * So the card is now in graveyard or in play now or somewhere else but
				 * no more in the stack.
				 */
				if (aborted) {
					// the current spell has just been aborted, so we have to remove it
					targetedList.clear();
					// the spell really abort
					MoveCard.moveCard((MCard) tokenCard, TestOn.OWNER, zoneAbortion,
							null, IdPositions.ON_THE_TOP, currentAbility, false);
				}
				Log.info("\t...restore context, " + tokenCard + "'s spell is done");
			}
		} else {
			/**
			 * it was a triggered ability, we remove it from the stack to put it in
			 * the abyss.
			 */
			Log.info("\t...restore context, triggered done " + triggered);
			if (!triggered.triggeredAbility.isHidden()) {
				// this ability has been put in the stack
				ZoneManager.stack.remove(triggered);
			}
			if (triggered.getAbilityContext() != null) {
				triggered.getAbilityContext().removeTimestamp();
			}
		}
		CONTEXTES.pop().restore();
	}

	/**
	 * enable abort action
	 * 
	 * @since 0.12 enable to abort mana pay of a spell/ability
	 */
	public static void enableAbort() {
		// enable cancel button
		MagicUIComponents.skipButton.setIcon(CANCEL_ICON);
		MagicUIComponents.skipButton.setToolTipText(TT_CANCEL);
		MagicUIComponents.skipMenu.setIcon(CANCEL_ICON);
		MagicUIComponents.skipMenu.setText(CANCEL_TXT);
		MagicUIComponents.skipMenu.setToolTipText(TT_CANCEL);
		if (actionManager.advancedMode) {
			MagicUIComponents.chosenCostPanel.cancelButton.setEnabled(true);
		}
	}

	/**
	 * disable abort action
	 * 
	 * @since 0.12 enable to abort mana pay of a spell/ability
	 */
	public static void disableAbort() {
		MagicUIComponents.skipButton.setIcon(DECLINE_ICON);
		MagicUIComponents.skipButton.setToolTipText(TT_DECLINE);
		MagicUIComponents.skipMenu.setIcon(DECLINE_ICON);
		MagicUIComponents.skipMenu.setToolTipText(TT_DECLINE);
		MagicUIComponents.skipMenu.setText(DECLINE_TXT);
		MagicUIComponents.chosenCostPanel.cancelButton.setEnabled(false);
		canBeAborted = false;
	}

	/**
	 * tell if it stills yet any abilities in the stack
	 * 
	 * @return true if it stills yet any abilities in the stack
	 */
	public static boolean isEmpty() {
		return CONTEXTES.isEmpty();
	}

	public MCard getSourceCard() {
		if (triggered != null) {
			/*
			 * the real card source of current ability is the card having generated
			 * this triggered
			 */
			return triggered.triggeredAbility.getCard();
		}
		// else this an ability or a spell directly placed into the stack
		return (MCard) tokenCard;
	}

	public void abortion(AbstractCard card, Ability source) {
		MagicUIComponents.logListing.append(0, "abortion of " + card + '\n');
		Log.info("abortion of " + card);

		boolean wasCurrent = card == tokenCard;
		if (wasCurrent) {
			/*
			 * normal end of the current spell, as it would be done by the normal
			 * process in resolveStack() method
			 */
			aborted = true;
			getInstance().abortingAbility = source;
			getInstance().finishSpell();
		} else {
			// the spell to remove is somewhere in the stack,
			if (card instanceof TriggeredCard) {
				/*
				 * it was a triggered ability, we remove it from the stack to put it in
				 * the abyss.
				 */
				if (triggered.triggeredAbility.isHidden()) {
					// nothing to do since this ability has not been put in the stack
					Log.info("restore context, triggered "
							+ triggered.triggeredAbility.getName() + "  is done (hidden)"
							+ MToolKit.getLogCardInfo(triggered.triggeredAbility.getCard()));
				} else {
					Log.info("restore context, triggered "
							+ triggered.triggeredAbility.getName() + " is done"
							+ MToolKit.getLogCardInfo(triggered.triggeredAbility.getCard()));
					ZoneManager.stack.remove(card);
				}
			} else if (card.isACopy()) {
				/*
				 * it was an ability, so since card is a copy in stack panel we have to
				 * remove it from stack to put it into the abyss.
				 */
				Log.info("restore context, " + card + "'s ability is aborted");
				ZoneManager.stack.remove(card);
			} else {

				/*
				 * It's a spell. This card has at least on action as part of it's
				 * effects that moves itself into another place like graveyard or hand.
				 * But in this case (abortion) the effects managing this end of spell
				 * have not been played, so we have to do it.
				 */
				Log.info("restore context, " + card
						+ "'s spell has been aborted, move it to abortion place");

				// clean the context of this spell
				StackContext context = getContextOf(card);
				context.getActionManager().clean(context.getActionManager());

				// restore available abilities
				((MCard) card).registerAbilities(zoneAbortion);

				card.moveCard(zoneAbortion, ((MCard) card).getOwner(), false, 0);
			}
			/*
			 * now we have to indicate that the ability has been aborted without
			 * destroying the stack of context
			 */
			for (int i = CONTEXTES.size(); i-- > 0;) {
				if (CONTEXTES.get(i).ctxtokenCard == card) {
					// the removed card has been found in the stack of context
					CONTEXTES.get(i).ctxaborted = true;
					return;
				}
			}
			throw new InternalError("couldn't find the card to abort");
		}
	}

	/**
	 * Return the target list.
	 * 
	 * @return the target list.
	 */
	public static List<Target> getTargetListAccess() {
		return getInstance().getTargetedList().list;
	}

	/**
	 * return the current player
	 * 
	 * @return the current player
	 */
	public static Player currentPlayer() {
		return PLAYERS[idCurrentPlayer];
	}

	/**
	 * return the active player
	 * 
	 * @return the active player
	 */
	public static Player activePlayer() {
		return PLAYERS[idActivePlayer];
	}

	/**
	 * return the non-active player
	 * 
	 * @return the non-active player
	 */
	public static Player nonActivePlayer() {
		return PLAYERS[1 - idActivePlayer];
	}

	/**
	 * Indicates if the current player is you. Current player is the player'turn.
	 * 
	 * @return true if the current player is you
	 */
	public static boolean currentIsYou() {
		return currentPlayer() == PLAYERS[0];
	}

	/**
	 * Indicates if the specified player is you
	 * 
	 * @param player
	 *          the player to test
	 * @return true if the specified player is you
	 */
	public static boolean isYou(Player player) {
		return StackManager.PLAYERS[0] == player;
	}

	/**
	 * Indicates if the specified replacement ability has already been used to
	 * replace the main action. This function verify this ability is not in the
	 * stack between the top and the last non-replacement ability.
	 * 
	 * @param ability
	 *          the replacement ability to search
	 * @return true if the specified replacement ability has already been used to
	 *         replace the main action.
	 */
	public static boolean isPlaying(ReplacementAbility ability) {
		if (currentAbility == ability) {
			return true;
		}
		if (currentAbility instanceof ReplacementAbility) {
			// we have to search in context the last non-replacement ability
			return isPlaying(ability, CONTEXTES.size() - 1);
		}
		// the current ability is not a replacement one
		return false;

	}

	/**
	 * Indicates if the specified replacement ability has already been used to
	 * replace the main action. This function verify this ability is not in the
	 * stack between the top and the last non-replacement ability.
	 * 
	 * @param index
	 *          the index of context to visit
	 * @return the real card of the current ability. If index is bellow 0 (the
	 *         bottom of the stack) an error is thrown.
	 */
	private static boolean isPlaying(ReplacementAbility ability, int index) {
		if (index < 0) {
			throw new InternalError("Cannot find a real card into the stack");
		}
		if (CONTEXTES.get(index).ctxcurrentAbility == ability) {
			return true;
		}
		if (CONTEXTES.get(index).ctxcurrentAbility instanceof ReplacementAbility) {
			return isPlaying(ability, index - 1);
		}
		return false;
	}

	/**
	 * The ability corresponding to the specified card
	 * 
	 * @param card
	 *          the card linked to the searched ability
	 * @return the ability corresponding to the specified card
	 */
	public static Ability getAbilityOf(MCard card) {
		if (tokenCard == card) {
			return currentAbility;
		}

		// this is not the current ability, search in the stack contexts
		return getActionManager(card).currentAbility;
	}

	/**
	 * Return the action context corresponding to the specified card. If the given
	 * card is not found in the context, an exception is thrown.
	 * 
	 * @param card
	 *          the card linked to the searched ability
	 * @return the action context corresponding to the specified card
	 */
	public static ActionManager getActionManager(AbstractCard card) {
		return getContextOf(card).getActionManager();
	}

	public ActionManager getActionManager() {
		return actionManager;
	}

	/**
	 * Return the context associated to the given card. Return <code>null</code>
	 * if the given card the current spell instance. If the given card is not
	 * found in the context, an exception is thrown.
	 * 
	 * @param card
	 *          the card linked to the searched ability
	 * @return the context corresponding to the specified card.
	 */
	public static StackContext getContextOf(AbstractCard card) {
		if (tokenCard == card) {
			// this is the current spell. No real context assiated yet.
			return getInstance();
		}

		// this is not the current ability, search in the stack contexts
		Iterator<StackElement> it = CONTEXTES.iterator();
		while (it.hasNext()) {
			StackElement context = it.next();
			if (context.ctxtokenCard == card) {
				return context;
			}
		}
		throw new RuntimeException("The searched card '" + card
				+ "' within the stack has not been found.");
	}

	/**
	 * Return the real card of the current ability. In fact, this method return
	 * the card owning the last non-replacement ability.<br>
	 * TODO is the given card is the card owning the current ability, in this case
	 * the parameter is obsolete.
	 * 
	 * @param owner
	 *          the card owning the action requesting the real card.
	 * @return the real card of the current ability.
	 */
	public static MCard getRealSource(MCard owner) {
		if ((currentAbility instanceof ReplacementAbility || owner == SystemCard.instance
				&& triggered != null)
				&& triggered.getAbilityContext() != null
				&& triggered.getAbilityContext() instanceof MContextCardCardIntInt
				&& !((MContextCardCardIntInt) triggered.getAbilityContext()).isNull2()) {
			return ((MContextCardCardIntInt) triggered.getAbilityContext())
					.getCard2();
		}
		return owner;
	}

	/**
	 * Return the player controlling the current spell
	 * 
	 * @return the player controlling the current spell
	 */
	public static Player getSpellController() {
		if (spellController == null) {
			return activePlayer();
		}
		return spellController;
	}

	/**
	 * For each cards having posted a refresh request, process to the refreshing
	 * 
	 * @return true if there was one or several posted request.
	 */
	public static boolean processRefreshRequests() {
		boolean res = false;
		if (!REFRESH_TYPES.isEmpty()) {
			for (MCard card : REFRESH_TYPES) {
				card.refreshIdCard();
			}
			REFRESH_TYPES.clear();
			res = true;
		}
		if (!REFRESH_CONTROLLER.isEmpty()) {
			for (MCard card : REFRESH_CONTROLLER) {
				card.refreshController();
			}
			REFRESH_CONTROLLER.clear();
			res = true;
		}
		if (!REFRESH_COLORS.isEmpty()) {
			for (MCard card : REFRESH_COLORS) {
				card.refreshIdColor();
			}
			REFRESH_COLORS.clear();
			res = true;
		}
		if (!REFRESH_PROPERTIES.isEmpty()) {
			for (PairCardInt pair : REFRESH_PROPERTIES) {
				pair.card.refreshProperties(pair.value);
			}
			REFRESH_PROPERTIES.clear();
			res = true;
		}
		if (!REFRESH_REGISTERS.isEmpty()) {
			for (PairCardInt pair : REFRESH_REGISTERS) {
				pair.card.refreshRegisters(pair.value);
			}
			REFRESH_REGISTERS.clear();
			res = true;
		}
		if (!REFRESH_ABILITIES.isEmpty()) {
			for (MCard card : REFRESH_ABILITIES) {
				card.refreshAbilities();
			}
			REFRESH_ABILITIES.clear();
			res = true;
		}
		return res;
	}

	/**
	 * Add the "lose status" to specified player
	 * 
	 * @param idPlayer
	 *          the loosing player.
	 * @since 0.85
	 */
	public static void postLoseGame(int idPlayer) {
		lostGameStatus |= idPlayer + 1;
	}

	/**
	 * Post a request to refresh the card types of a card. A card can be refreshed
	 * only once for the card types.
	 * 
	 * @param card
	 *          the card to refresh.
	 */
	public static void postRefreshIdCard(MCard card) {
		REFRESH_TYPES.add(card);
	}

	/**
	 * Post a request to refresh the abilities of a card. A card can be refreshed
	 * only once for the abilities.
	 * 
	 * @param card
	 *          the card to refresh.
	 */
	public static void postRefreshAbilities(MCard card) {
		REFRESH_ABILITIES.add(card);
	}

	/**
	 * Post a request to refresh the colors of a card. A card can be refreshed
	 * only once for the colors.
	 * 
	 * @param card
	 *          the card to refresh.
	 */
	public static void postRefreshColor(MCard card) {
		REFRESH_COLORS.add(card);
	}

	/**
	 * Post a request to refresh a property of a card. A card can be refreshed
	 * only once for a same register.
	 * 
	 * @param card
	 *          the card to refresh.
	 * @param property
	 *          the property to refresh.
	 */
	public static void postRefreshProperties(MCard card, int property) {
		REFRESH_PROPERTIES.add(new PairCardInt(card, property));
	}

	/**
	 * Post a request to refresh a register of a card. A card can be refreshed
	 * only once for a same register.
	 * 
	 * @param card
	 *          the card to refresh.
	 * @param index
	 *          the register index to refresh.
	 */
	public static void postRefreshRegisters(MCard card, int index) {
		REFRESH_REGISTERS.add(new PairCardInt(card, index));
	}

	/**
	 * Post a request to refresh the controller of a card. A card can be refreshed
	 * only once for the abilities.
	 * 
	 * @param card
	 *          the card to refresh.
	 */
	public static void postRefreshController(MCard card) {
		REFRESH_CONTROLLER.add(card);
	}

	/**
	 * Return the target saved into the specified ability
	 * 
	 * @param ability
	 *          the ability containing the saved component
	 * @return the target saved into the specified ability
	 */
	public static Target getSaved(Ability ability) {
		return ((TriggeredAbility) ability).getDelayedCard().saved;
	}

	/**
	 * Read game status to know if the continues or not.
	 * 
	 * @return true if the game is not ended.
	 * @since 0.85
	 */
	static boolean processGameLost() {
		switch (lostGameStatus) {
		case 0:
			return false;
		case 1:
			// You lose
			JOptionPane.showMessageDialog(MagicUIComponents.magicForm,
					LanguageManager.getString("youlose"), "End of Game",
					JOptionPane.INFORMATION_MESSAGE);
			return true;
		case 2:
			// You win
			JOptionPane.showMessageDialog(MagicUIComponents.magicForm,
					LanguageManager.getString("youwin"), "End of Game",
					JOptionPane.INFORMATION_MESSAGE);
			return true;
		case 3:
			// It's a draw
			JOptionPane.showMessageDialog(MagicUIComponents.magicForm,
					LanguageManager.getString("draw"), "End of Game",
					JOptionPane.INFORMATION_MESSAGE);
			return true;
		default:
			throw new InternalError("Unknown lost game status : " + lostGameStatus);
		}
	}

	/**
	 * Indicates the current action is waiting for a target.
	 * 
	 * @return true if the current action is waiting for a target.
	 */
	public static boolean isTargetMode() {
		return StackManager.currentAbility != null
				&& StackManager.actionManager.currentAction != null
				&& StackManager.actionManager.currentAction instanceof ChosenTarget;
	}

	/**
	 * Return the current context. Null if current ability is not a triggered one.
	 * 
	 * @return the current context. Null if current ability is not a triggered
	 *         one.
	 */
	public ContextEventListener getAbilityContext() {
		if (triggered == null) {
			return null;
		}
		return triggered.getAbilityContext();
	}

	/**
	 * Correspond to the bits of player added to lose game
	 * 
	 * @since 0.85
	 */
	private static int lostGameStatus;

	/**
	 * the player controlling the current spell
	 */
	public static Player spellController;

	/**
	 * the target option associated to the current spell
	 */
	public static TargetManager targetOptions;

	public TargetedList getTargetedList() {
		return targetedList;
	}

	/**
	 * Set a new targeted list.
	 * 
	 * @param savedTargeted
	 *          the new targeted list.
	 */
	public void setTargetedList(TargetedList savedTargeted) {
		this.targetedList = savedTargeted;
	}

	/**
	 * The target option of the current spell. this target option is owned by the
	 * current spell. May be reseted, changed by the spell itself.
	 */
	private TargetedList targetedList;

	/**
	 * The current integer list. This list is private to each ability.
	 */
	public static IntegerList intList;

	/**
	 * private registers for each spell/ability
	 */
	public static int[] registers = new int[2];

	/**
	 * card representing card in stack
	 */
	public static AbstractCard tokenCard;

	/**
	 * Current spell is a triggered ability if not null
	 */
	public static TriggeredCard triggered;

	/**
	 * the current ability in the stack
	 */
	public static Ability currentAbility;

	/**
	 * Is the previous place of current spell. So means something only when user
	 * can abort, and only if the card representing this spell is not tokenized.
	 * If is -1, abort spell cause the current spell in stack to be removed (not
	 * moved to an existing zone)
	 */
	public static int previousPlace;

	/**
	 * Indicates if the current spell can be aborted or not
	 */
	public static boolean canBeAborted;

	/**
	 * Is the game finished?
	 */
	static boolean gameLostProceed;

	/**
	 * Ability ID
	 */
	public static long abilityID = 0;

	/**
	 * The current action manager.
	 */
	public static volatile ActionManager actionManager;

	/**
	 * Identifier of last player having hand (not always active player). This id
	 * is set just before <code>idHandedPlayer</code> is set to <code>-1</code>
	 */
	public static volatile int oldIdHandedPlayer = -1;

	/**
	 * Identifier of active player
	 */
	public static volatile int idActivePlayer = 0;

	/**
	 * Current player
	 */
	public static volatile int idCurrentPlayer = 0; // Player's turn

	/**
	 * Identifier of player having hand (not always active player)
	 */
	public static volatile int idHandedPlayer = -1;

	/**
	 * players of the play
	 */
	public static final Player[] PLAYERS = new Player[2];

	/**
	 * Indicates the current ability has been previously aborted
	 */
	static boolean aborted;

	/**
	 * The current cards set waiting for a refresh of their id card
	 * 
	 * @since 0.72
	 */
	private static final Set<MCard> REFRESH_TYPES = new HashSet<MCard>();

	/**
	 * The current cards set waiting for a refresh of their abilities
	 * 
	 * @since 0.86
	 */
	private static final Set<MCard> REFRESH_ABILITIES = new HashSet<MCard>();

	/**
	 * The current cards set waiting for a refresh of their controller
	 * 
	 * @since 0.72
	 */
	private static final Set<MCard> REFRESH_CONTROLLER = new HashSet<MCard>();

	/**
	 * The current cards set waiting for a refresh of their id color
	 * 
	 * @since 0.72
	 */
	private static final Set<MCard> REFRESH_COLORS = new HashSet<MCard>();

	/**
	 * The current cards set waiting for a refresh of their registers
	 * 
	 * @since 0.72
	 */
	private static final Set<PairCardInt> REFRESH_REGISTERS = new HashSet<PairCardInt>();

	/**
	 * The current cards set waiting for a refresh of their properties
	 * 
	 * @since 0.72
	 */
	private static final Set<PairCardInt> REFRESH_PROPERTIES = new HashSet<PairCardInt>();

	/**
	 * The object used to exclusively manage player events.
	 */
	public static MMiniPipe noReplayToken = new MMiniPipe(false);

	/**
	 * represents the place where aborted spells would be placed
	 */
	public static int zoneAbortion;

	/**
	 * contains all contexts
	 */
	public static final Stack<StackElement> CONTEXTES = new Stack<StackElement>();

	/**
	 * Represents the target list saved during this turn. This list is
	 * automatically reseted at the beginning of a turn, and before any triggered
	 * or activated abilities can be played.
	 */
	public static final List<List<Target>> SAVED_TARGET_LISTS = new ArrayList<List<Target>>();

	/**
	 * All saved integer list.
	 */
	public static final List<IntegerList> SAVED_INT_LISTS = new ArrayList<IntegerList>();

	/**
	 * Private class mContext corresponding to the whole context of the actual
	 * play
	 * 
	 * @author Fabrice Daugan
	 * @since 0.11 registers, target options, target list, triggered card, spell,
	 *        'can be aborted' token, 'already paid' token, action index within
	 *        the current ability and the previous place of card
	 * @since 0.24 save and restore choice list of opponent too
	 * @since 0.6 choice list are no more saved
	 */
	static class StackElement implements StackContext {

		/**
		 * Save the whole context of the play
		 */
		protected StackElement() {
			/* save variables */
			ctxtargetOptions = StackManager.targetOptions;
			ctxtargetedList = StackManager.getInstance().getTargetedList();
			ctxintList = StackManager.intList;
			ctxregisters = StackManager.registers;
			ctxcanBeAborted = StackManager.canBeAborted;
			ctxtriggered = StackManager.triggered;
			ctxpreviousPlace = StackManager.previousPlace;
			ctxtokenCard = StackManager.tokenCard;
			ctxactionManager = StackManager.actionManager;
			ctxcurrentAbility = StackManager.currentAbility;
			ctxidActivePlayer = StackManager.idActivePlayer;
			ctxspellController = spellController;
			ctxabilityID = abilityID;
		}

		public TargetedList getTargetedList() {
			return ctxtargetedList;
		}

		public ContextEventListener getAbilityContext() {
			if (ctxtriggered == null) {
				return null;
			}
			return ctxtriggered.getAbilityContext();
		}

		public ActionManager getActionManager() {
			return ctxactionManager;
		}

		public MCard getSourceCard() {
			if (ctxtriggered != null) {
				/*
				 * the real card source of current ability is the card having generated
				 * this triggered
				 */
				return ctxtriggered.triggeredAbility.getCard();
			}
			// else this an ability or a spell directly placed into the stack
			return (MCard) ctxtokenCard;
		}

		/**
		 * Restore the whole context of the play
		 */
		protected void restore() {
			final ResolveStackHandler handler = currentAbility;
			MagicUIComponents.chosenCostPanel.clean();

			// restore player history
			StackManager.targetOptions = ctxtargetOptions;
			StackManager.getInstance().getTargetedList().clear();
			StackManager.getInstance().setTargetedList(ctxtargetedList);
			StackManager.registers = ctxregisters;
			StackManager.triggered = ctxtriggered;
			StackManager.canBeAborted = ctxcanBeAborted;
			StackManager.previousPlace = ctxpreviousPlace;
			StackManager.tokenCard = ctxtokenCard;
			StackManager.aborted = ctxaborted;
			StackManager.actionManager.clean(ctxactionManager);
			StackManager.actionManager = ctxactionManager;
			StackManager.intList.clear();
			StackManager.intList = ctxintList;
			abilityID = ctxabilityID;

			// restore old active player and also the current spell controller

			StackManager.currentAbility = ctxcurrentAbility;
			StackManager.idActivePlayer = ctxidActivePlayer;
			StackManager.spellController = ctxspellController;

			ctxspellController = null;
			ctxcurrentAbility = null;
			ctxtokenCard = null;
			ctxtriggered = null;
			ctxtargetedList = null;
			ctxintList = null;
			ctxregisters = null;
			ctxtargetOptions = null;
			ctxactionManager = null;

			// continue the current ability where we where
			handler.resolveStack();
		}

		public Ability getAbortingAbility() {
			return abortingAbility;
		}

		public void abortion(AbstractCard card, Ability source) {
			abortingAbility = source;
		}

		private Ability abortingAbility;

		private IntegerList ctxintList;

		private int ctxidActivePlayer;

		private TargetManager ctxtargetOptions;

		/**
		 * List of targets added by the current ability.
		 */
		protected TargetedList ctxtargetedList;

		private int[] ctxregisters;

		private TriggeredCard ctxtriggered;

		boolean ctxcanBeAborted;

		private int ctxpreviousPlace;

		/**
		 * Previous ability's representation
		 */
		protected AbstractCard ctxtokenCard;

		/**
		 * Previous ability.
		 */
		protected Ability ctxcurrentAbility;

		/**
		 * Previous information about abortion state.
		 */
		protected boolean ctxaborted;

		ActionManager ctxactionManager;

		private Player ctxspellController;

		long ctxabilityID;

	}

	/**
	 * Decline tooltip text
	 */
	public static final String TT_DECLINE = LanguageManager
			.getString("menu_game_skip.tooltip");

	/**
	 * The cancel tooltip text
	 */
	public static final String TT_CANCEL = LanguageManager
			.getString("menu_game_cancel.tooltip");

	/**
	 * The cancel text
	 */
	public static final String CANCEL_TXT = LanguageManager
			.getString("menu_game_cancel");

	/**
	 * The decline text
	 */
	public static final String DECLINE_TXT = LanguageManager
			.getString("menu_game_skip");

	/**
	 * icon of skip button
	 */
	private static final Icon CANCEL_ICON = UIHelper
			.getIcon("menu_game_cancel.gif");

	/**
	 * icon of next button
	 */
	private static final Icon DECLINE_ICON = UIHelper
			.getIcon("menu_game_skip.gif");

	/**
	 * Unique instance of this class.
	 */
	private static StackManager instance = new StackManager();

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class.
	 */
	public static StackManager getInstance() {
		return instance;
	}

	/**
	 * Return HTML representation of total mana paid for the specified card.
	 * 
	 * @param card
	 *          the card associated to a spell in the stack
	 * @return HTML representation of total mana paid for the specified card.
	 */
	public static String getHtmlManaPaid(MCard card) {
		final ActionContextWrapper[] context = StackManager.getActionManager(card)
				.getTotalActionContexts();
		final int[] res = new int[IdCommonToken.PAYABLE_COLOR_NAMES.length];
		if (context != null) {
			for (ActionContextWrapper contextI : context) {
				if (contextI != null && contextI.actionContext != null
						&& contextI.actionContext instanceof ManaCost) {
					final int[] manaPaid = ((ManaCost) contextI.actionContext).manaPaid;
					for (int j = IdCommonToken.COLOR_NAMES.length; j-- > 0;) {
						res[j] += manaPaid[j];
					}
				}
			}
		}
		return PayMana.toHtmlString(res);
	}

	/**
	 * Return HTML representation of total mana cost of the specified card.
	 * 
	 * @param card
	 *          the card associated to a spell in the stack
	 * @return HTML representation of total mana cost of the specified card.
	 */
	public static String getHtmlManaCost(MCard card) {
		final ActionContextWrapper[] context = StackManager.getActionManager(card)
				.getTotalActionContexts();
		final int[] res = new int[IdCommonToken.PAYABLE_COLOR_NAMES.length];
		if (context != null) {
			for (ActionContextWrapper contextI : context) {
				if (contextI != null && contextI.actionContext != null
						&& contextI.actionContext instanceof ManaCost) {
					final int[] manaCost = ((ManaCost) contextI.actionContext).manaCost;
					for (int j = IdCommonToken.PAYABLE_COLOR_NAMES.length; j-- > 0;) {
						res[j] += manaCost[j];
					}
				}
			}
		}
		return PayMana.toHtmlString(res);
	}

	/**
	 * Return total mana cost of the specified card.
	 * 
	 * @param card
	 *          the card associated to a spell in the stack
	 * @return total mana cost of the specified card.
	 */
	public static int getTotalManaCost(AbstractCard card) {
		final ActionContextWrapper[] context = StackManager.getActionManager(card)
				.getTotalActionContexts();
		if (context == null) {
			return 0;
		}
		int res = 0;
		for (ActionContextWrapper contextI : context) {
			if (contextI != null && contextI.actionContext != null
					&& contextI.actionContext instanceof ManaCost) {
				int[] manaCost = ((ManaCost) contextI.actionContext).manaCost;
				for (int j = IdCommonToken.PAYABLE_COLOR_NAMES.length; j-- > 0;) {
					res += manaCost[j];
				}
			}
		}
		return res;
	}

	/**
	 * Return total mana paid for the specified card.
	 * 
	 * @param card
	 *          the card associated to a spell in the stack
	 * @return total mana paid for the specified card.
	 */
	public static int getTotalManaPaid(MCard card) {
		final ActionContextWrapper[] context = StackManager.getActionManager(card)
				.getTotalActionContexts();
		if (context == null) {
			return 0;
		}
		int res = 0;
		for (ActionContextWrapper contextI : context) {
			if (contextI != null && contextI.actionContext != null
					&& contextI.actionContext instanceof ManaCost) {
				int[] manaPaid = ((ManaCost) contextI.actionContext).manaPaid;
				for (int j = IdCommonToken.COLOR_NAMES.length; j-- > 0;) {
					res += manaPaid[j];
				}
			}
		}
		return res;
	}

	/**
	 * Return mana paid for the specified card and specified color.
	 * 
	 * @param card
	 *          the card associated to a spell in the stack
	 * @param color
	 *          the color of mana paid.
	 * @return mana paid for the specified card and specified color.
	 */
	public static int getManaPaid(MCard card, int color) {
		final ActionContextWrapper[] context = StackManager.getActionManager(card)
				.getTotalActionContexts();
		if (context == null) {
			return 0;
		}
		int res = 0;
		for (ActionContextWrapper contextI : context) {
			if (contextI != null && contextI.actionContext != null
					&& contextI.actionContext instanceof ManaCost) {
				res += ((ManaCost) contextI.actionContext).manaPaid[color];
			}
		}
		return res;
	}

	public Ability getAbortingAbility() {
		return abortingAbility;
	}

	private Ability abortingAbility;

	private List<AdditionalCost> additionalCosts;
}