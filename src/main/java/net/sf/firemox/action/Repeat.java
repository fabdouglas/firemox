/*
 *    Firemox is a turn based strategy simulator
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

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.Int;
import net.sf.firemox.action.handler.InitAction;
import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.stack.StackManager;

/**
 * Repeat the next action n times. If the value is negative or zero, the next
 * action would be skipped.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public class Repeat extends UserAction implements StandardAction, InitAction {

	/**
	 * Create an instance of Repeat by reading a file Offset's file must pointing
	 * on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idNumber [2]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	Repeat(InputStream inputFile) throws IOException {
		super(inputFile);
		valueExpr = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * return the id of this action. This action has been read from the mdb file.
	 * 
	 * @see Actiontype
	 * @return the id of this action
	 */
	@Override
	public final Actiontype getIdAction() {
		return Actiontype.REPEAT_ACTION;
	}

	public boolean init(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		actionContext.actionContext = new Int(getRepeatValue(context, ability));
		if (((Int) actionContext.actionContext).getInt() == 0) {
			StackManager.actionManager.setHop(2);
		} else {
			StackManager.actionManager.setRepeat(((Int) actionContext.actionContext)
					.getInt());
		}
		return true;
	}

	public boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		// Ignored since the counter is present in context of all replayable actions
		return true;
	}

	/**
	 * Replay the next action the amount of context times.
	 * 
	 * @param actionContext
	 *          the context containing data saved by this action during the
	 *          'choose" process.
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          is the context attached to this action.
	 */
	public void replayOnDemand(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		StackManager.actionManager.setRepeat(((Int) actionContext.actionContext)
				.getInt());
	}

	public boolean play(ContextEventListener context, Ability ability) {
		if (getRepeatValue(context, ability) == 0) {
			StackManager.actionManager.setHop(2);
		} else {
			StackManager.actionManager.setRepeat(getRepeatValue(context, ability));
		}
		return true;
	}

	private int getRepeatValue(ContextEventListener context, Ability ability) {
		final int value = valueExpr.getValue(ability, null, context);
		if (value > 0) {
			// enter in the loop
			return value;
		}
		// we would never enter into the loop
		return 0;
	}

	/**
	 * return the string representation of this action
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @return the string representation of this action
	 * @see Object#toString()
	 */
	@Override
	public String toString(Ability ability) {
		try {
			return " x " + valueExpr.getValue(ability, null, null);
		} catch (Exception e) {
			return " x (*)";
		}
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		try {
			return " x " + valueExpr.getValue(ability, null, context);
		} catch (Exception e) {
			return " x " + MdbLoader.unknownSmlManaHtml;
		}
	}

	/**
	 * Return number times to repeat the next action exactly as it will be when
	 * the ability will be executed..Return <code>-1</code> if value cannot be
	 * preempted.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @return number times to repeat the next action.Return <code>-1</code> if
	 *         value is not static.
	 */
	public int getPreemptionTimes(Ability ability, Target tested) {
		try {
			return valueExpr.getPreemptionValue(ability, tested, null);
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * represents the number of times to repeat the next action. The complex
	 * expression to use for the right value. Is null if the IdToken number is not
	 * a complex expression.
	 */
	private Expression valueExpr;

}