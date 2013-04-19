/*
 * Created on 19 mars 2005
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
package net.sf.firemox.expression;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public class LowestAmong extends Expression {

	/**
	 * Creates a new instance of LowestAmong <br>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 */
	public LowestAmong(InputStream inputFile) throws IOException {
		super();
		restrictionZone = inputFile.read() - 1;
		test = TestFactory.readNextTest(inputFile);
		expr = ExpressionFactory.readNextExpression(inputFile);
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		final List<Target> validTargets = new ArrayList<Target>();
		if (restrictionZone != -1) {
			StackManager.PLAYERS[StackManager.idCurrentPlayer].zoneManager
					.getContainer(restrictionZone).checkAllCardsOf(test, validTargets,
							ability);
			StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].zoneManager
					.getContainer(restrictionZone).checkAllCardsOf(test, validTargets,
							ability);
		} else {
			StackManager.PLAYERS[StackManager.idCurrentPlayer].zoneManager
					.checkAllCardsOf(test, validTargets, ability);
			StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].zoneManager
					.checkAllCardsOf(test, validTargets, ability);
		}
		int lowest = 0;
		for (Target target : validTargets) {
			final int value = expr.getValue(ability, target, context);
			if (value < lowest) {
				lowest = value;
			}
		}
		return lowest;
	}

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		expr.extractTriggeredEvents(res, source, globalTest);
	}

	/**
	 * The zone identifant where the scan is restricted. If is equal to -1, there
	 * would be no restriction zone.
	 * 
	 * @see net.sf.firemox.token.IdZones
	 */
	private int restrictionZone;

	/**
	 * The test used to determine which card are considered in evaluation
	 */
	private Test test;

	/**
	 * The expression to evaluate for each valid card
	 */
	private Expression expr;

}
