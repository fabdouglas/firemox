/*
 * Created on 12 mars 2005
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
package net.sf.firemox.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.Priority;
import net.sf.firemox.clickable.ability.ReplacementAbility;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.tools.MToolKit;

/**
 * Is the tested card owns an ability
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
class HasAbility extends TestCard {

	/**
	 * Create an instance of HasAbility by reading a file. Offset's file must
	 * pointing on the first byte of this test <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>ability name [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	HasAbility(InputStream inputFile) throws IOException {
		super(inputFile);
		abilityName = MToolKit.readString(inputFile).intern();
	}

	@Override
	protected boolean testCard(Ability ability, MCard tested) {
		MCard card = on.getCard(ability, tested);
		for (List<Ability> abilities : MEventListener.CAN_I_CAST_ABILITIES) {
			for (Ability tAbility : abilities) {
				if (card == tAbility.getCard()
						&& abilityName.equals(tAbility.getName())) {
					return true;
				}
			}
		}
		for (List<Ability> abilities : MEventListener.TRIGGRED_ABILITIES.values()) {
			for (Ability tAbility : abilities) {
				if (card == tAbility.getCard()
						&& abilityName.equals(tAbility.getName())) {
					return true;
				}
			}
		}
		for (Map<Priority, List<ReplacementAbility>> triggeredList : MEventListener.REPLACEMENT_ABILITIES
				.values()) {
			for (List<ReplacementAbility> abilities : triggeredList.values()) {
				for (Ability tAbility : abilities) {
					if (card == tAbility.getCard()
							&& abilityName.equals(tAbility.getName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * The ability name to match during each test
	 */
	private final String abilityName;

}