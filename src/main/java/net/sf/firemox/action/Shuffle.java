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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;
import net.sf.firemox.zone.ZoneManager;

/**
 * Shuffle the zone of targeted player(s). Use the target list to know
 * player(s).
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
class Shuffle extends UserAction implements StandardAction {

	/**
	 * Create an instance of CountCard by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idAction [1]</li>
	 * <li>idZone [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	Shuffle(InputStream inputFile) throws IOException {
		super(inputFile);
		idZone = inputFile.read();
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.SHUFFLE;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		for (int p = StackManager.getInstance().getTargetedList().size(); p-- > 0;) {
			if (StackManager.getInstance().getTargetedList().get(p).isPlayer()) {
				((Player) StackManager.getInstance().getTargetedList().get(p)).zoneManager
						.getContainer(idZone).shuffle();
			}
		}
		return true;
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		if (actionName == null) {
			return super.toHtmlString(ability, context);
		}
		if (actionName.charAt(0) == '%') {
			return "";
		}
		// we return only the string representation
		return LanguageManagerMDB.getString("shuffle", LanguageManagerMDB
				.getString("zone." + ZoneManager.getZoneName(idZone)));
	}

	@Override
	public String toString(Ability ability) {
		return "shuffle zone " + idZone;
	}

	/**
	 * represents the place to shuffle
	 */
	private int idZone;

}