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
package net.sf.firemox.clickable.target.card;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.action.MoveCard;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.DetachmentAbility;
import net.sf.firemox.clickable.ability.RemoveModifier;
import net.sf.firemox.event.AttachedEvent;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.MovedCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.modifier.model.ModifierFactory;
import net.sf.firemox.modifier.model.ModifierModel;
import net.sf.firemox.test.And;
import net.sf.firemox.test.InZone;
import net.sf.firemox.test.IsTested;
import net.sf.firemox.test.Not;
import net.sf.firemox.test.ReplaceTested;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.IdPositions;
import net.sf.firemox.token.IdZones;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.91
 */
public class Attachment {

	private final Test validTarget;

	private final Test validAttachment;

	/**
	 * The test making valid the attachment with another component.
	 */
	private final List<MEventListener> linkedEvents;

	/**
	 * The modifiers brought by this attachment.
	 */
	private final ModifierModel[] modifiers;

	/**
	 * Creates a new instance of AttachList <br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>modifiers [ModifierModel]</li>
	 * <li>test validating the attachment [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 */
	Attachment(InputStream inputFile) throws IOException {
		modifiers = new ModifierModel[inputFile.read()];
		for (int i = 0; i < modifiers.length; i++) {
			modifiers[i] = ModifierFactory.readModifier(inputFile);
		}
		linkedEvents = new ArrayList<MEventListener>(5);
		validTarget = TestFactory.readNextTest(inputFile);
		validAttachment = TestFactory.readNextTest(inputFile);
		validAttachment.extractTriggeredEvents(linkedEvents, SystemCard.instance,
				new Not(new ReplaceTested(TestOn.ATTACHED_TO, validAttachment)));
	}

	/**
	 * Returns the modifiers brought by this attachment.
	 * 
	 * @return the modifiers brought by this attachment.
	 */
	public ModifierModel[] getModifiers() {
		return modifiers;
	}

	/**
	 * Returns the initial test making valid the attachment with another
	 * component.
	 * 
	 * @return the initial test making valid the attachment with another
	 *         component.
	 */
	public Test getValidTarget() {
		return validTarget;
	}

	/**
	 * Returns the test maintaining the attachment with another component.
	 * 
	 * @return the test maintaining the attachment with another component.
	 */
	public Test getValidAttachment() {
		return validAttachment;
	}

	/**
	 * @param context
	 *          the context of playing ability.
	 * @param ability
	 *          the playing ability.
	 * @param from
	 *          the card to attach.
	 * @param to
	 *          the attachment destination.
	 */
	public void attach(ContextEventListener context, Ability ability, MCard from,
			MCard to) {
		AttachedEvent.dispatchEvent(to, from);
		MoveCard.moveCard(from, TestOn.CONTROLLER, to.getIdZone()
				| (to.tapped ? IdZones.PLAY_TAPPED : 0), null, IdPositions.ON_THE_TOP,
				ability, false);
		AttachedEvent.dispatchEvent(to, from);

		// add the clean up ability to remove abilities during the detachment
		for (MEventListener listener : linkedEvents) {
			if (from.isAttached()) {
				// This card was already attached, we update the events
				for (Ability registeredAbility : MEventListener.TRIGGRED_ABILITIES
						.get(listener.getIdEvent())) {
					if (registeredAbility instanceof DetachmentAbility
							&& ((DetachmentAbility) registeredAbility).getAttachedCard() == from) {
						registeredAbility.setEvent(listener.clone(from));
					}
				}
			} else {
				final Ability tmpAbility = new DetachmentAbility(listener.clone(from),
						from, to, validTarget);
				tmpAbility.registerToManager();
				new RemoveModifier(new MovedCard(IdZones.PLAY, And.append(
						IsTested.TESTED_IS_ATTACHED_TO, new InZone(IdZones.PLAY,
								TestOn.ATTACHED_TO)), new Not(new InZone(IdZones.PLAY,
						TestOn.ATTACHED_TO)), ability.getCard()), tmpAbility)
						.registerToManager();
			}
		}
		for (ModifierModel modifier : modifiers) {
			if (from == ability.getCard()) {
				modifier.addModifierFromModel(ability, to);
			} else {
				modifier.addModifierFromModel(from.getDummyAbility(), to);
			}
		}
		from.getContainer().remove(from);
		to.add(from);
	}

}
