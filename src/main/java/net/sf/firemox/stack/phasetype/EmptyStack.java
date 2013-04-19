/*
 * Created on Aug 5, 2004 
 * Original filename was EmptyStack.java
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
package net.sf.firemox.stack.phasetype;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class EmptyStack extends StackCondition {

	/**
	 * Creates a new instance of MiddleResolution <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>playable idCards for current player [2]</li>
	 * <li>playable idCards for non-current player [2]</li>
	 * </ul>
	 * 
	 * @param inputStream
	 * @throws IOException
	 */
	EmptyStack(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	/**
	 * Tell if a player can cast a card with this idCard.
	 * 
	 * @param idCard
	 *          id of card we would casting
	 * @param isYou
	 *          is the current player waiting for that
	 * @return true if the player can cast a card with this idCard
	 */
	@Override
	public boolean canICast(int idCard, boolean isYou) {
		if (isYou) {
			return StackManager.isEmpty() && idCardsForYOU != 0
					&& (idCard == 0 || MCard.intersectionIdCard(idCardsForYOU, idCard));
		}
		return StackManager.isEmpty()
				&& idCardsForOPPONENT != 0
				&& (idCard == 0 || MCard.intersectionIdCard(idCardsForOPPONENT, idCard));
	}

}