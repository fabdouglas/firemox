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
package net.sf.firemox.zone;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.firemox.clickable.Clickable;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.IdPositions;
import net.sf.firemox.token.Visibility;
import net.sf.firemox.token.VisibilityChange;
import net.sf.firemox.token.Visible;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.Pair;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.ui.component.TableTop;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;
import net.sf.firemox.ui.layout.FlowLayout2;
import net.sf.firemox.ui.layout.WallpaperTypes;

/**
 * A zone is a cards container.
 * 
 * @author Fabrice Daugan
 * @since 0.2c
 * @since 0.3 feature "reverseImage" implemented
 * @since 0.4 you can now change wallpaper/color of this MZone and setting
 * @since 0.52 "enableReverse" option are saved.
 * @since 0.71 visibility of cards can be set. Cards come into this zone
 *        returned or not immediately
 */
public abstract class MZone extends JPanel implements MouseListener, Visible {

	/**
	 * create an instance of MZone
	 * 
	 * @param idZone
	 *          id place of this panel
	 * @param layout
	 *          is it's layout
	 * @param superPanel
	 *          scroll panel containing this panel
	 * @param reverseImage
	 *          if true the back picture will be reversed
	 * @param name
	 *          the untranslated zone name
	 * @see net.sf.firemox.token.IdZones
	 */
	protected MZone(int idZone, LayoutManager layout, JScrollPane superPanel,
			boolean reverseImage, String name) {
		super(layout);
		setName(name);

		this.superPanel = superPanel;
		this.reverseImage = reverseImage;
		this.idZone = idZone;

		// A L&F bug requires this UI initialization to be done here
		if (superPanel != null) {
			superPanel.setBorder(null);
		}
	}

	/**
	 * Initialize UI
	 */
	public void initUI() {
		final String getter = "zones." + getZoneName() + "."
				+ (reverseImage ? "up" : "down");
		this.doMosaic = Configuration.getBoolean(getter + ".mosaic", true);

		if (superPanel != null) {
			superPanel.setViewportView(this);
			MToolKit.addOverlay(superPanel);
		}

		setBorder(null);
		setAutoscrolls(true);
		setWallPaperFile(Configuration.getString(getter + ".wallpaper", ""));
		setBackground(new Color(Configuration.getInt(getter + ".background", 0)));
		addMouseListener(this);
	}

	@Override
	public void paintComponent(Graphics g) {
		if (getImage() != null) {
			int width = getWidth();
			int height = getHeight();
			if (doMosaic && ZoneManager.bgDelegate != null) {

				// Delegate the painting to the L&F painter ignoring the reverse options
				super.paintComponent(g);
			} else {
				if (reverseImage
						&& (Configuration.getBoolean("reverseSide", false) || Configuration
								.getBoolean("reverseArt", true))) {
					if (doMosaic) {
						for (int y = height / picHeight + 1; y-- > 0;) {
							for (int x = width / picWidth + 1; x-- > 0;) {
								g.drawImage(getImage(), x * picWidth + picWidth, y * picHeight
										+ picHeight, x * picWidth, y * picHeight, 0, 0,
										picWidth - 1, picHeight - 1, null);
							}
						}
					} else {
						g.drawImage(getImage(), width, height, 0, 0, 0, 0, picWidth,
								picHeight, null);
					}
				} else {
					if (doMosaic) {
						for (int y = height / picHeight + 1; y-- > 0;) {
							for (int x = width / picWidth + 1; x-- > 0;) {
								g.drawImage(getImage(), x * picWidth, y * picHeight, picWidth,
										picHeight, null);
							}
						}
					} else {
						g.drawImage(getImage(), 0, 0, width, height, null);
					}
				}
			}
		} else if (ZoneManager.bgDelegate != null) {

			// Delegate the painting to the L&F painter ignoring the reverse options
			super.paintComponent(g);
		}
	}

	/**
	 * return the card at index
	 * 
	 * @param index
	 *          is the index where is the element
	 * @return the card at the specified index
	 */
	public final MCard getCard(int index) {
		return (MCard) getComponent(index);
	}

	/**
	 * return the last card (top)
	 * 
	 * @return the last card
	 */
	public MCard getTop() {
		return getCard(0);
	}

