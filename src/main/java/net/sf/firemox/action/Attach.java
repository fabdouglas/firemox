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

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.Attachment;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.test.TestOn;

/**
 * Attach to the first card of the list, the other cards of the target list. So
 * this action is valid only if there are at least 2 cards in the target list.
 * The last card(s) are the card(s) to attach to the first one of the target
 * list. <br>
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.91 this action does not use attachment validation test which has
 *        been moved to attachment card's member.
 */
class Attach extends UserAction implements StandardAction {

	private final TestOn from;

	private final TestOn to;

	/**
	 * Creates a new instance of Attach <br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>[super]</li>
	 * <li>from [TestOn]</li>
	 * <li>to [TestOn]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          stream containing this action.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	Attach(InputStream inputFile) throws IOException {
		super(inputFile);
		from = TestOn.deserialize(inputFile);
		to = TestOn.deserialize(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.ATTACH_LIST;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		// check action validity

		final MCard from = this.from.getCard(ability, ability.getCard());
		final MCard to = this.to.getCard(ability, ability.getCard());
		// add the components to the card
		final Attachment attachment = from.getDatabase().getCardModel()
				.getAttachment();
		if (attachment != null) {
			attachment.attach(context, ability, from, to);
		} else {
			to.add(from);
		}
		from.reverseAsNeeded();
		to.getMUI().updateLayout();
		// to.getContainer().repaint();
		return true;
	}

	@Override
	public boolean equal(MAction constraintAction) {
		return constraintAction instanceof Attach
				&& constraintAction.getIdAction() == Actiontype.ATTACH
				&& actionName != null && actionName.length() > 0
				&& actionName.equals(((Attach) constraintAction).actionName);
	}

	@Override
	public String toString(Ability ability) {
		return "attach[from/to]";
	}
}