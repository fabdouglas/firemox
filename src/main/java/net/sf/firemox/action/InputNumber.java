/*
 * Created on Dec 31, 2004
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

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.Int;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.database.propertyconfig.PropertyProxyConfig;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.expression.IntValue;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;

/**
 * Display an input box waiting for an integer answer. It's possible to use the
 * multi-language ability for text if the string start with the '$' character.
 * The entered integer is used as 'value' for 'modifyregister'.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 * @since 0.91 use 'default' value and 'stric-max' constraint.
 */
class InputNumber extends MessagingAction {

	/**
	 * Create an instance of InputInteger by reading a file Offset's file must
	 * pointing on the first byte of this action. <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>modifier [ModifyRegister]</li>
	 * <li>min expression [...]</li>
	 * <li>max expression [...]</li>
	 * <li>default expression [...]</li>
	 * <li>strict-max boolean 1/0 [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	InputNumber(InputStream inputFile) throws IOException {
		super(inputFile);
		registerModifier = (ModifyRegister) ActionFactory.readAction(inputFile,
				null);
		minExpr = ExpressionFactory.readNextExpression(inputFile);
		maxExpr = ExpressionFactory.readNextExpression(inputFile);
		defaultExpr = ExpressionFactory.readNextExpression(inputFile);
		strictMax = inputFile.read() != 0;
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.INPUT_NUMBER;
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		final Player controller = (Player) this.controller.getTargetable(ability,
				context, null);
		controller.setHandedPlayer();
		if (controller.isYou()) {
			Log.debug("Opponent is waiting for our answer");
			PropertyProxyConfig.values.put("%maximum", maxExpr);
			PropertyProxyConfig.values.put("%minimum", minExpr);
			replayAction(context, ability,
					new net.sf.firemox.ui.wizard.InputNumber(ability, text, minExpr
							.getValue(ability, ability.getCard(), context), maxExpr.getValue(
							ability, ability.getCard(), context), false, defaultExpr
							.getValue(ability, ability.getCard(), context), strictMax));
		} else {
			Log.debug("Waiting for opponent's answer");
		}
		return false;
	}

	@Override
	public String toString(Ability ability) {
		// TODO Translate 'integer input box'
		return "integer inputbox";
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		return "<img src='file:///"
				+ MToolKit.getTbsHtmlPicture("actions/input-number.gif") + "'>&nbsp;";
	}

	@Override
	protected final CoreMessageType getMessagingActionId() {
		return CoreMessageType.INTEGER_ANSWER;
	}

	@Override
	protected void setAnswer(int optionAnswer, int indexAnswer,
			ContextEventListener context, Ability ability,
			ActionContextWrapper actionContext) {
		((IntValue) registerModifier.valueExpr).value = indexAnswer;
		if (actionContext != null) {
			actionContext.actionContext = new Int(
					((IntValue) registerModifier.valueExpr).value);
		}
		if (registerModifier.play(context, ability)) {
			StackManager.resolveStack();
		}
	}

	@Override
	public final boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		((IntValue) registerModifier.valueExpr).value = ((Int) actionContext.actionContext)
				.getInt();
		return registerModifier.play(context, ability);
	}

	/**
	 * The register modification action using the answer as input.
	 */
	private final ModifyRegister registerModifier;

	/**
	 * The maximal value accepted for the displayed wizard.
	 */
	private final Expression maxExpr;

	/**
	 * The minimal value accepted for the displayed wizard.
	 */
	private final Expression minExpr;

	/**
	 * The default value displayed in the diaLog. By default, the maximal allowed
	 * value is used (%max).
	 */
	private final Expression defaultExpr;

	/**
	 * Allow or not to enter a value exceeding the maximal value. If false
	 * (default), the user can enter a higher value with a warning displayed in
	 * the diaLog.
	 */
	private final boolean strictMax;
}