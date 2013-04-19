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
import java.io.OutputStream;
import java.util.List;

import net.sf.firemox.clickable.target.card.TriggeredCard;
import net.sf.firemox.clickable.target.card.TriggeredCardChoice;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.Log;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.91
 */
public enum Optimization {

	/**
	 * no optimization is done.
	 */
	none,

	/**
	 * An ability is not added in the TBZ if the last ability is the same. Only
	 * ability is compared, the context is ignored.
	 */
	follow,

	/**
	 * The ability would not be added if it already exists in the TBZ.
	 */
	first,

	/**
	 * The ability would replace any existing ability in the TBZ.
	 */
	last,

	/**
	 * Same ability is added only once per action. This important for
	 * LoopingAction.
	 */
	action,

	/**
	 * Ability is added to the TBZ but during the resolution a prompt asks to
	 * controller only one ability to be added to the stack.
	 */
	choice,

	/**
	 * Same ability is added only once per ability triggering this event.
	 */
	event,

	/**
	 * An ability is not added in the TBZ if the last ability is the same. Ability
	 * and context are compared.
	 */
	context;

	/**
	 * Add the specified ability to the TBZ.
	 * 
	 * @param ability
	 *          the ability to add
	 * @param context
	 *          the attached context
	 * @param where
	 *          the list where the constructed triggered card would be added
	 * @return true if the specified ability has been added
	 */
	public boolean addTo(Ability ability, ContextEventListener context,
			List<TriggeredCard> where) {
		// no optimization for this add
		switch (this) {
		case none:
			return where.add(ability.getTriggeredClone(context));
		case follow:
			if (!where.isEmpty()) {
				final TriggeredCard triggeredCard = where.get(where.size() - 1);
				if (ability.equals(context, triggeredCard.triggeredAbility,
						triggeredCard.getAbilityContext())) {
					// the last ability is the one to add
					return false;
				}
			}
			return where.add(ability.getTriggeredClone(context));
		case first:
			for (int i = where.size(); i-- > 0;) {
				final TriggeredCard triggeredCard = where.get(i);
				if (ability.equals(context, triggeredCard.triggeredAbility,
						triggeredCard.getAbilityContext())) {
					// this ability would not be added
					return false;
				}
			}
			return where.add(ability.getTriggeredClone(context));
		case last:
			for (int i = where.size(); i-- > 0;) {
				final TriggeredCard triggeredCard = where.get(i);
				if (ability.equals(context, triggeredCard.triggeredAbility,
						triggeredCard.getAbilityContext())) {
					// this ability would replace the old one
					where.remove(i);
					where.add(ability.getTriggeredClone(context));
					return false;
				}
			}
			return where.add(ability.getTriggeredClone(context));
		case action:
			return where.add(ability.getTriggeredClone(context));
		case choice:
			for (int i = where.size(); i-- > 0;) {
				final TriggeredCard triggeredCard = where.get(i);
				if (triggeredCard.abilityID == StackManager.abilityID
						&& ability.equals(context, triggeredCard.triggeredAbility,
								triggeredCard.getAbilityContext())) {
					/*
					 * Same ability as already generating one instance of this triggered
					 * ability
					 */
					((TriggeredCardChoice) where.get(i)).addChoice(ability, context);
					return false;
				}
			}
			return where.add(ability.getTriggeredCloneChoice(context));
		case event:
			for (int i = where.size(); i-- > 0;) {
				if (where.get(i).abilityID == StackManager.abilityID
						&& ability.equals(where.get(i).triggeredAbility)) {
					// Same ability as already generating one instance of this triggered
					// ability
					return false;
				}
			}
			return where.add(ability.getTriggeredClone(context));
		case context:
			if (!where.isEmpty()) {
				final TriggeredCard triggeredCard = where.get(where.size() - 1);
				if (ability.equals(context, triggeredCard.triggeredAbility,
						triggeredCard.getAbilityContext())
						&& context.equals(triggeredCard.getAbilityContext())) {
					// the last ability is the one to add
					return false;
				}
			}
			return where.add(ability.getTriggeredClone(context));
		default:
			Log.fatal("unmanaged optimization : " + this);
			return false;
		}
	}

	/**
	 * Write this enumeration to the given output stream.
	 * 
	 * @param out
	 *          the stream this enumeration would be written.
	 * @throws IOException
	 */
	public void write(OutputStream out) throws IOException {
		out.write(ordinal());
	}

	/**
	 * Read and return the enumeration from the given input stream.
	 * 
	 * @param input
	 *          the stream containing the enumeration to read.
	 * @return the enumeration from the given input stream.
	 * @throws IOException
	 */
	static Optimization valueOf(InputStream input) throws IOException {
		return values()[input.read()];
	}
}
