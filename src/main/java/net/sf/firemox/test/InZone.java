/*
 * TestIdZone.java 
 * Created on 25 feb. 2004
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
import net.sf.firemox.event.BecomeTapped;
import net.sf.firemox.event.BecomeUnTapped;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.MovedCard;
import net.sf.firemox.token.IdZones;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 */
public class InZone extends TestCard {

	/**
	 * Create an instance of HasColor by reading a file. Offset's file must
	 * pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone to test [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	InZone(InputStream inputFile) throws IOException {
		super(inputFile);
		idZone = inputFile.read();
	}

	/**
	 * Creates a new instance of InZone <br>
	 * 
	 * @param idZone
	 * @param on
	 */
	public InZone(int idZone, TestOn on) {
		super(on);
		this.idZone = idZone;
	}

	@Override
	public boolean testCard(Ability ability, MCard tested) {
		return on.getCard(ability, tested).isSameState(idZone);
	}

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		if (idZone >= IdZones.PLAY_TAPPED) {
			// we are looking for the [un]tapped state too
			res.add(new BecomeTapped(IdZones.PLAY, new IsTested(on), source));
			res.add(new BecomeUnTapped(IdZones.PLAY, new IsTested(on), source));
		}
		res.add(new MovedCard(IdZones.PLAY, And.append(new IsTested(on),
				new InZone(idZone, on)), globalTest, source));
		res.add(new MovedCard(IdZones.PLAY, new IsTested(on), And.append(
				new InZone(idZone, on), globalTest), source));
	}

	/**
	 * Is the color to match during each test
	 */
	private int idZone;

}