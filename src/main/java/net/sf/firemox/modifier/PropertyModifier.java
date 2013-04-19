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

import java.util.Set;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdConst;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86 properties set is calculated as colors/idcard and register : they
 *        follow timestamp rule.
 */
public class PropertyModifier extends Modifier {

	/**
	 * Creates a new instance of PropertyModifier without any chain<br>
	 * 
	 * @param context
	 *          the modifier context.
	 * @param propertyId
	 *          the property to add/remove
	 * @param addProperty
	 *          Indicates if this modifier remove occurrence of the given
	 *          property. If true, it adds one instance.
	 */
	public PropertyModifier(ModifierContext context, Expression propertyId,
			boolean addProperty) {
		super(context);
		this.propertyId = propertyId;
		this.addProperty = addProperty;
	}

	@Override
	public Modifier removeModifier(Modifier modifier) {
		if (this == modifier) {
			if (activated) {
				StackManager.postRefreshProperties(to, propertyId.getValue(ability, to,
						null));
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

	/**
	 * Tells if this modifier add/remove the given property.
	 * 
	 * @param propertyId
	 *          the matched property
	 * @param found
	 *          indicates that the property has been previously found.
	 * @return true if this modifier add/remove the given property.
	 */
	public boolean hasProperty(int propertyId, boolean found) {
		if (activated) {
			final int newPropertyId = this.propertyId.getValue(ability, to, null);
			if (newPropertyId == IdConst.ALL && !addProperty) {
				if (next != null) {
					return ((PropertyModifier) next).hasProperty(propertyId, false);
				}
				return false;
			}
			if (newPropertyId == propertyId) {
				if (next != null) {
					return ((PropertyModifier) next).hasProperty(propertyId, addProperty);
				}
				return addProperty;
			}
		}
		if (next != null) {
			return ((PropertyModifier) next).hasProperty(propertyId, found);
		}
		return found;
	}

	/**
	 * Tells if this modifier add/remove the given property ignoring these given
	 * by the specified card <code>creator</code>.
	 * 
	 * @param propertyId
	 *          the matched property
	 * @param found
	 *          indicates that the property has been previously found
	 * @param creator
	 *          is the card created modifiers would be ignored
	 * @return true if this modifier add/remove the given property ignoring these
	 *         given by the specified card <code>creator</code>.
	 */
	public boolean hasPropertyNotFromCreator(int propertyId, boolean found,
			MCard creator) {
		if (activated && creator != this.ability.getCard()) {
			int newPropertyId = this.propertyId.getValue(ability, to, null);
			if (newPropertyId == IdConst.ALL && !addProperty) {
				if (next != null) {
					return ((PropertyModifier) next).hasPropertyNotFromCreator(
							propertyId, addProperty, creator);
				}
				return false;
			}
			if (newPropertyId == propertyId) {
				if (next != null) {
					return ((PropertyModifier) next).hasPropertyNotFromCreator(
							propertyId, addProperty, creator);
				}
			}
			return addProperty;
		}
		if (next != null) {
			return ((PropertyModifier) next).hasPropertyNotFromCreator(propertyId,
					found, creator);
		}
		return found;
	}

	/**
	 * Add/remove the specified set by the manipulated properties of this
	 * modifier.
	 * 
	 * @param workSet
	 *          the set of properties already found
	 */
	public void fillProperties(Set<Integer> workSet) {
		if (activated) {
			if (addProperty) {
				workSet.add(propertyId.getValue(ability, to, null));
			} else {
				final int propertyId = this.propertyId.getValue(ability, to, null);
				if (IdConst.ALL == propertyId)
					workSet.clear();
				else {
					workSet.remove(propertyId);
				}
			}
		}
		if (next != null) {
			((PropertyModifier) next).fillProperties(workSet);
		}
	}

	@Override
	public void refresh() {
		final boolean oldActivated = activated;
		activated = whileCondition.test(ability, to);

		// this property has changed
		if (oldActivated != activated) {
			StackManager.postRefreshProperties(to, propertyId.getValue(ability, to,
					null));
		}
	}

	/**
	 * Indicates if this modifier remove occurrence of the given property. If
	 * True, it adds one instance of this property.
	 */
	protected boolean addProperty;

	/**
	 * Property to add/remove
	 */
	protected Expression propertyId;

}