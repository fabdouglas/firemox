/*
 * Mana.java
 * Created on 27 octobre 2002, 16:11
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
package net.sf.firemox.clickable.mana;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.PixelGrabber;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import net.sf.firemox.action.PayMana;
import net.sf.firemox.action.context.ManaCost;
import net.sf.firemox.clickable.Clickable;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.True;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.PairIntObject;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * Representes the mana pool of one color of one player : BLACK,BLUE,GREEN,RED,
 * WHITE,COLORLESS. A graphic, in function of mana's color is displayed with a
 * label representing the amount of manas for this colored(less) mana. if we
 * consider the opponent case, we have a reversed a rotation of PI to do.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @see net.sf.firemox.clickable.mana.ManaPool
 * @see net.sf.firemox.token.IdCommonToken#COLORLESS_MANA
 * @see net.sf.firemox.token.IdCommonToken#BLACK_MANA
 * @see net.sf.firemox.token.IdCommonToken#BLUE_MANA
 * @see net.sf.firemox.token.IdCommonToken#GREEN_MANA
 * @see net.sf.firemox.token.IdCommonToken#RED_MANA
 * @see net.sf.firemox.token.IdCommonToken#WHITE_MANA
 * @since 0.3 support reversed mana picture for opponent
 * @since 0.53 "enableReverse" option
 * @since 0.60 extends MClickable class
 * @since 0.83 new mana representation of mana pool
 */
public class Mana extends Clickable {

	// the dimensions of graphics
	private static final int MANA_WIDTH = Player.PLAYER_SIZE_HEIGHT;

	private static final int MANA_HEIGHT = MANA_WIDTH;

	/**
	 * Creates a new instance of MMana
	 * 
	 * @param idColor
	 *          if the color of this mana
	 * @param reverseImage
	 *          if true the mana picture will be flipped horizontally and
	 *          vertically.
	 */
	Mana(int idColor, boolean reverseImage) {
		super();
		this.color = idColor;
		this.reverseImage = reverseImage;
		setPreferredSize(new Dimension(MANA_WIDTH, MANA_HEIGHT));
		setToolTipText(LanguageManager.getString("Mana" + idColor));
		addMouseListener(this);
	}

