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
package net.sf.firemox.token;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.stack.EventManager;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public enum AbstractValue {

	/**
	 * Special access to the integer saved into the context of current spell.
	 */
	CONTEXT_INT("context.int"),

	/**
	 * Special access to the second integer saved into the context of current
	 * spell.
	 */
	CONTEXT_INT2("context.int2"),

	/**
	 * Special access to the current target list size
	 */
	TARGET_LIST_SIZE("target-list.size"),

	/**
	 * Special access to the static value corresponding to the turn number. The
	 * first turn should be 0.
	 */
	TURN_ID("turnid"),

	/**
	 * Special access to the first integer of current integer list
	 */
	INT_LIST_FIRST("int-list.first"),

	/**
	 * Special access to the last integer of current integer list
	 */
	INT_LIST_LAST("int-list.last"),

	/**
	 * Special access to the current integer list size
	 */
	INT_LIST_SIZE("int-list.size"),

	/**
	 * Special access to the saved integer list size
	 */
	SAVED_INT_LIST_SIZE("saved-int-list.size"),

	/**
	 * Special access to the saved target list size
	 */
	SAVED_TARGET_LIST_SIZE("saved-target-list.size"),

	/**
	 * Special access to current phase index.
	 */
	CURRENT_PHASE_INDEX("current-phase.index"),

	/**
	 * The owner of the second card saved in the context.
	 */
	ALL("?");

	private final String xsdName;

	private AbstractValue(String xsdName) {
		this.xsdName = xsdName;
	}

	/**
	 * Return the target on which the test would be applied
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param card
	 *          is the card owning the current ability.
	 * @param context
	 *          the current context.
	 * @param tested
	 *          the tested target
	 * @return the target to use for the test
	 */
	public int getValue(Ability ability, MCard card,
			ContextEventListener context, Target tested) {
		switch (this) {
		case TARGET_LIST_SIZE:
			if (StackManager.getInstance().getTargetedList() == null) {
				return 0;
			}
			return StackManager.getInstance().getTargetedList().size();
		case INT_LIST_SIZE:
			if (StackManager.intList == null) {
				return 0;
			}
			return StackManager.intList.size();
		case CONTEXT_INT2:
			MContextCardCardIntInt subContext2 = ((MContextCardCardIntInt) getContext(context));
			if (subContext2 != null) {
				return subContext2.getValue2();
			}
			return 0;
		case CONTEXT_INT:
			MContextCardCardIntInt subContext = ((MContextCardCardIntInt) getContext(context));
			if (subContext != null) {
				return subContext.getValue();
			}
			return 0;
		case INT_LIST_LAST:
			return StackManager.intList.getLastInt();
		case INT_LIST_FIRST:
			return StackManager.intList.getFirstInt();
		case SAVED_INT_LIST_SIZE:
			return StackManager.SAVED_INT_LISTS.size();
		case SAVED_TARGET_LIST_SIZE:
			return StackManager.SAVED_TARGET_LISTS.size();
		case CURRENT_PHASE_INDEX:
			return EventManager.phaseIndex;
		case TURN_ID:
			return MCommonVars.registers[IdTokens.TURN_ID];
		default:
			return 0;
		}
	}

	/**
	 * Write this enumeration to the given output stream.
	 * 
	 * @param out
	 *          the stream this enumeration would be written.
	 * @param xsdName
	 *          the XSD name of this TestOn.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static void serialize(OutputStream out, String xsdName)
			throws IOException {
		for (AbstractValue value : values()) {
			if (value.xsdName.equals(xsdName)) {
				value.serialize(out);
				return;
			}
		}
		throw new IllegalArgumentException("Invalid xsd attribute name : "
				+ xsdName);
	}

	/**
	 * Return null of enumeration value corresponding to the given XSD name.
	 * 
	 * @param xsdName
	 *          the XSD name of this abstract value.
	 * @return null of enumeration value corresponding to the given XSD name.
	 */
	public static AbstractValue valueOfXsd(String xsdName) {
		for (AbstractValue value : values()) {
			if (value.xsdName.equals(xsdName)) {
				return value;
			}
		}
		return null;
	}

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
	public static AbstractValue deserialize(InputStream input) throws IOException {
		return values()[input.read()];
	}

	/**
	 * Return the given context if not null. Returns the context of current
	 * ability otherwise.
	 * 
	 * @param currentContext
	 *          the known context.
	 * @return the given context if not null. Returns the context of current
	 *         ability otherwise.
	 */
	private ContextEventListener getContext(ContextEventListener currentContext) {
		if (currentContext == null)
			return StackManager.getInstance().getAbilityContext();
		return currentContext;
	}

}
