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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.MovePlayerCardContext;
import net.sf.firemox.action.handler.FollowAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.UncaughtException;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.IdPositions;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.zone.MZone;
import net.sf.firemox.zone.ZoneManager;

/**
 * To move any number of cards or the targeted player from their place to
 * another. New position within the new zone, and the new controller have to be
 * specified. <br>
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
class MovePlayerCard extends UserAction implements LoopAction, FollowAction {

	/**
	 * Create an instance of MovePlayerCard by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>new controller [TestOn]</li>
	 * <li>from zone [IdZone]</li>
	 * <li>destination zone [IdZone]</li>
	 * <li>idPosition [int16]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	MovePlayerCard(InputStream inputFile) throws IOException {
		super(inputFile);
		controller = TestOn.deserialize(inputFile);
		from = inputFile.read();
		to = inputFile.read();
		idPosition = MToolKit.readInt16(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.MOVE_PLAYER_CARD;
	}

	public boolean continueLoop(ContextEventListener context, int loopingIndex,
			Ability ability) {
		final Target target = StackManager.getInstance().getTargetedList().get(
				loopingIndex);
		if (!target.isPlayer()) {
			Log.fatal("The move-player-card action use "
					+ "the players placed in the target-list, "
					+ "nevertheless a non-player element has been found (" + target
					+ ") : \n" + debugData);
		}
		final MZone panel = ((Player) target).zoneManager.getContainer(from);
		if (panel.getComponentCount() == 0) {
			UncaughtException.dispatchEvent((Player) StackManager.getInstance()
					.getTargetedList().get(loopingIndex), "NoSuchElement");
			return true;
		} else if (MoveCard.moveCard(panel.getTop(), controller, to, context,
				idPosition, ability, false)) {
			return true;
		}
		return false;
	}

	public void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final MovePlayerCardContext bContext = (MovePlayerCardContext) actionContext.actionContext;

		for (int loopingIndex = bContext.srcZones.length; loopingIndex-- > 0;) {
			final MZone zoneSrc = bContext.srcZones[loopingIndex];
			final MCard card = bContext.cards[loopingIndex];
			if (zoneSrc == null)
				break;
			zoneSrc.remove(card);
			card.setIdZone(zoneSrc.getZoneId());
			card.controller = bContext.controllers[loopingIndex];

			// update positions and controller
			card.isHighLighted = false;
			card.reversed = card.needReverse();
			card.getMUI().updateLayout();

			// move to the destination this card
			switch (zoneSrc.getZoneId()) {
			case IdZones.PLAY:
				// update card UI
				card.tap(bContext.tapPosition[loopingIndex]);
				card.controller.zoneManager.play.addTop(card);
				break;
			case IdZones.NOWHERE:
				// the specified card will never be seen again
				break;
			default:
				zoneSrc.addTop(card);
			}
		}
	}

	public void simulate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final MovePlayerCardContext bContext = new MovePlayerCardContext(
				StackManager.getInstance().getTargetedList().size());
		actionContext.actionContext = bContext;
		for (int loopingIndex = bContext.srcZones.length; loopingIndex-- > 0;) {
			final MZone zoneSrc = ((Player) StackManager.getInstance()
					.getTargetedList().get(loopingIndex)).zoneManager.getContainer(from);
			if (zoneSrc.getComponentCount() > 0) {
				final MCard card = zoneSrc.getTop();
				final Player newController = (Player) controller.getTargetable(ability,
						card, context, null);
				final int destinationZone = MCard.getIdZone(to, context);
				final boolean newIsTapped = (to & IdZones.PLAY_TAPPED) == IdZones.PLAY_TAPPED;

				bContext.tapPosition[loopingIndex] = card.tapped;
				bContext.controllers[loopingIndex] = card.controller;
				bContext.srcZones[loopingIndex] = zoneSrc;

				// remove card from it's previous zone
				zoneSrc.remove(card);

				// update positions and controller
				card.controller = newController;
				card.setIdZone(destinationZone);
				card.isHighLighted = false;
				card.reversed = card.needReverse();
				card.tap(false);

				// move to the destination this card
				switch (destinationZone) {
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
						newController.zoneManager.getContainer(destinationZone)
								.addTop(card);
						break;
					case IdPositions.ON_THE_BOTTOM:
					default:
						newController.zoneManager.getContainer(destinationZone).addBottom(
								card);
					}
				}
			}
		}
	}

	/**
	 * Return the first index of this loop.
	 * 
	 * @return the first index of this loop.
	 */
	public int getStartIndex() {
		return StackManager.getInstance().getTargetedList().size() - 1;
	}

	@Override
	public String toString(Ability ability) {
		if (MCard.getIdZone(to, null) == IdZones.PLAY) {
			return "Put in play" + (MCard.isTapped(to) ? " tapped" : "");
		}
		if (to == IdZones.HAND) {
			return "Return to graveyard";
		}
		return "move from " + ZoneManager.getZoneName(from) + " to "
				+ ZoneManager.getZoneName(MCard.getIdZone(to, null));
	}

	/**
	 * the destination place
	 */
	private final int to;

	/**
	 * the source place
	 */
	private final int from;

	/**
	 * The new controller
	 */
	private final TestOn controller;

	/**
	 * The position where card would be placed
	 */
	private final int idPosition;

}