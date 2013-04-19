/*
 *   Magic-Project is a turn based strategy simulator
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
package net.sf.firemox.ui.wizard;

import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.ui.i18n.LanguageManager;


/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
public class Pile extends Ok {

	/**
	 * Create a new instance of this class.
	 * @param destinationZone
	 * @param position
	 * @param owner
	 */
	public Pile(int destinationZone, int position, Player owner) {

		super(null,LanguageManager.getString("wiz_arrange.title"), LanguageManager
				.getString("wiz_arrange.description", owner.zoneManager
						.getContainer(destinationZone)), "wiz_arrange.png", 600, 230);
		
		/*final JScrollPane scroll = new JScrollPane(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		container = new DropCardListener();
		this.movingCards = movingCards;
		this.order = order;
		this.owner = owner;
		for (int i = 0; i < movingCards.size(); i++) {
			final MCard card = movingCards.get(i);
			order[i] = i;
			container.add(card);
			card.getComponent(0).addMouseListener(this);
			card.getComponent(0).addMouseMotionListener(this);
			card.addComponentListener(container);
			card.setVisibility(Visibility.PUBLIC);
			card.tap(false);
		}
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		scroll.setAutoscrolls(true);
		scroll.setViewportView(container);
		gameParamPanel.add(scroll);*/
	}

	/* (non-Javadoc)
	 * @see net.sf.firemox.ui.wizard.Wizard#checkValidity()
	 */
	@Override
	protected boolean checkValidity() {
		// TODO Auto-generated method stub
		return false;
	}

}
