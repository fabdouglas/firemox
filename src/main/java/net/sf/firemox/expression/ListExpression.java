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
package net.sf.firemox.expression;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.intlist.ListType;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.92
 */
public class ListExpression {

	/**
	 * Creates a new instance of ListExpression <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>list type [1]</li>
	 * <li>nb values : [1]</li>
	 * <li>value i : Expression [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public ListExpression(InputStream inputFile) throws IOException {
		super();
		listType = ListType.values()[inputFile.read()];
		values = new Expression[inputFile.read()];
		for (int i = 0; i < values.length; i++) {
			values[i] = ExpressionFactory.readNextExpression(inputFile);
		}
	}

	/**
	 * Return the computed integer list.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @param context
	 *          is the context attached to this test.
	 * @return the computed integer list.
	 */
	public int[] getList(Ability ability, Target tested,
			ContextEventListener context) {
		final int[] result = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i].getValue(ability, ability.getCard(), context);
		}
		return listType.getList(result);
	}

	/**
	 * Is this list is empty.
	 * 
	 * @return true if this list is empty.
	 */
	public boolean isEmpty() {
		return values.length == 0;
	}

	/**
	 * The integer values of this expression
	 */
	private final Expression[] values;

	/**
	 * The type of this list
	 */
	private final ListType listType;

}
