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
package net.sf.firemox.modifier;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.action.RefreshModifier;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.RefreshAbility;
import net.sf.firemox.clickable.ability.RemoveModifier;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.MovedCard;
import net.sf.firemox.event.TriggeredEvent;
import net.sf.firemox.modifier.model.Layer;
import net.sf.firemox.test.And;
import net.sf.firemox.test.InZone;
import net.sf.firemox.test.IsTested;
import net.sf.firemox.test.Not;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.tools.Log;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public abstract class Modifier implements Unregisterable, ObjectModifier {

	/**
	 * Creates a new instance of Modifier <br>
	 * 
	 * @param context
	 *          the modifier context.
	 */
	protected Modifier(ModifierContext context) {
		this.name = context.getModel().getName();
		this.to = context.getTo();
		this.layer = context.getModel().getLayer();
		this.ability = context.getAbility();
		this.whileCondition = context.getModel().getWhileCondition();
		final MCard creator = this.ability.getCard();
		abilities = new ArrayList<Ability>();

		/*
		 * create the abilities linked to this modifier in order to maintain the
		 * coherence. Any event that could modify the "while-test" make a triggered
		 * ability to be created listening this event.
		 */
		if (context.getModel().getLinkedEvents() != null) {
			final RefreshModifier refreshAction = new RefreshModifier(this);
			for (MEventListener event : context.getModel().getLinkedEvents()) {
				final Ability refreshAbility = new RefreshAbility(
						(TriggeredEvent) event.clone(to), refreshAction,
						creator == null ? to : creator);
				refreshAbility.registerToManager();
				abilities.add(refreshAbility);
			}
		}

		// link creator <--> target
		if (context.getModel().isLinked() && creator != null
				&& creator != SystemCard.instance) {
			// when the creator moves to a different zone, this modifier is destroyed
			final Ability linkAbility = new RemoveModifier(new MovedCard(creator
					.getIdZone(), And.append(IsTested.TESTED_IS_ME, new InZone(creator
					.getIdZone(), TestOn.THIS)), new Not(new InZone(creator.getIdZone(),
					TestOn.THIS)), creator), this);
			linkAbility.registerToManager();
			abilities.add(linkAbility);
		}

		// when the until condition become true, this modifier is destroyed
		for (MEventListener event : context.getModel().getUntil()) {
			final Ability untilAbility = new RemoveModifier((TriggeredEvent) event
					.clone(to), this);
			untilAbility.registerToManager();
			abilities.add(untilAbility);
		}

		{
			// add the until event : if the 'to' moves, the modifier is destroyed
			final Ability untilAbility = new RemoveModifier(new MovedCard(to
					.getIdZone(), And.append(IsTested.TESTED_IS_ME, new InZone(to
					.getIdZone(), TestOn.THIS)), new Not(new InZone(to.getIdZone(),
					TestOn.THIS)), to), this);
			untilAbility.registerToManager();
			abilities.add(untilAbility);
		}

	}

	public int paintObject(Graphics g, int startX, int startY) {
		if (next != null) {
			return next.paintObject(g, startX, startY);
		}
		return startX;
	}

	/**
	 * Add a modifier to the end of the modifier chain. The current modifier stays
	 * the first modifier of this chain.
	 * 
	 * @param modifier
	 *          the property modifier to add.
	 * @return the new chain.
	 */
	public final Modifier addModifier(Modifier modifier) {
		assert modifier.next != null;
		if (modifier.next != null) {
			throw new InternalError("'next' modifier should not be specified");
		}

		// Is the specified modifier has lower layer?
		if (modifier.layer.ordinal() < layer.ordinal()) {
			// insert the modifier just before this modifier
			modifier.next = this;
			return modifier;
		}

		// Are we to the end of the chain?
		if (this.next == null) {
			// the modifier becomes the last one of the chain
			this.next = modifier;
			return this;
		}

		// continue within the chain to add this modifier to the end of this layer
		this.next = next.addModifier(modifier);
		return this;
	}

	/**
	 * remove from the manager a modifier
	 * 
	 * @param modifier
	 *          the modifier to remove
	 * @return the new chain
	 */
	protected Modifier removeModifier(Modifier modifier) {
		if (next != null) {
			next = next.removeModifier(modifier);
		} else {
			Log.warn("Cannot remove the modifier " + modifier.getClass() + ", name="
					+ modifier.name);
		}
		return this;
	}

	/**
	 * Refresh this modifier following the whileCondition.
	 */
	public abstract void refresh();

	public int getNbObjects(String objectName, Test objectTest) {
		if (next == null) {
			return 0;
		}
		return next.getNbObjects(objectName, objectTest);
	}

	public Modifier removeObject(String objectName, Test objectTest) {
		// if (next == null):no more instance of this object on the card -> ignored
		if (next != null) {
			next = next.removeObject(objectName, objectTest);
		}
		return this;
	}

	public void removeFromManager() {
		if (unregisteringModifier) {
			// we are currently unregistering this modifier
			return;
		}
		unregisteringModifier = true;
		for (int i = abilities.size(); i-- > 0;) {
			abilities.get(i).removeFromManager();
		}
		unregisteringModifier = false;
	}

	@Override
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		return name;
	}

	public MCard getCard() {
		return ability.getCard();
	}

	/**
	 * Tag indicating we are currently unregistering this modifier
	 */
	protected boolean unregisteringModifier;

	/**
	 * Tag indicating this modifier is completely unregistered and can be released
	 */
	protected boolean unregisteredModifier;

	/**
	 * The next modifier
	 */
	public Modifier next;

	/**
	 * The modifier name
	 */
	protected final String name;

	/**
	 * When this test is true, this modifier is activated. Otherwise this modifier
	 * does nothing.
	 */
	protected final Test whileCondition;

	/**
	 * Is this modifier is activated
	 */
	protected boolean activated;

	/**
	 * Target attached to.
	 */
	protected final MCard to;

	/**
	 * The ability having created this modifier.
	 */
	protected Ability ability;

	/**
	 * Abilities linked to this modifier
	 */
	protected final List<Ability> abilities;

	/**
	 * is the strategy used to add this modifier within the existing chain.
	 */
	public Layer layer;
}