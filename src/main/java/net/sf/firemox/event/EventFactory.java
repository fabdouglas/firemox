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
package net.sf.firemox.event;

import java.io.IOException;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.phase.BeforePhase;
import net.sf.firemox.event.phase.BeginningPhase;
import net.sf.firemox.event.phase.EndOfPhase;

/**
 * The event factory.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class EventFactory {

	/**
	 * Create a new instance of this class.
	 */
	private EventFactory() {
		// Nothing to do
	}

	/**
	 * return the next EventListener read from the current offset <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idEvent [1]</li>
	 * <li>idZone [1]</li>
	 * <li>test [...]</li>
	 * <li>event's fields [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @param card
	 *          is the card owning this event
	 * @return the next EventListener read from the current offset
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public static MEventListener readNextEvent(java.io.InputStream inputFile,
			MCard card) throws IOException {
		Event idEvent = Event.valueOf(inputFile);
		switch (idEvent) {
		case BEFORE_PHASE:
			return new BeforePhase(inputFile, card);
		case BEGINNING_PHASE:
			return new BeginningPhase(inputFile, card);
		case CAN_CAST_CARD:
			return new CanICast(inputFile, card);
		case MOVING_CARD:
			return new MovedCard(inputFile, card);
		case BECOMING_TAPPED:
			return new BecomeTapped(inputFile, card);
		case BECOMING_UNTAPPED:
			return new BecomeUnTapped(inputFile, card);
		case EXCEPTION:
			return new UncaughtException(inputFile, card);
		case CASTING:
			return new Casting(inputFile, card);
		case DEALTING_DAMAGE:
			return new AssignedDamage(inputFile, card);
		case DECLARED_ATTACKING:
			return new DeclaredAttacking(inputFile, card);
		case DECLARED_BLOCKING:
			return new DeclaredBlocking(inputFile, card);
		case DETACHED_CARD:
			return new Detached(inputFile, card);
		case EOP:
			return new EndOfPhase(inputFile, card);
		case LOSING_GAME:
			return new LoseGame(inputFile, card);
		case MODIFIED_IDCARD:
			return new ModifiedIdCard(inputFile, card);
		case MODIFIED_PROPERTY:
			return new ModifiedProperty(inputFile, card);
		case MODIFIED_IDCOLOR:
			return new ModifiedIdColor(inputFile, card);
		case MODIFIED_REGISTER:
			return new ModifiedRegister(inputFile, card);
		case MODIFIED_REGISTER_RANGE:
			return new ModifiedRegisterRange(inputFile, card);
		case MODIFIED_CONTROLLER:
			return new ModifiedController(inputFile, card);
		case NEVER_ACTIVATED:
			return new NeverTriggered(card);
		case UPDATE_TOUGHNESS:
			return new UpdateToughness(inputFile, card);
		case UPDATE_LIFE:
			return new UpdatedLife(inputFile, card);
		case LETHAL_DAMAGE:
			return new LethalDamaged(inputFile, card);
		case TARGETED:
			return new Targeted(inputFile, card);
		case GIVEN_MANA:
			return new GivenMana(inputFile, card);
		case ARRANGED_ZONE:
			return new ArrangedZone(inputFile, card);
		case FACED_UP:
			return new FacedUp(inputFile, card);
		case FACED_DOWN:
			return new FacedDown(inputFile, card);
		default:
			throw new InternalError("Unknown idEvents : " + idEvent);
		}
	}

}
