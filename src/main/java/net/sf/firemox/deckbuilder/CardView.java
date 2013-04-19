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
package net.sf.firemox.deckbuilder;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import net.sf.firemox.database.DatabaseCard;
import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.management.MonitorListener;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MCardCompare;

/**
 * @author <a href="mailto:goldeneyemdk@users.sourceforge.net">Sebastien Genete
 *         </a>
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public class CardView extends JComponent implements MonitorListener {

	private CardView() {
		setPreferredSize(new Dimension(IdConst.STD_WIDTH, IdConst.STD_HEIGHT));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());
		setSize(getPreferredSize());
	}

	public void notifyChange() {
		repaint();
	}

	/**
	 * Display the given card name. If this card name is not locally present, it
	 * will be downloaded from a web location depending on settings of current
	 * TBS. Theses settings are defined in the <code>tbs/XXX.xml</code>
	 * 
	 * @param card
	 *          the card name to display.
	 */
	public void setCard(MCardCompare card) {
		String errorMsg = null;
		try {
			databaseCard = DatabaseFactory.getDatabase(null, card.getModel(MdbLoader
					.getLastMdbStream()), null);
		} catch (Throwable e) {
			errorMsg = "<html>Error :" + e.getMessage();
		} finally {
			if (errorMsg != null) {
				databaseCard = null;
			}
		}
		repaint();
	}

	private DatabaseCard databaseCard;

	@Override
	public void paint(Graphics g) {
		final Image picture;
		if (databaseCard == null) {
			picture = DatabaseFactory.backImage;
		} else {
			picture = databaseCard.getImage(this);
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(picture, 0, 0, getSize().width, getSize().height, this);

		// Draw the eventual progress bar
		if (databaseCard != null) {
			databaseCard.updatePaintNotification(this, g);
		}
	}

	/**
	 * Unique instance of CardView class.
	 */
	private static CardView instance = new CardView();

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class.
	 */
	public static CardView getInstance() {
		return instance;
	}

}
