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
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.ui.layout.SectorLayout;

/**
 * Represents the play zone
 * 
 * @author Fabrice Daugan
 * @since 0.2d
 * @since 0.3 feature "reverseImage" implemented
 * @since 0.4 you can now change wallpaper/color of this MZone and setting are
 *        saved.
 * @since 0.72 border layout added
 * @since 0.91 use list instead of panel to represent a sector.
 */
public class Play extends MZone {

	/**
	 * The zone name.
	 */
	public static final String ZONE_NAME = "play";

	/**
	 * create a new instance of Play
	 * 
	 * @param reverseImage
	 *          if true the back picture will be reversed
	 * @since 0.3 feature "reverseImage" implemented
	 * @see IdZones
	 */
	public Play(boolean reverseImage) {
		super(IdZones.PLAY, null, null, reverseImage, ZONE_NAME);
		sectors = new ArrayList<ZoneSector>();
	}

	@Override
	public void initUI() {
		super.initUI();
		setMinimumSize(new Dimension(90, 60));
	}

	@Override
	public void updateReversed() {
		// need to update?
		if (oldReverse == (Configuration.getBoolean("reverseArt", true) && reverseImage)) {
			return;
		}
		synchronized (getTreeLock()) {
			oldReverse = !oldReverse;
			// TODO change the layout
		}

	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(super.getPreferredSize().width,
				getParent().getSize().height);
	}

	@Override
	public void remove(Component component) {
		if (component instanceof AbstractCard) {
			for (ZoneSector sector : sectors) {
				if (sector.remove((AbstractCard) component))
					break;
			}
			super.remove((AbstractCard) component);
			return;
		}
		throw new InternalError("should not be called");
	}

	@Override
	public void reset() {
		for (ZoneSector sector : sectors) {
			sector.getCards().clear();
		}
		sectors.clear();
		super.reset();
	}

	@Override
	public void remove(AbstractCard card) {
		for (ZoneSector sector : sectors) {
			if (sector.remove(card))
				break;
		}
		super.remove(card);
	}

	@Override
	public void add(MCard card, int pIndex) {
		boolean found = false;
		for (ZoneSector sector : sectors) {
			if (sector.getTest().test(null, card)) {
				sector.add(card);
				found = true;
				break;
			}
		}
		if (!found)
			sectors.get(0).add(card);
		super.add(card, pIndex);
	}

	@Override
	public final void highLight(Color targetColor) {
		// TODO Make a highlighted border
	}

	@Override
	public void disHighLight() {
		// TODO Make a highlighted border
	}

	@Override
	public void shuffle() {
		throw new InternalError("should not be called");
	}

	/**
	 * Initialize the sector configurations.
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>sectors [SectorConfiguration[]]</li>
	 * </ul>
	 * <br>
	 * 
	 * @param inputStream
	 *          the stream containing the layout configuration of this zone for a
	 *          specified layout
	 * @throws IOException
	 *           error while reading.
	 */
	public static void initSectorConfigurations(InputStream inputStream)
			throws IOException {
		sectorConfigurations = new ArrayList<SectorConfiguration>();
		for (int i = inputStream.read(); i-- > 0;) {
			sectorConfigurations.add(new SectorConfiguration(inputStream));
		}
		StackManager.PLAYERS[0].zoneManager.play.initSectors();
		StackManager.PLAYERS[1].zoneManager.play.initSectors();
	}

	/**
	 * Initialize the sectors of this instance.
	 */
	public void initSectors() {
		for (SectorConfiguration sectorConfiguration : sectorConfigurations) {
			sectors.add(new ZoneSector(sectorConfiguration));
		}
		setLayout(new SectorLayout(sectors));
	}

	private final List<ZoneSector> sectors;

	private static List<SectorConfiguration> sectorConfigurations;

	private boolean oldReverse = Configuration.getBoolean("reverseArt", true)
			&& reverseImage;

}