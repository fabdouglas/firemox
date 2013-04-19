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
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.AbilityFactory;
import net.sf.firemox.modifier.model.ModifierFactory;
import net.sf.firemox.modifier.model.ModifierModel;

/**
 * @version 0.91
 * @since 0.90
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @author <a href="mailto:kismet-sl@users.sourceforge.net">Stefano "Kismet"
 *         Lenzi</a>
 */
public class CardModelImpl extends AbstractCardModel {
	/**
	 * The shared modifier models (all cards with same id have the same modifier
	 * models objects)
	 */
	protected ModifierModel modifierModels;

	/**
	 * List of actions when casting : Ability[]
	 */
	protected Ability[] abilities;

	/**
	 * 
	 */
	protected Attachment attachment;

	/**
	 * Creates a new instance with the specified card name(id). The translated and
	 * English names are initialized to this id.
	 * 
	 * @param cardName
	 *          the card name (id) of this instance.
	 */
	public CardModelImpl(String cardName) {
		super(cardName);
		this.abilities = new Ability[0];
		this.attachment = null;
	}

	/**
	 * Complete this instance with content of specified stream.<br>
	 * Structure of InputStream : Data[size]
	 * <ul>
	 * <li>abstract card model</li>
	 * <li>abilities [Ability[]]</li>
	 * <li>modifiers [Modifiers[]]</li>
	 * <li>attachment 0/1 [1]</li>
	 * <li>attachments [Attachment[]]</li>
	 * </ul>
	 * Creates a new instance with the specified card name(id). The translated and
	 * English names are initialized to this id.
	 * 
	 * @param cardName
	 *          the card name (id) of this instance.
	 * @param inputStream
	 *          the input stream containing the data of this card.
	 */
	public CardModelImpl(String cardName, InputStream inputStream) {
		super(cardName, inputStream);
		try {
			// Read list of abilities
			abilities = new Ability[inputStream.read()];
			for (int a = abilities.length; a-- > 0;) {
				abilities[a] = AbilityFactory.readAbility(inputStream,
						SystemCard.instance);
			}

			// Read list of modifiers
			int count = inputStream.read();
			while (count-- > 0) {
				if (modifierModels == null) {
					modifierModels = ModifierFactory.readModifier(inputStream);
				} else {
					modifierModels.addModel(ModifierFactory.readModifier(inputStream));
				}
			}

			// Read attachment of this card
			if (inputStream.read() != 0)
				attachment = new Attachment(inputStream);
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
	public CardModelImpl(CardModelImpl other) {
		super(other);
		abilities = other.abilities;
		modifierModels = other.modifierModels;
		attachment = other.attachment;
	}

	@Override
	public Ability[] getAbilities() {
		return abilities;
	}

	@Override
	public ModifierModel getModifierModels() {
		return this.modifierModels;
	}

	@Override
	public void setModifierModels(ModifierModel modifierModels) {
		this.modifierModels = modifierModels;
	}

	@Override
	public Attachment getAttachment() {
		return this.attachment;
	}

	/**
	 * Set idCard.
	 * 
	 * @param idCard
	 *          the new idCard
	 */
	public void setIdCard(int idCard) {
		this.idCard = idCard;
	}

	/**
	 * Set color set.
	 * 
	 * @param idColor
	 *          the new color set.
	 */
	public void setIdColor(int idColor) {
		this.idColor = idColor;
	}

	/**
	 * @param abilities
	 */
	public void setAbilities(List<Ability> abilities) {
		this.abilities = abilities.toArray(new Ability[abilities.size()]);
	}

}