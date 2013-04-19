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
package net.sf.firemox.test;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.BooleanOption;
import net.sf.firemox.token.IdConst;

/**
 * Test the target list conatins a given object.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 */
class TargetListContains extends TestCard {

	/**
	 * Create an instance of this class from an input stream<br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>used target to search [TestOn]
	 * <li>is the 'raise-event' option must be set [Boolean]
	 * <li>container of used target-list [TestOn]
	 * </ul>
	 * 
	 * @param input
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	TargetListContains(InputStream input) throws IOException {
		super(input);
		input.read();
		listIndex = ExpressionFactory.readNextExpression(input);
		raisedEvent = BooleanOption.deserialize(input);
		container = TestOn.deserialize(input);
	}

	@Override
	protected boolean testCard(Ability ability, MCard tested) {
		int listIndex = this.listIndex.getValue(ability, tested, null);
		if (listIndex == IdConst.ALL) {
			listIndex = StackManager.SAVED_TARGET_LISTS.size() - 1;
		}
		if (listIndex < 0) {
			return StackManager.getContextOf(container.getCard(ability, tested))
					.getTargetedList().contains(on.getTargetable(ability, tested),
							raisedEvent.getValue());
		}
		return StackManager.SAVED_TARGET_LISTS.get(listIndex).contains(
				on.getTargetable(ability, tested));
	}

	/**
	 * The index of saved target list to use for this test
	 */
	private final Expression listIndex;

	/**
	 * The 'raise-event' constraint.
	 */
	private final BooleanOption raisedEvent;

	/**
	 * Container the search would be done.
	 */
	private final TestOn container;
}