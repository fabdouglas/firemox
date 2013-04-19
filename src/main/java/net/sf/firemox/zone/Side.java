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
package net.sf.firemox.zone;

import java.awt.FlowLayout;

import javax.swing.JScrollPane;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.token.IdZones;

/**
 * Represents the side zone.
 * 
 * @author Fabrice Daugan
 * @since 0.2d
 * @since 0.3 feature "reverseImage" implemented
 * @since 0.4 you can now change wallpaper/color of this MZone and setting are
 *        saved.
 */
public class Side extends MZone {

	/**
	 * The zone name.
	 */
	public static final String ZONE_NAME = "side";

	/**
	 * create a new instance of Side
	 * 
	 * @param superPanel
	 *          scroll panel containing this panel
	 * @param reverseImage
	 *          if true the back picture will be reversed
	 * @since 0.3 feature "reverseImage" implemented
	 * @see IdZones
	 */
	Side(JScrollPane superPanel) {
		super(IdZones.SIDE, new FlowLayout(), superPanel, false, ZONE_NAME);
	}

	/**
	 * Add a card at the top of this panel. If tag 'returnedCards' is true, this
	 * card comes returned into this zone. Be careful, you can use this function
	 * only if the specified component is not yet in this container.
	 * 
	 * @param card
	 *          the card to add to this zone.
	 */
	@Override
	public void addTop(MCard card) {
		addBottom(card);
	}

	@Override
	public boolean isMustBePaintedReversed(MCard card) {
		return false;
	}

}