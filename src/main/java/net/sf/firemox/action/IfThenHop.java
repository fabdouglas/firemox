/* 
 * IfThenHop.java
 * Created on 20 févr. 2004
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

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.Int;
import net.sf.firemox.action.handler.InitAction;
import net.sf.firemox.action.handler.RollBackAction;
import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;

/**
 * Depending on result of test, jump to the next action with hop equal to 1
 * (normal case) for a false result, and jump to a sepecified hop for the true
 * result. Like the if...then...else instruction. <br>
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 * @see net.sf.firemox.action.Hop
 */
class IfThenHop extends UserAction implements StandardAction, InitAction,
		RollBackAction {

	/**
	 * Create an instance of IfThenHop by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>test [...]</li>
	 * <li>"hop" for 'else' case. Is 1 for 'then' case [2]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	IfThenHop(InputStream inputFile) throws IOException {
		super(inputFile);
		condition = TestFactory.readNextTest(inputFile);
		valueExpr = ExpressionFactory.readNextExpression(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.IF_THEN_ELSE;
	}

	public boolean init(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		actionContext.actionContext = new Int(condition.test(ability, ability
				.getCard()) ? 1 : 0);
		return replay(actionContext, context, ability);
	}

	public boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		if (((Int) actionContext.actionContext).getInt() == 0) {
			StackManager.actionManager.setHop(valueExpr.getValue(ability, null,
					context));
		}
		return true;
	}

	public void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		// nothing to do
	}

	public boolean play(ContextEventListener context, Ability ability) {
		if (!condition.test(ability, ability.getCard())) {
			StackManager.actionManager.setHop(valueExpr.getValue(ability, null,
					context));
		}
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "If ... Then ... Else ...";
	}

	/**
	 * The condition checked to determine the 'hop' value
	 */
	private Test condition;

	/**
	 * The number of action to skip in case of false condition. In case of true
	 * condition, 'hop' is equal to 1. The complex expression to use for the right
	 * value. Is null if the IdToken number is not a complex expression.
	 */
	private Expression valueExpr = null;

}