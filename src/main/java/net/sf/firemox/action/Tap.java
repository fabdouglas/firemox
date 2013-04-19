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
import net.sf.firemox.action.context.BooleanArray;
import net.sf.firemox.action.handler.FollowAction;
import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.BecomeTapped;
import net.sf.firemox.event.BecomeUnTapped;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * Tap the target list. Error if target list contains objects not instance of
 * card.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.1
 */
class Tap extends UserAction implements StandardAction, FollowAction {

	/**
	 * Create an instance of Tap by reading a file Offset's file must pointing on
	 * the first byte of this action. <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>tap=1,untap=0 [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	Tap(InputStream inputFile) throws IOException {
		super(inputFile);
		tap = inputFile.read() != 0;
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.TAP;
	}

	public void rollback(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final BooleanArray bContext = (BooleanArray) actionContext.actionContext;
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 0;) {
			if (StackManager.getInstance().getTargetedList().get(i).isCard()) {
				final MCard card = (MCard) StackManager.getInstance().getTargetedList()
						.get(i);
				if (bContext.getBoolean(i)) {
					card.tap(!tap);
				}
			} else {
				Log
						.warn("In TAP action, target list contains non 'Card' object. Ignored");
			}
		}
	}

	public void simulate(ActionContextWrapper actionContext,
			ContextEventListener context, Ability ability) {
		final BooleanArray bContext = new BooleanArray(StackManager.getInstance()
				.getTargetedList().size());
		actionContext.actionContext = bContext;
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 0;) {
			if (StackManager.getInstance().getTargetedList().get(i).isCard()) {
				final MCard card = (MCard) StackManager.getInstance().getTargetedList()
						.get(i);
				if (card.tapped && !tap || !card.tapped && tap) {
					bContext.setBoolean(i, true);
					card.tap(tap);
				}
			} else {
				Log
						.warn("In TAP action, target list contains non 'Card' object. Ignored");
			}
		}
	}

	public boolean play(ContextEventListener context, Ability ability) {
		for (int i = StackManager.getInstance().getTargetedList().size(); i-- > 0;) {
			if (StackManager.getInstance().getTargetedList().get(i).isCard()) {
				final MCard card = (MCard) StackManager.getInstance().getTargetedList()
						.get(i);
				if (checkTimeStamp(context, card) && card.tapped != tap) {
					card.tap(tap);
					if (tap) {
						BecomeTapped.dispatchEvent(card);
					} else {
						BecomeUnTapped.dispatchEvent(card);
					}
				} else {
					Log.error("timestamp error or card tap not changing");
				}
			} else {
				Log
						.warn("In TAP action, target list contains non 'Card' object. Ignored");
			}
		}
		return true;
	}

	@Override
	public boolean equal(MAction constraintAction) {
		return constraintAction.getIdAction() == Actiontype.TAP
				&& ((Tap) constraintAction).tap == tap
				&& (constraintAction.getActionName() == null || (constraintAction
						.getActionName().equals(getActionName())));
	}

	@Override
	public String toString(Ability ability) {
		return LanguageManagerMDB.getString(tap ? "action-tap" : "action-untap");
	}

	/**
	 * Indicates if the list would be tapped or untapped
	 */
	private boolean tap;

}