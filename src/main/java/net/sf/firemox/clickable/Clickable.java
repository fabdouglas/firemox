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
package net.sf.firemox.clickable;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JComponent;

import net.sf.firemox.clickable.target.card.CardFactory;

/**
 * Any clickable component. This component can be highlighted, and relavant
 * mouse action are sent to the other players.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public abstract class Clickable extends JComponent implements MouseListener {

	/**
	 * Creates a new instance of MClickable <br>
	 */
	protected Clickable() {
		super();
		setBorder(null);
		setFocusable(false);
		setRequestFocusEnabled(false);
		setFocusCycleRoot(false);
	}

	/**
	 * send to opponent the message indicating that we've clicked on this
	 * component
	 */
	public abstract void sendClickToOpponent();

	/**
	 * The border will be highligthed to yellow
	 * 
	 * @param highlightedZone
	 *          the set of highlighted zone.
	 */
	public void highLight(boolean... highlightedZone) {
		highLight(CardFactory.ACTIVATED_COLOR);
	}

	/**
	 * Remove any color of the border
	 */
	public void disHighLight() {
		if (isHighLighted) {
			isHighLighted = false;
			repaint();
		}
	}

	/**
	 * The border will be highligted to highLightedColor
	 * 
	 * @param highLightColor
	 *          is the new border's color
	 */
	protected void highLight(Color highLightColor) {
		this.highLightColor = highLightColor;
		isHighLighted = true;
		repaint();
	}

	/**
	 * dishighlight the list of target
	 * 
	 * @param list
	 *          the list of target to dishighlight.
	 */
	public static void disHighlight(List<? extends Clickable> list) {
		for (Clickable clickable : list) {
			clickable.disHighLight();
		}
	}

	/**
	 * color of current highligth color
	 */
	public Color highLightColor;

	/**
	 * is this card is highlighted
	 */
	public boolean isHighLighted;

	public void mouseClicked(MouseEvent e) {
		// Nothing to do
	}

	public void mouseEntered(MouseEvent e) {
		// Nothing to do
	}

	public void mouseExited(MouseEvent e) {
		// Nothing to do
	}

	public void mousePressed(MouseEvent e) {
		// Nothing to do
	}

	public void mouseReleased(MouseEvent e) {
		// Nothing to do
	}
}