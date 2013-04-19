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
package net.sf.firemox.modifier.model;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.test.Test;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
public interface ObjectModifierModel extends Cloneable {

	/**
	 * Create modifier(s) on the specified target.
	 * 
	 * @param ability
	 *          is the ability owning causin the creation of this modifier. The
	 *          card component of this ability should correspond to the card
	 *          owning this test too.
	 * @param target
	 *          is the card this new modifier would be attached to.
	 */
	void addModifierFromModel(Ability ability, MCard target);

	/**
	 * Creates and returns a copy of this object. The precise meaning of "copy"
	 * may depend on the class of the object.
	 * 
	 * @return a clone of this instance.
	 * @see java.lang.Cloneable
	 */
	Object clone();

	/**
	 * Remove one instance of this object from the given card.
	 * 
	 * @param fromCard
	 * @param objectTest
	 *          The test applied on specific modifier to be removed.
	 */
	void removeObject(MCard fromCard, Test objectTest);

	/**
	 * Return the object name
	 * 
	 * @return the object name
	 */
	String getObjectName();

	/**
	 * Return occurrences number of the given object with the given name.
	 * 
	 * @param card
	 *          the card on which objects will be counted.
	 * @param objectTest
	 *          The test applied on specific modifier to be removed.
	 * @return occurrences number of this object attached to the given card.
	 */
	int getNbObject(MCard card, Test objectTest);

}
