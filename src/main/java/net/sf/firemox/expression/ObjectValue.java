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
package net.sf.firemox.expression;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.tools.MToolKit;

/**
 * This class allows to statically declare an object value, with the class name
 * given as the first string parameter of the input stream and the string
 * argument to pass to the constructor of the given class as the second string
 * parameter of the input stream.
 * 
 * @author <a href="mailto:hoani.cross@gmail.com">Hoani Cross</a>
 * @since 0.90
 */
public class ObjectValue extends Expression {

	/**
	 * The constructor used to compute the string value.
	 */
	private Constructor<?> constructor;

	/**
	 * The argument to pass the constructor of the class named by the
	 * <code>className</code> member while instanciating the object value of
	 * this expression.
	 */
	private String strValue;

	/**
	 * Creates a new instance of the <code>ObjectValue</code> class.<br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>class name + '\0' [...]</li>
	 * <li>string value [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public ObjectValue(InputStream inputFile) throws IOException {
		super();
		try {
			Class<?> clazz = Class.forName(MToolKit.readString(inputFile));

			if (clazz != String.class) {
				constructor = clazz.getConstructor(String.class);
				// else No need to create the String(String) constructor
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		strValue = MToolKit.readString(inputFile);
	}

	@Override
	public Object getObject(Ability ability, Target tested,
			ContextEventListener context) {
		if (constructor == null) {
			return strValue;
		}
		try {
			return constructor.newInstance(strValue);
		} catch (Exception e) {
			throw new InternalError("Error while creating a new instance of the ["
					+ constructor.getName() + "] class with the argument [" + strValue
					+ "]");
		}
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		if (getObjectClass() == Integer.class || getObjectClass() == int.class) {
			return ((Integer) getObject(ability, tested, context)).intValue();
		}

		// Try to transform the String or any other object to Integer
		try {
			return Integer.parseInt(getObject(ability, tested, context).toString());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public Class<?> getObjectClass() {
		return constructor == null ? String.class : constructor.getDeclaringClass();
	}

}
