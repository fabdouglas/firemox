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
import java.util.HashMap;

import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class TestFactory {

	/**
	 * Prevent this class to be instanciated.
	 */
	private TestFactory() {
		super();
	}

	/**
	 * Return the next Test read from the current offset.
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>test code [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this test
	 * @return the next Test read from the current offset
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public static Test readNextTest(InputStream inputFile) throws IOException {
		IdTest idTest = IdTest.deserialize(inputFile);

		switch (idTest) {
		case TRUE:
			return True.getInstance();
		case FALSE:
			return False.getInstance();
		case PHASE_IS:
			return new PhaseIs(inputFile);
		case VALID_TARGET_ATTACHMENT:
			return new ValidTargetAttachment(inputFile);
		case VALID_ATTACHMENT:
			return new ValidAttachment(inputFile);
		case REFERENCED_TEST:
			final String referenceName = MToolKit.readString(inputFile);
			final Test test = referencedTests.get(referenceName);
			if (test == null) {
				throw new InternalError("No test named as '" + referenceName
						+ "' exists in the rules declaration");
			}
			return test;
		case EQUAL:
			return new Equal(inputFile);
		case INF:
			return new Inf(inputFile);
		case SUP:
			return new Sup(inputFile);
		case DIFFERENT:
			return new Different(inputFile);
		case INF_EQUAL:
			return new InfEqual(inputFile);
		case AND:
			return new And(inputFile);
		case OR:
			return new Or(inputFile);
		case NOT:
			return new Not(inputFile);
		case XOR:
			return new Xor(inputFile);
		case HAS_ACTION:
			return new HasAction(inputFile);
		case HAS_COLOR:
			return new HasColor(inputFile);
		case CONTROLLER:
			return new IsController(inputFile);
		case DATABASE:
			return new Database(inputFile);
		case HAS_IDCARD:
			return new HasIdCard(inputFile);
		case IN_IDCARD:
			return new InIdCard(inputFile);
		case IN_ZONE:
			return new InZone(inputFile);
		case IDPOSITION:
			return new Position(inputFile);
		case OWNER:
			return new IsOwner(inputFile);
		case HAS_PROPERTY:
			return new HasProperty(inputFile);
		case HAS_PROPERTY_INTERSECTION_COLOR:
			return new HasPropertyIntersectionColor(inputFile);
		case HAS_PROPERTY_INTERSECTION_IDCARD:
			return new HasPropertyIntersectionIdCard(inputFile);
		case HAS_PROPERTY_INTERSECTION_PROPERTY:
			return new HasPropertyIntersectionProperty(inputFile);
		case HAS_PROPERTY_NOT_FROM:
			return new HasPropertyNotFromCreator(inputFile);
		case TARGET_LIST:
			return new TargetListContains(inputFile);
		case IS_TESTED:
			return new IsTested(inputFile);
		case IS_ABILITY:
			return new IsAbility(inputFile);
		case IS_PLAYER:
			return new IsPlayer(inputFile);
		case IS_SPELL:
			return new IsSpell(inputFile);
		case HAS_NAME:
			return new HasName(inputFile);
		case HAS_KEYWORD:
			return new HasKeyword(inputFile);
		case HAS_ABILITY:
			return new HasAbility(inputFile);
		case ISME_PLAYER:
			return new IsMePlayer(inputFile);
		case SOURCE_ACTION_IS:
			return new ActionSource(inputFile);
		case ABILITY_IS:
			return new AbilityIs(inputFile);
		case SOURCE_ABILITY_IS:
			return new AbilitySource(inputFile);
		case CONTEXT_TEST:
			return ContextTest.getInstance();
		case SUP_EQUAL:
			return new SupEqual(inputFile);
		case PLAYABLE_ABILITY:
			return new PlayableAbility(inputFile);
		case HAS:
			return new Has(inputFile);
		case IS_FACE_UP:
			return new IsFaceUp(inputFile);
		case IS_ABORTING:
			return new IsAborting(inputFile);
		case HAS_PRIVATE_OBJECT:
			return new HasPrivateObject(inputFile);
		case PREVIOUS_ZONE:
			return new PreviousZone(inputFile);
		case REPLACE_TESTED:
			return new ReplaceTested(inputFile);
		case IS_COPY:
			return new IsCopy(inputFile);
		case DECK_COUNTER:
			return new DeckCounter(inputFile);
		default:
			throw new InternalError("Unknown int test code: " + idTest);
		}
	}

	/**
	 * Read available action constraints and abilities references.
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>number of action constraints [1]</li>
	 * <li>id action constraint i [1]</li>
	 * <li>test as constraint i [...]</li>
	 * </ul>
	 * 
	 * @param dbStream
	 *          the mdb stream's header.
	 * @throws IOException
	 *           error during the test reading.
	 */
	public static void init(InputStream dbStream) throws IOException {
		if (referencedTests != null) {
			referencedTests.clear();
		} else {
			referencedTests = new HashMap<String, Test>();
		}
		for (int i = dbStream.read(); i-- > 0;) {
			final String key = MToolKit.readString(dbStream);
			// Log.debug("ref-test " + key);
			try {
				referencedTests.put(key, readNextTest(dbStream));
			} catch (Throwable t) {
				Log.error("Error reading reference test '" + key + "'", t);
			}
		}
	}

	/**
	 * Set of declared tests
	 */
	private static HashMap<String, Test> referencedTests = null;

}
