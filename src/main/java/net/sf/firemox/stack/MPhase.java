/*
 * Created on 23 oct. 2003
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
package net.sf.firemox.stack;

import static net.sf.firemox.token.IdConst.IMAGES_DIR;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EtchedBorder;

import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.stack.phasetype.PhaseType;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * Represents a phase of turn of one player. Severals breakpoint flags can be
 * attached to a phase to control the turn flow.
 * 
 * @author Fabrice Daugan
 * @since 0.21 a graphical representation of phase
 * @since 0.31 a graphical representation for each players
 * @since 0.4 phase settings are saved.
 * @since 0.80 medium skip flag added.
 */
public class MPhase extends JPanel implements MouseListener {

	/**
	 * The phase picture width
	 */
	private static final int PHASE_STD_WIDTH = 40;

	/**
	 * The phase picture height
	 */
	private static final int PHASE_STD_HEIGHT = 22;

	/**
	 * Mask used to indicate if this phase has the option "breakpoint to this
	 * phase" if any ability is activated during this phase, even if the option
	 * "skippAll" is set in the following phases.
	 * 
	 * @see MPhase#breakpoint()
	 * @see net.sf.firemox.Magic#manualSkip()
	 */
	private static final int MASK_BREAKPOINT = 0x01;

	/**
	 * Mask used to indicate if this phase has the option Set if this phase has
	 * the option "decline response to my effects until this phase".
	 * 
	 * @see MPhase#declineResponseMe()
	 * @see MPhase#setSkipAll(boolean)
	 * @see net.sf.firemox.Magic#manualSkip()
	 */
	private static final int MASK_SKIP_ALL = 0x0002;

	/**
	 * Mask used to indicate if this phase has the option Set if this phase has
	 * the option "decline response to my effects until this phase". This option
	 * will be disabled arriving to this phase.
	 * 
	 * @see MPhase#declineResponseMe()
	 * @see MPhase#setSkipAllTmp(boolean)
	 */
	private static final int MASK_TMP_SKIP_ALL = 0x0004;

	/**
	 * Mask used to indicate if this phase has the option "decline response to all
	 * effects until this phase".
	 * 
	 * @see MPhase#declineResponseOpponent()
	 * @see MPhase#setSkipAllVery(boolean)
	 */
	private static final int MASK_SKIP_ALL_VERY = 0x0008;

	/**
	 * Mask used to indicate if this phase has the option "decline response to all
	 * effects until this phase". This option will be disabled arriving to this
	 * phase.
	 * 
	 * @see MPhase#declineResponseOpponent()
	 * @see MPhase#setSkipAllVeryTmp(boolean)
	 */
	private static final int MASK_TMP_SKIP_ALL_VERY = 0x0010;

	/**
	 * Mask used to indicate if this phase has the option "decline response to all
	 * opponent's effects until this phase".
	 * 
	 * @see MPhase#declineResponseOpponent()
	 * @see MPhase#setSkipMedium(boolean)
	 */
	private static final int MASK_SKIP_ALL_MEDIUM = 0x0020;

	/**
	 * Mask used to indicate if this phase has the option "decline response to all
	 * opponent's effects until this phase".This option will be disabled arriving
	 * to this phase.
	 * 
	 * @see MPhase#declineResponseOpponent()
	 * @see MPhase#setSkipMediumTmp(boolean)
	 */
	private static final int MASK_TMP_SKIP_ALL_MEDIUM = 0x0040;

	/**
	 * Create a new instance of MPhase
	 * 
	 * @param phaseType
	 *          is the phase type associated to this phase
	 * @param idPlayer
	 *          is the player owning this phase
	 * @param settingFile
	 *          the setting file where phase settings would be read
	 * @throws IOException
	 *           if error occurred while reading settings from settingFile
	 */
	public MPhase(PhaseType phaseType, int idPlayer, InputStream settingFile)
			throws IOException {
		super();
		setOpaque(true);
		setMinimumSize(new Dimension(PHASE_STD_WIDTH, PHASE_STD_HEIGHT));
		setPreferredSize(new Dimension(PHASE_STD_WIDTH, PHASE_STD_HEIGHT));
		this.phaseType = phaseType;
		this.idPlayer = idPlayer;
		if (popupImg == null) {
			popupImg = Picture.loadImage(IMAGES_DIR + "popup.gif");
			breakpointImg = Picture.loadImage(IMAGES_DIR + "smlbreak.gif");
			skipAllImg = Picture.loadImage(IMAGES_DIR + "smlskip1.gif");
			skipAllOnceImg = Picture.loadImage(IMAGES_DIR + "smlskip2.gif");
			skipAllVeryImg = Picture.loadImage(IMAGES_DIR + "smlskip3.gif");
			skipAllOnceVeryImg = Picture.loadImage(IMAGES_DIR + "smlskip4.gif");
			skipAllMediumImg = Picture.loadImage(IMAGES_DIR + "smlskip5.gif");
			skipAllMediumOnceImg = Picture.loadImage(IMAGES_DIR + "smlskip4.gif");
		}
		mask = settingFile.read();
		setBorder(new EtchedBorder());
		setToolTipText(LanguageManagerMDB.getString(phaseType.phaseName));
		addMouseListener(this);
		reset();
	}

