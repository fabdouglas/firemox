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
package net.sf.firemox.clickable.ability;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.test.And;
import net.sf.firemox.test.InZone;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.TrueFalseAuto;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public class TriggeredAbilitySet extends TriggeredAbility {

	/**
	 * Create a new instance of this class.
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>super [ActivatedAbility]</li>
	 * <li>when [Test]</li>
	 * </ul>
	 * 
	 * @param input
	 *          file containing this ability
	 * @param card
	 *          referenced card
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */

	public TriggeredAbilitySet(InputStream input, MCard card) throws IOException {
		super(input, card);
		final Test when = TestFactory.readNextTest(input);
		final List<MEventListener> events = new ArrayList<MEventListener>();
		when.extractTriggeredEvents(events, card, And.append(new InZone(eventComing
				.getIdPlace(), TestOn.THIS), when));
		linkedAbilities = new ArrayList<Ability>(events.size());
		for (MEventListener event : events) {
			linkedAbilities.add(new NestedAbility(event));
		}
	}

	/**
	 * 
	 */
	private class NestedAbility extends TriggeredAbility {

		/**
		 * Create a new instance of this class.
		 * 
		 * @param event
		 */
		protected NestedAbility(MEventListener event) {
			super(TriggeredAbilitySet.this, event);
			this.playAsSpell = TrueFalseAuto.FALSE;
		}

	}

	@Override
	public Ability clone(MCard container) {
		final Collection<Ability> linkedAbilities = new ArrayList<Ability>(
				this.linkedAbilities.size());
		for (Ability ability : linkedAbilities) {
			linkedAbilities.add(ability.clone(container));
		}
		final TriggeredAbility clone = new TriggeredAbility(this, eventComing
				.clone(container));
		clone.playAsSpell = TrueFalseAuto.FALSE;
		clone.linkedAbilities = linkedAbilities;
		return clone;
	}

}
