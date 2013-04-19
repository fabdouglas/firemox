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
import net.sf.firemox.event.ModifiedPropertyIntersection;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.token.IdZones;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
abstract class HasPropertyIntersection extends TestCard {

	/**
	 * Create an instance of HasPropertyIntersection by reading a file. Offset's
	 * file must pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>property to test [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected HasPropertyIntersection(InputStream inputFile) throws IOException {
		super(inputFile);
		onMask = TestOn.deserialize(inputFile);
		propertyMask = ExpressionFactory.readNextExpression(inputFile);
	}

	@Override
	protected abstract boolean testCard(Ability ability, MCard tested);

	@Override
	public final void extractTriggeredEvents(List<MEventListener> res,
			MCard source, Test globalTest) {
		res.add(new ModifiedPropertyIntersection(IdZones.PLAY, globalTest, source,
				propertyMask));
	}

	/**
	 * The component on which the mask will be applied on
	 */
	protected TestOn onMask;

	/**
	 * The property id.
	 */
	protected Expression propertyMask;

}