	/**
	 * Update the background following the active player
	 * 
	 * @param currentPhase
	 *          if true, set this phase as activated
	 * @param activePlayer
	 *          if true, set this phase with normal background, otherwise it's
	 *          background is darker as normal
	 */
	void setActive(boolean currentPhase, boolean activePlayer) {
		if (this.currentPhase != currentPhase || this.activePlayer != activePlayer) {
			this.currentPhase = currentPhase;
			this.activePlayer = activePlayer;
			if (currentPhase) {
				if (activePlayer) {
					setBackground(Color.RED);
				} else {
					setBackground(Color.RED.darker());
				}
			} else if (activePlayer) {
				setBackground(null);
			} else {
				setBackground(Color.DARK_GRAY);
			}
		}
	}

	/**
	 * update this component
	 * 
	 * @param g
	 *          the graphics of this component
	 */
	@Override
	public void paint(Graphics g) {
		final Graphics2D g2D = (Graphics2D) g;
		super.paint(g);
		if (idPlayer == 1 && Configuration.getBoolean("reverseSide", false)) {
			g2D.translate(PHASE_STD_WIDTH - 1, PHASE_STD_HEIGHT - 1);
			g2D.rotate(Math.PI);
		}

		// Draw the phase picture
		if (currentPhase) {
			g2D.drawImage(phaseType.highLightedIcon, 2, 2, 16, 16, null);
		} else {
			g2D.drawImage(phaseType.normalIcon, 2, 2, 16, 16, null);
		}

		// update the little breakpoint button
		if (breakpoint()) {
			// draw the little breakpoint button
			g2D.drawImage(breakpointImg, 19, 2, 8, 8, null);
		}

		// draw the little skipAllVery button
		if (hasMaskSkipAllVery()) {
			// draw the little skipAllVery (once) button
			g2D.drawImage(skipAllVeryImg, 26, 2, 8, 8, null);
		} else if (hasMaskTmpSkipAllVery()) {
			// draw the little skipAllMedium button
			g2D.drawImage(skipAllOnceVeryImg, 26, 2, 8, 8, null);
		} else if (hasMaskSkipAllMedium()) {
			// draw the little skipAllMedium (once) button
			g2D.drawImage(skipAllMediumImg, 26, 2, 8, 8, null);
		} else if (hasMaskTmpSkipAllMedium()) {
			// draw the little skipAllVery button
			g2D.drawImage(skipAllMediumOnceImg, 26, 2, 8, 8, null);
		} else if (hasMaskSkipAll()) {
			// draw the little skipAll button
			g2D.drawImage(skipAllImg, 26, 2, 8, 8, null);
		} else if (hasMaskTmpSkipAll()) {
			// draw the little skipAl (once) button
			g2D.drawImage(skipAllOnceImg, 26, 2, 8, 8, null);
		}
	}

	/**
	 * Save settings of this phase to the specified output stream
	 * 
	 * @param out
	 *          is output stream where settings of this phase would be saved
	 * @throws IOException
	 *           if error occurred while writing settings to settingFile
	 */
	public void saveSettings(FileOutputStream out) throws IOException {
		out.write(mask);
	}

	/**
	 * Load settings of this phase from the specified input stream
	 * 
	 * @param input
	 *          is input stream where settings of this phase is saved
	 * @throws IOException
	 *           if error occurred while reading settings from settingFile
	 */
	public void loadSettings(InputStream input) throws IOException {
		mask = input.read();
	}

	/**
	 * remove all breakpoints and options of this phase
	 */
	public void reset() {
		setOpaque(true);
	}

	/**
	 * Tell if this phase has the option "breakpoint to this phase"
	 * 
	 * @return true if there is a breakpoint on this phase
	 * @see net.sf.firemox.Magic#manualSkip()
	 */
	public boolean breakpoint() {
		return (mask & MASK_BREAKPOINT) == MASK_BREAKPOINT;
	}

