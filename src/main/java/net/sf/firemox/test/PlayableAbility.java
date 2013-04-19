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

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.ActivatedAbility;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.CanICast;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;

/**
 * Test if the tested ability is playable for the fixed purposes. The tested
 * ability must be an activated one.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
class PlayableAbility extends Test {

	/**
	 * Create an instance of PlayableAbility by reading a file. Offset's file must
	 * pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idTest [1]</li>
	 * <li>activated ability playable idCard [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	PlayableAbility(InputStream inputFile) throws IOException {
		super(inputFile);
		idCard = ExpressionFactory.readNextExpression(inputFile);
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		int playable = ((CanICast) ((ActivatedAbility) ability).eventComing())
				.getPlayableIdCard();
		return playable == 0
				|| MCard.intersectionIdCard(idCard.getValue(ability, tested, null),
						playable);
	}

	/**
	 * set of playable idCard
	 */
	private Expression idCard;

}
