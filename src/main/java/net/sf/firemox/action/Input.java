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
import net.sf.firemox.action.context.Int;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.operation.OperationFactory;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdMessageBox;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.wizard.Ok;
import net.sf.firemox.ui.wizard.Wizard;
import net.sf.firemox.ui.wizard.YesNo;

/**
 * Display a message box with a customizable OK button and text. It's possible
 * to use the multi-language ability for text and button label if string starts
 * with the '$' character. In case of type is 'yes-no', the answer is 0 for
 * "YES", and 1 for "NO". To test the answer of this question, test the register
 * named 'stack' at the index '0'.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.71
 * @since 0.82 support controller
 */
public class Input extends MessagingAction {

	/**
	 * Create an instance of Msg by reading a file Offset's file must pointing on
	 * the first byte of this action. <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>controller [TestOn]</li>
	 * <li>text to display +'/0' [...]</li>
	 * <li>type of message box (idMessageBox) [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	Input(InputStream inputFile) throws IOException {
		super(inputFile);
		type = IdMessageBox.deserialize(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.MSG;
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		final Player controller = (Player) this.controller.getTargetable(ability,
				context, null);
		controller.setHandedPlayer();
		if (controller.isYou()) {
			Log.debug("Opponent is waiting for our answer");
			final Wizard wizard;
			switch (type) {
			case yesno:
				wizard = new YesNo(ability, ability.getName(), ability
						.getAbilityTitle(), "wiz_question.gif", 300, 250, text);
				break;
			case ok:
				wizard = new Ok(ability, ability.getName(), ability.getAbilityTitle(),
						"wiz_info.gif", 300, 250, text);
				break;
			default:
				throw new InternalError("Unknow message type : " + type);
			}

			// while the user has not given an answer

			replayAction(context, ability, wizard);
		} else {
			MagicUIComponents.logListing.append(controller.idPlayer,
					"Waiting for opponent's answer");
			Log.debug("Waiting for opponent's answer");
		}
		return false;
	}

	@Override
	public String toString(Ability ability) {
		return "msgbox";
	}

	@Override
	protected final CoreMessageType getMessagingActionId() {
		return CoreMessageType.ANSWER;
	}

	@Override
	protected void setAnswer(int optionAnswer, int indexAnswer,
			ContextEventListener context, Ability ability,
			ActionContextWrapper actionContext) {
		if (actionContext != null) {
			actionContext.actionContext = new Int(optionAnswer);
		}
		StackManager.registers[IdTokens.MSG_ANSWER_INDEX] = OperationFactory
				.getOperation(IdOperation.SET).process(0, optionAnswer);
		StackManager.resolveStack();
	}

	/**
	 * The type of message box
	 * 
	 * @see net.sf.firemox.token.IdMessageBox
	 */
	private final IdMessageBox type;

}