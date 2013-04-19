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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import net.sf.firemox.tools.Log;

import org.apache.commons.collections.CollectionUtils;

/**
 * Card properties operation returning integer.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
public enum CardPropertiesOperation {

	/**
	 * Intersection size.
	 */
	INTERSECTION_SIZE("intersection.size"),

	/**
	 * Disjonction size.
	 */
	DISJONCTION_SIZE("disjonction.size"),

	/**
	 * Union size.
	 */
	UNION_SIZE("union.size");

	/**
	 * Human readable name.
	 */
	private final String xsdName;

	/**
	 * Private constructor with real name. Create a new instance of this class.
	 * 
	 * @param xsdName
	 *          Human readable name.
	 */
	private CardPropertiesOperation(String xsdName) {
		this.xsdName = xsdName;
	}

	/**
	 * Wrtite this enum to the given output stream.
	 * 
	 * @param out
	 *          the stream ths enum would be written.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public void serialize(OutputStream out) throws IOException {
		out.write(ordinal());
	}

	/**
	 * Read and return the enum from the given input stream.
	 * 
	 * @param input
	 *          the stream containing the enum to read.
	 * @return the enum from the given input stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static CardPropertiesOperation deserialize(InputStream input)
			throws IOException {
		return values()[input.read()];
	}

	/**
	 * Return null of enum value corresponding to the given Xsd name.
	 * 
	 * @param xsdName
	 *          the Xsd name of this Aabstract value.
	 * @return null of enum value corresponding to the given Xsd name.
	 */
	public static CardPropertiesOperation valueOfXsd(String xsdName) {
		for (CardPropertiesOperation value : values()) {
			if (value.xsdName.equals(xsdName)) {
				return value;
			}
		}
		return null;
	}

	/**
	 * Return the result operation result applied on the given properties.
	 * 
	 * @param properties
	 *          first properties set.
	 * @param properties2
	 *          second properties set.
	 * @param lower
	 *          The minimal property value included used by the operation.
	 * @param higher
	 *          The maximal property value included used by the operation.
	 * @return the result operation result applied on the given properties.
	 */
	@SuppressWarnings("unchecked")
	public int getValue(Set<Integer> properties, Set<Integer> properties2,
			int lower, int higher) {
		final Collection<Integer> workingSet;
		switch (this) {
		case INTERSECTION_SIZE:
			workingSet = CollectionUtils.intersection(properties, properties2);
			break;
		case UNION_SIZE:
			workingSet = CollectionUtils.union(properties, properties2);
			break;
		case DISJONCTION_SIZE:
			workingSet = CollectionUtils.disjunction(properties, properties2);
			break;
		default:
			Log.fatal("Unhandled CardPropertiesOperation +" + this);
			workingSet = new ArrayList<Integer>();
		}
		int count = workingSet.size();
		for (Integer value : workingSet) {
			if (value < lower || higher > value) {
				count--;
			}
		}
		return count;
	}

}
