/*
 * Created on Jan 3, 2005
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
import net.sf.firemox.expression.IntValue;
import net.sf.firemox.expression.ListExpression;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
class InputColor extends MessagingAction {

	/**
	 * Create an instance of InputInteger by reading a file Offset's file must
	 * pointing on the first byte of this action. <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>super [Input]</li>
	 * <li>allowed colors [Expression[]]</li>
	 * <li>is colorless mana available [boolean]</li>
	 * <li>multi select [boolean]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	InputColor(InputStream inputFile) throws IOException {
		super(inputFile);
		registerModifier = (ModifyRegister) ActionFactory.readAction(inputFile,
				null);
		allowedColors = new ListExpression(inputFile);
		allowColorless = inputFile.read() == 1;
		multiselect = inputFile.read() == 1;
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.INPUT_COLOR;
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		final Player controller = (Player) this.controller.getTargetable(ability,
				context, null);
		controller.setHandedPlayer();
		if (controller.isYou()) {
			Log.debug("Opponent is waiting for our answer");
			int codeColors = 0;
			if (allowedColors.isEmpty())
				codeColors = 0xFF;
			else {
				for (int color : allowedColors.getList(ability, ability.getCard(),
						context))
					codeColors |= color;
			}
			replayAction(context, ability,
					new net.sf.firemox.ui.wizard.InputColor(ability, text,
							codeColors, allowColorless, multiselect));
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
			((IntValue) registerModifier.valueExpr).value = 0;
		} else {
			((IntValue) registerModifier.valueExpr).value = indexAnswer;
		}
		if (actionContext != null) {
			actionContext.actionContext = new Int(
					((IntValue) registerModifier.valueExpr).value);
		}
		if (registerModifier.play(context, ability)) {
			StackManager.resolveStack();
		}
	}

	@Override
	public String toString(Ability ability) {
		return "choose color";
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		return "<img src='file:///"
				+ MToolKit.getTbsHtmlPicture("actions/input-color.gif") + "'>&nbsp;";
	}

	@Override
	public final boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		((IntValue) registerModifier.valueExpr).value = ((Int) actionContext.actionContext)
				.getInt();
		return registerModifier.play(context, ability);
	}

	/**
	 * The register modifiaction action using the answer as input.
	 */
	private final ModifyRegister registerModifier;

	/**
	 * The allowed colors
	 */
	private final ListExpression allowedColors;

	/**
	 * Is colorless mana available?
	 */
	private final boolean allowColorless;

	/**
	 * Multi-select.
	 */
	private boolean multiselect;

}