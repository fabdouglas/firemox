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
package net.sf.firemox.action.target;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.TargetList;
import net.sf.firemox.action.handler.ChosenAction;
import net.sf.firemox.action.listener.WaitingCard;
import net.sf.firemox.action.listener.WaitingPlayer;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.event.context.MContextTarget;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.stack.ActionManager;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.stack.TargetHelper;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdTargets;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;

/**
 * Add to the target list card(s) or player(s) following the specified mode and
 * the specified type. If mode is 'choose' or 'opponent-choose', then a
 * 'targeted' event is raised when the choice is made. In case of 'all',
 * 'random', 'myself' or any target known with access register, no event is
 * generated. The friendly test, and the type are necessary only for the modes
 * 'random', 'all', 'choose' and 'opponent-choose' to list the valid targets.
 * The target list is used by the most actions. <br>
 * For example, if the next action 'damage', all cards/player from the target
 * list would be damaged. When a new ability is added to the stack, a new list
 * is created and attached to it. Actions may modify, access it until the
 * ability is completely resolved.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan</a>
 * @since 0.54
 * @since 0.70 added modes : 'current-player', 'non-current-player',
 *        'context.card', 'context.player'
 * @since 0.71 abortion (when no valid target are found) is supported. To
 *        support abortion, hop attribute must be set to 'all' or 'abort-me'
 * @since 0.82 manage "targeted" event generation for target action
 * @since 0.82 "share" options with 'same-name' option is supported. This option
 *        would add the target into the target list of the first ability of
 *        stack having the save name.
 * @since 0.82 restriction zone supported to optimize the target processing.
 * @since 0.86 new algorithm to determine valid targets
 * @since 0.86 a target cannot be added twice in the target-list
 */
