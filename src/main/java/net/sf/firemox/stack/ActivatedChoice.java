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

import java.util.LinkedList;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;

/**
 * MChoice.java Created on 27 octobre 2002, 12:15
 * 
 * @author Fabrice Daugan
 */
public class ActivatedChoice {

	/**
	 * creates a new instance of MChoice with a specified ability. Other abilities
	 * can be added.
	 * 
	 * @param ability
	 *          is the first ability of this choice
	 * @see #addAbility(Ability)
	 */
	public ActivatedChoice(Ability ability) {
		abilities.add(ability);
	}

	/**
	 * return the card owning these activated abilities
	 * 
	 * @return the card owning these activated abilities
	 */
	public MCard getCard() {
		return abilities.get(0).getCard();
	}

	/**
	 * highlight the card containing the abilities of this choice.
	 * 
	 * @param highlightedZone
	 *          the set of highlighted zone.
	 */
	public void highLight(boolean... highlightedZone) {
		getCard().highLight(highlightedZone);
	}

	/**
	 * dishighlight the card containing the abilities of this choice
	 */
	public void disHighLight() {
		getCard().disHighLight();
	}

	/**
	 * add an ability to the list of abilities of this choice
	 * 
	 * @param ability
	 *          is the ability to add to this choice
	 */
	public void addAbility(Ability ability) {
		abilities.add(ability);
	}

	/**
	 * remove all abilities activated for this card
	 */
	public void clear() {
		abilities.clear();
	}

	/**
	 * contains UserAbility items concerned by the current idEvent
	 */
	public List<Ability> abilities = new LinkedList<Ability>();

}