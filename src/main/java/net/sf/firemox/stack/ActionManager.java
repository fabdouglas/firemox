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
 */
package net.sf.firemox.stack;

import net.sf.firemox.action.BackgroundMessaging;
import net.sf.firemox.action.Hop;
import net.sf.firemox.action.LoopAction;
import net.sf.firemox.action.MAction;
import net.sf.firemox.action.PayMana;
import net.sf.firemox.action.Repeat;
import net.sf.firemox.action.WaitTriggeredBufferChoice;
import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.Int;
import net.sf.firemox.action.context.ManaCost;
import net.sf.firemox.action.handler.ChosenAction;
import net.sf.firemox.action.handler.FollowAction;
import net.sf.firemox.action.handler.InitAction;
import net.sf.firemox.action.handler.Replayable;
import net.sf.firemox.action.handler.RollBackAction;
import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.action.listener.Waiting;
import net.sf.firemox.action.listener.WaitingAbility;
import net.sf.firemox.action.listener.WaitingAction;
import net.sf.firemox.action.listener.WaitingCard;
import net.sf.firemox.action.listener.WaitingMana;
import net.sf.firemox.action.listener.WaitingPlayer;
import net.sf.firemox.action.listener.WaitingTriggeredCard;
import net.sf.firemox.action.target.RealTarget;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.action.JChosenAction;
import net.sf.firemox.clickable.mana.Mana;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.TriggeredCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.Casting;
import net.sf.firemox.event.MovedCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.token.IdCardColors;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.MagicUIComponents;

