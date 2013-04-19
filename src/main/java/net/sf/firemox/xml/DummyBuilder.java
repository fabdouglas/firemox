/*
 * Created on 28 mars 2005
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
package net.sf.firemox.xml;

import java.io.OutputStream;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public final class DummyBuilder implements XmlToMDB {

	/**
	 * Prevent this class to be instanciated
	 */
	private DummyBuilder() {
		super();
	}

	public int buildMdb(XmlParser.Node node, OutputStream out) {
		return 0;
	}

	private static DummyBuilder instance = new DummyBuilder();

	/**
	 * Return unique instance of this class.
	 * 
	 * @return unique instance of this class.
	 */
	public static XmlToMDB instance() {
		return instance;
	}
}
