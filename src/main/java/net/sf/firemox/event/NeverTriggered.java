/*
 * Created on Sep 13, 2004 
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
package net.sf.firemox.event;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.token.IdZones;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
final class NeverTriggered extends TriggeredEvent {

	/**
	 * Creates a new instance of this class with the SystemCard instance as
	 * container.<br>
	 * 
	 * @param card
	 *          the container.
	 */
	public NeverTriggered(MCard card) {
		super(IdZones.PLAY, null, card);
	}

	@Override
	public MEventListener clone(MCard card) {
		return new NeverTriggered(card);
	}

	@Override
	public void registerToManager(Ability ability) {
		// Never register this event
	}

	@Override
	public void removeFromManager(Ability ability) {
		// Never register this event
	}

	@Override
	public boolean isWellPlaced() {
		return super.isWellPlaced();
	}

	@Override
	public Event getIdEvent() {
		return EVENT;
	}

	/**
	 * The event type.
	 */
	public static final Event EVENT = Event.NEVER_ACTIVATED;

}
