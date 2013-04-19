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
package net.sf.firemox.modifier.model;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.modifier.ModifierContext;
import net.sf.firemox.modifier.PlayableZoneModifier;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public class PlayableZoneModifierModel extends ModifierModel {

	/**
	 * Creates a new instance of PlayableZoneModifierModel <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>hasNot tag [boolean]</li>
	 * <li>playable zone [Zone]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          the input stream where action will be read
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	PlayableZoneModifierModel(InputStream inputFile) throws IOException {
		super(inputFile);
		hasNot = inputFile.read() == 0;
		idZone = inputFile.read();
	}

	/**
	 * Create a new instance from the given model.
	 * 
	 * @param other
	 *          the instance to copy
	 */
	protected PlayableZoneModifierModel(PlayableZoneModifierModel other) {
		super(other);
		hasNot = other.hasNot;
		idZone = other.idZone;
	}

	@Override
	protected void addModifierFromModelPriv(Ability ability, MCard target) {
		final PlayableZoneModifier newModifier = new PlayableZoneModifier(
				new ModifierContext(this, target, ability), idZone, hasNot);
		target.addModifier(newModifier);
		newModifier.refresh();
	}

	@Override
	public PlayableZoneModifierModel clone() {
		return new PlayableZoneModifierModel(this);
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		return LanguageManagerMDB.getString("add-playable-modifier-"
				+ (hasNot ? "disable" : "enable"), LanguageManager.getString(String
				.valueOf(idZone)));
	}

	/**
	 * Indicates if this modifier remove occurrence of the given zone. If False,
	 * it adds one instance.
	 */
	private boolean hasNot;

	/**
	 * The zone to add/remove as playable zone
	 */
	private int idZone;

}
