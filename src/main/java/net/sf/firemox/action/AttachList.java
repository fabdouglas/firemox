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
import net.sf.firemox.stack.StackManager;

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
class AttachList extends UserAction implements StandardAction {

	/**
	 * Creates a new instance of AttachList <br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>idAction [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	AttachList(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.ATTACH_LIST;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		// check action validity
		if (StackManager.getInstance().getTargetedList().size() < 2) {
			throw new InternalError(
					"For the action 'attachlist' the target list must contain at least 2"
							+ " targets : the first of list is the master card. The others are"
							+ " the cards to attach to this master card.");
		}
		final MCard to = (MCard) StackManager.getInstance().getTargetedList()
				.get(0);
		// add the components to the card
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 1;) {
			final MCard card = (MCard) StackManager.getInstance().getTargetedList()
					.get(i);
			Attachment attachment = card.getDatabase().getCardModel().getAttachment();
			if (attachment != null) {
				attachment.attach(context, ability, card, to);
			} else {
				to.add(card);
			}
			card.reverseAsNeeded();
		}
		to.getMUI().updateLayout();
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "attach list";
	}
}