	/**
	 * return the first card (bottom)
	 * 
	 * @return the first card
	 */
	public MCard getBottom() {
		return getCard(getComponentCount() - 1);
	}

	/**
	 * Adds the specified card to this zone with the specified constraints at the
	 * specified index. Also notifies the layout manager to add the component to
	 * the this container's layout using the specified constraints object.
	 * 
	 * @param card
	 *          the card to be added
	 * @param constraints
	 *          an object expressing layout constraints for this
	 * @param position
	 *          the position in the container's list at which to insert the
	 *          component; <code>-1</code> means insert at the end card
	 * @see LayoutManager
	 */
	protected void add(MCard card, Object constraints, int position) {
		card.updateZoneVisibility(visibility);
		card.getMUI().isAutoAlign = true;
		if (position == -1) {
			super.add(card, constraints);
		} else {
			super.add(card, constraints, position);
		}
		updatePanel();
	}

	/**
	 * Indicates whether this card suits to the specified position code.
	 * 
	 * @param card
	 *          is the card to locate.
	 * @param position
	 *          the matching position code
	 * @return true if this card suits to the specified position code.
	 * @see net.sf.firemox.token.IdPositions#ON_THE_BOTTOM
	 * @see net.sf.firemox.token.IdPositions#ON_THE_TOP
	 */
	public final boolean isInPosition(MCard card, int position) {
		if (getCardCount() == 0) {
			return false;
		}
		if (position == IdPositions.ON_THE_BOTTOM) {
			return getBottom() == card;
		}
		return getCardCount() > position && getCard(position) == card;
	}

	/**
	 * Indicates whether this card suits to the specified position codes.
	 * 
	 * @param card
	 *          is the card to locate.
	 * @param min
	 *          the minimum position code
	 * @param max
	 *          the maximum position code
	 * @return true if this card suits to the specified position codes.
	 * @see net.sf.firemox.token.IdPositions#ON_THE_BOTTOM
	 * @see net.sf.firemox.token.IdPositions#ON_THE_TOP
	 */
	public final boolean isInPosition(MCard card, int min, int max) {
		int maximum;
		if(getCardCount() < max)
			maximum = getCardCount();
		else
			maximum = max;
		
		for(int i=min; i<= maximum; i++)
			if(getCard(i) == card)
				return true;
		
		return false;
	}

	/**
	 * Return the index of the specified card within this zone
	 * 
	 * @param card
	 *          the card to search
	 * @return the found index
	 */
	public Pair<Integer, Integer> getRealIndexOf(MCard card) {
		int index = super.getComponentZOrder(card);
		if (index != -1) {
			return new Pair<Integer, Integer>(index, 0);
		}
		// the card is not directly in play but maybe attached
		final Component[] components = getComponents();
		for (index = components.length; index-- > 0;) {
			final Component component = components[index];
			if (component instanceof Container) {
				final int subIndex = ((Container) component).getComponentZOrder(card);
				if (subIndex != -1) {
					return new Pair<Integer, Integer>(index, subIndex);
				}
			}
		}
		throw new InternalError("" + card + " has not been found in " + this);
	}

	/**
	 * return the backImage to display in this JPanel
	 * 
	 * @return the backImage to display in this JPanel
	 */
	private Image getImage() {
		return backImage;
	}

	/**
	 * return the translated name of this zone
	 * 
	 * @return the name of this panel
	 */
	@Override
	public String toString() {
		return LanguageManagerMDB.getString("zone." + getZoneName());
	}

	/**
	 * This function returns the result of Component#getName() This will return
	 * the untranslated name of this zone. The toString()method returns the
	 * translated name of this zone
	 * 
	 * @return the result of Component#getName()
	 * @see #toString()
	 */
	public String getZoneName() {
		return getName();
	}

	/**
	 * return the wallpaper file, return null if current panel is not in wallpaper
	 * mode.
	 * 
	 * @return current wallpaper file
	 */
	String getWallPaperFile() {
		String getter = "zones." + getZoneName() + "."
				+ (reverseImage ? "up" : "down");
		return Configuration.getString(getter + ".wallpaper", "");
	}

