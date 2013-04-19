/*
 *    Firemox is a turn based strategy simulator
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
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 * @since 0.82 timeStamp supported
 */
public class MContextTarget implements ContextEventListener {

	/**
	 * Creates a new instance of MContextTarget <br>
	 * 
	 * @param targetToSave
	 *          is the object to store
	 */
	public MContextTarget(Target targetToSave) {
		this(targetToSave, -1);
	}

	/**
	 * Creates a new instance of MContextTarget <br>
	 * 
	 * @param inTargetToSave
	 *          is the object to store
	 * @param maxTimeStamp
	 *          is the maximum timestamp allowed for destination card during the
	 *          resolution.
	 */
	public MContextTarget(Target inTargetToSave, int maxTimeStamp) {
		this.targetToSave = inTargetToSave.getOriginalTargetable();
		if (maxTimeStamp == -1 && this.targetToSave != null) {
			this.timeStamp = this.targetToSave.getTimestamp();
		} else {
			this.timeStamp = maxTimeStamp;
		}
		if (this.targetToSave != null) {
			this.targetToSave.addTimestampReference();
		}
		if (StackManager.currentAbility != null) {
			this.eventSourceCard = StackManager.currentAbility.getCard();
		}
	}

	/**
	 * Return the MTargetable object of this context considering it's timstamp.
	 * The returned object is the same as it was when this context has been
	 * created.
	 * 
	 * @return the MTargetable object of this context as it was when this context
	 *         has been created.
	 */
	public final Target getTargetable() {
		if (timeStamp > targetToSave.getTimestamp()) {
			return targetToSave.getLastKnownTargetable(targetToSave.getTimestamp());
		}
		return targetToSave.getLastKnownTargetable(timeStamp);
	}

	/**
	 * Return the MTargetable cast to MCard object of this context considering
	 * it's timestamp. The returned object is the same as it was when this context
	 * has been created.
	 * 
	 * @return the MTargetable cast to MCard object of this context as it was when
	 *         this context has been created.
	 * @see #getTargetable()
	 */
	public final MCard getCard() {
		return (MCard) getTargetable();
	}

	public void removeTimestamp() {
		if (timeStamp > targetToSave.getTimestamp()) {
			targetToSave.decrementTimestampReference(targetToSave.getTimestamp());
		} else {
			targetToSave.decrementTimestampReference(timeStamp);
		}
	}

	public boolean checkTimeStamp(MCard card) {
		if (targetToSave == card) {
			return card.getTimestamp() <= timeStamp;
		}
		return true;
	}

	/**
	 * Return the MTargetable object cast into MPlayer instance.
	 * 
	 * @return the MTargetable object cast into MPlayer instance.
	 */
	public final Player getPlayer() {
		return (Player) targetToSave;
	}

	/**
	 * Return the MTargetable object of this context without considering it's
	 * timestamp.
	 * 
	 * @return the MTargetable object of this context.
	 */
	public final Target getOriginalTargetable() {
		return targetToSave;
	}

	/**
	 * Return the MTargetable cast to MCard object of this context without
	 * considering it's timestamp.
	 * 
	 * @return the MTargetable cast to MCard object of this context without
	 *         considering it's timestamp.
	 */
	public final MCard getOriginalCard() {
		return (MCard) getOriginalTargetable();
	}

	@Override
	public boolean equals(Object context) {
		return super.equals(context)
				|| (context instanceof MContextTarget
						&& ((MContextTarget) context).targetToSave == targetToSave && ((MContextTarget) context).timeStamp == timeStamp);
	}

	@Override
	public int hashCode() {
		return targetToSave.hashCode();
	}

	public final MCard getEventSource() {
		return eventSourceCard;
	}

	@Override
	public String toString() {
		return new StringBuilder("{source=" + eventSourceCard).append(",context=")
				.append(targetToSave).append("}").toString();
	}

	public int getZoneContext() {
		// Complete to implement the AccessibleContext pattern
		return 0;
	}

	/**
	 * The stored MTargetable object.
	 */
	protected Target targetToSave;

	/**
	 * This timestamp corresponds to the amount of card movements.
	 */
	private int timeStamp;

	/**
	 * the card source of the event attached to this context.
	 */
	protected MCard eventSourceCard;

}