	/**
	 * Update the mana pictures following the specified emulated TBS
	 * 
	 * @param mdb
	 */
	public static void init(String mdb) {
		// load the mana picture
		int error = 0;
		for (int i = IdCommonToken.PAYABLE_COLOR_NAMES.length; i-- > 0;) {
			try {
				if (error == 0 && i != 0) {
					if (i <= 5) {
						IMAGES[i] = Picture.loadImage(
								MToolKit.getTbsPicture("mana/colored/big/"
										+ MdbLoader.coloredBigManas[i], false),
								new URL(MdbLoader.coloredManaBigURL
										+ MdbLoader.coloredBigManas[i])).getContent();
						if (IMAGES[i] == null) {
							error += (i + 1) * 2;
						} else {
							// check the small colored mana
							Picture.download(MToolKit.getTbsPicture("mana/colored/small/"
									+ MdbLoader.coloredSmlManas[i], false), new URL(
									MdbLoader.coloredManaSmlURL + MdbLoader.coloredSmlManas[i]));
						}
					} else {
						Picture.download(MToolKit.getTbsPicture("mana/colored/small/"
								+ MdbLoader.coloredSmlManas[i], false), new URL(
								MdbLoader.coloredManaSmlURL + MdbLoader.coloredSmlManas[i]));
					}
				} else if (error == 0) {
					IMAGES[0] = Picture.loadImage(
							MToolKit.getTbsPicture("mana/colorless/big/"
									+ MdbLoader.colorlessBigURL, false),
							new URL(MdbLoader.colorlessURL + MdbLoader.colorlessBigURL))
							.getContent();
					if (IMAGES[0] == null) {
						error += (i + 1) * 2 + MdbLoader.colorlessSmlManas.length;
					} else {
						// check the small colorless mana
						for (int j = MdbLoader.colorlessSmlManas.length; j-- > 0;) {
							Picture.download(MToolKit.getTbsPicture("mana/colorless/small/"
									+ MdbLoader.colorlessSmlManas[j], false), new URL(
									MdbLoader.colorlessURL + MdbLoader.colorlessSmlManas[j]));
						}
						Picture.download(MToolKit.getTbsPicture("mana/colorless/small/"
								+ MdbLoader.unknownSmlMana, false), new URL(
								MdbLoader.colorlessURL + MdbLoader.unknownSmlMana));
					}
				}
			} catch (Exception e1) {
				error += (i + 1) * 2;
				Log.error("ERROR : could not load mana picture " + i, e1);
			}

			// read a pixel to determine foreground colors
			if (IMAGES[i] != null) {
				final int[] pixels = new int[1];
				try {
					new PixelGrabber(IMAGES[i], 18, 1, 1, 1, pixels, 0, 1).grabPixels();
					TEXT_COLOR[i] = new Color(pixels[0]).darker();
				} catch (InterruptedException e) {
					TEXT_COLOR[i] = Color.black;
				}
			} else {
				TEXT_COLOR[i] = Color.black;
			}
		}

		if (error > 0) {
			// One or several error have been found, display a warning message
			JOptionPane
					.showMessageDialog(
							MagicUIComponents.magicForm,
							"One or several mana pictures ("
									+ error
									+ ") have not been found\nCheck your internet connection and/or you proxy configuration\nHave a look in console output for more details.",
							"Load error", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * return the amount of mana of this color
	 * 
	 * @return the amount of mana of this color
	 */
	public int getMana() {
		return getMana(null);
	}

	/**
	 * return the amount of mana of this color
	 * 
	 * @param abilityRequest
	 *          the ability containing action requesting this mana
	 * @return the amount of mana of this color
	 */
	public int getMana(Ability abilityRequest) {
		if (abilityRequest != null) {
			// mana get with restriction
			int result = getPlayer().registers[color];
			for (PairIntObject<Test> pair : restrictionList) {
				// restriction exists already, add this mana
				if (!pair.value.test(abilityRequest, abilityRequest.getCard())) {
					// restriction doen't allow this ability to be played
					result -= pair.key;
				}
			}
			return result;
		}
		return getPlayer().registers[color];
	}

	/**
	 * empty the mana pool of this mana
	 * 
	 * @return the old pool of this mana
	 */
	public int setToZero() {
		final int oldValue = getPlayer().registers[color];
		setTo(0);
		return oldValue;
	}

	/**
	 * set the pool of this mana
	 * 
	 * @param idNumber
	 *          is the new pool of this mana
	 * @return the new pool of this mana ( is idNumber)
	 */
	private int setTo(int idNumber) {
		getPlayer().registers[color] = idNumber;
		return idNumber;
	}

	/**
	 * Add a number of mana of this color
	 * 
	 * @param idNumber
	 *          is the number of mana to add to the mana pool
	 * @param restriction
	 *          the test defining mana usage
	 * @return the new pool of this mana
	 */
	public int addMana(int idNumber, Test restriction) {
		if (idNumber > 0) {
			setTo(getPlayer().registers[color] + idNumber);
			if (restriction != null && restriction != True.getInstance()) {
				// mana added with restriction
				for (PairIntObject<Test> pair : restrictionList) {
					if (pair.value == restriction) {
						// restriction exists already, add this mana
						pair.key += idNumber;
						return getPlayer().registers[color];
					}
				}
				restrictionList.add(new PairIntObject<Test>(idNumber, restriction));
			}
		}
		return getPlayer().registers[color];
	}

	/**
	 * Remove a number of mana of this color from the mana pool.
	 * 
	 * @param nb
	 *          is the number of mana to remove from the mana pool
	 * @param abilityRequest
	 *          the ability containing action requesting this mana
	 * @return amount of mana effectively removed from the mana pool.
	 */
	public int removeMana(int nb, Ability abilityRequest) {
		return removeMana(nb, abilityRequest, null);
	}

	/**
	 * Remove a number of mana of this color from the mana pool.
	 * 
	 * @param nb
	 *          is the number of mana to remove from the mana pool
	 * @param abilityRequest
	 *          the ability containing action requesting this mana
	 * @param manaContext
	 *          is the action context containing information on mana cost of the
	 *          requesting ability;
	 * @return amount of mana effectively removed from the mana pool.
	 */
	public int removeMana(int nb, Ability abilityRequest, ManaCost manaContext) {
		if (abilityRequest != null) {
			// mana removed with restriction
			int toRemove = nb;
			int i = restrictionList.size();
			while (i-- > 0 && toRemove > 0) {
				final PairIntObject<Test> restriction = restrictionList.get(i);
				if (!(restriction.value).test(abilityRequest, abilityRequest.getCard())) {
					// ability is ok, remove this mana with this restriction
					if (restriction.key - toRemove <= 0) {
						// no more mana with this restriction and no more mana to remove
						restrictionList.remove(i);
						if (restriction.key - toRemove == 0) {
							// no more mana to remove
							if (manaContext != null) {
								manaContext.addRestriction(color, restriction.value, toRemove);
							}
							break;
						}
						// else, it remains mana to remove but without this restriction
						if (manaContext != null) {
							manaContext.addRestriction(color, restriction.value,
									restriction.key);
						}
						toRemove -= restriction.key;
					} else {
						// no more mana to remove
						if (manaContext != null) {
							manaContext.addRestriction(color, restriction.value, toRemove);
						}
						restriction.key -= toRemove;
						break;
					}
				}
			}
		}
		if (nb <= getPlayer().registers[color]) {
			setTo(getPlayer().registers[color] - nb);
			return nb;
		}
		// no negative mana allowed --> 0
		return setToZero();
	}

	/**
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 * @since 0.3 faster reverse draw without use of rotation
	 */
	@Override
	public void paint(Graphics g) {
		if (PayMana.useMana && IMAGES[color] != null) {
			super.paint(g);
			final Graphics2D g2d = (Graphics2D) g;
			if (reverseImage && Configuration.getBoolean("reverseSide", false)) {
				g2d.rotate(Math.PI, MANA_WIDTH / 2, MANA_HEIGHT / 2);
			}
			g2d.drawImage(IMAGES[color], 0, 0, MANA_WIDTH, MANA_WIDTH, null);
			g2d.setColor(TEXT_COLOR[color % TEXT_COLOR.length]);

			if (StackManager.PLAYERS[0] != null
					&& StackManager.PLAYERS[0].registers != null) {
				final Rectangle2D stringDim = g2d.getFontMetrics().getStringBounds(
						String.valueOf(getPlayer().registers[color]), g);
				g2d.fill3DRect(MANA_WIDTH - (int) stringDim.getWidth() - 4,
						MANA_HEIGHT - 11, (int) stringDim.getWidth() + 3, 10, true);
				g2d.setColor(Color.black);
				g2d.drawString(String.valueOf(getPlayer().registers[color]), MANA_WIDTH
						- (int) stringDim.getWidth() - 2, MANA_HEIGHT - 2);

			}
		}
		/*
		 * draw the highlighted rectangle arround the card if not returned, or if we
		 * are in target mode.
		 */
		if (isHighLighted) {
			g.setColor(highLightColor);
			g.draw3DRect(0, 0, MANA_WIDTH - 1, MANA_HEIGHT - 1, true);
			g.draw3DRect(1, 1, MANA_WIDTH - 3, MANA_HEIGHT - 3, true);
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// only if left button is pressed
		StackManager.noReplayToken.take();
		try {
			if (ConnectionManager.isConnected()
					&& e.getButton() == MouseEvent.BUTTON1
					&& StackManager.idHandedPlayer == 0
					&& StackManager.actionManager.clickOn(this)) {
				StackManager.actionManager.succeedClickOn(this);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			StackManager.noReplayToken.release();
		}
	}

	/**
	 * This method is invoked when opponent has clicked on this object.
	 */
	public void clickOn() {
		// waiting for player information
		StackManager.actionManager.succeedClickOn(this);
	}

	@Override
	public void sendClickToOpponent() {
		// send this information to our opponent
		ConnectionManager.send(CoreMessageType.CLICK_MANA, reverseImage ? (byte) 0
				: (byte) 1, (byte) color);
	}

	@Override
	public String toString() {
		return IdCommonToken.PAYABLE_COLOR_NAMES[color];
	}

	/**
	 * return the player owning this mana
	 * 
	 * @return the player owning this mana
	 */
	public Player getPlayer() {
		return reverseImage ? StackManager.PLAYERS[1] : StackManager.PLAYERS[0];
	}

	/**
	 * represent all images used to represents energy source for each sort
	 */
	private static final Image[] IMAGES = new Image[IdCommonToken.PAYABLE_COLOR_NAMES.length];

	/**
	 * represent all foreground colors used to represent the amount of energy for
	 * each type
	 */
	private static final Color[] TEXT_COLOR = new Color[IMAGES.length];

	/**
	 * color of this mana
	 */
	public int color;

	/**
	 * List of pair [mana, restriction]
	 */
	private List<PairIntObject<Test>> restrictionList = new ArrayList<PairIntObject<Test>>();

	/**
	 * Indicates if graphics are reversed (PI rotation)
	 */
	private boolean reverseImage;

}