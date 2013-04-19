/*
 * Created on Nov 3, 2004 
 * Original filename was DetachmentAbility.java
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
 */
package net.sf.firemox.clickable.ability;

import net.sf.firemox.action.DetachMe;
import net.sf.firemox.action.MAction;
import net.sf.firemox.action.target.SingletonTarget;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.TrueFalseAuto;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class DetachmentAbility extends AbstractAbility {

	/**
	 * Creates a new instance of DetachmentAbility <br>
	 * 
	 * @param event
	 *          the event attached to this ability.
	 * @param from
	 *          the card is to going to be attached.
	 * @param to
	 *          the card is to going to be attached to.
	 * @param attachmentTest
	 *          the attachment condition.
	 */
	public DetachmentAbility(MEventListener event, MCard from, MCard to,
			Test attachmentTest) {
		super("detach myself", event, null, null, TrueFalseAuto.AUTO);
		actionList = new MAction[] { SingletonTarget
				.getInstance(TestOn.CURRENT_CARD) };
		effectList = new MAction[] { DetachMe.getInstance() };
		this.from = from;
		this.to = to;
		this.attachmentTest = attachmentTest;

		// override the resolver
		priority = Priority.hidden_high;
		// override the optimizer
		optimizer = Optimization.follow;
	}

	@Override
	public MCard getCard() {
		return eventComing.card;
	}

	@Override
	public Ability clone(MCard container) {
		return new DetachmentAbility(eventComing.clone(container), container, to,
				attachmentTest);
	}

	@Override
	public boolean triggerIt(ContextEventListener context) {
		super.triggerIt(context);
		eventComing.card.controller.zoneManager.triggeredBuffer.addHidden(this,
				context);
		return true;
	}

	@Override
	public String getLog(ContextEventListener context) {
		return "detachment of " + eventComing.card;
	}

	@Override
	public String getAbilityTitle() {
		return "DetachedAbility" + (getName() != null ? ": " + getName() : "")
				+ super.getAbilityTitle();
	}

	/**
	 * Returns the attached card.
	 * 
	 * @return the attached card.
	 */
	public MCard getAttachedCard() {
		return from;
	}

	/**
	 * The attached card.
	 */
	private final MCard from;

	private final MCard to;

	private final Test attachmentTest;
}
