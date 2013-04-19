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
package net.sf.firemox.event.context;

import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.63
 */
public class MContextCardCardIntInt extends MContextTarget {

	/**
	 * Creates a new instance of MContextCardCardIntInt <br>
	 * 
	 * @param dest
	 *          the MTargetable object to save to this context.
	 */
	public MContextCardCardIntInt(Target dest) {
		this(dest, null, 0, 0, -1, -1);
	}

	/**
	 * Creates a new instance of MContextCardCardIntInt <br>
	 * 
	 * @param dest
	 *          the MTargetable object to save to this context.
	 * @param value
	 *          the integer to save to this context.
	 */
	public MContextCardCardIntInt(Target dest, int value) {
		this(dest, null, value, 0, -1, -1);
	}

	/**
	 * Creates a new instance of MContextCardCardIntInt <br>
	 * 
	 * @param dest
	 *          the MTargetable object to save to this context.
	 * @param source
	 *          the card object to save to this context.
	 */
	public MContextCardCardIntInt(Target dest, MCard source) {
		this(dest, source, 0, 0, -1, -1);
	}

	/**
	 * Creates a new instance of MContextCardCardIntInt <br>
	 * 
	 * @param dest
	 *          the MTargetable object to save to this context.
	 * @param source
	 *          the card object to save to this context.
	 * @param value
	 *          the integer to save to this context.
	 * @param value2
	 *          the integer to save to this context.
	 */
	public MContextCardCardIntInt(Target dest, MCard source, int value, int value2) {
		this(dest, source, value, value2, -1, -1);
	}

	/**
	 * Creates a new instance of MContextCardCardIntInt <br>
	 * 
	 * @param dest
	 *          the MTargetable object to save to this context.
	 * @param source
	 *          the card object to save to this context.
	 * @param value
	 *          the integer to save to this context.
	 * @param value2
	 *          the integer to save to this context.
	 * @param maxTimeStamp1
	 *          is the maximum timestamp allowed for destination card during the
	 *          resolution.
	 * @param maxTimeStamp2
	 *          is the maximum timestamp allowed for source card during the
	 *          resolution.
	 */
	public MContextCardCardIntInt(Target dest, MCard source, int value,
			int value2, int maxTimeStamp1, int maxTimeStamp2) {
		super(dest, maxTimeStamp1);
		if (source != null) {
			this.card = (MCard) source.getOriginalTargetable();
			this.card.addTimestampReference();
		}
		if (maxTimeStamp2 == -1 && source != null) {
			this.timeStamp2 = this.card.getTimestamp();
		} else {
			this.timeStamp2 = maxTimeStamp2;
		}
		this.intToSave = value;
		this.value2 = value2;
	}

	/**
	 * Return the MTargetable cast to MCard object of this context considering
	 * it's timestamp. The returned object is the same as it was when this context
	 * has been created.
	 * 
	 * @return The other Card object that was saved.
	 */
	public MCard getCard2() {
		if (card == null) {
			return null;
		}
		if (timeStamp2 > card.getTimestamp()) {
			return (MCard) card.getLastKnownTargetable(card.getTimestamp());
		}
		return (MCard) card.getLastKnownTargetable(timeStamp2);
	}

	/**
	 * Return the MTargetable cast to MCard object of this context without
	 * considering it's timestamp.
	 * 
	 * @return the MTargetable cast to MCard object of this context without
	 *         considering it's timestamp.
	 */
	public MCard getOriginalCard2() {
		return card;
	}

	@Override
	public void removeTimestamp() {
		super.removeTimestamp();
		if (card != null) {
			if (timeStamp2 > card.getTimestamp()) {
				card.decrementTimestampReference(card.getTimestamp());
			} else {
				card.decrementTimestampReference(timeStamp2);
			}
		}
	}

	/**
	 * Returns The integer that was saved.
	 * 
	 * @return The integer that was saved.
	 */
	public final int getValue() {
		return intToSave;
	}

	/**
	 * Returns The other integer that was saved.
	 * 
	 * @return The other integer that was saved.
	 */
	public final int getValue2() {
		return value2;
	}

	@Override
	public boolean checkTimeStamp(MCard card) {
		if (this.card == card) {
			return card.getTimestamp() <= timeStamp2;
		}
		return super.checkTimeStamp(card);
	}

	/**
	 * Indicates whether the second card is null or not.
	 * 
	 * @return true if the second card is null.
	 */
	public boolean isNull2() {
		return card == null;
	}

	@Override
	public boolean equals(Object context) {
		return super.equals(context) && context instanceof MContextCardCardIntInt
				&& ((MContextCardCardIntInt) context).card == card
				&& ((MContextCardCardIntInt) context).intToSave == intToSave
				&& ((MContextCardCardIntInt) context).value2 == value2
				&& ((MContextCardCardIntInt) context).timeStamp2 == timeStamp2;
	}

	@Override
	public int hashCode() {
		return card.hashCode();
	}

	@Override
	public String toString() {
		return new StringBuilder("{source=").append(eventSourceCard).append(
				",context1=").append(targetToSave).append(",context2=").append(card)
				.append(",int1=").append(intToSave + ",int2=").append(value2).append(
						"}").toString();
	}

	/**
	 * This timestamp corresponds to the amount of card movements.
	 */
	private int timeStamp2;

	/**
	 * The second card that was saved.
	 */
	private MCard card;

	/**
	 * The integer that was saved.
	 */
	private int intToSave;

	/**
	 * The other integer that was saved.
	 */
	private int value2;

}