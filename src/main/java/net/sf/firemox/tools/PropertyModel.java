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
package net.sf.firemox.tools;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.92
 */
public class PropertyModel implements Comparable<PropertyModel> {

	/**
	 * The translated property name, is also thekey for the sort.
	 */
	public String name;

	/**
	 * The property key value.
	 */
	public int id;

	/**
	 * Creates a new instance of this class <br>
	 * 
	 * @param propertyName
	 *          the translated property name, is also thekey for the sort.
	 * @param propertyId
	 *          the property key value.
	 */
	public PropertyModel(String propertyName, int propertyId) {
		this.name = propertyName;
		this.id = propertyId;
	}

	public int compareTo(PropertyModel arg0) {
		return name.compareTo(arg0.name);
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof String) {
			return name.equals(arg0);
		} else if (arg0 instanceof PropertyModel) {
			PropertyModel pair = (PropertyModel) arg0;
			return id == pair.id;
		}
		return false;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
