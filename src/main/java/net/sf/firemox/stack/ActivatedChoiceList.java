/*
 * MChoiceList.java 
 * Created on 27 octobre 2002, 14:00
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
package net.sf.firemox.stack;

import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Log;
import net.sf.firemox.zone.MZone;

/**
 * This class is representing all abilities activated for an event. This
 * abilities are grouped by cards.
 * 
 * @author Fabrice Daugan
 */
public class ActivatedChoiceList {

	/**
	 * Creates a new instance of MChoiceList with the specified playable
	 * spells/abilities.
	 * 
	 * @param abilityList
	 *          is the specified playable spells/abilities
	 * @param advancedList
	 *          is the specified playable advanced spells/abilities
	 */
	public ActivatedChoiceList(List<Ability> abilityList,
			List<Ability> advancedList) {
		if (abilityList == null || abilityList.isEmpty()) {
			Log.debug("no playable abilities");
		} else {
			Log.debug("playable abilities:" + abilityList.size());
			choiceList = new ArrayList<ActivatedChoice>(abilityList.size());
			for (Ability ability : abilityList) {
				boolean found = false;
				for (ActivatedChoice choice : choiceList) {
					if (choice.getCard() == ability.getCard()) {
						// the card has been found, we add this ability to this choice
						choice.addAbility(ability);
						found = true;
						break;
					}
				}
				if (!found) {
					// no card was found, so it's a new playable card
					choiceList.add(new ActivatedChoice(ability));
				}
			}
			if (advancedList != null && !advancedList.isEmpty()) {
				advancedChoiceList = new ArrayList<ActivatedChoice>(advancedList.size());
				for (Ability ability : advancedList) {
					boolean found = false;
					for (ActivatedChoice choice : advancedChoiceList) {
						if (choice.getCard() == ability.getCard()) {
							// the card has been found, we add this ability to this choice
							choice.addAbility(ability);
							found = true;
							break;
						}
					}
					if (!found) {
						// no card was found, so it's a new playable card
						advancedChoiceList.add(new ActivatedChoice(ability));
					}
				}
			}
		}
	}

	/**
	 * remove all choices from the list
	 */
	public void clear() {
		disHighLight();
		for (ActivatedChoice choice : choiceList) {
			choice.clear();
		}
		choiceList.clear();
		if (advancedChoiceList != null) {
			for (ActivatedChoice choice : advancedChoiceList) {
				choice.clear();
			}
			advancedChoiceList.clear();
		}
	}

	/**
	 * return the list of playable abilities of this card
	 * 
	 * @param target
	 *          the card shearching it's playable abilities
	 * @return list of playable abilities of this card, or null if this
	 *         card/player has no playable ability.
	 */
	public List<Ability> abilitiesOf(Target target) {
		for (ActivatedChoice choice : choiceList) {
			if (choice.getCard() == target) {
				return choice.abilities;
			}
		}
		return null;
	}

	/**
	 * return the list of playable advanced abilities of this card
	 * 
	 * @param target
	 *          the card shearching it's playable abilities
	 * @return list of playable advanced abilities of this card, or null if this
	 *         card/player has no playable ability.
	 */
	public List<Ability> advancedAbilitiesOf(Target target) {
		if (advancedChoiceList != null) {
			for (ActivatedChoice choice : advancedChoiceList) {
				if (choice.getCard() == target) {
					return choice.abilities;
				}
			}
		}
		return null;
	}

	/**
	 * Hightlight all cards containing any ability of this list. The cards with
	 * only advanced abilities are not highlighted.
	 * 
	 * @return the list of highlighted zones
	 */
	public List<MZone> highLight() {
		final List<MZone> res = new ArrayList<MZone>();
		final boolean[] highlightedZones = new boolean[IdZones.NB_ZONE * 2];
		for (int a = choiceList.size(); a-- > 0;) {
			choiceList.get(a).highLight(highlightedZones);
		}
		for (int i = highlightedZones.length; i-- > 0;) {
			if (highlightedZones[i]) {
				res.add(StackManager.PLAYERS[i / IdZones.NB_ZONE].zoneManager
						.getContainer(i % IdZones.NB_ZONE));
				res.get(res.size() - 1).highLight(CardFactory.ACTIVATED_COLOR);
			}
		}
		return res;
	}

	/**
	 * Dishightlight all cards containing any ability of this list.
	 */
	public void disHighLight() {
		for (ActivatedChoice choice : choiceList) {
			choice.disHighLight();
		}
		if (advancedChoiceList != null) {
			for (ActivatedChoice choice : advancedChoiceList) {
				choice.disHighLight();
			}
		}
	}

	/**
	 * List of MChoice object
	 */
	private List<ActivatedChoice> choiceList;

	/**
	 * List of MChoice object (advanced)
	 */
	private List<ActivatedChoice> advancedChoiceList;
}