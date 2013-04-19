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
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.MToolKit;

/**
 * Create a new cardToAdd and add it to the current target list.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.71
 * @since 0.90 "creator" property is added to the created card.
 */
class CreateCard extends UserAction implements StandardAction {

	/**
	 * Create an instance of CreateCard by reading a file Offset's file must
	 * pointing on the first byte of this action
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>cardToAdd [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	CreateCard(InputStream inputFile) throws IOException {
		super(inputFile);
		cardToAdd = new MCard("_" + MToolKit.readString(inputFile), inputFile,
				StackManager.currentPlayer(), StackManager.currentPlayer(), null);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.CREATE_CARD;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		final MCard cloned = new MCard(cardToAdd, cardToAdd.getDatabase());
		cloned.setOwner(StackManager.getSpellController());
		cloned.setCreator(ability.getCard());
		StackManager.getTargetListAccess().add(cloned);
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "Create cardToAdd";
	}

	/**
	 * The card to add
	 */
	private MCard cardToAdd;
}