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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.context.ObjectArray;
import net.sf.firemox.action.handler.FollowAction;
import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.FacedDown;
import net.sf.firemox.event.FacedUp;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.Visibility;
import net.sf.firemox.token.VisibilityChange;
import net.sf.firemox.tools.Log;

/**
 * Action used to face up or down cards referenced by the target-list.
 * 
 * @author <a href="mailto:hoani.cross@gmail.com">Hoani CROSS</a>
 * @since 0.90
 * @since 0.91 'for' constraint attribute, default [everyone] is added.
 */
class Face extends UserAction implements StandardAction, FollowAction {

	/**
	 * Create an instance of Face action.
	 * <ul>
	 * Structure of stream :
	 * <li>idAction [1]</li>
	 * <li>int8 : 0 means face-down, >0 means face-up [1]</li>
	 * <li>visibility [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          the file to read the action from
	 * @throws IOException
	 *           if an IO error occurred
	 * @see Actiontype
	 */
	Face(InputStream inputFile) throws IOException {
		super(inputFile);
		faceUpFlag = inputFile.read() > 0;
		visibilityChange = VisibilityChange.deserialize(inputFile);
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.FACE;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		for (Target target : StackManager.getInstance().getTargetedList().list) {
			if (target.isCard()) {
				final MCard card = (MCard) target;
				if (checkTimeStamp(context, card)) {
					if (faceUpFlag) {
						FacedUp.dispatchEvent(card);
						card.increaseFor(card.getController(), visibilityChange);
					} else {
						FacedDown.dispatchEvent(card);
						card.decreaseFor(card.getController(), visibilityChange);
					}
				} else {
					Log.error("In FACE action, erreur timestamp for card " + card);
				}
			} else {
				Log
						.warn("In FACE action, target list contains non 'Card' object. Ignored");
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final ObjectArray<Visibility> bContext = (ObjectArray<Visibility>) actionContext.actionContext;
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 0;) {
			if (StackManager.getInstance().getTargetedList().get(i).isCard()) {
				final MCard card = (MCard) StackManager.getInstance().getTargetedList()
						.get(i);
				card.restoreVisibility(bContext.getObject(i));
			} else {
				Log
						.warn("In FACE action, target list contains non 'Card' object. Ignored");
			}
		}
	}

	public void simulate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final ObjectArray<Visibility> bContext = new ObjectArray<Visibility>(
				StackManager.getInstance().getTargetedList().size());
		actionContext.actionContext = bContext;
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 0;) {
			if (StackManager.getInstance().getTargetedList().get(i).isCard()) {
				final MCard card = (MCard) StackManager.getInstance().getTargetedList()
						.get(i);
				bContext.setObject(i, card.getVisibility());
				if (faceUpFlag) {
					card.setVisibility(Visibility.PUBLIC);
				} else {
					card.setVisibility(Visibility.HIDDEN);
				}
			} else {
				Log
						.warn("In FACE action, target list contains non 'Card' object. Ignored");
			}
		}
	}

	@Override
	public String toString(Ability ability) {
		return "face-" + (faceUpFlag ? "up" : "down");
	}

	/**
	 * <code>true</code> if the action if to face cards up, <code>false</code>
	 * if the action if to face them down.
	 */
	private final boolean faceUpFlag;

	/**
	 * The visibility to set.
	 */
	private final VisibilityChange visibilityChange;
}
