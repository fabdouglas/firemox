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
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;

/**
 * Remove from the current target-list or a saved one, the targets making
 * <code>false</code> the given test.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
class TargetListFilter extends TargetList {

	/**
	 * Create an instance of TargetListFilter by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>filter test [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	TargetListFilter(InputStream inputFile) throws IOException {
		super(inputFile);
		filter = TestFactory.readNextTest(inputFile);
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		final int listIndex = getlistIndex(ability, context);
		final List<Target> listToFilter;
		if (listIndex < 0) {
			listToFilter = StackManager.getTargetListAccess();
		} else {
			listToFilter = StackManager.SAVED_TARGET_LISTS.get(listIndex);
		}
		for (int i = listToFilter.size(); i-- > 0;) {
			if (!filter.test(ability, listToFilter.get(i))) {
				listToFilter.remove(i);
			}
		}
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "target-list.filter";
	}

	/**
	 * The filter sued to remove non-matching target of the target list.
	 */
	private Test filter;

}
