/*
 * Created on Nov 8, 2004 
 * Original filename was Counter.java
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
 * 
 */
package net.sf.firemox.expression;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.firemox.action.ModifyRegister;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.MovedCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.And;
import net.sf.firemox.test.InZone;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.test.True;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82 restriction zone supported to optimize the target processing.
 * @since 0.83 count-player option apply test on the players.
 * @since 0.85 objects may be counted
 */
public class Counter extends Expression {

	/**
	 * The byte indicating the player are parsed during the counter process or
	 * not.
	 */
	public static final int COUNT_PLAYER = 0x80;

	/**
	 * Creates a new instance of Counter <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>object's name + '\0' [...]
	 * <li>test used to fill counter [...]
	 * <li>idTestOn [1] (only if object's name specified)
	 * <li>restriction zone + enable count player [1] (only if object's name not
	 * specified)
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public Counter(InputStream inputFile) throws IOException {
		super();
		objectName = MToolKit.readString(inputFile);
		test = TestFactory.readNextTest(inputFile);
		if (objectName.length() == 0) {
			// object counter will be disabled
			objectName = null;
			restrictionZone = inputFile.read();
			countPlayer = (restrictionZone & COUNT_PLAYER) == COUNT_PLAYER;
			restrictionZone = (restrictionZone & ~COUNT_PLAYER) - 1;
		} else {
			// only objects will be counted on the specified card
			objectName = objectName.intern();
			on = TestOn.deserialize(inputFile);
		}
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		if (objectName != null) {
			// we count objects on the given component. The test is applied on creator
			return on.getCard(ability, tested).getNbObjects(objectName, test);
		}

		// we count cards, we save the upper counter test.
		Target previousTested = superTested;
		superTested = tested;
		int res = ModifyRegister.countAllCardsOf(test, ability, restrictionZone);
		if (countPlayer) {
			// we count players
			if (test.test(ability, StackManager.PLAYERS[0])) {
				res++;
			}
			if (test.test(ability, StackManager.PLAYERS[1])) {
				res++;
			}
		}

		// restore the previous upper counter test.
		superTested = previousTested;
		return res;
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

	/**
	 * The test to use for counters
	 */
	private Test test;

	/**
	 * The upper tested card. Since the tested card is overwritten by this
	 * counter, the tested card is saved before.
	 */
	public static Target superTested = null;

	/**
	 * The zone identifant where the scan is restricted. If is equal to -1, there
	 * would be no restriction zone.
	 * 
	 * @see net.sf.firemox.token.IdZones
	 */
	private int restrictionZone;

	/**
	 * Is this counter will counter players.
	 */
	private boolean countPlayer;

	/**
	 * Objet's name to count. Null if objects are not counted.
	 */
	private String objectName;

	/**
	 * Represents the component where the objects would be counted. Is null if
	 * objects are not counted.
	 */
	private TestOn on;

}
