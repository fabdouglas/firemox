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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.xml.XmlTools;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public class Register {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param on
	 */
	public Register(TestOn on) {
		this.on = on;
		this.ordinal = on.ordinal();
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param ordinal
	 */
	public Register(int ordinal) {
		this.on = null;
		this.ordinal = ordinal;
	}

	/**
	 * Returns the integer value of this expression
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @param context
	 *          is the context attached to this test.
	 * @param index
	 *          the index value access.
	 * @return the integer value of this expression
	 */
	public int getValue(Ability ability, Target tested,
			ContextEventListener context, int index) {
		if (on != null)
			return on.getTargetable(ability, tested).getValue(index);
		switch (ordinal) {
		case IdTokens.STACK:
			return StackManager.registers[index];
		case IdTokens.STATIC_REGISTER:
			return MCommonVars.registers[index];
		case IdTokens.REQUIRED_MANA:
			if (index == IdTokens.MANA_POOL) {
				return MToolKit.manaPool(StackManager.actionManager.getRequiredMana());
			}
			return StackManager.actionManager.getRequiredMana()[index];
		default:
			throw new InternalError("unknown idToken : " + index);
		}
	}

	/**
	 * Is this register is plugged on a target component.
	 * 
	 * @return true if this register is plugged on a target component.
	 */
	public boolean isTargetable() {
		return on != null;
	}

	/**
	 * Is this register is plugged on a set of target component.
	 * 
	 * @return true if this register is plugged on a set of target component.
	 */
	public boolean isGlobal() {
		return on == null;
	}

	/**
	 * Write this enum to the given output stream.
	 * 
	 * @param out
	 *          the stream ths enum would be written.
	 */
	public void serialize(OutputStream out) {
		if (!canBePreempted())
			XmlTools.testCanBePreempted = false;
		MToolKit.writeInt16(out, ordinal);
	}

	/**
	 * Read and return the enum from the given input stream.
	 * 
	 * @param input
	 *          the stream containing the enum to read.
	 * @return the enum from the given input stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static Register deserialize(InputStream input) throws IOException {
		return deserialize(MToolKit.readInt16(input));
	}

	/**
	 * Read and return the enum from the given input stream.
	 * 
	 * @param ordinal
	 *          the key.
	 * @return the enum from the given key.
	 */
	public static Register deserialize(int ordinal) {
		if (values == null)
			values = new HashMap<Integer, Register>();
		Register register = values.get(ordinal);
		if (register == null) {
			if (ordinal < TestOn.values().length) {
				register = new Register(TestOn.values()[ordinal]);
			} else {
				register = new Register(ordinal);
			}
			values.put(ordinal, register);
		}
		return register;
	}

	/**
	 * Return null of enum value corresponding to the given Xsd name.
	 * 
	 * @param xsdName
	 *          the Xsd name of this Register.
	 * @return null of enum value corresponding to the given Xsd name.
	 */
	public static Register valueOfXsd(String xsdName) {
		TestOn on = TestOn.valueOfXsd(xsdName);
		if (on != null) {
			return new Register(on);
		}
		final int index = Arrays.binarySearch(REGISTER_NAMES, xsdName);
		if (index >= 0)
			return Register.deserialize(REGISTER_VALUES[index]);
		return null;
	}

	/**
	 * Return the ID of this register.
	 * 
	 * @return the ID of this register.
	 */
	public int ordinal() {
		return ordinal;
	}

	/**
	 * Return the component associated to this register. May be null.
	 * 
	 * @return the component associated to this register. May be null.
	 */
	public TestOn getTargetable() {
		return on;
	}

	/**
	 * Return true if the associated value can be evaluated without ability
	 * context.
	 * 
	 * @return true if the associated value can be evaluated without ability
	 *         context.
	 */
	public boolean canBePreempted() {
		if (ordinal == IdTokens.STACK)
			return false;
		return on == null || on.canBePreempted();
	}

	@Override
	public String toString() {
		if (on != null)
			return on.toString();
		int index = ArrayUtils.indexOf(REGISTER_VALUES, ordinal);
		if (index != -1)
			return REGISTER_NAMES[index];
		return "?";
	}

	/**
	 * Accessible register names. <br>
	 * Preserve alphabetical order of this array.
	 */
	public static final String[] REGISTER_NAMES = { "ability", "card",
			"dealtable", "game.static", "player", "private-object", "required-mana",
			"stack", "target" };

	/**
	 * Accessible register values. <br>
	 * Preserve order of this array to suit to the <code>REGISTER_NAMES</code>'s
	 * order.
	 */
	public static final int[] REGISTER_VALUES = { IdTokens.DELAYED_REGISTERS,
			IdTokens.CARD, IdTokens.DEALTABLE, IdTokens.STATIC_REGISTER,
			IdTokens.PLAYER, IdTokens.PRIVATE_NAMED_TARGETABLE,
			IdTokens.REQUIRED_MANA, IdTokens.STACK, IdTokens.TARGET };

	/**
	 * The register may be identified to a player or a card. Is <code>null</code>
	 * if represents anything else such as stack, game register,...
	 */
	private final TestOn on;

	/**
	 * This values is equals to the TestOn ordinal when is not <code>null</code>,
	 * or represents the stack, game register,... identifier.
	 */
	private final int ordinal;

	/**
	 * Differents cached instances of this class.
	 */
	private static Map<Integer, Register> values;
}