	/**
	 * set the new wallpaper to the picture from a specified file. The current
	 * display mode of this panel will be now as "wallpaper"
	 * 
	 * @param newFile
	 *          the new wallpaper. If null or null sized, wallpaper is disabled
	 */
	final void setWallPaperFile(String newFile) {
		String getter = "zones." + getZoneName() + "."
				+ (reverseImage ? "up" : "down");
		if (newFile != null && newFile.length() > 0) {
			try {
				final Image backImage = Picture.loadImage(newFile);
				picWidth = backImage.getWidth(this);
				picHeight = backImage.getHeight(this);
				this.backImage = backImage;
				Configuration.setProperty(getter + ".wallpaper", newFile);
			} catch (MalformedURLException e) {
				// IGNORING
			}
		} else {
			Configuration.setProperty(getter + ".wallpaper", "");
			backImage = null;
		}
	}

	/**
	 * Set the new wallpaper for the current game.
	 * 
	 * @param inputStream
	 *          the input Stream containing the wallpaper configuration.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public void readWallPaperConfiguration(InputStream inputStream)
			throws IOException {
		WallpaperTypes type = WallpaperTypes.values()[inputStream.read()];
		switch (type) {
		case color:
			backImage = null;
			setBackground(new Color(MToolKit.readInt24(inputStream)));
			break;
		case watermark:
			// ignore water mark of opponent
			break;
		case picture:
			doMosaic = false;
			backImage = MToolKit.readImage(inputStream);
			picWidth = backImage.getWidth(this);
			picHeight = backImage.getHeight(this);
			break;
		case mosaicPicture:
		default:
			doMosaic = true;
			backImage = MToolKit.readImage(inputStream);
			picWidth = backImage.getWidth(this);
			picHeight = backImage.getHeight(this);
			break;
		}
	}

	/**
	 * Send the wallpaper configuration over the given output stream.
	 * 
	 * @param out
	 *          the output Stream containing the wallpaper configuration.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public void writeWallPaperConfiguration(OutputStream out) throws IOException {
		if (ZoneManager.bgDelegate != null) {
			out.write(WallpaperTypes.watermark.ordinal());
		} else if (backImage != null) {
			if (doMosaic) {
				out.write(WallpaperTypes.mosaicPicture.ordinal());
			} else {
				out.write(WallpaperTypes.picture.ordinal());
			}

			// Send the picture data
			MToolKit.writeFile(MToolKit.getFile(getWallPaperFile()), out);
		} else {
			out.write(WallpaperTypes.color.ordinal());
			MToolKit.writeInt24(out, getBackground().getRGB());
		}
	}

	@Override
	public void removeAll() {
		super.removeAll();
		updatePanel();
	}

	/**
	 * update this panel in function of it's components
	 */
	public void updatePanel() {
		// invalidate();
		doLayout();
		repaint();
	}

	/**
	 * Shuffle the zone
	 */
	public void shuffle() {
		final List<Component> components = Arrays.asList(getComponents());
		Collections.shuffle(components, MToolKit.random);
		removeAll();
		for (Component component : components) {
			add(component);
		}
		updatePanel();
	}

