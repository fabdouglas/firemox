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

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.Attachment;
import net.sf.firemox.clickable.target.card.MCard;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.91
 */
abstract class AbstractValidAttachment extends TestObject {

	/**
	 * Create an instance of AbstractValidAttachment by reading a file. Offset's
	 * file must pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>used target for test [1]
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	AbstractValidAttachment(InputStream inputFile) throws IOException {
		super(inputFile);
		from = TestOn.deserialize(inputFile);
	}

	@Override
	public boolean test(Ability ability, Target tested) {
		Attachment attachment = ((MCard) from.getTargetable(ability, tested))
				.getCardModel().getAttachment();
		return attachment != null
				&& attachment.getValidAttachment().test(ability,
						on.getTargetable(ability, tested));
	}

	/**
	 * @param ability
	 * @param tested
	 * @return the card to be attached.
	 */
	protected MCard getFrom(Ability ability, Target tested) {
		return (MCard) this.from.getTargetable(ability, tested);
	}

	/**
	 * @param ability
	 * @param from
	 * @return the ability associated to the card to attach.
	 */
	protected Ability getFromCardAbility(Ability ability, MCard from) {
		if (ability.getCard() != from) {
			return from.getDummyAbility();
		}
		return ability;
	}

	/**
	 * The component to be attached.
	 */
	protected TestOn from;
}
