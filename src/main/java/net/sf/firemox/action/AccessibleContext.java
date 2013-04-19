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
package net.sf.firemox.action;

import net.sf.firemox.clickable.target.Target;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public interface AccessibleContext {

	/**
	 * Return the shared number from the given attribute.
	 * 
	 * @param attribute
	 *          the requested target key.
	 * @return the shared number from the given attribute.
	 */
	int getAccessibleInt(String attribute);

	/**
	 * Return the shared target object from the given attribute.
	 * 
	 * @param attribute
	 *          the requested target key.
	 * @return the shared target from the given attribute.
	 */
	Target getAccessibleTargetable(String attribute);
}
