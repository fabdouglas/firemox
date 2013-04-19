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
import java.util.Arrays;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class StringMethod extends Expression {

	/**
	 * Creates a new instance of ReferenceValue <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>methodName + '\0' [...]</li>
	 * <li>nb argument [1]</li>
	 * <li>expression i [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public StringMethod(InputStream inputFile) throws IOException {
		super();
		final String methodName = MToolKit.readString(inputFile);
		arguments = new Expression[inputFile.read()];
		final Class<?>[] argumentClass = new Class[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = ExpressionFactory.readNextExpression(inputFile);
			argumentClass[i] = arguments[i].getObjectClass();
		}
		try {
			method = String.class.getMethod(methodName, argumentClass);
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalError("Error reflecting method '" + methodName + "("
					+ Arrays.toString(argumentClass) + ")'");
		}
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		if (testedString == null) {
			throw new InternalError("Tested String is null");
		}
		final Object[] values = new Object[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			values[i] = arguments[i].getObject(ability, tested, context);
		}
		try {
			return (Integer) method.invoke(testedString, values);
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalError("Error invoking method '"
					+ method.toGenericString() + "', values='" + Arrays.toString(values)
					+ "', string='" + testedString + "'");
		}
	}

	/**
	 * The method call
	 */
	private java.lang.reflect.Method method;

	/**
	 * Integer arguments of method
	 */
	private Expression[] arguments;

	/**
	 * The tested string.
	 */
	public static String testedString;
}
