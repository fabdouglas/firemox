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
package net.sf.firemox.modifier;

import java.awt.Graphics;

import net.sf.firemox.test.Test;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public interface ObjectModifier {

	/**
	 * remove from the manager a occurrence of object with the specified name
	 * 
	 * @param objectName
	 *          the object's name to remove
	 * @param objectTest
	 *          The test applied on specific modifier to be removed.
	 * @return the new chain
	 */
	Modifier removeObject(String objectName, Test objectTest);

	/**
	 * Paint all objects of the given MCard component.
	 * 
	 * @param g
	 *          the graphics used ot paint this object.
	 * @param startX
	 *          the x position where this object can be paint.
	 * @param startY
	 *          the y position where this object can be paint.
	 * @return the next free object position.
	 */
	int paintObject(Graphics g, int startX, int startY);

	/**
	 * Return occurrences number of the given object with the given name.
	 * 
	 * @param objectName
	 *          the object's name to find within this chain.
	 * @param objectTest
	 *          The test applied on specific modifier to be removed.
	 * @return occurrences number of the given object with the given name.
	 */
	int getNbObjects(String objectName, Test objectTest);

}
