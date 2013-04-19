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
package net.sf.firemox.modifier.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.action.AddModifier;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.event.EventFactory;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.test.True;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85 Recalculate attribute is supported
 */
public abstract class ModifierModel {

	/**
	 * Creates a new instance of ModifierModel <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>modifier name [String]</li>
	 * <li>live update [boolean]</li>
	 * <li>linked to creator [boolean]</li>
	 * <li>activationZone [IdZone]</li>
	 * <li>activated while this condition [Test]</li>
	 * <li>exists until this triggered event [Event[]]</li>
	 * <li>layer [integer]</li>
	 * </ul>
	 * The while condition must not refer to any register value of this modifier
	 * 
	 * @param input
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected ModifierModel(InputStream input) throws IOException {
		super();
		name = MToolKit.readString(input);
		liveUpdate = input.read() == 1;
		activationZone = input.read();

		// while
		whileCondition = TestFactory.readNextTest(input);
		linkedEvents = new ArrayList<MEventListener>();
		whileCondition.extractTriggeredEvents(linkedEvents, SystemCard.instance,
				True.getInstance());

		// creator link
		linked = input.read() == 1;

		// until events
		int count = input.read();
		until = new ArrayList<MEventListener>(count + 1);
		while (count-- > 0) {
			until.add(EventFactory.readNextEvent(input, SystemCard.instance));
		}

		layer = Layer.deserialize(input);
	}

	/**
	 * Create a new instance from the given model.
	 * 
	 * @param other
	 *          the instance to copy
	 */
	protected ModifierModel(ModifierModel other) {
		this.name = other.name;
		this.linked = other.linked;
		this.until = other.until;
		this.whileCondition = other.whileCondition;
		this.layer = other.layer;
		this.liveUpdate = other.liveUpdate;
		this.linkedEvents = new ArrayList<MEventListener>(other.linkedEvents);
		this.activationZone = other.activationZone;
		if (other.next != null) {
			next = other.next.clone();
		}
	}

	/**
	 * Add to the list of modifierModels of the specified card, this instance.
	 * 
	 * @param modelToAdd
	 *          This new modifier will be added to the tail of modifier list.
	 */
	public final void addModel(ModifierModel modelToAdd) {
		if (next == null) {
			next = modelToAdd;
		} else {
			next.addModel(modelToAdd);
		}
	}

	@Override
	public abstract ModifierModel clone();

	/**
	 * Create modifier(s) on the specified target. The zone activation is checked.
	 * 
	 * @param ability
	 *          is the ability owning causing the creation of this modifier. The
	 *          card component of this ability should correspond to the card
	 *          owning this test too.
	 * @param target
	 *          is the card this new modifier would be attached to.
	 */
	public final void addModifierFromModel(Ability ability, MCard target) {
		// Check the working zone
		if (activationZone == ability.getCard().getIdZone()
				|| IdZones.ANYWHERE == ability.getCard().getIdZone()
				|| ability.isSystemAbility()
				|| StackManager.actionManager.currentAction instanceof AddModifier) {
			addModifierFromModelPriv(ability, target);
		}

		// proceed the next modifier model
		if (next != null) {
			next.addModifierFromModel(ability, target);
		}
	}

	/**
	 * Create modifier(s) on the specified target.
	 * 
	 * @param ability
	 *          is the ability owning causing the creation of this modifier. The
	 *          card component of this ability should correspond to the card
	 *          owning this test too.
	 * @param target
	 *          is the card this new modifier would be attached to.
	 */
	protected abstract void addModifierFromModelPriv(Ability ability, MCard target);

	/**
	 * Return the HTML code representing this action. If this action is named,
	 * it's name will be returned. If not, if existing, the picture associated to
	 * this action is returned. Otherwise, built-in action's text will be
	 * returned.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          the context needed by event activated
	 * @return the HTML code representing this action. If this action is named,
	 *         it's name will be returned. If not, if existing, the picture
	 *         associated to this action is returned. Otherwise, built-in action's
	 *         text will be returned.
	 * @since 0.86
	 */
	public abstract String toHtmlString(Ability ability,
			ContextEventListener context);

	/**
	 * Remove one instance of this object from the given card.
	 * 
	 * @param fromCard
	 * @param objectTest
	 *          The test applied on specific modifier to be removed.
	 */
	public void removeObject(MCard fromCard, Test objectTest) {
		if (next != null) {
			next.removeObject(fromCard, objectTest);
		}
	}

	/**
	 * When this test is true, this modifier is activated. Otherwise this modifier
	 * does nothing.
	 * 
	 * @return activation condition.
	 */
	public Test getWhileCondition() {
		return this.whileCondition;
	}

	/**
	 * Return The modifier name.
	 * 
	 * @return The modifier name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * When this event is generated this modifier is removed from the attached
	 * object. If this event is equal to <code>null</code>, this modifier can
	 * be removed only by the attached component.
	 * 
	 * @return this event is generated this modifier is removed from the attached
	 *         object.
	 */
	public List<MEventListener> getUntil() {
		return this.until;
	}

	/**
	 * Represents the events forcing this event to be refreshed.
	 * 
	 * @return the events forcing this event to be refreshed.
	 */
	public List<MEventListener> getLinkedEvents() {
		return this.linkedEvents;
	}

	/**
	 * Indicates this modifier must be destroyed if the creator of this modifier
	 * has been destroyed or has moved
	 * 
	 * @return this modifier must be destroyed if the creator of this modifier has
	 *         been destroyed or has moved
	 */
	public boolean isLinked() {
		return this.linked;
	}

	/**
	 * Return Is the strategy used to add this modifier within the existing chain
	 * 
	 * @return the strategy used to add this modifier within the existing chain
	 */
	public Layer getLayer() {
		return this.layer;
	}

	/**
	 * Return the activation zone checked before applying this modifier.
	 * 
	 * @return the activation zone checked before applying this modifier.
	 */
	public int getActivationZone() {
		return activationZone;
	}

	/**
	 * When this test is true, this modifier is activated. Otherwise this modifier
	 * does nothing.
	 */
	protected Test whileCondition;

	/**
	 * The modifier name.
	 */
	protected final String name;

	/**
	 * When this event is generated this modifier is removed from the attached
	 * object. If this event is equal to null, this modifier can be removed only
	 * by the attached component.
	 */
	protected final List<MEventListener> until;

	/**
	 * Represents the events forcing this event to be refreshed
	 */
	protected final List<MEventListener> linkedEvents;

	/**
	 * The next register modifier
	 */
	protected ModifierModel next = null;

	/**
	 * Indicates this modifier must be destroyed if the creator of this modifier
	 * has been destroyed or has moved
	 */
	protected boolean linked;

	/**
	 * Is the parameters are updated with triggers.
	 */
	protected final boolean liveUpdate;

	/**
	 * Is the strategy used to add this modifier within the existing chain
	 */
	protected Layer layer;

	/**
	 * The activation zone to match with the modifier creator zone.
	 */
	private final int activationZone;
}
