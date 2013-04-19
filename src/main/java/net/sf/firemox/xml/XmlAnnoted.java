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
package net.sf.firemox.xml;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.firemox.annotation.XmlTestElement;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
public abstract class XmlAnnoted implements XmlToMDB {

	/**
	 * XmlElement associated to this builder
	 */
	protected XmlTestElement xmlElement;

	/**
	 * The annoted class
	 */
	protected Class<?> annotedClass;

	/**
	 * Set XmlElement associated to this builder
	 * 
	 * @param xmlElement
	 *          XmlElement associated to this builder.
	 */
	public void setXmlElement(XmlTestElement xmlElement) {
		this.xmlElement = xmlElement;
	}

	/**
	 * Set the annoted class.
	 * 
	 * @param annotedClass
	 *          the annoted class.
	 */
	public void setAnnotedClass(Class<?> annotedClass) {
		this.annotedClass = annotedClass;
	}

	public abstract int buildMdb(XmlParser.Node node, OutputStream out)
			throws IOException;
}
