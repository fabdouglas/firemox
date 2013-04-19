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
import net.sf.firemox.tools.Pair;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class MoveContext implements ActionContext {

	/**
	 * Create a new context with a new boolean array
	 * 
	 * @param size
	 *          the array's size of this context.
	 */
	@SuppressWarnings("unchecked")
	public MoveContext(int size) {
		this.controllers = new Player[size];
		this.idZones = new int[size];
		this.tapPosition = new boolean[size];
		this.indexes = new Pair[size];
		this.attachedTo = new MCard[size];
	}

	/**
	 * The previous controller.
	 */
	public final Player[] controllers;

	/**
	 * The previous attachedTo.
	 */
	public final MCard[] attachedTo;

	/**
	 * The previous zone.
	 */
	public final int[] idZones;

	/**
	 * The previous 'tap' position.
	 */
	public final boolean[] tapPosition;

	/**
	 * The indexes within the zone.
	 */
	public Pair<Integer, Integer>[] indexes;
}
