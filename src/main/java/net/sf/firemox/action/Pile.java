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

import javax.swing.JOptionPane;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.Int;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;

/**
 * Put cards on a Pile for the player to arrange them in the specified zone, in the specified position.
 * 
 * @author <a href="mailto:ricardo_barbano@hotmail.com">Riclas </a>
 * @since 0.92
 */
class Pile extends MessagingAction {

	/**
	 * Create an instance of Pile by reading a file Offset's file must
	 * pointing on the first byte of this action. <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
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
	Pile(InputStream inputFile) throws IOException {
		super(inputFile);
		destination = inputFile.read();
		idPosition = MToolKit.readInt16(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.PILE;
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		final Player controller = (Player) this.controller.getTargetable(ability,
				context, null);
		controller.setHandedPlayer();
		if (controller.isYou()) {
			Log.debug("Opponent is waiting for our answer");
			replayAction(context, ability,
					new net.sf.firemox.ui.wizard.Pile(destination, idPosition, StackManager.currentPlayer()));
		} else {
			Log.debug("Waiting for opponent's answer");
		}
		return false;
	}

	@Override
	public final CoreMessageType getMessagingActionId() {
		return CoreMessageType.COLOR_ANSWER;
	}

	@Override
	protected void setAnswer(int optionAnswer, int indexAnswer,
			ContextEventListener context, Ability ability,
			ActionContextWrapper actionContext) {
		if (optionAnswer == JOptionPane.NO_OPTION) {
		}
	}

	@Override
	public String toString(Ability ability) {
		return "pile";
	}

	@Override
	public final boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		StackManager.actionManager.setHop(((Int) actionContext.actionContext)
				.getInt());
		return true;
	}

	/**
	 * the destination place
	 * 
	 * @see IdZones
	 */
	private final int destination;

	/**
	 * The position where card would be placed
	 * 
	 * @see net.sf.firemox.token.IdPositions
	 */
	private final int idPosition;

}