	public void mouseReleased(MouseEvent e) {
		mousePressed(e);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() > 1) {
			for (Component component : getComponents()) {
				((AbstractCard) component).getMUI().isAutoAlign = true;
			}
			doLayout();
		}
	}

	public void mouseEntered(MouseEvent e) {
		// Ignore this event
	}

	// Ignore this event
	public void mouseExited(MouseEvent e) {
		// Ignore this event
	}

	/**
	 * Update the "reversed" state of this component.
	 */
	public void updateReversed() {
		// Ignore this event
	}

	/**
	 * Update the layout of this panel depending on the owner.
	 * 
	 * @param panel
	 *          the panel to update
	 * @param reverse
	 *          is this zone is reversed.
	 */
	protected static void updateLayouts(JComponent panel, boolean reverse) {
		if (reverse) {
			panel.setLayout(new FlowLayout2());
		} else {
			panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		}
	}

	/**
	 * is called when you click on me
	 * 
	 * @param e
	 *          is the mouse event
	 */
	public void mousePressed(MouseEvent e) {
		PopupManager.instance.mousePressed(e, this);
	}

	/**
	 * Save wallpaper name, display options and back colors of this panel to a
	 * specified output stream
	 */
	void saveSettings() {
		String setter = "zones." + getZoneName() + "."
				+ (reverseImage ? "up" : "down");
		Configuration.setProperty(setter + ".mosaic", doMosaic);
		Configuration.setProperty(setter + ".background", getBackground().getRGB());
	}

	/**
	 * Add a card to this panel. If tag 'returnedCards' is true, this card comes
	 * returned into this zone.
	 * 
	 * @param card
	 *          the card to add to this zone.
	 */
	public void remove(AbstractCard card) {
		super.remove(card);
		updatePanel();
	}

	/**
	 * return the number of cards in this panel
	 * 
	 * @return the number of cards in this panel
	 */
	public int getCardCount() {
		return getComponentCount();
	}

	/**
	 * Checks all cards corresponding to the specified constraints including
	 * attached cards.
	 * 
	 * @param test
	 *          applied to count valid cards
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @return amount of card matching with the specified test
	 */
	public int countAllCardsOf(Test test, Ability ability) {
		int result = 0;
		for (Component component : getComponents()) {
			result += ((AbstractCard) component).countAllCardsOf(test, ability, true);
		}
		return result;
	}

	/**
	 * Checks all cards corresponding to the specified constraints including
	 * attached cards.
	 * 
	 * @param test
	 *          applied to count valid cards
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param limit
	 *          is the desired count.
	 * @param canBePreempted
	 *          <code>true</code> if the valid targets can be determined before
	 *          runtime.
	 * @return amount of cards matching with the specified test. Highest value is
	 *         <code>limit</code>.
	 */
	public int countAllCardsOf(Test test, Ability ability, int limit,
			boolean canBePreempted) {
		int result = 0;
		int index = getCardCount();
		while (index-- > 0 && result < limit) {
			result += ((AbstractCard) getComponent(index)).countAllCardsOf(test,
					ability, canBePreempted);
		}
		return result;
	}

	/**
	 * Checks all cards corresponding to this constraints
	 * 
	 * @param test
	 *          applied to count valid cards
	 * @param list
	 *          the list containing the founded cards
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 */
	public void checkAllCardsOf(Test test, List<Target> list, Ability ability) {
		for (Component component : getComponents()) {
			((AbstractCard) component).checkAllCardsOf(test, list, ability);
		}
	}

	/**
	 * Disable all cards of this zone manager
	 */
	public void disHighLightAll() {
		for (Component component : getComponents()) {
			if (component instanceof Clickable) {
				((Clickable) component).disHighLight();
			}
		}
		disHighLight();
	}

	/**
	 * Disable only this component, not the components of this zone.
	 */
	public void disHighLight() {
		// reset the tabbed panel background of this panel
		int id = TableTop.getInstance().tabbedPane.indexOfComponent(superPanel);
		if (id == -1) {
			Log.debug("error");
			throw new InternalError("index=-1; zone=" + this);
		} else if (TableTop.getInstance().tabbedPane.getBackgroundAt(id) != null) {
			TableTop.getInstance().tabbedPane.setBackgroundAt(id, null);
		}
	}

	/**
	 * Highlight only this component, not the components of this zone. Use instead
	 * the specific highlight color of the desired zone.
	 * 
	 * @param color
	 *          the color of highlight.
	 */
	public void highLight(Color color) {
		TableTop.getInstance().tabbedPane.setSelectedComponent(superPanel);
		TableTop.getInstance().tabbedPane.setBackgroundAt(
				TableTop.getInstance().tabbedPane.indexOfComponent(superPanel), color);
	}

	/**
	 * Remove all cards of this zone
	 */
	public void reset() {
		removeAll();
		visibility = Visibility.HIDDEN;
	}

	public void setVisibility(Visibility visibility) {
		if (this.visibility != visibility) {
			this.visibility = visibility;
			// update the cards
			for (Component component : getComponents()) {
				if (component instanceof MCard) {
					((MCard) component).setVisibility(visibility);
				}
			}
			repaint();
		}
	}

	/**
	 * Add a card at the bottom of this panel. If tag 'returnedCards' is true,
	 * this card comes returned into this zone.
	 * 
	 * @param card
	 *          the card to add to this zone.
	 */
	public void addBottom(MCard card) {
		add(card, -1);
	}

	/**
	 * Add a card at the top of this panel. If tag 'returnedCards' is true, this
	 * card comes returned into this zone. Be careful, you can use this function
	 * only if the specified component is not yet in this container.
	 * 
	 * @param card
	 *          the card to add to this zone.
	 */
	public void addTop(MCard card) {
		add(card, 0);
	}

	/**
	 * Add a card to this panel. If tag 'returnedCards' is true, this card comes
	 * returned into this zone.
	 * 
	 * @param card
	 *          the card to add to this zone.
	 * @param position
	 *          the position index of insertion.
	 */
	public void add(MCard card, int position) {
		add(card, null, position);
	}

	/**
	 * Return player identifier of controller of this zone.
	 * 
	 * @return player identifier of controller of this zone.
	 */
	public int getControllerIdPlayer() {
		return reverseImage ? 1 : 0;
	}

	/**
	 * Return controller of this zone.
	 * 
	 * @return controller of this zone.
	 */
	public Player getController() {
		return StackManager.PLAYERS[getControllerIdPlayer()];
	}

	/**
	 * Return the zone identifier
	 * 
	 * @return the zone identifier
	 */
	public final int getZoneId() {
		return idZone;
	}

	/**
	 * Start the drag and drop management for the given card.
	 * 
	 * @param card
	 *          The drag and drop component.
	 * @param mousePoint
	 *          The drag and drop starting point.
	 * @return <code>true</code> if the drag and drop is managed by this zone.
	 */
	public boolean startDragAndDrop(MCard card, Point mousePoint) {
		dragAndDropComponent = card;
		this.mousePoint = mousePoint;
		return true;
	}

	/**
	 * Return <code>true</code> if the given card should be painted as reversed
	 * card.
	 * 
	 * @param card
	 *          the card to draw.
	 * @return <code>true</code> if the given card should be painted as reversed
	 *         card.
	 */
	public boolean isMustBePaintedReversed(MCard card) {
		return card.reversed && Configuration.getBoolean("reverseArt", true);
	}

	/**
	 * Return <code>true</code> if the given card should be painted entirely.
	 * 
	 * @param card
	 *          the card to draw.
	 * @return <code>true</code> if the given card should be painted entirely.
	 */
	public boolean isMustBePainted(MCard card) {
		return true;
	}

	/**
	 * Is this zone is shared with all players.
	 * 
	 * @return <code>true</code> if this zone is shared with all players.
	 */
	public boolean isShared() {
		return false;
	}

	public boolean isVisibleForYou() {
		return visibility.isVisibleForYou(getController());
	}

	public boolean isVisibleForOpponent() {
		return visibility.isVisibleForOpponent(getController());
	}

	public void increaseFor(Player player, VisibilityChange visibilityChange) {
		setVisibility(visibility.increaseFor(player, visibilityChange));
	}

	public void decreaseFor(Player player, VisibilityChange visibilityChange) {
		setVisibility(visibility.decreaseFor(player, visibilityChange));
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void restoreVisibility() {
		if (previousVisibility != null) {
			restoreVisibility(previousVisibility);
		}
	}

	public void restoreVisibility(Visibility visibility) {
		this.previousVisibility = null;
		setVisibility(visibility);
	}

	/**
	 * The previous visibility of this zone.
	 */
	protected Visibility previousVisibility;

	/**
	 * picture displayed in the panel
	 */
	protected Image backImage = null;

	/**
	 * The picture's height
	 */
	private int picHeight;

	/**
	 * The picture's width
	 */
	private int picWidth;

	/**
	 * Is the background picture is drawn as mosaic. Otherwise, is centered.
	 */
	boolean doMosaic;

	/**
	 * Are the images are reversed in this zone.
	 */
	protected boolean reverseImage;

	/**
	 * Indicates all cards of this zone are returned or not. All cards coming into
	 * this zone would be returned or not depending this flag.
	 * 
	 * @since 0.80 cards are hidden by default
	 * @since 0.90 use Visibility class instead of boolean
	 */
	private Visibility visibility = Visibility.PUBLIC;

	/**
	 * the parent scroll pane
	 */
	public final JScrollPane superPanel;

	/**
	 * The zone identifier.
	 */
	private final int idZone;

	/**
	 * The drag and drop starting point
	 */
	public Point mousePoint;

	/**
	 * The drag and drop component.
	 */
	public MCard dragAndDropComponent = null;

}