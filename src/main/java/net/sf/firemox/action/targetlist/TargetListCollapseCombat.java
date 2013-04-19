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
import java.util.LinkedList;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
class TargetListCollapseCombat extends TargetList {

	/**
	 * Create an instance of TargetListCollapseCombat by reading a file Offset's
	 * file must pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>nb attacking group : Expression [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	TargetListCollapseCombat(InputStream inputFile) throws IOException {
		super(inputFile);
		nbGroup = ExpressionFactory.readNextExpression(inputFile);
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		int nbGroup = this.nbGroup.getValue(ability, null, context);
		final int listIndex = getlistIndex(ability, context);
		/**
		 * <ul>
		 * From this point the saved target lists are : Content[index]
		 * <li>first group of attacking creatures [listIndex]</li>
		 * <li>second group of attacking creatures [listIndex+1]</li>
		 * <li>n th group of attacking creatures [listIndex+n]</li>
		 * <li>last group of attacking creatures [listIndex+nb-group-1]</li>
		 * <li>list of pair of {attacking,blocking} [listIndex+nb-group]</li>
		 * </ul>
		 */
		int oldTop = StackManager.SAVED_TARGET_LISTS.size();
		for (int i = listIndex; i < listIndex + nbGroup; i++) {
			final List<Target> attackGroup = StackManager.SAVED_TARGET_LISTS.get(i);
			final List<Target> associatedGroup = new LinkedList<Target>();
			for (int j = attackGroup.size(); j-- > 0;) {
				final MCard attacking = (MCard) attackGroup.get(j);
				// search associated card of this attacking card in the association
				for (int k = listIndex + nbGroup; k < oldTop; k++) {
					final List<Target> pairList = StackManager.SAVED_TARGET_LISTS.get(k);
					if (pairList.get(0) == attacking) {
						associatedGroup.add(pairList.get(pairList.size() - 1));
					}
				}
			}
			StackManager.SAVED_TARGET_LISTS.add(attackGroup);
			StackManager.SAVED_TARGET_LISTS.add(associatedGroup);
		}

		// remove the previous configuration
		while (oldTop-- > listIndex) {
			StackManager.SAVED_TARGET_LISTS.remove(listIndex);
		}

		/**
		 * <ul>
		 * From this point the saved target lists are : Content[index]
		 * <li>first group of attacking creatures [listIndex]</li>
		 * <li>first group of blocking creaturess [listIndex+1]</li>
		 * <li>second group of attacking creatures [listIndex+2]</li>
		 * <li>second group of blocking creatures [listIndex+3]</li>
		 * <li>n th group of blocking creatures [listIndex+n*2-1]</li>
		 * <li>n th group of attacking creatures [listIndex+n*2]</li>
		 * <li>last group of attacking creatures [listIndex+nb-group*2-1]</li>
		 * <li>last group of blocking creatures [listIndex+nb-group*2]</li>
		 * </ul>
		 */
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "target-list.collapse-combat";
	}

	/**
	 * Number of group of attacking creatures.
	 */
	private Expression nbGroup;

}
