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
package net.sf.firemox.zone;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public class SectorConfiguration {

	/**
	 * The sector test.
	 */
	private final Test test;

	/**
	 * The container constraint of this sector.
	 */
	private final Object constraint;

	/**
	 * Create a new instance of this class.
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>sector test [Test]</li>
	 * <li>constraint [String]</li>
	 * </ul>
	 * 
	 * @param inputStream
	 *          the stream containing the layout configuration of this zone for a
	 *          specified layout
	 * @throws IOException
	 *           error during the configuration read.
	 */
	SectorConfiguration(InputStream inputStream) throws IOException {
		test = TestFactory.readNextTest(inputStream);
		constraint = MToolKit.readString(inputStream);
	}

	/**
	 * The container constraint of this sector.
	 * 
	 * @return The container constraint of this sector.
	 */
	public Object getConstraint() {
		return this.constraint;
	}

	/**
	 * The sector test.
	 * 
	 * @return The sector test.
	 */
	public Test getTest() {
		return this.test;
	}

}
