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
import java.util.List;

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.Register;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.MagicUIComponents;

/**
 * Add all valid targets in the list following the specified type. No event is
 * generated. The friendly test, and the type are required to list the valids
 * targets. The target list is used by the most actions. <br>
 * For example, if the next action 'damage', all cards/player from the target
 * list would be damaged. When a new ability is added to the stack, a new list
 * is created and attached to it. Actions may modify, acceess it until the
 * ability is completely resolved.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
class TargetAllNoEvent extends UserAction implements StandardAction {

	/**
	 * Create an instance by reading a file Offset's file must pointing on the
	 * first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>type [Register]</li>
	 * <li>test [Test]</li>
	 * <li>restriction Zone id [int]</li>
	 * <li>can be preempted [boolean]</li>
	 * </ul>
	 * For the available options, see interface IdTargets
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 * @see net.sf.firemox.token.IdTargets
	 */
	TargetAllNoEvent(InputStream inputFile) throws IOException {
		super(inputFile);
		type = Register.deserialize(inputFile).ordinal();
		test = TestFactory.readNextTest(inputFile);
		restrictionZone = inputFile.read() - 1;
		// consume the no needed preemption value
		inputFile.read();
	}

	public final boolean play(ContextEventListener context, Ability ability) {
		final List<Target> validTargets = new ArrayList<Target>();
		String targetTxt = null;
		switch (type) {
		case IdTokens.DEALTABLE:
			// Target is a player or card
			targetTxt = "[all creatures and all players]";
			if (test
					.test(ability, StackManager.PLAYERS[StackManager.idCurrentPlayer])) {
				validTargets.add(StackManager.PLAYERS[StackManager.idCurrentPlayer]);
			}
			if (test.test(ability,
					StackManager.PLAYERS[1 - StackManager.idCurrentPlayer])) {
				validTargets
						.add(StackManager.PLAYERS[1 - StackManager.idCurrentPlayer]);
			}
			if (restrictionZone != -1) {
				StackManager.PLAYERS[StackManager.idCurrentPlayer].zoneManager
						.getContainer(restrictionZone).checkAllCardsOf(test, validTargets,
								ability);
				StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].zoneManager
						.getContainer(restrictionZone).checkAllCardsOf(test, validTargets,
								ability);
			} else {
				StackManager.PLAYERS[StackManager.idCurrentPlayer].zoneManager
						.checkAllCardsOf(test, validTargets, ability);
				StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].zoneManager
						.checkAllCardsOf(test, validTargets, ability);
			}
			break;
		case IdTokens.PLAYER:
			// Target is a player
			targetTxt = "[all players]";
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
			targetTxt = "[all cards]";
			if (restrictionZone != -1) {
				StackManager.PLAYERS[StackManager.idCurrentPlayer].zoneManager
						.getContainer(restrictionZone).checkAllCardsOf(test, validTargets,
								ability);
				StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].zoneManager
						.getContainer(restrictionZone).checkAllCardsOf(test, validTargets,
								ability);
			} else {
				StackManager.PLAYERS[StackManager.idCurrentPlayer].zoneManager
						.checkAllCardsOf(test, validTargets, ability);
				StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].zoneManager
						.checkAllCardsOf(test, validTargets, ability);
			}
			break;
		default:
			throw new InternalError("Unknown type :" + type);
		}
		if (StackManager.getInstance().getTargetedList().list != null) {
			validTargets.removeAll(StackManager.getInstance().getTargetedList().list);
		}
		Log.debug("all mode, size=" + validTargets.size() + ", ignored="
				+ StackManager.getInstance().getTargetedList().list);
		MagicUIComponents.magicForm.moreInfoLbl.setText(targetTxt + " - all mode)");
		if (!validTargets.isEmpty()) {
			StackManager.getInstance().getTargetedList().list.addAll(validTargets);
		}
		// continue the stack process
		return true;
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.TARGET_ALL;
	}

	@Override
	public String toString(Ability ability) {
		switch (type) {
		case IdTokens.CARD:
			return "[all cards]";
		case IdTokens.PLAYER:
			return "[all players]";
		case IdTokens.DEALTABLE:
			return "[all cards and players]";
		default:
			throw new InternalError("Unknown type : " + type);
		}
	}

	/**
	 * represents the test applied to target before to be added to the list
	 */
	protected Test test;

	/**
	 * represents the type of target (player, card, dealtable)
	 * 
	 * @see net.sf.firemox.token.IdTargets
	 */
	protected int type;

	/**
	 * The zone identifant where the scan is restricted. If is equal to -1, there
	 * would be no restriction zone.
	 * 
	 * @see net.sf.firemox.token.IdZones
	 */
	protected int restrictionZone;
}