	/**
	 * Set if this phase has the option "breakpoint to this phase"
	 * 
	 * @param really
	 *          indication if we enable or disable this option
	 * @see MPhase#MASK_BREAKPOINT
	 */
	void setBreakpoint(boolean really) {
		if (really) {
			mask |= MASK_BREAKPOINT;
		} else {
			mask &= ~MASK_BREAKPOINT;
		}
	}

	/**
	 * Indicates whether this phase has the option "decline response to my effects
	 * until this phase".
	 * 
	 * @return true if there is a 'skip all' on this phase
	 * @see net.sf.firemox.Magic#manualSkip()
	 */
	public boolean declineResponseMe() {
		return hasMaskSkipAllVery() || hasMaskTmpSkipAllVery() || hasMaskSkipAll()
				|| hasMaskTmpSkipAll();
	}

	/**
	 * Set if this phase has the option "decline response to all effects until
	 * this phase".
	 * 
	 * @param really
	 *          indication if we enable or disable this option
	 * @see MPhase#MASK_BREAKPOINT
	 */
	void setSkipAll(boolean really) {
		if (really) {
			mask |= MASK_SKIP_ALL;
		} else {
			mask &= ~MASK_SKIP_ALL;
		}
	}

	/**
	 * Set if this phase has the option "decline response to my effects until this
	 * phase". This option will be disabled arriving to this phase.
	 * 
	 * @param really
	 *          indication if we enable or disable this option
	 * @see MPhase#MASK_TMP_SKIP_ALL
	 */
	void setSkipAllTmp(boolean really) {
		if (really) {
			mask |= MASK_TMP_SKIP_ALL;
		} else {
			mask &= ~MASK_TMP_SKIP_ALL;
		}
	}

	/**
	 * Indicates whether this phase has the option "decline response to opponent's
	 * effects until this phase".
	 * 
	 * @return true if this phase has the option "decline response to opponent's
	 *         effects until this phase".
	 * @see net.sf.firemox.Magic#manualSkip()
	 */
	public boolean declineResponseOpponent() {
		return hasMaskSkipAllVery() || hasMaskTmpSkipAllVery()
				|| hasMaskTmpSkipAllMedium() || hasMaskSkipAllMedium();
	}

	/**
	 * Set if this phase has the option "decline response to all opponent's
	 * effects until this phase".
	 * 
	 * @param really
	 *          indication if we enable or disable this option
	 * @see MPhase#MASK_SKIP_ALL_MEDIUM
	 */
	void setSkipMedium(boolean really) {
		if (really) {
			mask |= MASK_SKIP_ALL_MEDIUM;
		} else {
			mask &= ~MASK_SKIP_ALL_MEDIUM;
		}
	}

	/**
	 * Set if this phase has the option "decline response to all opponent's
	 * effects until this phase". This option will be disabled arriving to this
	 * phase.
	 * 
	 * @param really
	 *          indication if we enable or disable this option
	 * @see MPhase#MASK_TMP_SKIP_ALL_MEDIUM
	 */
	void setSkipMediumTmp(boolean really) {
		if (really) {
			mask |= MASK_TMP_SKIP_ALL_MEDIUM;
		} else {
			mask &= ~MASK_TMP_SKIP_ALL_MEDIUM;
		}
	}

	/**
	 * Set if this phase has the option "breakpoint to this phase"
	 * 
	 * @param really
	 *          indication if we enable or disable this option
	 * @see MPhase#MASK_BREAKPOINT
	 */
	void setSkipAllVery(boolean really) {
		if (really) {
			mask |= MASK_SKIP_ALL_VERY;
		} else {
			mask &= ~MASK_SKIP_ALL_VERY;
		}
	}

	/**
	 * Set if this phase has the option "decline response to all effects until
	 * this phase". This option will be disabled arriving to this phase.
	 * 
	 * @param really
	 *          indication if we enable or disable this option
	 * @see MPhase#MASK_BREAKPOINT
	 */
	void setSkipAllVeryTmp(boolean really) {
		if (really) {
			mask |= MASK_TMP_SKIP_ALL_VERY;
		} else {
			mask &= ~MASK_TMP_SKIP_ALL_VERY;
		}
	}

