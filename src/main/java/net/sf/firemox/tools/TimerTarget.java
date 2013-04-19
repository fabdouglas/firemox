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
package net.sf.firemox.tools;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.TimerGlassPane;

/**
 * Internal implementation detail: we happen to use javax.swing.Timer currently,
 * which sends its timing events to an ActionListener. This internal private
 * class is our ActionListener that traps these calls and forwards them to the
 * TimingController.timingEvent() method.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.84
 */
public class TimerTarget implements ActionListener {

	private TimerGlassPane timerPanel;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param timerPanel
	 *          the associated timer panel.
	 */
	public TimerTarget(TimerGlassPane timerPanel) {
		super();
		this.thresholdBusyCpu = Configuration.getInt("timer.hourglass-delay");
		this.thresholdDeadlock = Configuration.getInt("timer.deadlock-delay");
		this.thresholdPlayerThinking = Configuration.getInt("timer.waiting-delay");
		this.timerPanel = timerPanel;
		try {
			busyCpuPicture = Picture.loadImage(IdConst.IMAGES_DIR + "hourglass.jpg");
			playerThinkingPicture = busyCpuPicture;
			deadlockPicture = Picture.loadImage(IdConst.IMAGES_DIR + "deadlock.gif");
			byGifPictures = new Image[6];
			for (int i = byGifPictures.length; i-- > 0;) {
				byGifPictures[i] = Picture.loadImage(IdConst.IMAGES_DIR + "progress"
						+ i + ".gif");
			}
		} catch (Exception e) {
			// IGNORING
		}
	}

	public void actionPerformed(ActionEvent e) {
		deadlyCounter++;
		if (StackManager.idHandedPlayer == 1) {
			if (deadlyCounter >= thresholdPlayerThinking) {
				timerPanel.updatePicture(playerThinkingPicture, null);
				if (MagicUIComponents.magicForm.getGlassPane() == timerPanel
						&& !timerPanel.isVisible()) {
					timerPanel.setVisible(true);
					timerPanel.repaint();
				}
			}
		} else if (StackManager.idHandedPlayer == -1) {
			if (deadlyCounter >= thresholdBusyCpu
					&& deadlyCounter < thresholdDeadlock) {
				timerPanel.updatePicture(busyCpuPicture, byGifPictures);
			} else if (deadlyCounter == thresholdDeadlock) {
				timerPanel.updatePicture(deadlockPicture, null);
			}
		}
		if (MagicUIComponents.backgroundBtn.isEnabled()) {
			MagicUIComponents.backgroundBtn.nextPicture();
		}
	}

	/**
	 * Reset counter
	 */
	public void resetCounter() {
		timerPanel.updatePicture(null, null);
		deadlyCounter = 0;
	}

	private int deadlyCounter;

	private int thresholdBusyCpu;

	private int thresholdDeadlock;

	private int thresholdPlayerThinking;

	private Image busyCpuPicture;

	private Image deadlockPicture;

	private Image playerThinkingPicture;

	/**
	 * The progress bar pictures.
	 */
	public Image[] byGifPictures;

	/**
	 * Save editable string settings of timer.
	 */
	public void saveSettings() {
		Configuration.setProperty("timer.hourglass-delay", thresholdBusyCpu);
		Configuration.setProperty("timer.deadlock-delay", thresholdDeadlock);
		Configuration.setProperty("timer.waiting-delay", thresholdPlayerThinking);
	}

}