/**
 * The most important class of this application, and also the hardest to
 * understand. This manager schedules the actions handlers and the ability stack
 * process.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class ActionManager {

	/**
	 * Create a new instance from the StackManager. Additional cost are inserted
	 * at the beginning of cost part.
	 */
	ActionManager(Ability currentAbility, MAction[] additionalCost) {
		this.currentAbility = currentAbility;
		this.advancedEffectMode = false;
		this.requiredMana = new int[IdCardColors.CARD_COLOR_NAMES.length];
		this.overMana = new int[IdCardColors.CARD_COLOR_NAMES.length];
		if (currentAbility == null) {
			this.nbCosts = 0;
			this.effectList = null;
			this.actionList = null;
		} else {
			if (additionalCost != null) {
				this.actionList = new MAction[currentAbility.actionList().length
						+ additionalCost.length];
				System.arraycopy(additionalCost, 0, this.actionList, 0,
						additionalCost.length);
				System.arraycopy(currentAbility.actionList(), 0, this.actionList,
						additionalCost.length, currentAbility.actionList().length);
			} else {
				this.actionList = currentAbility.actionList();
			}
			this.idHandler = HANDLER_INITIALIZATION;
			this.nbCosts = this.actionList.length;
			this.effectList = currentAbility.effectList();
		}
		restart(currentAbility);
	}

	void restart(Ability currentAbility) {
		hop = 1;
		loopingIndex = 0;
		currentIdAction = -1;
		internalCounter = 0;
		actionsContextsWrapper = null;
		advancedMode = nbCosts > 0 && !currentAbility.isHidden()
				&& !currentAbility.isSystemAbility();
		restoreTargetList();
		if (nbCosts > 0) {
			this.idHandler = HANDLER_INITIALIZATION;
		} else {
			this.idHandler = HANDLER_MIDDLE;
		}
	}

	/**
	 * Clean up this object, and initialize the previous interrupted ActionManager
	 * instance. The hop value of this one is reseted to <code>1</code>
	 * 
	 * @param previousInterrupted
	 *          the previous interrupted action manager.
	 */
	void clean(ActionManager previousInterrupted) {
		actionsContextsWrapper = null;
		validatedActionsContextsWrapper = null;
		currentAbility = null;
		currentAction = null;
		// previousInterrupted.internalCounter = 0;
		previousInterrupted.hop = 1;
	}

	/**
	 * Rollback a specified action index.
	 * 
	 * @param actionIndex
	 *          the action index to rollback.
	 */
	private void rollbackAction(int actionIndex) {
		final MAction action = actionList()[actionIndex];
		final ContextEventListener context = StackManager.getInstance()
				.getAbilityContext();
		if (action instanceof RollBackAction) {
			((RollBackAction) action).rollback(actionsContextsWrapper[actionIndex],
					context, currentAbility);
		} else if (action instanceof Replayable) {
			((Replayable) action).replay(actionsContextsWrapper[actionIndex],
					context, currentAbility);
			// else
			// TODO Is this check is necessary?
			// throw new InternalError("Unsupported action (id=" + actionIndex
			// + ") for rollback purpose : " + action.getClass());
		}
	}

	/**
	 * Rollback all actions starting from the last executed action.
	 */
	void rollback() {
		if (currentIdAction >= nbCosts) {
			currentIdAction = nbCosts - 1;
		}
		for (int actionIndex = currentIdAction + 1; actionIndex-- > 0;) {
			if (rollbackPath[actionIndex]) {
				rollbackAction(actionIndex);
			}
		}
	}

	/**
	 * Pause stack resolution : wait a mana source ability or a ChosenAction
	 * activation
	 */
	void waitActionChoice() {
		completeChosenAction(MagicUIComponents.chosenCostPanel
				.playFirstUncompleted());
	}

	/**
	 * Action/Effect list.
	 * 
	 * @return Action/Effect list.
	 */
	public MAction[] actionList() {
		if (advancedEffectMode) {
			return effectList;
		}
		return actionList;
	}

	/**
	 * Restore the target list as it was at the beginning of the current bloc
	 * (cost/effects)
	 */
	private void restoreTargetList() {
		if (!advancedEffectMode
				&& StackManager.getInstance().getTargetedList() != null) {
			StackManager.getInstance().getTargetedList().clear();
		} else {
			StackManager.getInstance().setTargetedList(savedTargeted);
		}
	}

	/**
	 * Return the current ability's context.
	 * 
	 * @return the current ability's context.
	 */
	public ContextEventListener getAbilityContext() {
		return StackManager.getInstance().getAbilityContext();
	}

	boolean playNextAction() {
		// The middle effect is not yet finished, return to the previous entrance...
		if (waitingOnMiddle) {
			StackManager.activePlayer().waitTriggeredBufferChoice(true);
			return false;
		}

		// Looping action first
		if (loopingIndex > 0) {
			loopingIndex--;
			return playLoopingAction();
		}

		// Set up the next action
		goNextAction();

		// manage handler
		if (!advancedMode && idHandler == HANDLER_INITIALIZATION) {
			if (currentIdAction >= nbCosts) {
				idHandler = HANDLER_MIDDLE;
				currentIdAction = 0;
			} else {
				currentAction = actionList[currentIdAction];
				return playCurrentAction();
			}
		} else if (advancedMode && idHandler == HANDLER_INITIALIZATION) {

			// play remaining InitAction
			while (currentIdAction < nbCosts) {
				currentAction = actionList()[currentIdAction];
				if (currentAction instanceof InitAction) {
					if (!(((InitAction) currentAction).init(getActionContext(),
							getAbilityContext(), currentAbility))) {
						return false;
					}
				} else if (currentAction instanceof ChosenAction) {
					// we initialize the context of this ChosenAction
					getActionContext();
				}
				goNextAction();
			}

			// All InitAction have been initialized, [re]play all actions
			currentIdAction = 0;
			idHandler = HANDLER_AD_SERIALIZATION;

			// restore target list as it was at the beginning of cost/effects part
			restoreTargetList();

			// execute all InitAction until the first ChosenAction
			while (currentIdAction < nbCosts) {
				currentAction = actionList()[currentIdAction];
				rollbackPath[currentIdAction] = true;
				// do not replay "repeat" action in the serialization handler
				if (!(currentAction instanceof Repeat)) {
					if (currentAction instanceof Hop) {
						((Hop) currentAction).replay(getActionContext(), StackManager
								.getInstance().getAbilityContext(), currentAbility);
						goNextAction();
					} else if (currentAction instanceof ChosenAction) {
						// activate the ChosenAction panel
						return MagicUIComponents.chosenCostPanel.reset(StackManager
								.getInstance().getSourceCard(), actionsContextsWrapper);
					} else if (currentAction instanceof InitAction) {
						((InitAction) currentAction).replay(getActionContext(),
								getAbilityContext(), currentAbility);
					} else if (currentAction instanceof FollowAction) {
						((FollowAction) currentAction).simulate(getActionContext(),
								getAbilityContext(), currentAbility);
					} else {
						throw new InternalError("Unsupported action (id=" + currentIdAction
								+ ") for serialization purpose : " + currentAction.getClass());
					}
				}
				goNextAction();
			}
			// here and no ChosenAction have been found
			idHandler = HANDLER_AD_PREPARE_REPLAY;
		} else if (advancedMode && idHandler == HANDLER_AD_SERIALIZATION) {
			// first, play all actions until the next ChosenAction
			if (!(currentAction instanceof ChosenAction)) {
				throw new InternalError(
						"The serialization handler must start to process a ChosenAction,class="
								+ currentAction.getClass().getName() + " : " + currentAction);
			}

			if (currentIdAction < nbCosts) {
				// First pass : play all actions until the next ChosenAction)
				while (currentIdAction < nbCosts) {
					currentAction = actionList()[currentIdAction];
					rollbackPath[currentIdAction] = true;
					// do not replay initialize action in serialization handler
					if (!(currentAction instanceof Repeat)) {
						if (currentAction instanceof Hop) {
							((Hop) currentAction).replay(getActionContext(), StackManager
									.getInstance().getAbilityContext(), currentAbility);
						} else if (currentAction instanceof FollowAction) {
							((FollowAction) currentAction).simulate(getActionContext(),
									getAbilityContext(), currentAbility);
						} else if (currentAction instanceof RealTarget) {
							// is always considered as completed
							if (!getActionContext().isCompleted()) {
								throw new InternalError(
										"RealTarget should be completed in Serialization");
							}
							getActionContext().done++;
							((InitAction) currentAction).replay(getActionContext(),
									getAbilityContext(), currentAbility);
						} else if (currentAction instanceof ChosenAction) {
							if (!getActionContext().isCompleted()) {
								// all follow actions have been played, wait player's choice
								waitActionChoice();
								return false;
							}
						} else if (currentAction instanceof InitAction) {
							((InitAction) currentAction).replay(getActionContext(),
									getAbilityContext(), currentAbility);
						}
					}
					goNextAction();
				}
			}
			// here and no ChosenAction have been found --> next handler
			idHandler = HANDLER_AD_PREPARE_REPLAY;
		}

		if (advancedMode && idHandler == HANDLER_AD_PREPARE_REPLAY) {
			// rollback all actions
			currentIdAction = nbCosts;
			rollback();
			idHandler = HANDLER_AD_REPLAY;
			currentIdAction = 0;
		}

		if (advancedMode && idHandler == HANDLER_PLAY_INIT) {
			// replay all action
			while (currentIdAction < nbCosts) {
				currentAction = actionList()[currentIdAction];
				if (currentAction instanceof ChosenAction) {
					// replay initAction
					if (!((ChosenAction) currentAction).choose(getActionContext(),
							getAbilityContext(), currentAbility)) {
						// wait player answer
						return false;
					}
				} else if (currentAction instanceof InitAction) {
					// replay initAction
					((InitAction) currentAction).replay(getActionContext(), StackManager
							.getInstance().getAbilityContext(), currentAbility);
				} else {
					// normal action
					if (!playCurrentAction()) {
						// stack resolution is broken
						return false;
					}
				}
				goNextAction();
			}
			// here and no ChosenAction have been found
			idHandler = HANDLER_MIDDLE;
		}

		if (advancedMode && idHandler == HANDLER_AD_REPLAY) {
			while (currentIdAction < nbCosts) {
				currentAction = actionList()[currentIdAction];
				if (currentAction instanceof Repeat) {
					if (currentIdAction + 1 < actionList().length
							&& !(actionList()[currentIdAction + 1] instanceof InitAction)
							&& !(actionList()[currentIdAction + 1] instanceof ChosenAction)) {
						((Repeat) currentAction).replayOnDemand(getActionContext(),
								getAbilityContext(), currentAbility);
					}
				} else {
					if (currentAction instanceof InitAction) {
						// replay the initAction
						if (!((InitAction) currentAction).replay(getActionContext(),
								getAbilityContext(), currentAbility)) {
							return false;
						}
					} else if (currentAction instanceof ChosenAction) {
						// replay the ChosenAction
						((ChosenAction) currentAction).replay(getActionContext(),
								getAbilityContext(), currentAbility);
					} else {
						// Play the normal action
						if (!playCurrentAction()) {
							return false;
						}
						// Looping action first
						while (loopingIndex-- > 0) {
							if (!playLoopingAction()) {
								// TODO Looping action broken in the HANDLER_AD_REPLAY step,
								// unwanted behavior may happen (not yet implemented
								Log
										.warn("Looping action broken in the HANDLER_AD_REPLAY step, unwanted behavior may happen (not yet implemented");
								return false;
							}
						}
					}
				}
				goNextAction();
			}
			idHandler = HANDLER_MIDDLE;
		}

		if (idHandler == HANDLER_MIDDLE) {
			if (advancedEffectMode) {
				idHandler = HANDLER_EFFECTS;
			} else {
				if (currentIdAction > nbCosts) {
					/*
					 * TODO Abort ability is supported, but we should not be here
					 */
					Log
							.debug("WARNING : action id is greater than the amount of actions in cost part of "
									+ currentAbility);
				}
				// Is the game is finished?
				if (StackManager.processGameLost()) {
					// OK, stop stack resolution now
					StackManager.gameLostProceed = true;
					return false;
				}
				rollbackPath = null;
				// save the contexts of cost part.
				validatedActionsContextsWrapper = actionsContextsWrapper;
				actionsContextsWrapper = null;
				advancedMode = false;

				// Is the resolution is automatically performed with this ability?
				if (StackManager.triggered != null || currentAbility.isAutoResolve()) {
					/*
					 * OK, so no player gets priority, nevertheless all hidden triggered
					 * abilities are stacked and played.
					 */
					if (!currentAbility.isHidden()) {
						StackManager.actionManager.currentAction = WaitTriggeredBufferChoice
								.getInstance();
						if (!StackManager.activePlayer().processHiddenTriggered()
								&& !StackManager.nonActivePlayer().processHiddenTriggered()) {
							if (StackManager.tokenCard instanceof MCard) {
								final MCard currentCard = (MCard) StackManager.tokenCard;
								if (StackManager.triggered == null
										&& currentCard.getIdZone() == IdZones.STACK) {
									/*
									 * Simulate the move PreviousZone --> Stack since now the cost
									 * has completely been paid.
									 */
									if (!currentAbility.isAutoResolve()) {
										currentCard.setIdZone(StackManager.previousPlace);
										MovedCard.dispatchEvent(currentCard, IdZones.STACK,
												currentCard.getController(), false);
										currentCard.setIdZone(IdZones.STACK);
									}
								}
								Casting.dispatchEvent(currentCard);
							}
						} else {
							return false;
						}
					}
					prepareEffects();
					currentIdAction = 0;
				} else {
					/*
					 * No, so we generate the casting event and let the player getting
					 * priority. Since cast part is finished and since active player would
					 * get priority, we stack the triggered abilities we set the active
					 * action as the WaitTriggeredBufferChoice one, and the active player
					 * gets priority (if no triggered have to be stacked)
					 */
					prepareEffects();
					currentIdAction = -1;
					final MCard currentCard = (MCard) StackManager.tokenCard;
					if (StackManager.triggered == null
							&& currentCard.getIdZone() == IdZones.STACK) {
						/*
						 * Simulate the move PreviousZone --> Stack since now the cost has
						 * completely been paid.
						 */
						currentCard.setIdZone(StackManager.previousPlace);
						MovedCard.dispatchEvent(currentCard, IdZones.STACK, currentCard
								.getController(), false);
						currentCard.setIdZone(IdZones.STACK);
					}
					Casting.dispatchEvent(currentCard);
					waitingOnMiddle = true;
					/* Some hidden abilities have be added to the stack and played */
					StackManager.activePlayer().waitTriggeredBufferChoice(true);
					waitingOnMiddle = false;
					return false;
				}
			}
		}

		if (idHandler == HANDLER_EFFECTS) {

			// this part corresponds to effect of this ability
			if (currentIdAction == effectList.length) {
				// ability is finished
				StackManager.getInstance().finishSpell();
			} else {
				// ability is not finished
				if (advancedEffectMode) {
					throw new IllegalStateException(
							"In HANDLER_EFFECTS & advancedEffectMode with uncompleted ability");
				}
				if (currentIdAction == 0) {
					// re-check targets before proceeding to the effects
					if (!currentAbility.recheckTargets()) {
						// since all targets are invalid, this ability will be aborted
						StackManager.getInstance().abortion(StackManager.tokenCard,
								currentAbility);
						return false;
					}

					// play remaining InitAction
					if (!currentAbility.isSystemAbility()) {
						while (currentIdAction < effectList.length) {
							if (effectList[currentIdAction] instanceof PayMana
									|| effectList[currentIdAction] instanceof RealTarget) {
								// at least one ChosenAction in effect part
								this.advancedEffectMode = true;
								this.advancedMode = true;
								this.savedTargeted = StackManager.getInstance()
										.getTargetedList().cloneList();
								this.nbCosts = effectList.length;
								restart(currentAbility);
								playNextAction();
								return false;
							}
							currentIdAction++;
						}
						currentIdAction = 0;
					}
				}
				currentAction = effectList[currentIdAction];
				return playCurrentAction();
			}
		}
		return false;
	}

	private void prepareEffects() {
		idHandler = HANDLER_EFFECTS;
	}

	private boolean playLoopingAction() {
		if (StackManager.triggered != null) {
			return ((LoopAction) currentAction).continueLoop(StackManager
					.getInstance().getAbilityContext(), loopingIndex, currentAbility);
		}
		return ((LoopAction) currentAction).continueLoop(null, loopingIndex,
				currentAbility);
	}

	/**
	 * Play the current action
	 * 
	 * @return true if the stack resolution can continue. False if the stack
	 *         resolution is broken.
	 */
	private boolean playCurrentAction() {
		if (currentAction instanceof LoopAction) {
			if (currentAction instanceof BackgroundMessaging) {
				// This special return value indicates a break in stack resolution
				final int loopingIndexTMP = ((LoopAction) currentAction)
						.getStartIndex();
				if (loopingIndexTMP == Integer.MAX_VALUE) {
					return false;
				}
				loopingIndex = loopingIndexTMP;
			} else {
				loopingIndex = ((LoopAction) currentAction).getStartIndex();
			}
			if (loopingIndex > -1) {
				return playLoopingAction();
			}
			return true;
		}
		// Proceed to the handler management
		return ((StandardAction) currentAction).play(StackManager.getInstance()
				.getAbilityContext(), currentAbility);
	}

	/**
	 * Send the message "manualSkip" to the active action of play. If the
	 * "skip/cancel" event is accepted by this action, we resolve the stack.
	 */
	public void manualSkip() {
		if (currentAction instanceof Waiting
				&& ((Waiting) currentAction).manualSkip()) {
			StackManager.resolveStack();
		}
	}

	/**
	 * Will repeat the next action <code>nbNextAction</code> times
	 * 
	 * @param nbNextAction
	 *          is the times that the next action will be repeated
	 */
	public void setRepeat(int nbNextAction) {
		internalCounter = nbNextAction;
		currentIdAction += 1;
		hop = 1;
	}

	/**
	 * The current action will become the current action index + "hop". The "hop"
	 * value is reseted to 1
	 */
	private void goNextAction() {
		if (internalCounter > 0) {
			// repeat this action -> do not go to the next action
			internalCounter--;
		} else {
			currentIdAction += hop;
		}
		hop = 1;
	}

	/**
	 * Returns the context associated to the specified action id.
	 * 
	 * @return the context associated to the specified action id.
	 */
	public ActionContextWrapper getActionContextNull() {
		if (actionsContextsWrapper == null) {
			return null;
		}
		return actionsContextsWrapper[currentIdAction];
	}

	/**
	 * Returns the context associated to the specified action id.
	 * 
	 * @param contextId
	 *          the requestion acion id.
	 * @return the context associated to the specified action id.
	 */
	public ActionContextWrapper getActionContext(int contextId) {
		if (actionsContextsWrapper == null) {
			// first access to action contexts
			actionsContextsWrapper = new ActionContextWrapper[actionList().length];

			// create action path for rolback purpose
			rollbackPath = new boolean[nbCosts];
		}
		if (actionsContextsWrapper[contextId] == null) {
			// first access to this context
			if (contextId > 0 && actionsContextsWrapper[contextId - 1] != null
					&& actionsContextsWrapper[contextId - 1].action instanceof Repeat) {
				actionsContextsWrapper[contextId] = new ActionContextWrapper(contextId,
						actionList()[contextId], null,
						((Int) actionsContextsWrapper[contextId - 1].actionContext)
								.getInt());
			} else {
				actionsContextsWrapper[contextId] = new ActionContextWrapper(contextId,
						actionList()[contextId], null, 1);
			}
		}
		return actionsContextsWrapper[contextId];
	}

	/**
	 * Returns the context associated to the current action id.
	 * 
	 * @return the context associated to the current action id.
	 */
	public ActionContextWrapper getActionContext() {
		return getActionContext(currentIdAction);
	}

	/**
	 * Returns the whole action contexts of current step : cost OR effect.
	 * 
	 * @return the whole action contexts of current step : cost OR effect.
	 * @see #getAllActionContexts()
	 */
	public ActionContextWrapper[] getAllActionContexts() {
		return actionsContextsWrapper;
	}

	/**
	 * Returns the whole action contexts : cost AND effect.
	 * 
	 * @return the whole action contexts : cost AND effect.
	 * @see #getAllActionContexts()
	 */
	public ActionContextWrapper[] getTotalActionContexts() {
		if (validatedActionsContextsWrapper != null) {
			if (actionsContextsWrapper != null) {
				final ActionContextWrapper[] total = new ActionContextWrapper[validatedActionsContextsWrapper.length
						+ actionsContextsWrapper.length];
				System.arraycopy(validatedActionsContextsWrapper, 0, total, 0,
						validatedActionsContextsWrapper.length);
				System.arraycopy(actionsContextsWrapper, 0, total,
						validatedActionsContextsWrapper.length,
						actionsContextsWrapper.length);
			} else {
				return validatedActionsContextsWrapper;
			}
		} else if (actionsContextsWrapper != null) {
			return actionsContextsWrapper;
		}
		return new ActionContextWrapper[0];
	}

	/**
	 * Set the jump to do for the next action. If the specified "hop" is 0, the
	 * next action would be the current one.
	 * 
	 * @param hop
	 *          the jump to do for the next action.
	 */
	public void setHop(int hop) {
		if (hop == IdConst.ALL) {
			currentIdAction = effectList.length - 1;
			this.hop = 1;
			prepareEffects();
		} else if (idHandler == HANDLER_EFFECTS || advancedEffectMode) {
			this.hop = hop;
		} else if (hop + currentIdAction >= actionList().length) {
			this.hop = hop + 1;
			prepareEffects();
		} else {
			this.hop = hop;
		}
		internalCounter = 0;
		loopingIndex = -1;
	}

	/**
	 * Called to specify the player choice for the current action
	 * 
	 * @param card
	 *          the clicked card by the active player for the current action
	 * @return true if this click has been managed. Return false if this click has
	 *         been ignored
	 */
	public boolean clickOn(MCard card) {
		if (currentAction instanceof WaitingCard) {
			return ((WaitingCard) currentAction).clickOn(card);
		}
		return false;
	}

	/**
	 * Called to specify the player choice for the current action
	 * 
	 * @param player
	 *          the clicked player by the active player for the current action
	 * @return true if this click has been managed. Return false if this click has
	 *         been ignored
	 */
	public boolean clickOn(Player player) {
		if (currentAction instanceof WaitingPlayer) {
			return ((WaitingPlayer) currentAction).clickOn(player);
		}
		return false;
	}

	/**
	 * Called to specify the player choice for the current action
	 * 
	 * @param triggeredCard
	 *          the clicked triggered card by the active player for the current
	 *          action
	 * @return true if this click has been managed. Return false if this click has
	 *         been ignored
	 */
	public boolean clickOn(TriggeredCard triggeredCard) {
		if (currentAction instanceof WaitingTriggeredCard) {
			return ((WaitingTriggeredCard) currentAction).clickOn(triggeredCard);
		}
		return false;
	}

	/**
	 * Called to specify the player choice for the current action
	 * 
	 * @param ability
	 *          the clicked ability by the active player for the current action
	 * @return true if this click has been managed. Return false if this click has
	 *         been ignored
	 */
	public boolean clickOn(Ability ability) {
		if (currentAction instanceof WaitingAbility) {
			return ((WaitingAbility) currentAction).clickOn(ability);
		}
		return false;
	}

	/**
	 * Called to specify the player choice for the current action
	 * 
	 * @param mana
	 *          the clicked mana by the active player for the current action
	 * @return true if this click has been managed. Return false if this click has
	 *         been ignored
	 */
	public boolean clickOn(Mana mana) {
		if (currentAction instanceof WaitingMana) {
			return ((WaitingMana) currentAction).clickOn(mana);
		}
		return false;
	}

	/**
	 * Called to specify the player choice for the current action.
	 * 
	 * @param action
	 *          the clicked action by the active player for the current action
	 * @return true if this click has been managed. Return false if this click has
	 *         been ignored
	 */
	public boolean clickOn(JChosenAction action) {
		if (currentAction instanceof WaitingAction) {
			return ((WaitingAction) currentAction).clickOn(action);
		}
		return false;
	}

	/**
	 * This function should be called by the 'clickOn' caller in case of the
	 * specified card has been handled during the checking validity of this click
	 * in the <code>clickOn(Card)</code> function. <br>
	 * <ul>
	 * The calls chain is :
	 * <li>actionListener call clickOn(Card)
	 * <li>if returned value is false we give hand to the player and exit, else
	 * we continue
	 * <li>actionListener call succeedClickOn(Card)
	 * </ul>
	 * 
	 * @param card
	 *          the card that was clicked and successfully handled by the
	 *          <code>clickOn(Card)</code> function.
	 * @see #clickOn(MCard)
	 */
	public void succeedClickOn(MCard card) {
		Player.unsetHandedPlayer();
		final boolean res = ((WaitingCard) currentAction).succeedClickOn(card);
		StackManager.actionManager.completeChosenAction(res);
	}

	/**
	 * This function should be called by the 'clickOn' caller in case of the
	 * specified ability has been handled during the checking validity of this
	 * click in the <code>clickOn(Ability)</code> function. <br>
	 * <ul>
	 * The calls chain is :
	 * <li>actionListener call clickOn(Ability)
	 * <li>if returned value is false we give hand to the player and exit, else
	 * we continue
	 * <li>actionListener call succeedClickOn(Ability)
	 * </ul>
	 * 
	 * @param ability
	 *          the ability that was clicked and successfully handled by the
	 *          <code>clickOn(Ability)</code> function.
	 * @see #clickOn(Ability)
	 */
	public void succeedClickOn(Ability ability) {
		Player.unsetHandedPlayer();
		if (((WaitingAbility) currentAction).succeedClickOn(ability)) {
			StackManager.resolveStack();
		}
	}

	/**
	 * This function should be called by the 'clickOn' caller in case of the
	 * specified triggered card has been handled during the checking validity of
	 * this click in the <code>clickOn(MTriggeredCard)</code> function. <br>
	 * <ul>
	 * The calls chain is :
	 * <li>actionListener call clickOn(MTriggeredCard)
	 * <li>if returned value is false we give hand to the player and exit, else
	 * we continue
	 * <li>actionListener call succeedClickOn(MTriggeredCard)
	 * </ul>
	 * 
	 * @param card
	 *          the triggered card that was clicked and successfully handled by
	 *          the <code>clickOn(MTriggeredCard)</code> function.
	 * @see #clickOn(TriggeredCard)
	 */
	public void succeedClickOn(TriggeredCard card) {
		Player.unsetHandedPlayer();
		final boolean res = ((WaitingTriggeredCard) currentAction)
				.succeedClickOn(card);
		StackManager.actionManager.completeChosenAction(res);
	}

	/**
	 * This function should be called by the 'clickOn' caller in case of the
	 * specified mana has been handled during the checking validity of this click
	 * in the <code>clickOn(MMana)</code> function. <br>
	 * <ul>
	 * The calls chain is :
	 * <li>actionListener call clickOn(MMana)
	 * <li>if returned value is false we give hand to the player and exit, else
	 * we continue
	 * <li>actionListener call succeedClickOn(MMana)
	 * </ul>
	 * 
	 * @param mana
	 *          the mana that was clicked and successfully handled by the
	 *          <code>clickOn(MMana)</code> function.
	 * @see #clickOn(Mana)
	 */
	public void succeedClickOn(Mana mana) {
		Player.unsetHandedPlayer();
		final boolean res = ((WaitingMana) currentAction).succeedClickOn(mana);
		StackManager.actionManager.completeChosenAction(res);
	}

	/**
	 * This function should be called by the 'clickOn' caller in case of the
	 * specified player has been handled during the checking validity of this
	 * click in the <code>clickOn(Player)</code> function. <br>
	 * <ul>
	 * The calls chain is :
	 * <li>actionListener call clickOn(Player)
	 * <li>if returned value is false we give hand to the player and exit, else
	 * we continue
	 * <li>actionListener call succeedClickOn(Player)
	 * </ul>
	 * 
	 * @param player
	 *          the player that was clicked and successfully handled by the
	 *          <code>clickOn(Player)</code> function.
	 * @see #clickOn(Player)
	 */
	public void succeedClickOn(Player player) {
		Player.unsetHandedPlayer();
		final boolean res = ((WaitingPlayer) currentAction).succeedClickOn(player);
		StackManager.actionManager.completeChosenAction(res);
	}

	/**
	 * This function should be called by the 'clickOn' caller in case of the
	 * specified player has been handled during the checking validity of this
	 * click in the <code>clickOn(JChosenAction)</code> function. <br>
	 * <ul>
	 * The calls chain is :
	 * <li>actionListener call clickOn(JChosenAction)
	 * <li>if returned value is false we give hand to the player and exit, else
	 * we continue
	 * <li>actionListener call succeedClickOn(JChosenAction)
	 * </ul>
	 * 
	 * @param action
	 *          the action that was clicked and successfully handled by the
	 *          <code>clickOn(JChosenAction)</code> function.
	 * @see #clickOn(JChosenAction)
	 */
	public void succeedClickOn(JChosenAction action) {
		Player.unsetHandedPlayer();
		final boolean res = ((WaitingAction) currentAction).succeedClickOn(action);
		StackManager.actionManager.completeChosenAction(res);
	}

	/**
	 * Complete the current action.
	 * 
	 * @param unitaryCompleted
	 *          is this action is completed as many times as required?
	 */
	public void completeChosenAction(boolean unitaryCompleted) {
		if (advancedMode) {
			if (unitaryCompleted) {
				// this action is unitary completed
				if (MagicUIComponents.chosenCostPanel.completeAction(
						StackManager.currentAbility, StackManager.getInstance()
								.getAbilityContext(), getActionContext())) {
					internalCounter = 0;
					// ok, process now all FollowAction associated to this action
					StackManager.resolveStack();
				} else {
					// this action need to be repeated
					if (currentAction instanceof InitAction) {
						if (((InitAction) currentAction).init(getActionContext(),
								getAbilityContext(), StackManager.currentAbility)) {
							StackManager.resolveStack();
						}
					} else if (((ChosenAction) currentAction).choose(getActionContext(),
							getAbilityContext(), StackManager.currentAbility)) {
						StackManager.resolveStack();
					}
				}
			} else {
				// this action is not unitary completed
				// StackManager.getSpellController().setHandedPlayer();
				StackManager.enableAbort();
			}
		} else if (unitaryCompleted) {
			StackManager.resolveStack();
		}
	}

	/**
	 * Reactivate the current action
	 */
	public void reactivate() {
		if (!(currentAction instanceof Waiting)
				|| !(currentAction instanceof ChosenAction)) {
			Log.error(
					"The serialization handler must start to process a Waiting/ChosenAction,class="
							+ currentAction.getClass().getName() + " : " + currentAction,
					new InternalError());
			// So, we should not be there, resolve the stack
			playNextAction();
		} else {
			((ChosenAction) currentAction).choose(getActionContext(), StackManager
					.getInstance().getAbilityContext(), currentAbility);
			MagicUIComponents.chosenCostPanel.initUI(StackManager.getInstance()
					.getSourceCard(), actionsContextsWrapper);
		}
	}

	/**
	 * Update the required mana of current ability.
	 * 
	 * @param op
	 *          the operation to apply to the required mana.
	 * @param reg
	 *          the register index to update
	 * @param value
	 *          the value to update.
	 */
	public void updateRequiredMana(Operation op, int reg, int value) {
		requiredMana[reg] = op.process(requiredMana[reg], value);
	}

	/**
	 * Update the given required mana with the global required mana of attached
	 * ability. This a complex process since in case of negative amounts, the
	 * previous mana contexts are updated to honor as much as possible an
	 * equilibrum.
	 * 
	 * @param requiredMana
	 *          the required mana to update.
	 */
	public void updateRequiredMana(int[] requiredMana) {
		for (int i = this.requiredMana.length; i-- > 0;) {
			this.requiredMana[i] += requiredMana[i];
			if (requiredMana[i] < 0) {
				if (this.overMana[i] > 0) {
					/*
					 * This color is already reduced beyond 0, so we have not found any
					 * way to fix the previous contexts.
					 */
					this.overMana[i] -= requiredMana[i];
				} else {
					/*
					 * This color has not been reduced yet, this is the interesting part
					 * of the process of this operation.
					 */
					for (ActionContextWrapper context : getTotalActionContexts()) {
						if (context != null && context.actionContext != null
								&& context.actionContext instanceof ManaCost) {
							final ManaCost manaContext = (ManaCost) context.actionContext;
							this.overMana[i] = manaContext
									.reduceManaCost(i, this.overMana[i]);
							if (this.overMana[i] == 0) {
								break;
							}
							// Else, try the next context
						}
					}
				}
				requiredMana[i] = 0;
			} else if (this.overMana[i] > 0) {
				if (requiredMana[i] >= this.overMana[i]) {
					requiredMana[i] -= this.overMana[i];
					this.overMana[i] = 0;
				} else {
					this.overMana[i] -= requiredMana[i];
					requiredMana[i] = 0;
				}
			}
		}
	}

	/**
	 * Return required mana. May contain some negative amount.
	 * 
	 * @return required mana. May contain some negative amount.
	 */
	public int[] getRequiredMana() {
		return requiredMana;
	}

	/**
	 * like M68k processor bra instruction, indicates the jump to do to go to the
	 * next action. This jump is equal to 1 for normal ability, but for hop=3, the
	 * 2 actions following the current one will be skipped. Hop=0 means infinite
	 * loop.
	 * 
	 * @since 0.52
	 */
	public int hop;

	/**
	 * the current ability in the stack
	 */
	public Ability currentAbility;

	/**
	 * the active action managing the next player action
	 */
	public MAction currentAction;

	/**
	 * This tag indicate the index of current action using a 'for' or 'while'
	 * instruction, generating several events. While this tag is greater than 0,
	 * instead of resolving stacks, the current action still called as if the
	 * setRepeat() method was called.
	 */
	public int loopingIndex;

	/**
	 * The index of current action
	 */
	public int currentIdAction;

	/**
	 * The current context of actions.
	 */
	private ActionContextWrapper[] actionsContextsWrapper = null;

	/**
	 * The validated context of actions. Is <code>null</code> while no contexts
	 * have been totally validated for a cost part, so arriving to the
	 * HANDLER_MIDDLE handler.
	 */
	private ActionContextWrapper[] validatedActionsContextsWrapper = null;

	/**
	 * Are we waiting for triggered/activated choice
	 */
	public boolean waitingOnMiddle;

	/**
	 * counter used for draw/targets count spells like a loop "for(i = 0 ... )"
	 */
	private int internalCounter = 0;

	private TargetedList savedTargeted;

	/**
	 * Values are :<br>
	 * HANDLER_INITIALIZATION<br>
	 * HANDLER_AD_SERIALIZATION<br>
	 * HANDLER_AD_PREPARE_REPLAY<br>
	 * HANDLER_AD_REPLAY<br>
	 * HANDLER_PLAY_INIT<br>
	 * HANDLER_MIDDLE<br>
	 * HANDLER_EFFECTS<br>
	 */
	public int idHandler = 0;

	/**
	 * The first handler : initialization.
	 */
	public static final int HANDLER_INITIALIZATION = 0;

	/**
	 * The second handler : serialization.
	 */
	private static final int HANDLER_AD_SERIALIZATION = 1;

	/**
	 * The third handler : prepare replay.
	 */
	private static final int HANDLER_AD_PREPARE_REPLAY = 2;

	/**
	 * The final handler : replay.
	 */
	public static final int HANDLER_AD_REPLAY = 3;

	/**
	 * The old init handler without rollback use.
	 */
	private static final int HANDLER_PLAY_INIT = 4;

	/**
	 * The old middle handler without rollback use.
	 */
	private static final int HANDLER_MIDDLE = 5;

	/**
	 * The old effects handler without rollback use.
	 */
	private static final int HANDLER_EFFECTS = 6;

	/**
	 * Is the advanced mode is used there for cost part.
	 */
	public boolean advancedMode;

	private int nbCosts;

	private boolean[] rollbackPath;

	/**
	 * The cost part of current ability.
	 */
	private final MAction[] actionList;

	/**
	 * The effect part of current ability.
	 */
	private final MAction[] effectList;

	/**
	 * Is the advanced mode is used there for effects part.
	 */
	public boolean advancedEffectMode;

	/**
	 * Required mana of attached ability. May contain some negative amount.
	 */
	private final int[] requiredMana;

	/**
	 * Negative amount of mana that have not been removed from other 'pay-mana'
	 * actions.
	 */
	private final int[] overMana;
}
