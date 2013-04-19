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
import net.sf.firemox.test.TestOn;

/**
 * Remove all copy effects from a card.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
class RestoreCard extends UserAction implements StandardAction {

	/**
	 * Create an instance of RestoreCard by reading a file Offset's file must
	 * pointing on the first byte of this action
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>card to restore [TestOn]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	RestoreCard(InputStream inputFile) throws IOException {
		super(inputFile);
		restore = TestOn.deserialize(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.RESTORE_CARD;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		MCard restore = this.restore.getCard(ability, context, null);
		if (restore.hasDirtyDataBase()) {
			restore.setDataBase(restore.getOriginalDatabase());
		}
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "restore card";
	}

	/**
	 * The card to restore.
	 */
	private final TestOn restore;

}