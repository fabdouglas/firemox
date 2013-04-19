/* 
 * MPhaseType.java 
 * Created on 21 févr. 2004
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

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.Picture;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public class PhaseType {

	/**
	 * Creates a new instance of PhaseType reading in the specified stream and
	 * following the following structure :
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>phase name + '\0' [...]</li>
	 * <li>phase identifier [1]</li>
	 * <li>empty stack playable, idCards for current player [2]</li>
	 * <li>empty stack playable, idCards for non-current player [2]</li>
	 * <li>middle resolution, playable idCards for current player [2]</li>
	 * <li>middle resolution, playable idCards for non-current player [2]</li>
	 * </ul>
	 * Picture's name used for this phase corresponds to this phase name. <br>
	 * 
	 * @param dbFile
	 *          stream where phase name would be read
	 * @throws IOException
	 *           if input exception occurred
	 */
	public PhaseType(InputStream dbFile) throws IOException {
		this.phaseName = MToolKit.readString(dbFile);
		id = dbFile.read();
		emptyStack = new EmptyStack(dbFile);
		middleResolution = new MiddleResolution(dbFile);
		// Log.debug("new PHASE " + phaseName + "(id=" + id + ")");
		normalIcon = Picture.loadImage(MToolKit.getTbsPicture("phases/" + phaseName
				+ IdConst.TYPE_PIC));
		highLightedIcon = Picture.loadImage(MToolKit.getTbsPicture("phases/"
				+ phaseName + "h.gif"));
	}

	/**
	 * Tell if we can cast a card with idCard
	 * 
	 * @param idActivePlayer
	 *          the active player identifier
	 * @param idCard
	 *          id of card we would casting
	 * @return true if we can cast a card with idCard
	 */
	public boolean canICast(int idActivePlayer, int idCard) {
		final boolean isYou = idActivePlayer == StackManager.idCurrentPlayer;
		return emptyStack.canICast(idCard, isYou)
				|| middleResolution.canICast(idCard, isYou);
	}

	private StackCondition emptyStack;

	private StackCondition middleResolution;

	/**
	 * Phase name of this phase type
	 */
	public String phaseName;

	/**
	 * Identifier of this phase type
	 */
	public int id;

	/**
	 * 
	 */
	public Image highLightedIcon;

	/**
	 * 
	 */
	public Image normalIcon;

}