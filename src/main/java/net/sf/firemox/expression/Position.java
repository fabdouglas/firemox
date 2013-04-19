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
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.ArrangedZone;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdZones;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82 restriction zone supported to optimize the target processing.
 * @since 0.83 count-player option apply test on the players.
 * @since 0.85 objects may be counted
 */
public class Position extends Expression {

	/**
	 * Creates a new instance of Counter <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idTestOn [1] card to locate specified)
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public Position(InputStream inputFile) throws IOException {
		super();
		on = TestOn.deserialize(inputFile);
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		return on.getCard(ability, tested).getContainer().getRealIndexOf(
				on.getCard(ability, tested)).key;
	}

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		res
				.add(new ArrangedZone(IdZones.PLAY, globalTest, source, IdConst.NO_CARE));
	}

	/**
	 * Card to locate
	 */
	private TestOn on;

}