public abstract class ChosenTarget extends net.sf.firemox.action.Target
		implements WaitingPlayer, WaitingCard, ChosenAction {

	/**
	 * Create an instance by reading a file Offset's file must pointing on the
	 * first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>hop for abort [Expression]</li>
	 * <li>test [Test]</li>
	 * <li>restriction Zone id [int]</li>
	 * <li>is preempted [boolean]</li>
	 * </ul>
	 * For the available options, see interface IdTargets
	 * 
	 * @param type
	 *          the concerned target's type previously read by the factory
	 * @param options
	 *          the options previously read by the factory
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 * @see IdTargets
	 * @since 0.82 options contains "share" option too
	 */
	ChosenTarget(int type, int options, InputStream inputFile) throws IOException {
		super(inputFile);
		this.type = type;
		this.options = options;
		hop = ExpressionFactory.readNextExpression(inputFile).getValue(null, null,
				null);
		test = TestFactory.readNextTest(inputFile);
		restrictionZone = inputFile.read() - 1;
		canBePreempted = inputFile.read() == 1;
	}

	@Override
	public final void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		if (actionContext.actionContext != null) {
			((TargetList) actionContext.actionContext).rollback();
		}
	}

	/**
	 * No generated event. Let the active player playing this action.
	 * 
	 * @param actionContext
	 *          the context containing data saved by this action during the
	 *          'choose" process.
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          is the context attached to this action.
	 * @return true if the stack can be resolved just after this call.
	 */
	public boolean init(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final int choiceMode = options & 0x0F;
		final MCard owner = ability.getCard();
		final List<Target> validTargets = new ArrayList<Target>();
		String targetTxt = null;
		switch (type) {
		case IdTokens.DEALTABLE:
			// Target is a player or card
			targetTxt = "Target is creature or player";
			if (test
					.test(ability, StackManager.PLAYERS[StackManager.idCurrentPlayer])) {
				validTargets.add(StackManager.PLAYERS[StackManager.idCurrentPlayer]);
			}
			if (test.test(ability,
					StackManager.PLAYERS[1 - StackManager.idCurrentPlayer])) {
				validTargets
						.add(StackManager.PLAYERS[1 - StackManager.idCurrentPlayer]);
			}

			processCardSearch(validTargets, ability, context);
			break;
		case IdTokens.PLAYER:
			// Target is a player
			targetTxt = "Target is player";
			if (test
					.test(ability, StackManager.PLAYERS[StackManager.idCurrentPlayer])) {
				validTargets.add(StackManager.PLAYERS[StackManager.idCurrentPlayer]);
			}
			if (test.test(ability,
					StackManager.PLAYERS[1 - StackManager.idCurrentPlayer])) {
				validTargets
						.add(StackManager.PLAYERS[1 - StackManager.idCurrentPlayer]);
			}
			break;
		case IdTokens.CARD:
			// Target is a card
			targetTxt = "Target is card";
			processCardSearch(validTargets, ability, context);
			break;
		default:
			throw new InternalError("Unknown type :" + type);
		}
		if (StackManager.getInstance().getTargetedList().list != null) {
			validTargets.removeAll(StackManager.getInstance().getTargetedList().list);
		}
		Log.debug("## size=" + validTargets.size() + ", ignored="
				+ StackManager.getInstance().getTargetedList().list);
		switch (choiceMode) {
		case IdTargets.ALL:
			Log.debug("all mode, size=" + validTargets.size());
			MagicUIComponents.magicForm.moreInfoLbl.setText(targetTxt
					+ " - all mode)");
			if (!validTargets.isEmpty()) {
				if (actionContext == null) {
					StackManager.getInstance().getTargetedList().list
							.addAll(validTargets);
				} else if (actionContext.actionContext == null) {
					actionContext.actionContext = new TargetList();
					((TargetList) actionContext.actionContext).targetList = validTargets;
				} else {
					((TargetList) actionContext.actionContext).targetList
							.addAll(validTargets);
				}
				// continue the stack process
				return true;
			}
			break;
		case IdTargets.RANDOM:
			if (!validTargets.isEmpty()) {
				// random target, at least on target
				MagicUIComponents.magicForm.moreInfoLbl.setText(targetTxt
						+ " - is random)");
				final Target target = validTargets.get(MToolKit.getRandom(validTargets
						.size()));
				StackManager.getInstance().getTargetedList().list.add(target);
				if (StackManager.actionManager.advancedMode) {
					TargetList targetList = (TargetList) StackManager.actionManager
							.getActionContext().actionContext;
					if (targetList == null) {
						targetList = new TargetList();
					}
					targetList.add(target, test);
					StackManager.actionManager.getActionContext().actionContext = targetList;
				}
				return true;
			}
			break;
		default:
			// unique target to choose
		}

		if (validTargets.isEmpty()) {
			// no valid target --> this spell abort
			// if (restrictionZone!=-1)

			if (hop == IdConst.ALL) {
				// abortion is not managed by this spell
				MagicUIComponents.magicForm.moreInfoLbl.setText(targetTxt
						+ "  -> no valid target, abortion");
				Log.debug(MagicUIComponents.magicForm.moreInfoLbl.getText());
				StackManager.getInstance().abortion(StackManager.tokenCard,
						StackManager.currentAbility);
				return false;
			}
			MagicUIComponents.magicForm.moreInfoLbl.setText(targetTxt
					+ "  -> no valid target, hop is " + hop);
			Log.debug(MagicUIComponents.magicForm.moreInfoLbl.getText());
			StackManager.actionManager.setHop(hop);
			return true;
		}

		// At least one valid target --> determines the controller
		String message = null;
		Player nextHandedPlayer = null;
		switch (choiceMode & 0x07) {
		case IdTargets.CHOOSE:
			// target will be chosen by the active player
			message = "youchoose";
			nextHandedPlayer = owner.getController();
			break;
		case IdTargets.OPPONENT_CHOOSE:
			// target will be chosen by the non-active player
			message = "opponentchoose";
			nextHandedPlayer = owner.getController().getOpponent();
			break;
		case IdTargets.CONTEXT_CHOOSE:
			// target will be chosen by the player of current context's event
			message = "contextchoose";
			nextHandedPlayer = ((MContextTarget) context).getTargetable().isPlayer() ? ((MContextTarget) context)
					.getPlayer()
					: ((MContextTarget) context).getCard().getController();
			break;
		case IdTargets.TARGET_CHOOSE:
			// target will be chosen by the player of current context's event
			message = "target-choose";
			nextHandedPlayer = (Player) StackManager.getInstance().getTargetedList()
					.get(0);
			break;
		case IdTargets.ATTACHED_TO_CONTROLLER_CHOOSE:
			// target will be chosen by the player of current context's event
			message = "attachedto.controller-choose";
			nextHandedPlayer = ((MCard) owner.getParent()).getController();
			break;
		case IdTargets.STACK0_CHOOSE:
			/*
			 * target will be chosen by the player identified by the id stored in
			 * index 0 of register of current ability.
			 */
			message = "-stack0-choose";
			nextHandedPlayer = StackManager.PLAYERS[StackManager.registers[0]];
			break;
		default:
			throw new InternalError("Unknown choice type :" + choiceMode);
		}
		MagicUIComponents.magicForm.moreInfoLbl.setText(targetTxt + " - mode='"
				+ message + "'");

		StackManager.targetOptions.manageTargets(validTargets);

		// This player has to choose a target
		nextHandedPlayer.setHandedPlayer();
		return false;
	}

	private void processCardSearch(List<Target> validTargets, Ability ability,
			ContextEventListener context) {
		Player controllerOptimize = test.getOptimizedController(ability, context);
		if (restrictionZone != -1) {
			if (controllerOptimize != null) {
				controllerOptimize.zoneManager.getContainer(restrictionZone)
						.checkAllCardsOf(test, validTargets, ability);
			} else {
				StackManager.PLAYERS[StackManager.idCurrentPlayer].zoneManager
						.getContainer(restrictionZone).checkAllCardsOf(test, validTargets,
								ability);
				StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].zoneManager
						.getContainer(restrictionZone).checkAllCardsOf(test, validTargets,
								ability);
			}
		} else {
			if (controllerOptimize != null) {
				controllerOptimize.zoneManager.checkAllCardsOf(test, validTargets,
						ability);
			} else {
				StackManager.PLAYERS[StackManager.idCurrentPlayer].zoneManager
						.checkAllCardsOf(test, validTargets, ability);
				StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].zoneManager
						.checkAllCardsOf(test, validTargets, ability);
			}
		}
	}

	public void disactivate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		StackManager.targetOptions.clearTarget();
	}

	@Override
	public final boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		if (actionContext != null && actionContext.actionContext != null) {
			final TargetList targetList = (TargetList) actionContext.actionContext;
			targetList
					.replay(
							options & 0x30,
							StackManager.actionManager.idHandler == ActionManager.HANDLER_AD_REPLAY);
		}
		return true;
	}

	public boolean choose(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		return init(actionContext, context, ability);
	}

	@Override
	public final boolean play(ContextEventListener context, Ability ability) {
		return init(null, context, ability);
	}

	public final boolean clickOn(MCard target) {
		if (isValidTarget(target)) {
			return true;
		}
		MagicUIComponents.magicForm.moreInfoLbl.setText("Card:" + target
				+ " is not a valid target");
		return false;
	}

	public final boolean clickOn(Player target) {
		if (isValidTarget(target)) {
			return true;
		}
		MagicUIComponents.magicForm.moreInfoLbl.setText("Player:" + target
				+ " is not a valid target");
		return false;
	}

	public final boolean succeedClickOn(Player player) {
		MagicUIComponents.magicForm.moreInfoLbl.setText("Player:" + player
				+ " added as target");
		MagicUIComponents.logListing.append(0, "Player:" + player
				+ " added as target");
		Log.debug("Player:" + player + " added as target");
		return succeedClickOn((Target) player);
	}

	public final boolean succeedClickOn(MCard card) {
		MagicUIComponents.magicForm.moreInfoLbl.setText("Card:" + card
				+ " added as target");
		MagicUIComponents.logListing.append(0, "Card:" + card + " added as target");
		Log.debug("Card:" + card + " added as target");
		return succeedClickOn((Target) card);
	}

	/**
	 * @param target
	 * @return true if this action is completed
	 */
	protected boolean succeedClickOn(Target target) {
		finished();
		if (StackManager.actionManager.advancedMode) {
			TargetList targetList = (TargetList) StackManager.actionManager
					.getActionContext().actionContext;
			if (targetList == null) {
				targetList = new TargetList();
				StackManager.actionManager.getActionContext().actionContext = targetList;
			}
			targetList.add(target, test);
		} else {
			StackManager.getInstance().getTargetedList().addTarget(options & 0x30,
					target, test, false);
		}
		return true;
	}

	public final void finished() {
		// we exit from the target mode
		StackManager.targetOptions.clearTarget();
	}

	/**
	 * Is the specified card is a valid target?
	 * 
	 * @param target
	 *          the tested card.
	 * @return true if the given card is a valid target
	 * @since 0.86 a target cannot be added twice in the target-list
	 */
	public final boolean isValidTarget(MCard target) {
		return (type == IdTokens.CARD
				&& (restrictionZone == -1 || target.getIdZone() == restrictionZone) || type == IdTokens.DEALTABLE)
				&& !StackManager.getTargetListAccess().contains(target)
				&& test.test(StackManager.currentAbility, target);
	}

	/**
	 * Is the specified player is a valid target?
	 * 
	 * @param target
	 *          the tested player.
	 * @return true if the given card is a valid target
	 */
	public final boolean isValidTarget(Player target) {
		return (type == IdTokens.PLAYER || type == IdTokens.DEALTABLE)
				&& test.test(StackManager.currentAbility, target);
	}

	private boolean canCancel() {
		if ((options & IdTargets.ALLOW_CANCEL) != 0) {
			if (StackManager.actionManager.advancedMode) {
				return StackManager.actionManager.getActionContext().repeat != IdConst.ALL
						|| StackManager.actionManager.getActionContext().done > 0;
			}
			return true;
		}
		return false;
	}

	public final boolean manualSkip() {
		// Is this target is required?
		if (canCancel()) {
			StackManager.targetOptions.clearTarget();
			StackManager.actionManager.setHop(hop);
			if (StackManager.actionManager.advancedMode) {
				// complete this action even if we'd do more
				StackManager.actionManager.getActionContext().recordIndex = 1;
			}
			return true;
		}

		// No, the target is requested, cancel ability if we're in advanced mode
		if (StackManager.actionManager.advancedMode) {
			StackManager.cancel();
			return false;
		}

		// Re-launch "wait again player's choice for target"
		return init(
				StackManager.actionManager.advancedMode ? StackManager.actionManager
						.getActionContext() : null, StackManager.getInstance()
						.getAbilityContext(), StackManager.currentAbility);
	}

	@Override
	public String toString(Ability ability) {
		String res = null;
		switch (type) {
		case IdTokens.CARD:
			if (options == IdTargets.ALL) {
				return "[all cards]";
			}
			if (options == IdTargets.RANDOM) {
				return "[a random card]";
			}
			res = "[a card ";
			break;
		case IdTokens.PLAYER:
			if (options == IdTargets.ALL) {
				return "[all players]";
			}
			if (options == IdTargets.RANDOM) {
				return "[a random player]";
			}
			res = "[a player ";
			break;
		case IdTokens.DEALTABLE:
			if (options == IdTargets.ALL) {
				return "[all cards and players]";
			}
			res = "[a dealtable ";
			break;
		default:
			res = "[? ";
		}
		switch (options & 0x07) {
		case IdTargets.CHOOSE:
			return res + "you choose]";
		case IdTargets.OPPONENT_CHOOSE:
			return res + "opponent chooses]";
		case IdTargets.CONTEXT_CHOOSE:
			return res + "context player chooses]";
		case IdTargets.TARGET_CHOOSE:
			return res + "player chooses]";
		case IdTargets.ATTACHED_TO_CONTROLLER_CHOOSE:
			return res + "controller of attached-to chooses]";
		case IdTargets.STACK0_CHOOSE:
			return res + "-stack0- chooses]";
		default:
			return res + "]";
		}
	}

	public String toHtmlString(Ability ability, ContextEventListener context,
			ActionContextWrapper actionContext) {
		return super.toHtmlString(ability, context);
	}

	/**
	 * Is this action is really targeting.
	 * 
	 * @return true if this action is really targeting.
	 */
	public abstract boolean isTargeting();

	/**
	 * Verify if this target action may cause abortion of the current ability.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param hopCounter
	 *          the minimum valid target to have. If is negative or zero return
	 *          true.
	 * @return true if this target action may cause abortion of the current
	 *         ability.
	 * @since 0.86 new algorythme to determine valid targets
	 */
	public final boolean checkTarget(Ability ability, int hopCounter) {
		if (hopCounter <= 0) {
			return true;
		}
		try {
			return TargetHelper.getInstance().checkTarget(ability, ability.getCard(),
					test, type, options & 0x0F, null, restrictionZone, hopCounter,
					canBePreempted);
		} catch (Throwable t) {
			// Ignore this error since target can depend on some contexts
			return true;
		}
	}

	/**
	 * Represents the options of this target action
	 */
	protected int options;

	/**
	 * represents the test applied to target before to be added to the list
	 */
	protected Test test;

	/**
	 * represents the type of target (player, card, dealtable)
	 * 
	 * @see IdTargets
	 */
	protected int type;

	/**
	 * Jump to do in case of abortion. Note if this value is equal to 0, this
	 * target process cannot been aborted since the next action would the current
	 * one.
	 */
	protected int hop;

	/**
	 * The zone identifant where the scan is restricted. If is equal to -1, there
	 * would be no restriction zone.
	 * 
	 * @see net.sf.firemox.token.IdZones
	 */
	protected int restrictionZone;

	/**
	 * <code>true</code> if the valid targets can be derterminated before
	 * runtime.
	 */
	private boolean canBePreempted;
}