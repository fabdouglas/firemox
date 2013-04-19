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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.CardModelImpl;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.database.DatabaseCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.ListExpression;
import net.sf.firemox.test.TestOn;

/**
 * Copy a card on the specified card.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
class CopyCard extends UserAction implements StandardAction {

	/**
	 * Create an instance of CopyCard by reading a file Offset's file must
	 * pointing on the first byte of this action
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>card to copy [TestOn]</li>
	 * <li>card to apply copy [TestOn]</li>
	 * <li>excludes name [boolean]</li>
	 * <li>excludes colors [ListExpression]</li>
	 * <li>excludes idcards [ListExpression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	CopyCard(InputStream inputFile) throws IOException {
		super(inputFile);
		copySource = TestOn.deserialize(inputFile);
		copyDestination = TestOn.deserialize(inputFile);
		excludeName = inputFile.read() == 1;
		excludesColors = new ListExpression(inputFile);
		excludesIdCards = new ListExpression(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.COPY_CARD;
	}

	public boolean play(ContextEventListener context, Ability ability) {
		MCard source = copySource.getCard(ability, context, null);
		MCard to = copyDestination.getCard(ability, context, null);

		CardModelImpl sourceModel = (CardModelImpl) source.getCardModel();
		CardModelImpl targetModel = (CardModelImpl) to.getCardModel();
		CardModelImpl newModel = new CardModelImpl(sourceModel);

		// Copy abilities
		newModel.setAbilities(source.cachedAbilities);

		// Card colors
		int idColor = newModel.getIdColor();
		for (int color : excludesColors.getList(ability, null, context)) {
			idColor = (sourceModel.getIdColor() & ~color)
					| (targetModel.getIdColor() & color);
		}
		newModel.setIdColor(idColor);

		// Card types
		int idCard = newModel.getIdCard();
		for (int cardType : excludesIdCards.getList(ability, null, context)) {
			idCard = (sourceModel.getIdCard() & ~cardType)
					| (targetModel.getIdCard() & cardType);
		}
		newModel.setIdCard(idCard);

		// Card name
		final DatabaseCard database;
		if (excludeName) {
			newModel.setCardName(targetModel.getCardName());
		}

		database = new DatabaseCard(newModel, to.getDatabase().getDataProxy(), to
				.getDatabase().getPictureProxies());

		// Update the database
		to.setDataBase(database);
		to.refreshAbilities();
		return true;
	}

	@Override
	public String toString(Ability ability) {
		return "copy card";
	}

	/**
	 * The card to copy.
	 */
	private final TestOn copySource;

	/**
	 * The copy destination.
	 */
	private final TestOn copyDestination;

	/**
	 * Is the card name is copied.
	 */
	private final boolean excludeName;

	/**
	 * The colors to exclude.
	 */
	private final ListExpression excludesColors;

	/**
	 * The idcards to exclude.
	 */
	private final ListExpression excludesIdCards;

}