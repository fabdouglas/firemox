/*
 * Created on Aug 5, 2004 
 * Original filename was StackCondition.java
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
 * 
 */
package net.sf.firemox.stack.phasetype;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.tools.MToolKit;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 */
public abstract class StackCondition {

	/**
	 * Creates a new instance of StackCondition <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>playable idCards for current player [2]</li>
	 * <li>playable idCards for non-current player [2]</li>
	 * </ul>
	 * 
	 * @param inputStream
	 * @throws IOException
	 */
	protected StackCondition(InputStream inputStream) throws IOException {
		idCardsForYOU = MToolKit.readInt16(inputStream);
		idCardsForOPPONENT = MToolKit.readInt16(inputStream);
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
	public abstract boolean canICast(int idCard, boolean isYou);

	/**
	 * IdCard that current player can play during this phase
	 */
	public int idCardsForYOU;

	/**
	 * IdCard that non-current player can play during this phase
	 */
	public int idCardsForOPPONENT;

}