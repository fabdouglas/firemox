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
package net.sf.firemox.stack;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public interface StackContext {

	/**
	 * Return the target option of the current spell. this target option is owned
	 * by the current spell. May be reseted, changed by the spell itself.
	 * 
	 * @return the targeted list of this context.
	 */
	TargetedList getTargetedList();

	/**
	 * Return the current context. Null if current ability is not a triggered one.
	 * 
	 * @return the current context. Null if current ability is not a triggered
	 *         one.
	 */
	ContextEventListener getAbilityContext();

	/**
	 * Return the action manager of this context.
	 * 
	 * @return the action manager of this context.
	 */
	ActionManager getActionManager();

	/**
	 * Return the card source of the current capcity/spell in the stack
	 * 
	 * @return the card source of the current capcity/spell in the stack
	 */
	MCard getSourceCard();

	/**
	 * Return the ability causing the abortion of this ability.
	 * 
	 * @return the ability causing the abortion of this ability.
	 */
	Ability getAbortingAbility();

	/**
	 * Remove the specified card from the stack. If it's a spell, it goes to the
	 * abortion place, otherwise it would be simply removed. After calling this
	 * method, caller should call the normal 'resolveStack' method.
	 * 
	 * @param card
	 *          the card to make abort.
	 * @param source
	 *          the ability source.
	 */
	void abortion(AbstractCard card, Ability source);
}
