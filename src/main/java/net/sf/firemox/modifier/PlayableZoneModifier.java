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
package net.sf.firemox.modifier;

import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public class PlayableZoneModifier extends Modifier {

	/**
	 * Creates a new instance of PlayableZoneModifier <br>
	 * 
	 * @param context
	 *          the modifier context.
	 * @param idZone
	 *          the zone to add/remove as playable zone
	 * @param hasNot
	 *          Indicates if this modifier remove occurrence of the given zone. If
	 *          False, it adds one instance. modifier.
	 */
	public PlayableZoneModifier(ModifierContext context, int idZone,
			boolean hasNot) {
		super(context);
		this.idZone = idZone;
		this.hasNot = hasNot;
	}

	/**
	 * Indicates if this modifier allow the specified zone
	 * 
	 * @param idZone
	 *          The previous idCard.
	 * @param found
	 *          indicates that this zone has been previously allowed
	 * @return the modified idCard.
	 */
	public boolean playableIn(int idZone, boolean found) {
		if (activated) {
			if (this.idZone == idZone) {
				return !hasNot;
			}
			if (next != null) {
				return ((PlayableZoneModifier) next).playableIn(idZone, found);
			}
			return found;
		}
		if (next != null) {
			return ((PlayableZoneModifier) next).playableIn(idZone, found);
		}
		return found;
	}

	@Override
	public Modifier removeModifier(Modifier modifier) {
		if (this == modifier) {
			if (activated) {
				StackManager.postRefreshIdCard(to);
			}
			return next;
		}
		return super.removeModifier(modifier);
	}

	@Override
	public final void removeFromManager() {
		super.removeFromManager();
		if (!unregisteredModifier) {
			to.removeModifier(this);
		}
		unregisteredModifier = true;
	}

	@Override
	public void refresh() {
		boolean oldActivated = activated;
		activated = whileCondition.test(ability, to);

		// this card type identifier has changed
		if (oldActivated != activated) {
			to.updateAbilities();
		}
	}

	/**
	 * The zone to add/remove as playable zone
	 */
	private int idZone;

	/**
	 * Indicates if this modifier remove occurrence of the given zone. If False,
	 * it adds one instance.
	 */
	private boolean hasNot;
}