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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sf.firemox.DeckBuilder;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.ui.TimerGlassPane;

/**
 * Internal implementation detail: we happen to use javax.swing.Timer currently,
 * which sends its timing events to an ActionListener. This internal private
 * class is our ActionListener that traps these calls and forwards them to the
 * TimingController.timingEvent() method.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
public class CardLoader implements ActionListener {

	private int deadlyCounter;

	private final int thresholdCpu;

	private Image playerThinkingPicture;

	/**
	 * The progress bar pictures.
	 */
	private final Image[] byGifPictures;

	private TimerGlassPane timerPanel;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param timerPanel
	 *          the associated timer panel.
	 */
	public CardLoader(TimerGlassPane timerPanel) {
		super();
		this.thresholdCpu = Configuration.getInt("timer.waiting-delay");
		this.byGifPictures = new Image[6];
		this.timerPanel = timerPanel;
		try {
			playerThinkingPicture = Picture.loadImage(IdConst.IMAGES_DIR
					+ "hourglass.jpg");
			for (int i = byGifPictures.length; i-- > 0;) {
				byGifPictures[i] = Picture.loadImage(IdConst.IMAGES_DIR + "progress"
						+ i + ".gif");
			}
			timerPanel.updatePicture(playerThinkingPicture, byGifPictures);
		} catch (Exception e) {
			// IGNORING
		}
	}

	public void actionPerformed(ActionEvent e) {
		deadlyCounter++;
		if (deadlyCounter >= thresholdCpu) {
			timerPanel.updatePicture(playerThinkingPicture, byGifPictures);
			if (DeckBuilder.form.getGlassPane() == timerPanel
					&& !timerPanel.isVisible()) {
				timerPanel.setVisible(true);
				timerPanel.repaint();
			}
		}
	}

	/**
	 * Reset counter
	 */
	public void resetCounter() {
		timerPanel.updatePicture(null, null);
		deadlyCounter = 0;
	}

}
