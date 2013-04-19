/*
 * Created on Nov 2, 2004 
 * Original filename was PhaseIs.java
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
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.phase.BeginningPhase;
import net.sf.firemox.event.phase.EndOfPhase;
import net.sf.firemox.event.phase.PhaseFilter;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.token.IdZones;

/**
 * Test the current phase
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.72
 */
class PhaseIs extends Test {

	/**
	 * Create an instance of PhaseIs by reading a file. Offset's file must
	 * pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idTest [1]</li>
	 * <li>tested on (idTestedOn) [1]</li>
	 * <li>phase filter [1]</li>
	 * <li>phase index/id : Expression [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	PhaseIs(InputStream inputFile) throws IOException {
		super(inputFile);
		phaseFilter = PhaseFilter.valueOf(inputFile);
		idPhase = ExpressionFactory.readNextExpression(inputFile);
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		return phaseFilter.getPhaseFilter() == idPhase.getValue(ability, tested,
				null);
	}

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		res.add(new EndOfPhase(IdZones.PLAY, globalTest, source, idPhase,
				phaseFilter));
		res.add(new BeginningPhase(IdZones.PLAY, globalTest, source, idPhase,
				phaseFilter));
	}

	/**
	 * Is the phase to match during each test
	 */
	private Expression idPhase;

	/**
	 * The phase filter to use.
	 */
	protected PhaseFilter phaseFilter;

}
