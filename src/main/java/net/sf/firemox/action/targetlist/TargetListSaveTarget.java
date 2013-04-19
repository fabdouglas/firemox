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
package net.sf.firemox.action.targetlist;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.TestOn;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
class TargetListSaveTarget extends TargetListSave {

	/**
	 * Create an instance of TargetListSaveTarget by reading a file Offset's file
	 * must pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>index [Expression]</li>
	 * <li>target [TestOn]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	TargetListSaveTarget(InputStream inputFile) throws IOException {
		super(inputFile);
		index = ExpressionFactory.readNextExpression(inputFile);
		on = TestOn.deserialize(inputFile);
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		final int listIndex = getlistIndex(ability, context);
		final int index = this.index.getValue(ability, null, context);
		final Target target = on.getTargetable(ability, context, null);
		if (listIndex < 0) {
			if (index < 0) {
				StackManager.getTargetListAccess().add(target);
			} else {
				StackManager.getTargetListAccess().add(index, target);
			}
		} else {
			if (index < 0) {
				StackManager.SAVED_TARGET_LISTS.get(listIndex).add(target);
			} else {
				StackManager.SAVED_TARGET_LISTS.get(listIndex).add(index, target);
			}
		}
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "target-list.save-target";
	}

	/**
	 * The target to remove.
	 */
	private TestOn on;

	/**
	 * The index of target to save.
	 */
	private Expression index;

}
