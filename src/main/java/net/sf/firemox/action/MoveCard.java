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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.MoveContext;
import net.sf.firemox.action.handler.FollowAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.Detached;
import net.sf.firemox.event.MovedCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.IdPositions;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;
import net.sf.firemox.ui.wizard.Arrange;
import net.sf.firemox.ui.wizard.Wizard;
import net.sf.firemox.zone.MZone;
import net.sf.firemox.zone.ZoneManager;

/**
 * To move the current target list from their place to another. New position
 * within the new zone, and the new controller have to be specified. <br>
 * 
 * @version 0.91
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @author <a href="mailto:kismet-sl@users.sourceforge.net">Stefano "Kismet"
 *         Lenzi</a>
 * @since 0.54
 * @since 0.80 activated abilities of card are registered before the 'moved
 *        card' is generated
 * @since 0.80 support replacement
 * @since 0.82 card is moved into the destination zone before the event is
 *        generated. During the event dispatching there is an incoherence.
 * @since 0.82 timestamp is checked
 * @since 0.82 if there are several cards to move, controller chooses order
 * @since 0.86 action ignore non-card element present in the target list
 */
public class MoveCard extends UserAction implements LoopAction, FollowAction,
		BackgroundMessaging, AccessibleContext {

	/**
	 * Create an instance of MoveCardList by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>new controller [TestOn]</li>
	 * <li>destination zone [IdZone]</li>
	 * <li>idPosition [int16]</li>
	 * <li>silent [boolean]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	MoveCard(InputStream inputFile) throws IOException {
		super(inputFile);
		controller = TestOn.deserialize(inputFile);
		destination = inputFile.read();
		idPosition = MToolKit.readInt16(inputFile);
		silent = inputFile.read() == 1;
	}

	public final void replayAction(ContextEventListener context, Ability ability,
			Wizard wizard) {
		wizard.setVisible(true);
		if (Wizard.optionAnswer != Wizard.BACKGROUND_OPTION) {
			// valid answer
			boolean taken = false;
			if (!StackManager.noReplayToken.takeNoBlock()) {
				taken = true;
			}

			final List<MCard> toBeSortedCardsCurrent = new ArrayList<MCard>();
			final List<MCard> toBeSortedCardsNonCurrent = new ArrayList<MCard>();
			fillList(toBeSortedCardsCurrent, toBeSortedCardsNonCurrent);
			final int[] order = ((Arrange) wizard).order;
			final byte[] toSend = new byte[order.length];
			if (((Arrange) wizard).owner == StackManager.currentPlayer()) {
				// current player (you) has arranged these cards

				for (int i = order.length; i-- > 0;) {
					toSend[i] = (byte) order[i];
					StackManager.getTargetListAccess().remove(
							toBeSortedCardsCurrent.get(order[i]));
					StackManager.getTargetListAccess().add(
							toBeSortedCardsCurrent.get(order[i]));
				}
				// we send our choice
				Log.debug("Order sent : " + Arrays.toString(toSend));
				ConnectionManager.send(CoreMessageType.MOVE_ORDER_ANSWER, toSend);

				// then, check the cards owned by non-current player
				if (toBeSortedCardsNonCurrent.size() > 1) {
					// now we wait the non current player choice
					Log.debug("Opponent is arranging moving cards he/she owns");
					StackManager.currentPlayer().getOpponent().setHandedPlayer();
					// stop here since the recursive call manages the non-current case
				} else {
					// all is done : current and non-current player have made their choice
					StackManager.actionManager.loopingIndex = StackManager
							.getTargetListAccess().size();

					// free the locking-token
					if (taken) {
						StackManager.noReplayToken.release();
					}

					StackManager.resolveStack();
				}
			} else {
				// non current player (you) has arranged these cards

				for (int i = order.length; i-- > 0;) {
					toSend[i] = (byte) order[i];
					StackManager.getTargetListAccess().remove(
							toBeSortedCardsNonCurrent.get(order[i]));
					StackManager.getTargetListAccess().add(
							toBeSortedCardsNonCurrent.get(order[i]));
				}

				// we send our choice
				Log.debug("Order sent : " + Arrays.toString(toSend));
				ConnectionManager.send(CoreMessageType.MOVE_ORDER_ANSWER, toSend);

				// all is done : current and non-current player have made their choice
				StackManager.actionManager.loopingIndex = StackManager
						.getTargetListAccess().size();

				// free the locking-token
				if (taken) {
					StackManager.noReplayToken.release();
				}

				StackManager.resolveStack();
			}
		}
	}

	/**
	 * return the id of this action. This action has been read from the MDB file.
	 * 
	 * @see Actiontype
	 * @return the id of this action
	 */
	@Override
	public final Actiontype getIdAction() {
		return Actiontype.MOVE_CARD;
	}

	/**
	 * Move a card in a zone with a specified new controller.
	 * 
	 * @param card
	 *          the card to move.
	 * @param controller
	 *          the new controller.
	 * @param destination
	 *          the new zone.
	 * @param context
	 *          the context of current ability.
	 * @param idPosition
	 *          the new state of this card.
	 * @param ability
	 *          the current ability.
	 * @param silentMode
	 *          Is the silent mode is enabled while playing.
	 * @return true if the card has been moved.
	 */
	public static boolean moveCard(MCard card, TestOn controller,
			int destination, ContextEventListener context, int idPosition,
			Ability ability, boolean silentMode) {

		// check timestamp
		if (!checkTimeStamp(context, card)) {
			// we ignore this action
			return true;
		}

		return moveCard(card, (Player) controller.getTargetable(ability, card,
				context, null), destination, context, idPosition, ability, silentMode);
	}

	/**
	 * @param movingCard
	 *          the card to move.
	 * @param controller
	 *          the new controller.
	 * @param destination
	 *          the new zone.
	 * @param context
	 *          the context of current ability.
	 * @param idPosition
	 *          the new state of this card.
	 * @param ability
	 *          the current ability.
	 * @param silentMode
	 *          Is the silent mode is enabled for this move.
	 * @return true if the card has been moved.
	 */
	public static boolean moveCard(MCard movingCard, Player controller,
			int destination, ContextEventListener context, int idPosition,
			Ability ability, boolean silentMode) {
		final MCard card = (MCard) movingCard.getOriginalTargetable();
		final int idDestination = MCard.getIdZone(destination, context);
		// add new abilities before the card is moved
		card.registerReplacementAbilities(idDestination);
		if (!MovedCard.tryAction(card, idDestination, controller, silentMode)) {
			// this action has been replaced
			return false;
		}

		if (idDestination == IdZones.PLAY) {
			/*
			 * the card comes into play : the card is moved before the 'moved' event
			 * is generated.
			 */
			final int previousIdZone = card.getIdZone();
			card.moveCard(idDestination, controller,
					(destination & IdZones.PLAY_TAPPED) == IdZones.PLAY_TAPPED,
					idPosition);

			// add the modifiers of this card activated only when card is in play
			if (card.getModifierModels() != null && previousIdZone != IdZones.PLAY) {
				card.getModifierModels().addModifierFromModel(ability, card);
			}

			// add new abilities
			card.registerAbilities(IdZones.PLAY);

			// but we do not update the idZone now.
			card.setIdZone(previousIdZone);

			/*
			 * FROM THIS POINT THERE IS AN INCOHERENCE IN MCard#idZone since the card
			 * is in a zone Y but MCard#idZone value is X, assuming the card is moving
			 * from X to Y
			 */

			// dispatch the event before performing this action
			MovedCard.dispatchEvent(card, idDestination, controller, silentMode);

			// we update the idZone now.
			card.setIdZone(IdZones.PLAY);

			/*
			 * FROM THIS POINT THERE IS NO MORE INCOHERENCE IN MCard#idZone
			 */
		} else {
			/*
			 * Since the card does not move into play, we proceed as usual : generate
			 * events, and then move physically the card.
			 */

			// dispatch the event before performing this action
			MovedCard.dispatchEvent(card, idDestination, controller, silentMode);

			// add new abilities
			card.registerAbilities(idDestination);

			// move now the card
			final int previousIdZone = card.getIdZone();
			card.moveCard(idDestination, controller, false, idPosition);

			// Add the modifiers
			if (card.getModifierModels() != null && previousIdZone != idDestination) {
				card.getModifierModels().addModifierFromModel(card.getDummyAbility(),
						null);
			}

			// detachment event?
			if (previousIdZone == IdZones.PLAY && !silentMode) {
				/*
				 * since we are leaving play, we generate the event concerning
				 * detachment for each components
				 */
				for (MCard attachedCard : card.getAttachedCards()) {
					Detached.dispatchEvent(attachedCard, card);
				}
			}
		}

		if (silentMode) {
			for (MCard attachedCard : card.getAttachedCards()) {
				attachedCard.setIdZone(idDestination);
				attachedCard.clearDamages();
			}
		}

		// unregister useless abilities from the eventManager
		card.unregisterAbilities();
		return true;
	}

	public boolean continueLoop(ContextEventListener context, int loopingIndex,
			Ability ability) {
		Log.debug("\t...continue loop, index : " + loopingIndex);
		if (!StackManager.getInstance().getTargetedList().get(loopingIndex)
				.isCard()
				|| moveCard((MCard) StackManager.getInstance().getTargetedList().get(
						loopingIndex), controller, destination, context, idPosition,
						ability, silent)) {
			return true;
		}
		return false;
	}

	/**
	 * This method is called when the opponent has finished to choose the order of
	 * moves.
	 * 
	 * @param data
	 *          array corresponding to the order of cards owned by opponent.
	 */
	public synchronized void receiveMoveOrder(byte[] data) {
		Log.debug("receiveMoveOrder : " + Arrays.toString(data));
		final List<MCard> toBeSortedCardsCurrnt = new ArrayList<MCard>();
		final List<MCard> toBeSortedCardsNonCurrent = new ArrayList<MCard>();
		fillList(toBeSortedCardsCurrnt, toBeSortedCardsNonCurrent);
		if (StackManager.currentIsYou()) {
			/*
			 * we are receiving the move order of non current player, and we have
			 * already made our choice since we are the current player. Apply
			 * arrangement following specified order.
			 */
			for (byte i : data) {
				StackManager.getTargetListAccess().remove(
						toBeSortedCardsNonCurrent.get(i));
				StackManager.getTargetListAccess()
						.add(toBeSortedCardsNonCurrent.get(i));
			}
			// Now all players have made their choice, we resolve the stack
			StackManager.actionManager.loopingIndex = StackManager
					.getTargetListAccess().size();
			StackManager.resolveStack();
		} else {
			/*
			 * we are receiving the move order of current player. We are the
			 * non-current player. Apply arrangement following specified order.
			 */
			for (int i = data.length; i-- > 0;) {
				StackManager.getTargetListAccess().remove(
						toBeSortedCardsCurrnt.get(data[i]));
				StackManager.getTargetListAccess().add(
						toBeSortedCardsCurrnt.get(data[i]));
			}

			// and then, the non-current player can make order choice if needed
			if (toBeSortedCardsNonCurrent.size() > 1) {
				Log.debug("You are arranging moving cards you own");
				StackManager.currentPlayer().getOpponent().setHandedPlayer();
				replayAction(StackManager.getInstance().getAbilityContext(),
						StackManager.currentAbility, new Arrange(MCard.getIdZone(
								destination, StackManager.getInstance().getAbilityContext()),
								toBeSortedCardsNonCurrent, new int[toBeSortedCardsNonCurrent
										.size()], StackManager.currentPlayer().getOpponent()));
			} else {
				// all is done : current and non-current player have made their choice
				StackManager.actionManager.loopingIndex = StackManager
						.getTargetListAccess().size();
				StackManager.resolveStack();
			}
		}
	}

	private void fillList(List<MCard> toBeSortedCardsCurrnt,
			List<MCard> toBeSortedCardsNonCurrent) {
		/*
		 * list all cards controlled by CURRENT player in order to put them in the
		 * destination zone
		 */
		for (int i = 0; i < StackManager.getInstance().getTargetedList().size(); i++) {
			if (StackManager.getInstance().getTargetedList().get(i).isCard()) {
				if (((MCard) StackManager.getInstance().getTargetedList().get(i))
						.getOwner().isCurrentPlayer()) {
					toBeSortedCardsCurrnt.add((MCard) StackManager.getTargetListAccess()
							.get(i));
				} else {
					toBeSortedCardsNonCurrent.add((MCard) StackManager
							.getTargetListAccess().get(i));
				}
			}
		}
	}

	public synchronized int getStartIndex() {
		final int count = StackManager.getInstance().getTargetedList().size();
		final int idDestination = MCard.getIdZone(destination, StackManager
				.getInstance().getAbilityContext());
		if (count > 1 && idDestination >= IdZones.FIRST_ADDITIONAL_ZONE
				&& idDestination <= IdZones.LAST_ADDITIONAL_ZONE) {
			final List<MCard> toBeSortedCardsCurrent = new ArrayList<MCard>(count);
			final List<MCard> toBeSortedCardsNonCurrent = new ArrayList<MCard>(count);
			fillList(toBeSortedCardsCurrent, toBeSortedCardsNonCurrent);

			if (toBeSortedCardsCurrent.size() > 1) {
				// current player can arrange order of cards
				final int[] order = new int[toBeSortedCardsCurrent.size()];
				if (StackManager.currentIsYou()) {
					Log.debug("You (current player) are arranging moving cards you own");
					StackManager.currentPlayer().setHandedPlayer();
					replayAction(StackManager.getInstance().getAbilityContext(),
							StackManager.currentAbility, new Arrange(idDestination,
									toBeSortedCardsCurrent, order, StackManager.currentPlayer()));
				} else {
					Log
							.debug("Opponent (current player) is arranging moving cards he/she owns");
					StackManager.currentPlayer().setHandedPlayer();
				}
				return Integer.MAX_VALUE;
			}
			if (toBeSortedCardsNonCurrent.size() > 1) {
				// now we wait the non current player choice
				if (StackManager.currentIsYou()) {
					Log
							.debug("Opponent (non-current player) is arranging moving cards he/she owns");
					StackManager.currentPlayer().getOpponent().setHandedPlayer();
				} else {
					Log
							.debug("You (non-current player) are arranging moving cards you own");
					StackManager.currentPlayer().getOpponent().setHandedPlayer();
					final int[] order = new int[toBeSortedCardsNonCurrent.size()];
					StackManager.currentPlayer().getOpponent().setHandedPlayer();
					replayAction(StackManager.getInstance().getAbilityContext(),
							StackManager.currentAbility, new Arrange(idDestination,
									toBeSortedCardsNonCurrent, order, StackManager
											.currentPlayer().getOpponent()));
				}
				return Integer.MAX_VALUE;
			}
		}
		return count - 1;
	}

	public void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final MoveContext bContext = (MoveContext) actionContext.actionContext;
		for (int loopingIndex = 0; loopingIndex < StackManager.getInstance()
				.getTargetedList().size(); loopingIndex++) {
			if (StackManager.getInstance().getTargetedList().get(loopingIndex)
					.isCard()) {
				final MCard card = (MCard) ((MCard) StackManager.getInstance()
						.getTargetedList().get(loopingIndex)).getOriginalTargetable();
				final MZone zoneSrc = card.controller.zoneManager.getContainer(card
						.getIdZone());

				zoneSrc.remove(card);
				card.setIdZone(bContext.idZones[loopingIndex]);
				card.controller = bContext.controllers[loopingIndex];
				if (bContext.attachedTo[loopingIndex] != null) {
					bContext.attachedTo[loopingIndex].add(card);
					// this card was attached to another one in play
					bContext.attachedTo[loopingIndex].getMUI().updateLayout();
				}

				// update positions and controller
				card.isHighLighted = false;
				card.reversed = card.needReverse();
				card.getMUI().updateLayout();

				// move to the destination this card
				switch (bContext.idZones[loopingIndex]) {
				case IdZones.PLAY:
					// update card UI
					card.tap(bContext.tapPosition[loopingIndex]);
					if ((Integer) bContext.indexes[loopingIndex].value != 0) {
						card.controller.zoneManager.play.getCard(
								bContext.indexes[loopingIndex].key).add(card,
								bContext.indexes[loopingIndex].value.intValue());
					} else {
						card.controller.zoneManager.play.add(card,
								bContext.indexes[loopingIndex].key);
					}
					break;
				case IdZones.NOWHERE:
					// the specified card will never be seen again
					break;
				default:
					MZone zone = card.controller.zoneManager
							.getContainer(bContext.idZones[loopingIndex]);
					if ((Integer) bContext.indexes[loopingIndex].value != 0) {
						zone.getCard(bContext.indexes[loopingIndex].key).add(card,
								bContext.indexes[loopingIndex].value.intValue());
					} else {
						zone.add(card, bContext.indexes[loopingIndex].key);
					}
				}
			} else {
				Log
						.warn("In MOVE-CARD action, target list contains non 'Card' object. Ignored");
			}
		}
	}

	public void simulate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final MoveContext bContext = new MoveContext(StackManager.getInstance()
				.getTargetedList().size());
		actionContext.actionContext = bContext;
		final int idDestination = MCard.getIdZone(destination, context);
		final boolean newIsTapped = (destination & IdZones.PLAY_TAPPED) == IdZones.PLAY_TAPPED;
		for (int loopingIndex = StackManager.getInstance().getTargetedList().size(); loopingIndex-- > 0;) {
			if (StackManager.getInstance().getTargetedList().get(loopingIndex)
					.isCard()) {
				final MCard card = (MCard) ((MCard) StackManager.getInstance()
						.getTargetedList().get(loopingIndex)).getOriginalTargetable();
				final Player newController = (Player) controller.getTargetable(ability,
						card, context, null);
				final MZone zoneSrc = card.controller.zoneManager.getContainer(card
						.getIdZone());
				// remove card from it's previous zone
				bContext.tapPosition[loopingIndex] = card.tapped;
				bContext.controllers[loopingIndex] = card.controller;
				bContext.idZones[loopingIndex] = card.getIdZone();
				bContext.indexes[loopingIndex] = zoneSrc.getRealIndexOf(card);

				if (card.getIdZone() == IdZones.PLAY) {
					final MCard attachedTo = card.getParent() instanceof MCard ? (MCard) card
							.getParent()
							: null;
					bContext.attachedTo[loopingIndex] = attachedTo;
					zoneSrc.remove(card);
					card.tap(false);
					if (attachedTo != null) {
						// this card was attached to another one in play
						attachedTo.getMUI().updateLayout();
					}
				} else if (card.getParent() != null) {
					zoneSrc.remove(card);
				}

				// update positions ans controller
				card.controller = newController;
				card.setIdZone(idDestination);
				card.isHighLighted = false;
				card.reversed = card.needReverse();
				card.getMUI().updateLayout();

				// move to the destination this card
				switch (idDestination) {
				case IdZones.PLAY:
					// update card UI
					card.tap(newIsTapped);
					newController.zoneManager.play.addBottom(card);
					break;
				case IdZones.NOWHERE:
					// the specified card will never be seen again
					break;
				default:
					switch (idPosition) {
					case IdPositions.ON_THE_TOP:
						newController.zoneManager.getContainer(idDestination).addTop(card);
						break;
					case IdPositions.ON_THE_BOTTOM:
					default:
						newController.zoneManager.getContainer(idDestination).addBottom(
								card);
					}
				}
			} else {
				Log
						.warn("In MOVE-CARD action, target list contains non 'Card' object. Ignored");
			}
		}
	}

	@Override
	public String toString(Ability ability) {
		if (destination == IdZones.PLAY_TAPPED) {
			return LanguageManagerMDB.getString("move-"
					+ ZoneManager.getZoneName(IdZones.PLAY) + "-tapped");
		}
		if (destination == IdZones.PLAY) {
			return LanguageManagerMDB.getString("move-"
					+ ZoneManager.getZoneName(IdZones.PLAY));
		}
		if (idPosition == IdPositions.ON_THE_BOTTOM) {
			return LanguageManagerMDB.getString("move-"
					+ ZoneManager.getZoneName(MCard.getIdZone(destination, null))
					+ "-bottom");
		}
		return LanguageManagerMDB.getString("move-"
				+ ZoneManager.getZoneName(MCard.getIdZone(destination, null)) + "-top");
	}

	/**
	 * the destination place
	 * 
	 * @see IdZones
	 */
	private final int destination;

	/**
	 * The new controller
	 */
	private final TestOn controller;

	/**
	 * The position where card would be placed
	 * 
	 * @see net.sf.firemox.token.IdPositions
	 */
	private final int idPosition;

	/**
	 * Is the silent mode is enabled while playing.
	 */
	private final boolean silent;

	public int getAccessibleInt(String attribute) {
		// TODO complete to support the AccessibleContext pattern
		return 0;
	}

	public Target getAccessibleTargetable(String attribute) {
		// TODO complete to support the AccessibleContext pattern
		return null;
	}

	/**
	 * Shared attrribute to access to the destination zone id.
	 */
	// public static final String SHARE_DESTINATION = "destination";
	/**
	 * Shared attrribute to access to the destination controller player.
	 */
	// public static final String SHARE_CONTROLLER = "controller";
}