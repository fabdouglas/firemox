/*
 * Created on Jul 14, 2004 
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
package net.sf.firemox.clickable.ability;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.sf.firemox.clickable.target.card.TriggeredCard;
import net.sf.firemox.event.MEventListener;

/**
 * Possible optimizations for abilities management in the TBZ <br>
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public enum Priority {

	/**
	 * As HIDDEN tag, but the ability is chosen before the other hidden abilities
	 * without this tag .
	 */
	hidden_high,

	/**
	 * Indicates whether this ability is resolved completely before player can get
	 * priority (like isAutoResolve), and stack is resolved immediately after it
	 * has been added to the stack. No information displayed or prompted to the
	 * players. Users will not see this ability played.
	 */
	hidden,

	/**
	 * As hidden tag, but the ability is chosen after the other hidden abilities
	 * without this tag .
	 */
	hidden_low,

	/**
	 * Indicates whether this ability is chosen in priority to the others without
	 * this tag.
	 */
	high,

	/**
	 * Indicates whether this ability is resolved completely before player can get
	 * priority.
	 */
	auto,

	/**
	 * Indicates the ability would be resolved depending on the current flow.
	 * Often, need user intervention to be played and resolved.
	 */
	normal;

	/**
	 * Indicates whether this ability is chosen in priority to the others without
	 * this tag.
	 * 
	 * @return true if this ability is chosen in priority to the others without
	 *         this tag.
	 */
	public boolean hasHighPriority() {
		return this == high || isHidden();
	}

	/**
	 * Indicates if this ability is immediately after it has been added to the
	 * stack. Note it's not says immediately it has been triggered or playable,
	 * but says it has been activated - so added directly to the stack -, or has
	 * been triggered - so added to the triggered buffer zone - and then has been
	 * selected to be moved to the stack.
	 * 
	 * @return true if this ability is immediately after it has been added to the
	 *         stack.
	 */
	public boolean isAutoResolve() {
		return this == auto || isHidden();
	}

	/**
	 * Indicates whether this ability is resolved completely before player can get
	 * priority (like isAutoResolve), and stack is resolved immediately after it
	 * has been added to the stack. No information displayed or prompted to the
	 * players. Users will not see this ability played.
	 * 
	 * @return true if this ability is resolved completely before player can get
	 *         priority (like isAutoResolve), and stack is resolved immediately
	 *         after it has been added to the stack.
	 */
	public boolean isHidden() {
		return this == hidden_high || this == hidden || this == hidden_low;
	}

	/**
	 * Unregister the specified replacement ability.
	 * 
	 * @param ability
	 *          the replacement ability to unregister.
	 */
	public void removeFromManager(ReplacementAbility ability) {
		MEventListener.REPLACEMENT_ABILITIES
				.get(ability.eventComing().getIdEvent()).get(this).remove(ability);
	}

	/**
	 * Following the class, returns the abstract zone where the specified ability
	 * should be added.
	 * 
	 * @param abstractLowestZone
	 *          the abstract zone corresponding to the zone containing abilities
	 *          with the lowest priority of hidden abilities.
	 * @param abstractZone
	 *          the abstract zone corresponding to the zone containing abilities
	 *          with the normal priority of hidden abilities.
	 * @param abstractHighestZone
	 *          the abstract zone corresponding to the zone containing abilities
	 *          with the highest priority of hidden abilities.
	 * @return the abstract zone where the specified ability should be added.
	 */
	public List<TriggeredCard> getAbstractZone(
			List<TriggeredCard> abstractLowestZone, List<TriggeredCard> abstractZone,
			List<TriggeredCard> abstractHighestZone) {
		switch (this) {
		case hidden_high:
			return abstractHighestZone;
		case hidden:
			return abstractZone;
		case hidden_low:
			return abstractLowestZone;
		default:
			throw new InternalError("should not be called");
		}
	}

	/**
	 * Register the specified replacement ability.
	 * 
	 * @param ability
	 *          the replacement ability to register.
	 */
	public void registerToManager(ReplacementAbility ability) {
		final List<ReplacementAbility> replacementAbilities = MEventListener.REPLACEMENT_ABILITIES
				.get(ability.eventComing().getIdEvent()).get(this);
		if (!replacementAbilities.contains(ability)) {
			replacementAbilities.add(ability);
		}
	}

	/**
	 * Write this enumeration to the given output stream.
	 * 
	 * @param out
	 *          the stream this enumeration would be written.
	 * @throws IOException
	 */
	public void write(OutputStream out) throws IOException {
		out.write(ordinal());
	}

	/**
	 * Read and return the enumeration from the given input stream.
	 * 
	 * @param input
	 *          the stream containing the enumeration to read.
	 * @return the enumeration from the given input stream.
	 * @throws IOException
	 */
	static Priority valueOf(InputStream input) throws IOException {
		return values()[input.read()];
	}
}