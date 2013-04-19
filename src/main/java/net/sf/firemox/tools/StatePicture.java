/*   Firemox is a turn based strategy simulator
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.ui.MagicUIComponents;

/**
 * StatePicture.java Created on 3 mars 2004 Followin the state, draw a picture
 * at a determined position.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 * @since 0.82 the register index is now used. If state value is 0, the string
 *        representing the value of register is written on the graphic.
 */
public class StatePicture {

	/**
	 * read from the specified stream the state picture options. The current
	 * offset of the stream must pointing on the number of state pictures. <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>picture name + '\0' [...]</li>
	 * <li>state name + '\0' [...]</li>
	 * <li>state value [2]</li>
	 * <li>state index [1]</li>
	 * <li>x [2]</li>
	 * <li>y [2]</li>
	 * <li>width [2]</li>
	 * <li>height [2]</li>
	 * <li>display test [...]
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing the states picture
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public StatePicture(InputStream inputFile) throws IOException {
		final String statePictureName = "properties/"
				+ MToolKit.readString(inputFile);
		final String statePictureFile = MToolKit.getTbsPicture(statePictureName);
		if (statePictureFile != null) {
			picture = Picture.loadImage(statePictureFile);
		} else if (MagicUIComponents.isUILoaded()) {
			Log.error("Unable to load state picture '"
					+ MToolKit.getTbsPicture(statePictureName, false) + "'");
		}
		name = MToolKit.readString(inputFile);
		state = MToolKit.readInt16(inputFile);
		index = inputFile.read();
		x = MToolKit.getConstant(MToolKit.readInt16(inputFile));
		y = MToolKit.getConstant(MToolKit.readInt16(inputFile));
		width = MToolKit.readInt16(inputFile);
		height = MToolKit.readInt16(inputFile);
		displayTest = TestFactory.readNextTest(inputFile);
	}

	/**
	 * Draw this state on the specified card using the given graphics. If the test
	 * of this state is evaluated to <code>false</code>, no picture and no text
	 * will be drawn and false value will be returned.
	 * 
	 * @param owner
	 *          the card this state is applied on.
	 * @param g
	 *          the graphics.
	 * @param currentX
	 *          current relative X position of cursor
	 * @param currentY
	 *          current relative Y position of cursor
	 * @return true if this state picture has been drawn just after the given
	 *         relative position of cursor.
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	public boolean paint(MCard owner, Graphics g, int currentX, int currentY) {
		final int regValue = owner.getValue(index);
		if (regValue > 0 && displayTest.test(null, owner)) {
			if (state == 0) {
				// we draw the string
				if (picture != null) {
					g.drawImage(picture, x == -1 ? currentX : x, y == -1 ? currentY : y,
							width, height, null);
				}
				g.setColor(Color.BLACK);
				g.drawString(String.valueOf(regValue), x, y);
				g.setColor(Color.RED);
				g.drawString(String.valueOf(regValue), x + 1, y);
				return x == -1;
			} else if (hasState(regValue)) {
				if (picture != null) {
					g.drawImage(picture, x == -1 ? currentX : x, y == -1 ? currentY : y,
							width, height, null);
				}
				return x == -1;
			}
		}
		return false;
	}

	/**
	 * Indicates if this state suits to the specified stete
	 * 
	 * @param fullState
	 *          the full state number
	 * @return true if this state suits to the specified stete
	 */
	public boolean hasState(int fullState) {
		return state != 0 && (fullState & state) == state;
	}

	/**
	 * The picture to draw when the state value is the right one
	 */
	private Image picture;

	/**
	 * The state name
	 */
	private String name;

	/**
	 * this state value is used to determine if the picture must be drawn or not
	 */
	private int state;

	/**
	 * x where picture would be drawn
	 */
	private int x;

	/**
	 * y where picture would be drawn
	 */
	private int y;

	/**
	 * picture's width
	 */
	private int width;

	/**
	 * picture's height
	 */
	private int height;

	/**
	 * The register index
	 */
	private int index;

	/**
	 * The test determining if the picture associated to this state will be
	 * displayed or not.
	 */
	private Test displayTest;

	/**
	 * Return the state name.
	 * 
	 * @return the state name.
	 */
	@Override
	public String toString() {
		return name;
	}
}