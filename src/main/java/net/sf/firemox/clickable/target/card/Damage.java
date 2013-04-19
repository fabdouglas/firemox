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
 * 
 */
package net.sf.firemox.clickable.target.card;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JComponent;

import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.Reversable;
import net.sf.firemox.ui.Tappable;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class Damage extends JComponent implements MouseListener, Tappable,
		Reversable {

	/**
	 * creates a new instance of MDamage
	 * 
	 * @param cardSource
	 *          is the card source of this damage
	 * @param damageNumber
	 *          the amount of damage to do
	 * @param damageType
	 *          the type of this damage (prevent/dealt, lose life or gain life).
	 */
	public Damage(MCard cardSource, int damageNumber, int damageType) {
		this.cardSource = cardSource;
		this.damageType = damageType;
		this.damageNumber = damageNumber;
		addMouseListener(this);
		updateSize();
	}

	@Override
	public void paintComponent(Graphics g) {
		// super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;

		// draw the rounded black rectangle
		if (isHighLighted) {
			g2D.setColor(highLightColor);
		} else {
			g2D.setColor(Color.BLACK);
		}
		if (tapped) {
			g2D.fillRoundRect(0, 0, CardFactory.cardHeight, CardFactory.cardWidth, 3,
					3);
		} else {
			g2D.fillRoundRect(0, 0, CardFactory.cardWidth, CardFactory.cardHeight, 3,
					3);
		}

		if (reversed && Configuration.getBoolean("reverseArt", true)) {
			if (tapped) {
				g2D.translate(0, CardFactory.cardWidth);
				g2D.rotate(-Math.PI / 2);
			} else {
				g2D.translate(CardFactory.cardWidth, CardFactory.cardHeight);
				g2D.rotate(Math.PI);
			}
		} else {
			if (tapped) {
				g2D.translate(CardFactory.cardHeight, 0);
				g2D.rotate(Math.PI / 2);
			}
		}
		g2D.drawImage(scaledImage(), 0, 0, null);
		g2D.dispose();
	}

	/**
	 * return the card's picture
	 * 
	 * @return the card's picture
	 */
	protected Image image() {
		return DatabaseFactory.damageImage;
	}

	/**
	 * return the scaled card's picture
	 * 
	 * @return the scaled card's picture
	 */
	protected Image scaledImage() {
		return DatabaseFactory.damageScaledImage;
	}

	/**
	 * @param idDamageType
	 *          the damage type to compare to this
	 * @return true if the specified damage type is the same
	 */
	public boolean isSameDamage(int idDamageType) {
		return idDamageType == this.damageType;
	}

	/**
	 * Tap/untap this card
	 * 
	 * @param tapped
	 *          if true the card will be tapped
	 */
	public void tap(boolean tapped) {
		if (this.tapped != tapped) {
			this.tapped = tapped;
			updateSize();
		}
	}

	/**
	 * 
	 */
	private void updateSize() {
		// Update the bounds
		if (tapped) {
			setPreferredSize(new Dimension(CardFactory.cardHeight,
					CardFactory.cardWidth));
		} else {
			setPreferredSize(new Dimension(CardFactory.cardWidth,
					CardFactory.cardHeight));
		}
		setSize(getPreferredSize());
	}

	/**
	 * is called when mouse is on this card, will disp a preview
	 * 
	 * @param e
	 *          is the mouse event
	 * @since 0.71 art author and rules author have been added to the tooltip
	 */
	public void mouseEntered(MouseEvent e) {
		CardFactory.previewCard.setImage(image(), null);
		StringBuilder toolTip = new StringBuilder();

		// html header and card name
		toolTip.append("<html><b>");
		toolTip.append(LanguageManager.getString("source"));
		toolTip.append(": </b>");
		toolTip.append(cardSource);
		toolTip.append("<br><b>");
		toolTip.append(LanguageManager.getString("amount"));
		toolTip.append(": </b>");
		toolTip.append(damageNumber);
		toolTip.append("<br><b>");
		toolTip.append(LanguageManager.getString("damageType"));
		toolTip.append(": </b><br>");
		for (int i = damageTypes.length; i-- > 0;) {
			if (isSameDamage(damageTypes[i])) {
				toolTip.append("&nbsp;&nbsp;&nbsp;&nbsp;");
				toolTip.append(LanguageManagerMDB.getString(damageTypesName[i]));
			}
		}

		toolTip.append("</html>");
		setToolTipText(toolTip.toString());
	}

	/**
	 * @param dbStream
	 *          the mdb stream's header.
	 * @throws IOException
	 *           error during the header read.
	 */
	public static void init(FileInputStream dbStream) throws IOException {
		int count = dbStream.read();
		Log.debug("exportedDamageTypeNames (" + count + ")");
		damageTypes = new int[count];
		damageTypesName = new String[damageTypes.length];
		for (int i = count; i-- > 0;) {
			damageTypesName[i] = MToolKit.readString(dbStream);
			damageTypes[i] = MToolKit.readInt16(dbStream);
		}
	}

	/**
	 * The shared scaling transformation for all cards
	 */
	protected AffineTransform atImageSpace;

	public void mouseClicked(MouseEvent e) {
		// Ignore this event
	}

	public void mouseExited(MouseEvent e) {
		// Ignore this event
	}

	public void mousePressed(MouseEvent e) {
		// Ignore this event
	}

	public void mouseReleased(MouseEvent e) {
		// Ignore this event
	}

	public void reverse(boolean reversed) {
		this.reversed = reversed;
	}

	/**
	 * Indicates if this card should be tapped or not
	 */
	public boolean tapped;

	/**
	 * card dealting this damages
	 */
	public MCard cardSource;

	/**
	 * type of damages
	 */
	public int damageType;

	/**
	 * type of damages
	 */
	public int damageNumber;

	/**
	 * color of current highligth > yellow or red
	 */
	protected Color highLightColor;

	/**
	 * is this card is highlighted
	 */
	public boolean isHighLighted;

	private boolean reversed;

	private static int[] damageTypes;

	private static String[] damageTypesName;
}