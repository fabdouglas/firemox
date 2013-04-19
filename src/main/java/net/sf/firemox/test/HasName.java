/*
 * Created on Dec 29, 2004
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
package net.sf.firemox.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
class HasName extends TestCard {

	/**
	 * Create an instance of HasName by reading a file. Offset's file must
	 * pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>card name + '\0' [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	HasName(InputStream inputFile) throws IOException {
		super(inputFile);
		cardName = MToolKit.readString(inputFile).intern();
	}

	/**
	 * Return the result of test beetwen left and right op applied on the
	 * specified card.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @return true if the specified card matches with the test to do
	 */
	@Override
	protected boolean testCard(Ability ability, MCard tested) {
		if (cardName.length() == 0) {
			return on.getCard(ability, tested).getCardName().equalsIgnoreCase(
					tested.getCardName());
		}
		return on.getCard(ability, tested).getCardName().equalsIgnoreCase(cardName);
	}

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		// res.add(new MEventModifiedName(IdZones.ID__PLAY, TestNull.getInstance(),
		// cardName));
	}

	/**
	 * Is the card name to test
	 */
	private String cardName;

}
