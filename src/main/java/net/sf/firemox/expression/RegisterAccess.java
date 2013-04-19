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
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.ModifiedRegister;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.operation.Any;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.True;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.token.Register;

/**
 * Return the register value of a component considering the modifiers.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class RegisterAccess extends Expression {

	/**
	 * Creates a new instance from inputStream<br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>register [Register]</li>
	 * <li>index [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public RegisterAccess(InputStream inputFile) throws IOException {
		super();
		register = Register.deserialize(inputFile);
		index = ExpressionFactory.readNextExpression(inputFile);
	}

	/**
	 * Is this value is X value.
	 * 
	 * @return true if this value is X value.
	 */
	public boolean isXvalue() {
		return register.ordinal() == IdTokens.STACK;
	}

	@Override
	public int getValue(Ability ability, Target tested,
			ContextEventListener context) {
		return register.getValue(ability, tested, context, index.getValue(ability,
				tested, context));
	}

	/**
	 * Return the integer value of this expression exactly as it will be when the
	 * ability will be executed.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @return the integer value of this expression
	 */
	public int getPreemptionValue(Ability ability, Target tested) {
		if (register.getTargetable() != null)
			return register.getTargetable().getPreemptedValue(ability,
					index.getPreemptionValue(ability, tested, null));
		return -1;
	}

	@Override
	public boolean canBePreempted() {
		return register.canBePreempted() && index.canBePreempted();
	}

	@Override
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		if (register.getTargetable() != null) {
			if (register.getTargetable().isCard()) {
				res.add(new ModifiedRegister(IdZones.PLAY, globalTest, True
						.getInstance(), source, Any.getInstance(), Register
						.deserialize(IdTokens.CARD), index));
			} else if (register.getTargetable().isPlayer()) {
				res.add(new ModifiedRegister(IdZones.PLAY, globalTest, True
						.getInstance(), source, Any.getInstance(), Register
						.deserialize(IdTokens.PLAYER), index));
			}
		}
	}

	/**
	 * The register index
	 */
	protected Expression index;

	/**
	 * The register acces
	 */
	protected Register register;
}
