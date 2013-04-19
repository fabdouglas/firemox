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

import net.sf.firemox.annotation.XmlTestElement;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.FacedDown;
import net.sf.firemox.event.FacedUp;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.token.Visibility;

/**
 * @author <a href="mailto:hoani.cross@gmail.com">Hoani CROSS</a>
 * @since 0.90
 */
@XmlTestElement(id = IdTest.IS_FACE_UP)
public class IsFaceUp extends TestCard {

	/**
	 * Create an instance of IsFaceUp by reading a file. Offset's file must
	 * pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	IsFaceUp(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	@Override
	public boolean testCard(Ability ability, MCard tested) {
		return on.getCard(ability, tested).getVisibility() == Visibility.PUBLIC;
	}

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		// we watch either Face-down either Face-up event
		res.add(new FacedUp(IdZones.PLAY, And.append(new IsTested(on), globalTest),
				source));
		res.add(new FacedDown(IdZones.PLAY, And
				.append(new IsTested(on), globalTest), source));
	}
}