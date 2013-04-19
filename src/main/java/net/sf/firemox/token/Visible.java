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
package net.sf.firemox.token;

import net.sf.firemox.clickable.target.player.Player;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
public interface Visible {

	/**
	 * Increase visibility of this component for the given player.
	 * 
	 * @param player
	 *          The controller of component to modify.
	 * @param visibilityChange
	 *          The visibility change.
	 */
	void increaseFor(Player player, VisibilityChange visibilityChange);

	/**
	 * Decrease visibility of this component for a given player.
	 * 
	 * @param player
	 *          The controller of component to modify.
	 * @param visibilityChange
	 *          The visibility change.
	 */
	void decreaseFor(Player player, VisibilityChange visibilityChange);

	/**
	 * Is the component is visible for the controller.
	 * 
	 * @return true if the component is visible for the controller.
	 */
	boolean isVisibleForYou();

	/**
	 * Is the component is visible for the controller.
	 * 
	 * @return true if the component is visible for the controller.
	 */
	boolean isVisibleForOpponent();

	/**
	 * Return the visibility of this component.
	 * 
	 * @return the visibility of this component.
	 */
	Visibility getVisibility();

	/**
	 * Set the visibility of this component.
	 * 
	 * @param visibility
	 *          the new visibility
	 */
	void setVisibility(Visibility visibility);

	/**
	 * Restore the previous visibility of this component.
	 * 
	 */
	void restoreVisibility();

	/**
	 * Set the visibility of this component.
	 * 
	 * @param visibility
	 *          the new visibility
	 */
	void restoreVisibility(Visibility visibility);
}
