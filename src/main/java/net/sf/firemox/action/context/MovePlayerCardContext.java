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
package net.sf.firemox.action.context;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.zone.MZone;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class MovePlayerCardContext implements ActionContext {

	/**
	 * Create a new context with a moved card list and source zone.
	 * 
	 * @param size
	 *          the array's size of this context.
	 */
	public MovePlayerCardContext(int size) {
		this.controllers = new Player[size];
		this.tapPosition = new boolean[size];
		this.cards = new MCard[size];
		this.srcZones = new MZone[size];
	}

	/**
	 * The previous 'tap' position.
	 */
	public final boolean[] tapPosition;

	/**
	 * The previous controller.
	 */
	public final Player[] controllers;

	/**
	 * The previous controller.
	 */
	public final MCard[] cards;

	/**
	 * The previous zones.
	 */
	public final MZone[] srcZones;

}
