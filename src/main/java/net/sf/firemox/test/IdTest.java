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
import java.io.OutputStream;

/**
 */
public enum IdTest {

	/**
	 * Value of type of test representing the NO TEST TO DO. When this test is
	 * applied on any component (card or player) would return always true.
	 */
	TRUE,

	/**
	 */
	IS_PLAYER,

	/**
	 */
	IS_SPELL,

	/**
	 */
	IS_ABILITY,

	/**
	 * Code test on action source of triggered event.
	 */
	SOURCE_ACTION_IS,

	/**
	 */
	HAS_PROPERTY_INTERSECTION_PROPERTY,

	/**
	 */
	HAS_PROPERTY_INTERSECTION_IDCARD,

	/**
	 */
	HAS_PROPERTY_INTERSECTION_COLOR,

	/**
	 * Value of type of test representing the test on id of current phase.
	 */
	PHASE_IS,

	/**
	 * Value of type of test representing a referenced test.
	 */
	REFERENCED_TEST,

	/**
	 * This value identify the test 'nb1 == nb2'.
	 */
	EQUAL,

	/**
	 * This value identify the test 'nb1 > nb2'.
	 */
	SUP,

	/**
	 * This value identify the test 'nb1 < nb2'.
	 */
	INF,

	/**
	 * This value identify the test 'nb1 != nb2'.
	 */
	DIFFERENT,

	/**
	 * This value identify the test 'nb1 <= nb2'.
	 */
	INF_EQUAL,

	/**
	 * Value of type of test representing the ALWAYS false.
	 */
	FALSE,

	/**
	 * Compare the controller of the current card to the player identified by a
	 * code.
	 */
	CONTROLLER,

	/**
	 * Compare the owner of the current card to the player identified by a code.
	 */
	OWNER,

	/**
	 * Compare the property of the current card to the property identified by a
	 * code.
	 */
	HAS_PROPERTY,

	/**
	 * Compare the card type identifier of the current card to the card type
	 * identified by a code.
	 */
	HAS_IDCARD,

	/**
	 * Compare the color of the current card to the color identified by a code.
	 */
	HAS_COLOR,

	/**
	 * Compare the zone of the current card to the zone identified by a code.
	 */
	IN_ZONE,

	/**
	 * Compare the position of the current card to the zone identified by a code.
	 */
	IDPOSITION,

	/**
	 * Compare the card type identifier of the current card to the card type
	 * identified by a code.
	 */
	IN_IDCARD,

	/**
	 * 
	 */
	ISME_PLAYER,

	/**
	 * Compare the card tested card to the card given by the TestedOn object.
	 */
	IS_TESTED,

	/**
	 * Compare the property of the current card to the property identified by a
	 * code.
	 */
	HAS_PROPERTY_NOT_FROM,

	/**
	 * Compare the name of tested card with the specified card.
	 */
	HAS_NAME,

	/**
	 * Target list test : contains
	 */
	TARGET_LIST,

	/**
	 * Is the tested card has the specified ability
	 */
	HAS_ABILITY,

	/**
	 * To test the ability source's name of triggered event.
	 */
	SOURCE_ABILITY_IS,

	/**
	 * Evaluate the first test, then if it was false, evaluate the second and
	 * return it's value.
	 */
	OR,

	/**
	 * Evaluate the first test, then if it was true, evaluate the second and
	 * return it's value.
	 */
	AND,

	/**
	 * Evaluate the test and return it's opposite value.
	 */
	NOT,

	/**
	 * Evaluate the both test and return the XOR value.
	 */
	XOR,

	/**
	 */
	CONTEXT_TEST,

	/**
	 */
	HAS_ACTION,

	/**
	 * To test the current ability's name.
	 */
	ABILITY_IS,

	/**
	 * This value identify the test 'nb1 >= nb2'.
	 */
	SUP_EQUAL,

	/**
	 * To test if the activated ability is playable or not.
	 */
	PLAYABLE_ABILITY,

	/**
	 */
	HAS,

	/**
	 * To test if the card is face up or not.
	 */
	IS_FACE_UP,

	/**
	 * Test if the tested component has a specified private object.
	 */
	HAS_PRIVATE_OBJECT,

	/**
	 * Test if the tested card has a specified keyword.
	 */
	HAS_KEYWORD,

	/**
	 * Referring to the attachment condition of the tested card.
	 */
	VALID_TARGET_ATTACHMENT,

	/**
	 * Referring to the attachment maintaining condition of the tested card.
	 */
	VALID_ATTACHMENT,

	/**
	 * Test the previous place of component. Valid only for card component.
	 */
	PREVIOUS_ZONE,

	/**
	 * Is this card is aborting
	 */
	IS_ABORTING,

	/**
	 * Replace nested test.
	 */
	REPLACE_TESTED,

	/**
	 * Is copy.
	 */
	IS_COPY,

	/**
	 * Has database property/value.
	 */
	DATABASE,

	/**
	 * Deck counter.
	 */
	DECK_COUNTER;

	/**
	 * Write this enumeration to the given output stream.
	 * 
	 * @param out
	 *          the stream this enumeration would be written.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public void serialize(OutputStream out) throws IOException {
		out.write(ordinal());
	}

	/**
	 * Read and return the enumeration from the given input stream.
	 * 
	 * @param input
	 *          the stream containing the enumeration to read.
	 * @return the enumeration from the given input stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static IdTest deserialize(InputStream input) throws IOException {
		return values()[input.read()];
	}
}