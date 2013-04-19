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

package net.sf.firemox.clickable.target.card;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.modifier.model.ModifierModel;

/**
 * @since 0.90
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @author <a href="mailto:kismet-sl@users.sourceforge.net">Stefano "Kismet"
 *         Lenzi</a>
 */
public interface CardModel extends Comparable<CardModel> {

	/**
	 * Returns the language used for this card.
	 * 
	 * @return the language used for this card.
	 */
	String getLanguage();

	/**
	 * Returns the file-serializable card name.
	 * 
	 * @return the file-serializable card name.
	 */
	String getKeyName();

	/**
	 * Returns the english name of this card. May contain any character. If null
	 * or empty, it would be equals to the card name (id).
	 * 
	 * @return the english card name.
	 */
	String getCardName();

	/**
	 * XML rule designer of loaded cards.
	 * 
	 * @return XML rule designer of loaded cards.
	 */
	String getRulesCredit();

	/**
	 * Indicates this card contains or not the specified keyword. Keywords are not
	 * case sensitive.
	 * 
	 * @param keyword
	 *          the tested keyword.
	 * @return true if the keyword is known by this card.
	 */
	boolean hasKeywords(String keyword);

	/**
	 * Returns the translated name. If null or empty, it would be equals to the
	 * given english name.
	 * 
	 * @return the translated card name.
	 */
	String getLocalName();

	/**
	 * Set the translated card name.
	 * 
	 * @param localName
	 *          the new local name.
	 */
	void setLocalName(String localName);

	/**
	 * Set XML rule designer of this card.
	 * 
	 * @param cardRulesCredits
	 *          the XML rule designer of loaded cards.
	 */
	void setRulesCredits(String cardRulesCredits);

	/**
	 * Return the type of this card
	 * 
	 * @return Card type of this card
	 * @since 0.91
	 */
	int getIdCard();

	/**
	 * Return the printed Colors of this card.
	 * 
	 * @return printed Colors of this card.
	 * @since 0.91
	 */
	int getIdColor();

	/**
	 * Return shared registers of this target. This register would never be
	 * modifier.
	 * 
	 * @return shared registers of this target.
	 * @since 0.91
	 */
	int[] getStaticRegisters();

	/**
	 * Return the properties of this card. These properties are these one readable
	 * on card itself.
	 * 
	 * @return the properties for this card.
	 * @since 0.91
	 */
	int[] getProperties();

	/**
	 * Return the list of actions when casting
	 * 
	 * @return List of actions when casting
	 */
	Ability[] getAbilities();

	/**
	 * Set the shared modifier models. All cards with same id have the same
	 * modifier models objects
	 * 
	 * @param modifierModels
	 *          The new modifierModels to set.
	 * @since 0.91
	 */
	void setModifierModels(ModifierModel modifierModels);

	/**
	 * Return the shared modifier models. All cards with same id have the same
	 * modifier models objects
	 * 
	 * @since 0.91
	 * @return the shared modifier models.
	 */
	ModifierModel getModifierModels();

	/**
	 * Returns the optional attachment condition and modifiers brought by this
	 * card.
	 * 
	 * @return the optional attachment condition and modifiers brought by this
	 *         card. Return <code>null</code> if there is no attachment brought
	 *         by this card.
	 */
	Attachment getAttachment();

}