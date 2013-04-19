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
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.Register;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;

/**
 * Add a random valid target in the list following the specified type. No event
 * is generated. The friendly test, and the type are required to list the valids
 * targets. The target list is used by the most actions. <br>
 * For example, if the next action 'damage', a random card/player from the
 * target list would be damaged. When a new ability is added to the stack, a new
 * list is created and attached to it. Actions may modify, acceess it until the
 * ability is completely resolved.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
class TargetRandomNoEvent extends UserAction implements StandardAction {

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
	TargetRandomNoEvent(InputStream inputFile) throws IOException {
		super(inputFile);
		type = Register.deserialize(inputFile).ordinal();
		test = TestFactory.readNextTest(inputFile);
		restrictionZone = inputFile.read() - 1;
		// consume the no needed preemption value
		inputFile.read();
	}

	public final boolean play(ContextEventListener context, Ability ability) {
		String targetTxt = null;
		Target random = null;
		switch (type) {
		case IdTokens.PLAYER:
			// Target is a player
			targetTxt = "[random players]";
			if (test
					.test(ability, StackManager.PLAYERS[StackManager.idCurrentPlayer])) {
				if (test.test(ability,
						StackManager.PLAYERS[1 - StackManager.idCurrentPlayer])) {
					random = StackManager.PLAYERS[MToolKit.getRandom(1)];
				} else {
					random = StackManager.PLAYERS[StackManager.idCurrentPlayer];
				}
			} else if (test.test(ability,
					StackManager.PLAYERS[1 - StackManager.idCurrentPlayer])) {
				random = StackManager.PLAYERS[1 - StackManager.idCurrentPlayer];
			}
			break;
		case IdTokens.CARD:
			// Target is a card
			targetTxt = "[random cards]";
			final List<Target> validTargets = new ArrayList<Target>();
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
			random = validTargets.get(MToolKit.getRandom(validTargets.size()));
			break;
		default:
			throw new InternalError("Unknown type :" + type);
		}
		Log.debug("random mode, target=" + random);
		MagicUIComponents.magicForm.moreInfoLbl.setText(targetTxt
				+ " - random mode)");
		if (random != null) {
			StackManager.getInstance().getTargetedList().list.add(random);
		}
		// continue the stack process
		return true;
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.TARGET_RANDOM;
	}

	@Override
	public String toString(Ability ability) {
		switch (type) {
		case IdTokens.CARD:
			return "[random cards]";
		case IdTokens.PLAYER:
			return "[random players]";
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