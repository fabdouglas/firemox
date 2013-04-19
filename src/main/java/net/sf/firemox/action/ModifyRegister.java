/* 
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

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.operation.OperationFactory;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * This action is used to modifiy a public token or a private one of the card.
 * This action can use the target list when is played : the address must be
 * IdTokens#TARGET. So the target list must set before this action would be
 * played.
 * 
 * @since 0.53 support STACK registers <br>
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 * @since 0.72 support counter ability
 */
public abstract class ModifyRegister extends UserAction implements
		StandardAction {

	/**
	 * Create an instance of ModifyRegister by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idAction [1]</li>
	 * <li>register [Register]</li>
	 * <li>index [Expression]</li>
	 * <li>operation [Operation]</li>
	 * <li>value [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected ModifyRegister(InputStream inputFile) throws IOException {
		super(inputFile);
		this.index = ExpressionFactory.readNextExpression(inputFile);
		this.op = OperationFactory.getOperation(IdOperation.deserialize(inputFile));
		this.valueExpr = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * Create an instance of ModifyRegister with the specified card owning the new
	 * created action
	 * 
	 * @param actionName
	 *          the action name.
	 * @param index
	 *          the modified register index.
	 * @param op
	 *          The operation applied to the token id and the value
	 * @param valueExpr
	 *          the used right expression.
	 */
	protected ModifyRegister(String actionName, Expression index, Operation op,
			Expression valueExpr) {
		super(actionName);
		this.index = index;
		this.op = op;
		this.valueExpr = valueExpr;
	}

	@Override
	public abstract Actiontype getIdAction();

	public abstract boolean play(ContextEventListener context, Ability ability);

	/**
	 * Checks all cards corresponding to this constraints
	 * 
	 * @param test
	 *          applied to count valid cards
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param restrictionZone
	 *          the restriction zone. If is <code>-1</code> the scan would be
	 *          processed on all zones.
	 * @return the amount of found cards.
	 */
	public static int countAllCardsOf(Test test, Ability ability,
			int restrictionZone) {
		if (restrictionZone != -1) {
			return StackManager.PLAYERS[0].zoneManager.getContainer(restrictionZone)
					.countAllCardsOf(test, ability)
					+ StackManager.PLAYERS[1].zoneManager.getContainer(restrictionZone)
							.countAllCardsOf(test, ability);
		}
		return StackManager.PLAYERS[0].zoneManager.countAllCardsOf(test, ability)
				+ StackManager.PLAYERS[1].zoneManager.countAllCardsOf(test, ability);
	}

	@Override
	public abstract String toString(Ability ability);

	@Override
	public final String toHtmlString(Ability ability, ContextEventListener context) {
		if (actionName != null && actionName.charAt(0) != '%'
				&& actionName.charAt(0) != '@' && actionName.indexOf("%n") != -1) {
			int value = 0;
			try {
				value = valueExpr.getValue(ability, null, context);
			} catch (Exception e) {
				value = -1;
			}
			if (value == 1) {
				return LanguageManagerMDB.getString(actionName.replaceAll("%n", "1"));
			}
			if (value == -1) {
				return LanguageManagerMDB.getString(actionName).replaceAll("%n",
						MdbLoader.unknownSmlManaHtml);
			}
			if (value > 0) {
				return LanguageManagerMDB.getString(actionName).replaceAll("%n",
						"" + value);
			}
		}
		// we return only the string representation
		return super.toHtmlString(ability, context);
	}

	/**
	 * Return the final value of this test is this value was a counter.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested component
	 * @param context
	 *          is the context attached to this test.
	 * @return he final value of this test is this value was a counter.
	 */
	protected int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		return valueExpr.getValue(ability, tested, context);
	}

	/**
	 * The index to modify.
	 */
	protected Expression index = null;

	/**
	 * The complex expression to use for the right value. Is null if the IdToken
	 * number is not a complex expression.
	 */
	protected Expression valueExpr = null;

	/**
	 * The operation applied to the token id and the value
	 */
	protected Operation op;

}
