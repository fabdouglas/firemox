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
package net.sf.firemox.deckbuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.test.And;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.test.True;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
public class DeckConstraints {

	/**
	 * The null deck constraint key name.
	 */
	public static final String DECK_CONSTRAINT_NAME_NONE = "none";

	/**
	 * The defined deck constraints.
	 */
	private static final SortedSet<DeckConstraint> CONSTRAINTS = new TreeSet<DeckConstraint>();

	private static int minProperty;

	private static int maxProperty;

	/**
	 * MASTER property for the game. Example: MTG -> Changeling
	 */
	public static int MASTER;

	/**
	 * Singleton constructor.
	 */
	private DeckConstraints() {
		// Nothing to do
	}

	/**
	 * Return the constraint associated to the given name.
	 * 
	 * @param deckConstraint
	 *          the constraint name.
	 * @return the constraint associated to the given name.
	 */
	public static DeckConstraint getDeckConstraint(String deckConstraint) {
		for (DeckConstraint constraint : CONSTRAINTS) {
			if (constraint.getName().equalsIgnoreCase(deckConstraint))
				return constraint;
		}
		return null;
	}

	/**
	 * Return the available constraints.
	 * 
	 * @return the available constraints.
	 */
	public static Collection<DeckConstraint> getDeckConstraints() {
		return CONSTRAINTS;
	}

	/**
	 * Initialize the deck constraints.
	 * 
	 * @param dbStream
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static void init(FileInputStream dbStream) throws IOException {
		// Add the 'none' deck constraint
		CONSTRAINTS.clear();
		CONSTRAINTS.add(new DeckConstraint(DECK_CONSTRAINT_NAME_NONE, True
				.getInstance()));

		// Read the user defined constraints
		minProperty = ExpressionFactory.readNextExpression(dbStream).getValue(null,
				null, null);
		maxProperty = ExpressionFactory.readNextExpression(dbStream).getValue(null,
				null, null);
		MASTER = ExpressionFactory.readNextExpression(dbStream).getValue(null,
				null, null);
		final int count = dbStream.read();
		for (int i = count; i-- > 0;) {
			// Read constraint name
			String deckName = MToolKit.readString(dbStream);

			// Read extend
			String extend = MToolKit.readString(dbStream);

			// Read constraint
			Test constraint = TestFactory.readNextTest(dbStream);

			// Create the deck constraint with extend
			if (extend.length() > 0) {
				DeckConstraint extendConstraint = getDeckConstraint(extend);
				if (extendConstraint == null) {
					throw new RuntimeException(
							"'"
									+ deckName
									+ "' is supposed extending '"
									+ extend
									+ "' but has not been found. Note that declaration order is important.");
				}
				constraint = And.append(getDeckConstraint(extend).getConstraint(),
						constraint);
			}
			CONSTRAINTS.add(new DeckConstraint(deckName, constraint));
		}
	}

	/**
	 * Return
	 * 
	 * @return Returns the minProperty.
	 */
	public static int getMinProperty() {
		return minProperty;
	}

	/**
	 * Return
	 * 
	 * @return Returns the maxProperty.
	 */
	public static int getMaxProperty() {
		return maxProperty;
	}
}
