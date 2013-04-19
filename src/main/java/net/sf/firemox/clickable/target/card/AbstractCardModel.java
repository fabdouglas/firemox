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

import java.io.InputStream;
import java.util.HashSet;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.modifier.model.ModifierModel;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.apache.commons.lang.StringUtils;

/**
 * @version 0.95
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
abstract class AbstractCardModel implements CardModel {

	/**
	 * Properties of this card. These properties are these one readable on these
	 * card.
	 */
	protected int[] properties;

	/**
	 * Shared registers of this target. This register would never be modifier.
	 */
	protected int[] staticRegisters;

	/**
	 * Colors of this card.
	 */
	protected int idColor;

	/**
	 * Card type of this card.
	 */
	protected int idCard;

	/**
	 * Set of keywords.
	 */
	protected java.util.Set<String> keywords;

	/**
	 * The translated name. If null or empty, it would be equals to the given
	 * English name.
	 */
	protected String localName;

	/**
	 * The English name of this card. May contain any character. If null or empty,
	 * it would be equals to the card name (id).
	 */
	protected String cardName;

	/**
	 * The file-serializable card name.
	 */
	protected String keyName;

	/**
	 * XML rule designer of loaded cards.
	 */
	protected String cardRulesCredits;

	/**
	 * The language used for this card. By default, is
	 * <code>LanguageManager.DEFAULT_LANGUAGE</code>
	 */
	private String language = LanguageManager.DEFAULT_LANGUAGE;

	public String getLanguage() {
		return language;
	}

	public String getKeyName() {
		return keyName;
	}

	public String getLocalName() {
		return localName;
	}

	public String getCardName() {
		return cardName;
	}

	public void setRulesCredits(String cardRulesCredits) {
		if (cardRulesCredits == null || cardRulesCredits.trim().length() == 0) {
			this.cardRulesCredits = null;
		} else {
			this.cardRulesCredits = cardRulesCredits.intern();
		}
	}

	public void setLocalName(String localName) {
		if (isValidValue(localName)) {
			this.localName = localName;
		}
	}

	public String getRulesCredit() {
		return cardRulesCredits;
	}

	public boolean hasKeywords(String keyword) {
		return keywords != null && keywords.contains(keyword);
	}

	/**
	 * Creates a new instance with the specified card name(id). The translated and
	 * English names are initialized to this id.
	 * 
	 * @param cardName
	 *          the card name (id) of this instance.
	 */
	public AbstractCardModel(String cardName) {
		setCardName(cardName);
		this.properties = new int[0];
		this.staticRegisters = new int[0];
		this.keywords = new HashSet<String>(0);
	}

	/**
	 * Complete this instance with content of specified stream.<br>
	 * Structure of InputStream : Data[size]
	 * <ul>
	 * <li>card name [String]</li>
	 * <li>art author [String]</li>
	 * <li>rules author [String]</li>
	 * <li>keywords [String[]]</li>
	 * <li>registers [IdTokens.CARD_REGISTER_SIZE]</li>
	 * <li>idCard [2]</li>
	 * <li>idColor [1]</li>
	 * <li>sorted properties [int[]]</li>
	 * </ul>
	 * Creates a new instance with the specified card name(id). The translated and
	 * English names are initialized to this id.
	 * 
	 * @param cardName
	 *          the card name (id) of this instance.
	 * @param inputStream
	 *          the input stream containing the data of this card.
	 */
	public AbstractCardModel(String cardName, InputStream inputStream) {
		this(cardName);
		try {
			setRulesCredits(MToolKit.readString(inputStream));
			int count = inputStream.read();
			if (count > 0) {
				while (count-- > 0) {
					keywords.add(MToolKit.readString(inputStream));
				}
			}

			// read registers
			staticRegisters = new int[IdTokens.CARD_REGISTER_SIZE];
			for (int i = 0; i < IdTokens.CARD_REGISTER_SIZE; i++) {
				staticRegisters[i] = inputStream.read();
			}

			// Read card type, coded with 2 byte
			idCard = MToolKit.readInt16(inputStream);

			// Read card color, coded with one byte
			idColor = inputStream.read();

			// Read list of properties, i.e. first strike,trample,Legend,Knight,...
			properties = new int[inputStream.read()];
			for (int a = 0; a < properties.length; a++) {
				properties[a] = MToolKit.readInt16(inputStream);
			}

		} catch (Throwable e) {
			throw new RuntimeException("reading card " + CardFactory.lastCardName, e);
		}
	}

	/**
	 * Create a new instance of this class from another instance.
	 * 
	 * @param other
	 *          the model's properties to copy to this instance.
	 */
	public AbstractCardModel(AbstractCardModel other) {
		this(other.getCardName());
		keywords = other.keywords;
		cardRulesCredits = other.cardRulesCredits;
		staticRegisters = other.staticRegisters;
		idCard = other.idCard;
		idColor = other.idColor;
		properties = other.properties;
	}

	private static boolean isValidValue(String str) {
		return !StringUtils.isBlank(str);
	}

	@Override
	public String toString() {
		return localName;
	}

	public int getIdCard() {
		return idCard;
	}

	public int getIdColor() {
		return idColor;
	}

	public int[] getStaticRegisters() {
		return this.staticRegisters;
	}

	public int[] getProperties() {
		return properties;
	}

	public int compareTo(CardModel o) {
		return cardName.compareTo(o.getCardName());
	}

	/**
	 * The the new card name.
	 * 
	 * @param cardName
	 *          the new card name.
	 */
	public void setCardName(String cardName) {
		this.cardName = cardName;
		CardFactory.lastCardName = cardName;
		this.localName = cardName;
		this.keyName = MToolKit.getKeyName(cardName);
	}

	public abstract Ability[] getAbilities();

	public abstract Attachment getAttachment();

	public abstract ModifierModel getModifierModels();

	public abstract void setModifierModels(ModifierModel modifierModels);

	@Override
	public int hashCode() {
		return keyName.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return this == other
				|| (other instanceof CardModel && ((CardModel) other).getKeyName()
						.equals(keyName));
	}
}