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
package net.sf.firemox.zone;

import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.test.Test;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public class ZoneSector {

	private final List<AbstractCard> cards;

	private final SectorConfiguration configuration;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param configuration
	 *          the sector configuration.
	 */
	ZoneSector(SectorConfiguration configuration) {
		this.configuration = configuration;
		cards = new ArrayList<AbstractCard>();
	}

	/**
	 * The container constraint of this sector.
	 * 
	 * @return The container constraint of this sector.
	 */
	public Object getConstraint() {
		return this.configuration.getConstraint();
	}

	/**
	 * The sector test.
	 * 
	 * @return The sector test.
	 */
	public Test getTest() {
		return this.configuration.getTest();
	}

	/**
	 * Return the cards in this sector.
	 * 
	 * @return the cards in this sector.
	 */
	public List<AbstractCard> getCards() {
		return cards;
	}

	/**
	 * Add a card to the end of list.
	 * 
	 * @param card
	 *          the card to add.
	 */
	public void add(MCard card) {
		cards.add(card);
	}

	/**
	 * Remove the card from the list.
	 * 
	 * @param card
	 *          the card to remove.
	 * @return <tt>true</tt> if this list contained the specified element
	 */
	public boolean remove(AbstractCard card) {
		return cards.remove(card);
	}

}
