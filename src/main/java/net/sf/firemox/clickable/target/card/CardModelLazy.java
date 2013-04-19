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

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.modifier.model.ModifierModel;

/**
 * @version 0.95
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class CardModelLazy extends AbstractCardModel {

	/**
	 * Creates a new instance with the specified card name(id). The translated and
	 * English names are initialized to this id.
	 * 
	 * @param cardName
	 *          the card name (id) of this instance.
	 */
	public CardModelLazy(String cardName) {
		super(cardName);
	}

	/**
	 * Complete this instance with content of specified stream.<br>
	 * Structure of InputStream : Data[size]
	 * <ul>
	 * <li>abstract card model</li>
	 * </ul>
	 * Creates a new instance with the specified card name(id). The translated and
	 * English names are initialized to this id.
	 * 
	 * @param cardName
	 *          the card name (id) of this instance.
	 * @param inputStream
	 *          the input stream containing the data of this card.
	 */
	public CardModelLazy(String cardName, InputStream inputStream) {
		super(cardName, inputStream);
	}

	/**
	 * Create a new instance of this class from another instance.
	 * 
	 * @param other
	 *          the model's properties to copy to this instance.
	 */
	public CardModelLazy(CardModelLazy other) {
		super(other);
	}

	@Override
	public String toString() {
		return localName;
	}

	@Override
	public Ability[] getAbilities() {
		throw new InternalError("Lazy model");
	}

	@Override
	public Attachment getAttachment() {
		throw new InternalError("Lazy model");
	}

	@Override
	public ModifierModel getModifierModels() {
		throw new InternalError("Lazy model");
	}

	@Override
	public void setModifierModels(ModifierModel modifierModels) {
		throw new InternalError("Lazy model");
	}

}