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
package net.sf.firemox.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.ModifiedIdColor;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.token.IdZones;

/**
 * Test the color of a given component. The color expression is a
 * {@link Expression} instance whereas HasColor's color a simple value.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class HasColor extends TestCard {

	/**
	 * Create an instance of HasColor by reading a file. Offset's file must
	 * pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>color to test [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	HasColor(InputStream inputFile) throws IOException {
		super(inputFile);
		colorExpr = ExpressionFactory.readNextExpression(inputFile);
	}

	@Override
	protected boolean testCard(Ability ability, MCard tested) {
		return on.getCard(ability, tested).hasIdColor(
				colorExpr.getValue(ability, tested, null));
	}

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		res.add(new ModifiedIdColor(IdZones.PLAY, globalTest, source, colorExpr));
	}

	/**
	 * Is the color to match during each test
	 */
	private Expression colorExpr;
}
