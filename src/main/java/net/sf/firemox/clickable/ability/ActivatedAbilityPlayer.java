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

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.test.TestOn;

/**
 * An activated ability associated to a player.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class ActivatedAbilityPlayer extends ActivatedAbility {

	/**
	 * Creates a new instance of ActivatedAbility <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>super [ActivatedAbility]</li>
	 * <li>controller [TestOn]</li>
	 * </ul>
	 * 
	 * @param input
	 *          stream containing this ability.
	 * @param card
	 *          referenced card owning this ability.
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream.
	 */
	public ActivatedAbilityPlayer(InputStream input, MCard card)
			throws IOException {
		super(input, card);
		controller = TestOn.deserialize(input);
	}

	@Override
	public Ability clone(MCard container) {
		return new ActivatedAbilityPlayer(this, container);
	}

	/**
	 * Create a fresh instance from another instance of ActivatedAbility
	 * 
	 * @param other
	 *          the instance to clone.
	 * @param card
	 *          referenced card owning the new copy.
	 */
	protected ActivatedAbilityPlayer(ActivatedAbilityPlayer other, MCard card) {
		super(other, card);
		this.controller = other.controller;
	}

	@Override
	public Player getController() {
		return controller.getPlayer(this, null);
	}

	/**
	 * The player controlling this ability.
	 */
	private TestOn controller;
}
