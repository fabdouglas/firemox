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

import java.util.List;

import net.sf.firemox.action.MAction;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.token.TrueFalseAuto;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public abstract class AbstractAbility extends Ability {

	/**
	 * Creates a new instance of AbstractAbility <br>
	 * 
	 * @param name
	 *          the ability name
	 * @param event
	 *          the event attached to this ability.
	 * @param pictureName
	 *          the optional picture used to display this ability.
	 * @param linkedAbilities
	 *          the linked abilities. May be null.
	 * @param playAsSpell
	 *          play-as-spell.
	 */
	public AbstractAbility(String name, MEventListener event, String pictureName,
			List<Ability> linkedAbilities, TrueFalseAuto playAsSpell) {
		super(name, Optimization.none, Priority.hidden, pictureName);
		this.playAsSpell = playAsSpell;
		this.linkedAbilities = linkedAbilities;
		eventComing = event;
	}

	@Override
	public MCard getCard() {
		return SystemCard.instance;
	}

	@Override
	public boolean isMatching() {
		return true;
	}

	@Override
	public MAction[] actionList() {
		return actionList;
	}

	@Override
	public MAction[] effectList() {
		return effectList;
	}

	@Override
	public boolean recheckTargets() {
		return true;
	}

	/**
	 * will contains MAction objects of cost part
	 */
	protected MAction[] actionList;

	/**
	 * will contains MAction objects of effect part
	 */
	protected MAction[] effectList;
}