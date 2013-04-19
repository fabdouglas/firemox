/*
 * Created on 21 mars 2005
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
package net.sf.firemox.clickable.target.card;

import java.util.Set;

import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.modifier.ColorModifier;
import net.sf.firemox.modifier.ControllerModifier;
import net.sf.firemox.modifier.IdCardModifier;
import net.sf.firemox.modifier.PlayableZoneModifier;
import net.sf.firemox.modifier.PropertyModifier;
import net.sf.firemox.modifier.RegisterIndirection;
import net.sf.firemox.modifier.RegisterModifier;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.token.IdTokens;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public class LastKnownCard extends MCard implements LastKnownCardInfo {

	/**
	 * @param originalCard
	 *          the original card reference.
	 * @param destinationZone
	 *          destination id zone.
	 * @param idColor
	 *          the original color id.
	 * @param idCard
	 *          the original card type.
	 * @param tapped
	 *          the original tapped position.
	 * @param registers
	 *          the original registers.
	 * @param controller
	 *          the original controller.
	 * @param owner
	 *          the original owner.
	 * @param properties
	 *          the original properties.
	 * @param timestamp
	 *          the snapshot timestamp.
	 * @param timestampReferences
	 *          the references to the cards.
	 */
	public LastKnownCard(MCard originalCard, int destinationZone, int idColor,
			int idCard, boolean tapped, int[] registers, Player controller,
			Player owner, Set<Integer> properties, int timestamp,
			int timestampReferences) {
		// the current zone of the referenced card and not the last known zone.
		super();
		if (originalCard != null) {
			this.database = originalCard.database;
		}
		this.idZone = destinationZone;
		this.cachedIdColor = idColor;
		this.cachedIdCard = idCard;
		this.cachedRegisters = registers;
		this.cachedProperties = properties;
		this.tapped = tapped;
		this.controller = controller;
		this.owner = owner;
		this.originalCard = originalCard;
		this.timeStamp = timestamp;
		this.timestampReferences = timestampReferences;
	}

	@Override
	public void addTimestampReference() {
		originalCard.addTimestampReference();
	}

	@Override
	public void reverse(boolean reversed) {
		originalCard.reverse(reversed);
	}

	public LastKnownCard createLastKnownCard() {
		return this;
	}

	@Override
	public Target getOriginalTargetable() {
		return originalCard;
	}

	/**
	 * Remove a reference to the given timestamp of card.
	 * 
	 * @param timestamp
	 *          the timestamp reference
	 * @return true if the given timestamp is no more referenced.
	 */
	public boolean removeTimestamp(int timestamp) {
		return --timestampReferences <= 0;
	}

	@Override
	public boolean isSameState(int zoneConstaint) {
		return originalCard.isSameState(zoneConstaint);
	}

	@Override
	public boolean isSameIdZone(int idZone) {
		return originalCard.isSameIdZone(idZone);
	}

	@Override
	public int getIdZone() {
		return originalCard.getIdZone();
	}

	@Override
	public void tap(boolean tapped) {
		originalCard.tap(tapped);
	}

	@Override
	public void moveCard(int newIdPlace, Player newController,
			boolean newIsTapped, int idPosition) {

		// restore available abilities
		originalCard.registerAbilities(newIdPlace);
		originalCard.moveCard(newIdPlace, newController, newIsTapped, idPosition);
	}

	// TODO making the difference between 'register indirection' and 'register
	@Override
	public int getValue(int index) {
		if (index == IdTokens.MANA_POOL) {
			int res = 0;
			for (int i = 6; i-- > 0;) {
				res += cachedRegisters[i];
			}
			return res;
		}
		return cachedRegisters[index];
	}

	@Override
	public void addModifier(ColorModifier modifier) {
		originalCard.addModifier(modifier);
	}

	@Override
	public void addModifier(IdCardModifier modifier) {
		originalCard.addModifier(modifier);
	}

	@Override
	public void addModifier(PropertyModifier modifier) {
		originalCard.addModifier(modifier);
	}

	@Override
	public void addModifier(ControllerModifier modifier) {
		originalCard.addModifier(modifier);
	}

	@Override
	public void addModifier(PlayableZoneModifier modifier) {
		originalCard.addModifier(modifier);
	}

	@Override
	public void addModifier(RegisterModifier modifier, int index) {
		originalCard.addModifier(modifier, index);
	}

	@Override
	public void addModifier(RegisterIndirection modifier, int index) {
		originalCard.addModifier(modifier, index);
	}

	@Override
	public void removeModifier(RegisterModifier modifier, int index) {
		originalCard.removeModifier(modifier, index);
	}

	@Override
	public void removeModifier(RegisterIndirection indirection, int index) {
		originalCard.removeModifier(indirection, index);
	}

	@Override
	public void removeModifier(IdCardModifier modifier) {
		originalCard.removeModifier(modifier);
	}

	@Override
	public void removeModifier(ControllerModifier modifier) {
		originalCard.removeModifier(modifier);
	}

	@Override
	public void removeModifier(PropertyModifier modifier) {
		originalCard.removeModifier(modifier);
	}

	@Override
	public void removeModifier(PlayableZoneModifier modifier) {
		originalCard.removeModifier(modifier);
	}

	@Override
	public void removeModifier(ColorModifier modifier) {
		originalCard.removeModifier(modifier);
	}

	@Override
	public boolean playableZone(int supposedZone, int idZone) {
		return originalCard.playableZone(supposedZone, idZone);
	}

	@Override
	public void setValue(int index, Operation operation, int rightValue) {
		originalCard.setValue(index, operation, rightValue);
	}

	@Override
	public boolean isInPosition(int position) {
		return originalCard.isInPosition(position);
	}

	/**
	 * The referenced card.
	 */
	private MCard originalCard;

}
