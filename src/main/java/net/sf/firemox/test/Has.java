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
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.MovedCard;
import net.sf.firemox.expression.Counter;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.zone.ZoneManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class Has extends Test {

	/**
	 * Create an instance of Has by reading a file. Offset's file must pointing on
	 * the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>test used to fill counter [...]
	 * <li>restriction zone [1]
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	Has(InputStream inputFile) throws IOException {
		super(inputFile);
		test = TestFactory.readNextTest(inputFile);
		restrictionZone = inputFile.read() - 1;
	}

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		if (test != null) {
			test.extractTriggeredEvents(res, source, globalTest);
			if (restrictionZone != -1) {
				res.add(new MovedCard(IdZones.PLAY, new InZone(restrictionZone,
						TestOn.TESTED), globalTest, source));
				res.add(new MovedCard(IdZones.PLAY, True.getInstance(), And.append(
						new InZone(restrictionZone, TestOn.TESTED), globalTest), source));
			}
		}
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		return test(ability, tested, true);
	}

	@Override
	public boolean testPreemption(Ability ability, Target tested) {
		return test(ability, tested, false);
	}

	private boolean test(Ability ability, Target tested, boolean canBePreempted) {
		// we count cards, we save the upper counter test.
		Target previousTested = Counter.superTested;
		Counter.superTested = tested;
		try {
			if (restrictionZone != -1) {
				return StackManager.PLAYERS[0].zoneManager
						.getContainer(restrictionZone).countAllCardsOf(test, ability, 1,
								canBePreempted) > 0
						|| StackManager.PLAYERS[1].zoneManager
								.getContainer(restrictionZone).countAllCardsOf(test, ability,
										1, canBePreempted) > 0;
			}
			return StackManager.PLAYERS[0].zoneManager.countAllCardsOf(test, ability,
					1, canBePreempted) > 0
					|| StackManager.PLAYERS[1].zoneManager.countAllCardsOf(test, ability,
							1, canBePreempted) > 0;
		} finally {
			// restore the previous upper counter test.
			Counter.superTested = previousTested;
		}
	}

	@Override
	public String toString() {
		if (restrictionZone != -1) {
			return "HAS (" + test.toString() + ") IN "
					+ ZoneManager.getZoneName(restrictionZone);
		}
		return "HAS (" + test.toString() + ")";
	}

	/**
	 * The test used to count cards
	 */
	private Test test;

	/**
	 * The zone identifier where the scan is restricted. If is equal to -1, there
	 * would be no restriction zone.
	 * 
	 * @see net.sf.firemox.token.IdZones
	 */
	private int restrictionZone;
}