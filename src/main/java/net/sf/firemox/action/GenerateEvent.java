/*
 * Created on 12 mars 2005
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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.DeclaredAttacking;
import net.sf.firemox.event.DeclaredBlocking;
import net.sf.firemox.event.Event;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.test.TestOn;

/**
 * Change the normal jump(1) in the current actions chain, to another one . This
 * jump can be positive or negative. Zero mean infinite loop.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
class GenerateEvent extends UserAction implements StandardAction {

	private TestOn blocking;

	private TestOn attacking;

	/**
	 * Create an instance of Hop by reading a file Offset's file must pointing on
	 * the first byte of this action
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idAction [1]</li>
	 * <li>event id [1]</li>
	 * <li>attacking [TestOn]</li>
	 * <li>blocking [TestOn]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	GenerateEvent(InputStream inputFile) throws IOException {
		super(inputFile);
		idEvent = Event.valueOf(inputFile);
		switch (idEvent) {
		case DECLARED_ATTACKING:
			attacking = TestOn.deserialize(inputFile);
			break;
		case DECLARED_BLOCKING:
			attacking = TestOn.deserialize(inputFile);
			blocking = TestOn.deserialize(inputFile);
			break;
		default:
			throw new InternalError("\t>>Unsupported event '" + idEvent
					+ "' for GenerateEvent action");
		}
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.GENERATE_EVENT;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		switch (idEvent) {
		case DECLARED_ATTACKING:
			if (!DeclaredAttacking.tryAction(attacking.getCard(ability, context,
					ability.getCard()))) {
				return false;
			}
			DeclaredAttacking.dispatchEvent(attacking.getCard(ability, context,
					ability.getCard()));
			return true;
		case DECLARED_BLOCKING:
			if (!DeclaredBlocking.tryAction(blocking.getCard(ability, context,
					ability.getCard()), attacking.getCard(ability, context, ability
					.getCard()))) {
				return false;
			}
			DeclaredBlocking.dispatchEvent(blocking.getCard(ability, context, ability
					.getCard()), attacking.getCard(ability, context, ability.getCard()));
			return true;
		default:
			return true;
		}
	}

	@Override
	public String toString(Ability ability) {
		return "Generate event";
	}

	/**
	 * The event id to genrate
	 */
	private Event idEvent;

}