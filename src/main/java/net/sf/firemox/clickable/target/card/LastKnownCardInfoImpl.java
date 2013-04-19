/*
 * Created on 25 mars 2005
 * 
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
package net.sf.firemox.clickable.target.card;

import java.util.HashSet;
import java.util.Set;

import net.sf.firemox.clickable.target.player.Player;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public final class LastKnownCardInfoImpl implements LastKnownCardInfo {

	@SuppressWarnings("unchecked")
	LastKnownCardInfoImpl(MCard originalCard, int timestampReferences,
			int destinationZone) {
		assert timestampReferences > 0;
		this.originalCard = originalCard;
		this.destinationZone = destinationZone;
		this.idColor = originalCard.getIdColor();
		this.idCard = originalCard.getIdCard();
		this.tapped = originalCard.tapped;
		this.cachedRegisters = originalCard.cachedRegisters.clone();
		this.cachedProperties = (Set<Integer>) ((HashSet<Integer>) originalCard.cachedProperties)
				.clone();

		this.controller = originalCard.controller;
		this.owner = originalCard.owner;
		this.timestamp = originalCard.getTimestamp();
		this.timestampReferences = timestampReferences;
	}

	/**
	 * Create and return a LastKnownCard object build with the last known
	 * informations.
	 * 
	 * @return a LastKnownCard object build with the last known informations.
	 */
	public LastKnownCard createLastKnownCard() {
		return new LastKnownCard(originalCard, destinationZone, idColor, idCard,
				tapped, cachedRegisters, controller, owner, cachedProperties,
				timestamp, timestampReferences);
	}

	/**
	 * Remove a reference to the given timestamp of card.
	 * 
	 * @param timestamp
	 *          the timestamp reference
	 * @return true if the given timestamp is no more referenced.
	 */
	public boolean removeTimestamp(int timestamp) {
		return --timestampReferences <= 0;
	}

	/**
	 * The last known colors.
	 */
	private int idColor;

	/**
	 * The last known card identifier.
	 */
	private int idCard;

	/**
	 * The last known registers.
	 */
	private int[] cachedRegisters;

	/**
	 * The last known controller.
	 */
	private Player controller;

	/**
	 * The last known owner.
	 */
	private Player owner;

	/**
	 * The last known properties.
	 */
	private Set<Integer> cachedProperties;

	/**
	 * The last known information on tap state.
	 */
	private boolean tapped;

	/**
	 * The original card.
	 */
	private MCard originalCard;

	/**
	 * The timestamp of card when it has been saved.
	 */
	private int timestamp;

	/**
	 * The amount of references to the original card with the specific timestamp.
	 */
	private int timestampReferences;

	/**
	 * The destination zone of this card before it moved.
	 */
	private int destinationZone;
}
