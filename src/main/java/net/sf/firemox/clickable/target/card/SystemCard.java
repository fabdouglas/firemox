/*   Firemox is a turn based strategy simulator
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
package net.sf.firemox.clickable.target.card;

import java.awt.event.ActionEvent;

import javax.swing.JTabbedPane;

import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.TargetFactory;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.zone.ExpandableZone;
import net.sf.firemox.zone.MZone;

/**
 * MSystemCard.java Created on 5 mars 2004
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 */
public final class SystemCard extends MCard {

	/**
	 * The unique instance of this class
	 */
	public static SystemCard instance = new SystemCard();

	/**
	 * Create a new instance of this class. Private status is there to prevent
	 * user to instantiate this class
	 */
	private SystemCard() {
		super();
		this.database = DatabaseFactory.getDatabase(null, CardFactory.getCardModel(
				"system", null), null);
		idZone = IdZones.PLAY;
	}

	@Override
	public Player getController() {
		return StackManager.activePlayer();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		final String action = evt.getActionCommand();
		final MZone container = TargetFactory.triggerTargetable.getContainer();
		if (action == CardFactory.STR_GATHER || action == CardFactory.STR_EXPAND) {
			// expand/gather container
			((ExpandableZone) container).toggle();
			// } else if (evt.getSource() == CardFactory.javaDebugItem) {
			// TODO java debug option
		} else if (evt.getSource() == CardFactory.databaseCardInfoItem) {
			((JTabbedPane) MagicUIComponents.databasePanel.getParent())
					.setSelectedComponent(MagicUIComponents.databasePanel);
		} else if (evt.getSource() == CardFactory.reloadPictureItem) {
			TargetFactory.triggerTargetable.getDatabase().invalidateImage();
			TargetFactory.triggerTargetable.repaint();
		}
	}

	@Override
	public void decrementTimestampReference(int timestamp) {
		// Nothing to do
	}

	@Override
	public void addTimestampReference() {
		// Nothing to do
	}

	@Override
	public Target getLastKnownTargetable(int timeStamp) {
		return this;
	}

	@Override
	public String toString() {
		return "system";
	}
}