	/**
	 * is called when you click on me
	 * 
	 * @param e
	 *          is the mouse event
	 */
	public void mouseClicked(MouseEvent e) {
		StackManager.noReplayToken.take();
		try {
			if (ConnectionManager.isConnected()) {
				if (e.getButton() != MouseEvent.BUTTON1) {
					// e.isPopupTrigger() may not work
					/*
					 * right button is pressed, show the options popup menu and mark the
					 * phase clicked triggerPhase as this.
					 */
					triggerPhase = this;
					((JCheckBoxMenuItem) optionsMenu.getComponent(0))
							.setSelected(breakpoint());
					((JCheckBoxMenuItem) optionsMenu.getComponent(1))
							.setSelected(hasMaskSkipAll());
					((JCheckBoxMenuItem) optionsMenu.getComponent(2))
							.setSelected(hasMaskTmpSkipAll());
					((JCheckBoxMenuItem) optionsMenu.getComponent(3))
							.setSelected(hasMaskSkipAllMedium());
					((JCheckBoxMenuItem) optionsMenu.getComponent(4))
							.setSelected(hasMaskTmpSkipAllMedium());
					((JCheckBoxMenuItem) optionsMenu.getComponent(5))
							.setSelected(hasMaskSkipAllVery());
					((JCheckBoxMenuItem) optionsMenu.getComponent(6))
							.setSelected(hasMaskTmpSkipAllVery());
					// show the history popup menu
					optionsMenu.show(e.getComponent(), e.getX(), e.getY());
				} else {
					// left button is pressed
					// enable or disable the skip as longer as opponent doesn't do
					// anything
					setSkipAllTmp(true);
					repaint();
					if (StackManager.idHandedPlayer == 0
							&& this != EventManager.currentPhase()) {
						MagicUIComponents.magicForm.manualSkip();
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			StackManager.noReplayToken.release();
		}
	}

	private boolean hasMaskTmpSkipAllVery() {
		return (mask & MASK_TMP_SKIP_ALL_VERY) == MASK_TMP_SKIP_ALL_VERY;
	}

	private boolean hasMaskSkipAllMedium() {
		return (mask & MASK_SKIP_ALL_MEDIUM) == MASK_SKIP_ALL_MEDIUM;
	}

	private boolean hasMaskSkipAllVery() {
		return (mask & MASK_SKIP_ALL_VERY) == MASK_SKIP_ALL_VERY;
	}

	private boolean hasMaskTmpSkipAllMedium() {
		return (mask & MASK_TMP_SKIP_ALL_MEDIUM) == MASK_TMP_SKIP_ALL_MEDIUM;
	}

	private boolean hasMaskTmpSkipAll() {
		return (mask & MASK_TMP_SKIP_ALL) == MASK_TMP_SKIP_ALL;
	}

	private boolean hasMaskSkipAll() {
		return (mask & MASK_SKIP_ALL) == MASK_SKIP_ALL;
	}

	public void mousePressed(MouseEvent e) {
		// Ignore this event
	}

	public void mouseReleased(MouseEvent e) {
		// Ignore this event
	}

	public void mouseEntered(MouseEvent e) {
		// Ignore this event
	}

	public void mouseExited(MouseEvent e) {
		// Ignore this event
	}

	/**
	 * is the popupMenu displayed when you right-click to see options of this
	 * phase.
	 */
	public static JPopupMenu optionsMenu;

	/**
	 * image for little popupMenu's button
	 */
	private static Image popupImg;

	/**
	 * image for little bookmark button
	 */
	private static Image breakpointImg;

	/**
	 * image for little skipAll button
	 */
	private static Image skipAllImg;

	/**
	 * image for little skipAll (once) button
	 */
	private static Image skipAllOnceImg;

	/**
	 * image for little skipAllVery button
	 */
	private static Image skipAllVeryImg;

	/**
	 * image for little skipAllVery button (once)
	 */
	private static Image skipAllOnceVeryImg;

	/**
	 * image for little skipAllMedium button
	 */
	private static Image skipAllMediumOnceImg;

	/**
	 * image for little skipAllMedium (once) button
	 */
	private static Image skipAllMediumImg;

	/**
	 * the last phase where popup trigger has been recorded
	 */
	public static MPhase triggerPhase;

	/**
	 * Contain all phase objects of players
	 */
	public static MPhase[][] phases = null;

	/**
	 * Indicates if this phase is the current one or not
	 */
	private boolean currentPhase = false;

	/**
	 * Mask used for skip options
	 */
	private int mask;

	/**
	 * idPlayer of this player
	 */
	private int idPlayer;

	/**
	 * Phase type associated to this phase component
	 */
	public PhaseType phaseType;

	private boolean activePlayer;

	/**
	 * Indicates if this phase has to be skipped. The beginning_of_... and
	 * phase_... events would not be raised. Nevertheless, the before_phase_...
	 * trigger the current phase, the the awakened abilities should be mana source
	 * and played in the background as abstract abilities.
	 */
	public boolean skipThisPhase;

}