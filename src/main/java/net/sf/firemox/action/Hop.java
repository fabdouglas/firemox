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

/**
 * Change the normal jump(1) in the current actions chain, to another one . This
 * jump can be positive or negative. Zero mean infinite loop.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 * @since 0.72 support counter ability
 */
public class Hop extends UserAction implements StandardAction, InitAction,
		RollBackAction {

	/**
	 * Create an instance of Hop by reading a file Offset's file must pointing on
	 * the first byte of this action
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idAction [1]</li>
	 * <li>hop value [2]</li>
	 * <li>+ test if counter mode [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	Hop(InputStream inputFile) throws IOException {
		super(inputFile);
		valueExpr = ExpressionFactory.readNextExpression(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.HOP;
	}

	public boolean init(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final int hopValue = valueExpr
				.getValue(ability, ability.getCard(), context);
		actionContext.actionContext = new Int(hopValue);
		StackManager.actionManager.setHop(hopValue);
		return true;
	}

	public boolean replay(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final int hopValue = ((Int) actionContext.actionContext).getInt();
		if (hopValue > 0) {
			StackManager.actionManager.setHop(hopValue);
		}
		return true;
	}

	public void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		// nothing to do
	}

	public boolean play(ContextEventListener context, Ability ability) {
		StackManager.actionManager.setHop(valueExpr.getValue(ability, ability
				.getCard(), context));
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "Hop";
	}

	/**
	 * The amount of actions to skip. The complex expression to use for the right
	 * value. Is null if the IdToken number is not a complex expression.
	 */
	private Expression valueExpr;

}