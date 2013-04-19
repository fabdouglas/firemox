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
package net.sf.firemox.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.clickable.target.card.CardModel;
import net.sf.firemox.clickable.target.card.LoadMode;
import net.sf.firemox.clickable.target.card.MCard;

/**
 * This class represents card's name and offset of card's code in mdb file.
 * 
 * @author Jan Blaha
 * @author Fabrice Daugan
 * @since 0.53
 */
public class MCardCompare implements Comparator<MCardCompare>,
		Comparable<MCardCompare> {

	/**
	 * Create a new instance of MCardCompare
	 */
	public MCardCompare() {
		super();
		name = null;
		mdbOffset = 0L;
	}

	/**
	 * Create a new instance of MCardCompare
	 * 
	 * @param name
	 *          card's name.
	 * @param mdbOffset
	 *          The MDB offset of first byte of card.
	 */
	public MCardCompare(String name, long mdbOffset) {
		this.name = name;
		this.mdbOffset = mdbOffset;
	}

	/**
	 * Create a new instance of MCardCompare
	 * 
	 * @param name
	 *          card's name.
	 * @param amount
	 *          amount of card
	 * @param properties
	 *          the optional properties attached to these cards.
	 * @param mdbOffset
	 *          The MDB offset of first byte of card.
	 */
	public MCardCompare(String name, int amount, Map<String, String> properties,
			long mdbOffset) {
		this(name, mdbOffset);
		this.amount = amount;
		if (properties != null) {
			this.constraints.putAll(properties);
		}
	}

	/**
	 * return card's name
	 * 
	 * @return card's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * return the associated integer
	 * 
	 * @return the associated integer
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * Add some cards.
	 * 
	 * @param amount
	 *          the amount of card to add.
	 */
	public void add(int amount) {
		this.amount += amount;
	}

	/**
	 * Remove some cards.
	 * 
	 * @param amount
	 *          the amount of card to remove.
	 */
	public void remove(int amount) {
		this.amount -= amount;
	}

	/**
	 * @return the list of properties of the current card
	 */
	public Map<String, String> getConstraints() {
		return constraints;
	}

	/**
	 * return the string representation of this item.
	 * 
	 * @return <b>name </b>; <b>amount </b>; <b>constraints </b>
	 */
	@Override
	public String toString() {
		if (amount == 0 && constraints.isEmpty()) {
			return name;
		}
		StringBuffer buffer = new StringBuffer(name);
		if (amount > 0) {
			buffer.append(";");
			buffer.append(amount);
		}
		for (Map.Entry<String, String> constraint : constraints.entrySet()) {
			buffer.append(";");
			buffer.append(constraint.getKey());
			buffer.append("=");
			buffer.append(constraint.getValue());
		}
		return buffer.toString();
	}

	public int compare(MCardCompare o1, MCardCompare o2) {
		return o1.name.toLowerCase().compareTo(o2.name.toLowerCase());
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof String) {
			return ((String) other).equalsIgnoreCase(name);
		}
		return ((MCardCompare) other).name.equalsIgnoreCase(name);
	}

	@Override
	public int hashCode() {
		return name.toLowerCase().hashCode();
	}

	public int compareTo(MCardCompare o) {
		return name.toLowerCase().compareTo(o.name.toLowerCase());
	}

	/**
	 * Return the MDB offset of first byte of card.
	 * 
	 * @return the MDB offset of first byte of card.
	 */
	public long getMdbOffset() {
		return this.mdbOffset;
	}

	/**
	 * The list of properties associated to the card.
	 */
	private Map<String, String> constraints = new HashMap<String, String>(5);

	/**
	 * represents the card's name
	 */
	private String name;

	/**
	 * The MDB offset of first byte of card.
	 */
	private long mdbOffset;

	/**
	 * represents the associated amount
	 */
	private int amount;

	/**
	 * @param realCardName
	 */
	public void setName(String realCardName) {
		this.name = realCardName;
	}

	/**
	 * @param amount
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * @param mdbOffset
	 *          The mdbOffset to set.
	 */
	public void setMdbOffset(long mdbOffset) {
		this.mdbOffset = mdbOffset;
	}

	/**
	 * Return an implementation of card corresponding to this card name. The
	 * returned instance cannot be modified and only contains the database data.
	 * 
	 * @return a lightweight implementation of a card corresponding to this card
	 *         name.
	 * @param dbStream
	 *          the MDB stream's header.
	 * @throws IOException
	 *           if input exception occurred.
	 */
	public Target getCard(FileInputStream dbStream) throws IOException {
		if (card == null) {
			dbStream.getChannel().position(this.mdbOffset);
			card = new MCard(this.name, dbStream, null, null, constraints);
		}
		return card;
	}

	/**
	 * Return an implementation of card corresponding to this card name. The
	 * returned instance cannot be modified and only contains the database data.
	 * 
	 * @return a lightweight implementation of a card corresponding to this card
	 *         name.
	 * @param dbStream
	 *          the MDB stream's header.
	 * @throws IOException
	 *           if input exception occurred.
	 */
	public CardModel getModel(FileInputStream dbStream) throws IOException {
		dbStream.getChannel().position(this.mdbOffset);
		return CardFactory.getCardModel(name, dbStream, LoadMode.lazy);
	}

	@Override
	public MCardCompare clone() {
		MCardCompare clone = new MCardCompare(name, amount, constraints, mdbOffset);
		clone.card = card;
		return clone;
	}

	/**
	 * A lightweight implementation of a card corresponding to this card name.
	 */
	private MCard card = null;
}