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
package net.sf.firemox.expression;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.IntegerList;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public abstract class SavedListExpression extends Expression {

	/**
	 * Creates a new instance of SavedListExpression <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>test on id [1]</li>
	 * <li>list index : Expression [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected SavedListExpression(InputStream inputFile) throws IOException {
		super();
		listIndex = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * Return the integer list to be used with this expression.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @param context
	 *          is the context attached to this test.
	 * @return the integer list to be used with this expression.
	 */
	protected IntegerList getIntList(Ability ability, Target tested,
			ContextEventListener context) {
		int listIndex = this.listIndex.getValue(ability, tested, context);
		if (listIndex < 0) {
			return StackManager.intList;
		}
		return StackManager.SAVED_INT_LISTS.get(listIndex);
	}

	/**
	 * Return the object list to be used with this expression.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @param context
	 *          is the context attached to this test.
	 * @return the object list to be used with this expression.
	 */
	protected List<Target> getList(Ability ability, Target tested,
			ContextEventListener context) {
		final int listIndex = this.listIndex.getValue(ability, tested, context);
		if (listIndex == IdConst.ALL) {
			if (StackManager.SAVED_TARGET_LISTS.isEmpty()) {
				throw new RuntimeException(
						"Non empty saved target-list was expected ability="
								+ ability.getLog(context) + ", action=" + this);
			}
			return StackManager.SAVED_TARGET_LISTS
					.get(StackManager.SAVED_TARGET_LISTS.size() - 1);
		}
		if (listIndex < 0) {
			return StackManager.getTargetListAccess();
		}
		return StackManager.SAVED_TARGET_LISTS.get(listIndex);
	}

	/**
	 * The integer value of this expression
	 */
	protected Expression listIndex;

}
