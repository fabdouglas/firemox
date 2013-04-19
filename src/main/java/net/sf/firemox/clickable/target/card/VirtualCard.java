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
package net.sf.firemox.clickable.target.card;

import static net.sf.firemox.clickable.target.card.CardFactory.ROTATE_SCALE;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;

import net.sf.firemox.action.WaitActivatedChoice;
import net.sf.firemox.action.target.ChosenTarget;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.TargetFactory;
import net.sf.firemox.database.DatabaseCard;
import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.stack.TargetHelper;
import net.sf.firemox.token.IdCardColors;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.StatePicture;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.Reversable;
import net.sf.firemox.ui.Tappable;
import net.sf.firemox.ui.TooltipFilter;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;
import net.sf.firemox.zone.MZone;

import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public class VirtualCard extends JComponent implements MouseListener, Tappable,
		Reversable, MouseMotionListener {

	/**
	 * Creates a new instance of VirtualCard <br>
	 * 
	 * @param card
	 *          the real card
	 */
	public VirtualCard(MCard card) {
		this.card = card;
		setOpaque(false);
		addMouseListener(this);
		addMouseMotionListener(this);
		tap(false);
	}

	/**
	 * Return the tooltip filter of this card
	 * 
	 * @return the tooltip filter of this card
	 */
	private TooltipFilter refreshToolTipFilter() {
		TooltipFilter privateFilter = cardInfoFilter;
		if (privateFilter == null) {
			privateFilter = TooltipFilter.fullInstance;
			for (TooltipFilter tooltipFilter : CardFactory.tooltipFilters) {
				if (tooltipFilter.suits(card)) {
					// the filter has been found
					privateFilter = tooltipFilter;
					break;
				}
			}
		}
		cardInfoFilter = privateFilter;
		return privateFilter;
	}

	@SuppressWarnings("null")
	@Override
	public void paintComponent(Graphics g) {
		final Graphics2D g2D = (Graphics2D) g;

		// Renderer
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Optimization card painting
		final MZone container = card.getContainer();
		final boolean shortPaint = container == null
				|| !container.isMustBePainted(card);

		// tap/reverse operation : PI/2, PI, -PI/2 rotation
		if (!shortPaint) {
			if (container.isMustBePaintedReversed(card)) {
				if (card.tapped) {
					g2D.translate(rotateTransformY, CardFactory.cardWidth
							+ rotateTransformX);
					g2D.rotate(angle - Math.PI / 2);
				} else {
					g2D.translate(CardFactory.cardWidth + rotateTransformX,
							CardFactory.cardHeight + rotateTransformY);
					g2D.rotate(Math.PI - angle);
				}
			} else {
				if (card.tapped) {
					g2D.translate(CardFactory.cardHeight + rotateTransformY,
							rotateTransformX);
					g2D.rotate(Math.PI / 2 + angle);
				} else {
					g2D.translate(rotateTransformX, rotateTransformY);
					g2D.rotate(angle);
				}
			}
		}

		if (container != null) {
			if (container.isVisibleForOpponent() && !card.isVisibleForOpponent()
					&& card.isVisibleForYou()) {
				/*
				 * This card is visible for you but not for opponent in despite of the
				 * fact the container is public.
				 */
				g2D.drawImage(card.scaledImage(), null, null);
				g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
						0.5f));
				g2D.drawImage(DatabaseFactory.scaledBackImage, null, null);
			} else if (!card.isVisibleForYou()) {
				g2D.drawImage(DatabaseFactory.scaledBackImage, null, null);
			} else {
				g2D.drawImage(card.scaledImage(), null, null);
			}
		}

		if (shortPaint)
			return;

		/*
		 * The card picture is displayed as a rounded rectangle with 0,90,180 or
		 * 270°
		 */
		if (card.isHighLighted
				&& (card.isVisibleForYou() || StackManager.idHandedPlayer == 0)) {
			// Draw the rounded colored rectangle
			g2D.setColor(card.highLightColor);
			g2D.draw3DRect(0, 0, CardFactory.cardWidth - 2,
					CardFactory.cardHeight - 2, false);
		}

		// Draw the eventual progress bar
		card.database.updatePaintNotification(card, g);

		// Cursor for our pretty pictures
		int px = 3;
		int py = 3;
		int maxX = CardFactory.cardWidth - 2;

		// for in-play, visible cards
		if (card.isVisibleForYou()
				|| (container != null && container.isVisibleForYou())) {

			// draw registers above the card's picture
			if (refreshToolTipFilter().powerANDtoughness) {
				// power / toughness
				final String powerANDtoughness = String.valueOf(card
						.getValue(IdTokens.POWER))
						+ "/" + card.getValue(IdTokens.TOUGHNESS);
				g2D.setFont(g2D.getFont().deriveFont(Font.BOLD, 13));
				final Rectangle2D stringDim = g2D.getFontMetrics().getStringBounds(
						powerANDtoughness, g2D);
				g2D.setColor(CardFactory.powerToughnessColor);
				g2D.drawString(powerANDtoughness, (int) (CardFactory.cardWidth
						- stringDim.getWidth() - 3), CardFactory.cardHeight - 5);
				g2D.setColor(Color.BLUE);
				g2D.drawString(powerANDtoughness, (int) (CardFactory.cardWidth
						- stringDim.getWidth() - 3), CardFactory.cardHeight - 6);
			}

			/*
			 * START drawing additional pictures
			 */

			// draw state pictures
			for (StatePicture statePicture : CardFactory.statePictures) {
				if (px + 13 > maxX) {
					px = 3;
					py += 13;
				}
				if (statePicture.paint(card, g2D, px, py)) {
					px += 13;
				}
			}

			// draw properties
			if (card.cachedProperties != null) {
				for (Integer property : card.cachedProperties) {
					if (Configuration.getBoolean("card.property.picture", true)
							&& CardFactory.propertyPictures.get(property) != null) {
						// There is an associated picture to this property
						if (px + 13 > maxX) {
							px = 3;
							py += 13;
						}
						g2D.drawImage(CardFactory.propertyPictures.get(property), px, py,
								12, 12, null);
						px += 13;
					}
				}
			}
		}

		// draw attached objects
		int startX = 3;
		int startY = CardFactory.cardHeight - 18;
		if (card.registerModifiers != null) {
			for (int i = card.registerModifiers.length; i-- > 0;) {
				if (card.registerModifiers[i] != null) {
					startX = card.registerModifiers[i].paintObject(g, startX, startY);
				}
			}
		}
		if (card.propertyModifier != null) {
			startX = card.propertyModifier.paintObject(g, startX, startY);
		}
		if (card.idCardModifier != null) {
			card.idCardModifier.paintObject(g, startX, startY);
			/*
			 * END drawing additional pictures
			 */
		}

		// Draw the target Id if helper said it
		final String id = TargetHelper.getInstance().getMyId(card);
		if (id != null) {
			if (px + 13 > maxX) {
				px = 3;
				py += 13;
			}
			if (id == TargetHelper.STR_CONTEXT1) {
				// TODO I am in the context 1, draw a picture
				g2D.setColor(Color.BLUE);
				g2D.setFont(g2D.getFont().deriveFont(Font.BOLD, 60 / id.length()));
				g2D.drawString(id, 5, CardFactory.cardHeight - 15);
			} else if (id == TargetHelper.STR_CONTEXT2) {
				// TODO I am in the context 2, draw a picture
				g2D.setColor(Color.BLUE);
				g2D.setFont(g2D.getFont().deriveFont(Font.BOLD, 60 / id.length()));
				g2D.drawString(id, 5, CardFactory.cardHeight - 15);
			} else if (id != TargetHelper.STR_SOURCE) {
				// } else if (id == TargetHelper.STR_SOURCE) {
				// TODO I am the source, draw a picture
				// } else {
				// I am a target
				g2D.drawImage(TargetHelper.getInstance().getTargetPictureSml(), px, py,
						null);
				py += 12;
			}
		}

		g2D.dispose();
	}

	/**
	 * is called when mouse is on this card, will display a preview
	 * 
	 * @param e
	 *          is the mouse event
	 * @since 0.71 art author and rules author have been added to the tooltip
	 */
	public void mouseEntered(MouseEvent e) {
		if (card.isVisibleForYou()) {
			CardFactory.previewCard.setImage(card.image(), card.getCardName());
			MagicUIComponents.databasePanel.revalidate(card);
		}
		setToolTipText(getTooltipString());
		if (card.getContainer().getZoneId() == IdZones.STACK) {
			TargetHelper.getInstance().addTargetedBy(StackManager.getContextOf(card));
		}
	}

	/**
	 * Return HTML tooltip string of this card.
	 * 
	 * @return HTML tooltip string of this card.
	 */
	public String getTooltipString() {
		final StringBuilder toolTip = new StringBuilder(400);

		// played activated ability
		if (card.getIdZone() == IdZones.STACK && card.isACopy()) {
			/*
			 * -> this card is not the source of ability too. Only the information of
			 * ability is displayed
			 */
			toolTip.append(CardFactory.ttHeaderAbility);
			toolTip.append(StackManager.getAbilityOf(card).toHtmlString(
					StackManager.getInstance().getAbilityContext()));
			toolTip.append(CardFactory.ttAbiltityEnd);
			toolTip.append(CardFactory.ttManacost);
			toolTip.append(StackManager.getHtmlManaCost(card));
			toolTip.append("(");
			toolTip.append(StackManager.getTotalManaPaid(card));
			toolTip.append(CardFactory.ttManapaid);
			toolTip.append(StackManager.getHtmlManaPaid(card));
		} else {

			// html header and card name
			toolTip.append(CardFactory.ttHeader);
			final MZone container = card.getContainer();
			if (!card.isVisibleForYou()
					&& (container == null || !container.isVisibleForYou())) {
				toolTip.append("??");
			} else {
				if (!card.isVisibleForYou()) {
					toolTip.append(LanguageManager.getString("unkown.card.name"));
				} else {
					toolTip.append(card.database.getLocalName());
				}

				// get the tooltip filter
				final TooltipFilter privateFilter = refreshToolTipFilter();

				// card types
				if (privateFilter.types && card.getIdCard() != 0) {
					int startIndex = CardFactory.exportedIdCardValues.length;
					int writtenTypes = 0;
					StringBuilder typeSet = null;
					while (startIndex-- > 0 && card.getIdCard() != 0) {
						if (MCard.hasIdCard(card.getIdCard(),
								CardFactory.exportedIdCardValues[startIndex])) {
							if (MCard.hasIdCard(writtenTypes,
									CardFactory.exportedIdCardValues[startIndex])) {
								// is a type sets
								if (typeSet == null) {
									typeSet = new StringBuilder(30);
									typeSet.append(" (");
								} else {
									typeSet.append(", ");
								}
								typeSet.append(CardFactory.exportedIdCardNames[startIndex]);
							} else {
								if (writtenTypes == 0) {
									toolTip.append(CardFactory.ttTypes);
								} else {
									toolTip.append(", ");
								}
								toolTip.append(CardFactory.exportedIdCardNames[startIndex]);
								writtenTypes |= CardFactory.exportedIdCardValues[startIndex];
							}
						}
					}
					if (typeSet != null) {
						toolTip.append(typeSet);
						toolTip.append(')');
					}
				}

				// card colors
				if (privateFilter.colors) {
					int writtenColors = 0;
					for (int i = IdCardColors.CARD_COLOR_VALUES.length; i-- > 1;) {
						if (card.hasIdColor(IdCardColors.CARD_COLOR_VALUES[i])) {
							if (writtenColors == 0) {
								toolTip.append(CardFactory.ttColors);
							} else {
								toolTip.append(", ");
							}
							toolTip.append(StringUtils.capitalize(LanguageManager
									.getString(IdCardColors.CARD_COLOR_NAMES[i])));
							writtenColors = 1;
						}
					}
				}

				if (privateFilter.properties) {
					// card properties
					final Iterator<Integer> it = card.cachedProperties.iterator();
					if (it.hasNext()) {
						toolTip.append(CardFactory.ttProperties);
						while (it.hasNext()) {
							final Integer property = it.next();
							boolean hasProperty = false;
							toolTip.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;");

							if (Configuration.getBoolean("card.property.tooltip.picture",
									true)
									&& CardFactory.propertyPicturesHTML.get(property) != null) {
								// There is an associated picture to this property
								toolTip.append(CardFactory.propertyPicturesHTML.get(property));
								hasProperty = true;
							}

							if (Configuration.getBoolean("card.property.tooltip.name", true)) {
								final String propertyName = CardFactory.exportedProperties
										.get(property);
								if (hasProperty)
									toolTip.append("&nbsp; - &nbsp;");
								hasProperty = true;
								if (propertyName == null) {
									// not name associated to this property
									toolTip.append("<font color='red'>(").append(property)
											.append(")</font>");
								} else {
									toolTip.append(propertyName);
								}
							}
							if (Configuration.getBoolean("card.property.tooltip.id", true)) {
								if (hasProperty)
									toolTip.append("&nbsp; - &nbsp;");
								toolTip.append(property);
							}
						}
					}
				}

				if (privateFilter.powerANDtoughness) {
					// power
					toolTip.append(CardFactory.ttPower);
					toolTip.append(card.getValue(IdTokens.POWER));
					// toughness
					toolTip.append(CardFactory.ttToughness);
					toolTip.append(card.getValue(IdTokens.TOUGHNESS));
				}

				if (card.isVisibleForYou()) {
					// states
					int value = card.getValue(IdCommonToken.STATE);
					if (value != 0) {
						int writtenStates = 0;
						toolTip.append(CardFactory.ttState);
						for (StatePicture statePicture : CardFactory.statePictures) {
							if (statePicture.hasState(value)) {
								if (writtenStates != 0) {
									toolTip.append(", ");
								}
								toolTip.append(LanguageManagerMDB.getString(statePicture
										.toString()));
								writtenStates = 1;
							}
						}
					}

					// damages
					value = card.getValue(IdCommonToken.DAMAGE);
					if (privateFilter.damage && value > 0) {
						toolTip.append(CardFactory.ttDamage);
						toolTip.append(value);
					}
				}

				// Display abilities
				if (card.getIdZone() == IdZones.STACK) {
					// played activated ability from this card
					toolTip.append(CardFactory.ttAbility);
					try {
						toolTip.append(StackManager.getAbilityOf(card).toHtmlString(
								StackManager.getInstance().getAbilityContext()));
					} catch (Throwable e) {
						// The is not yet in the stack now.
						Log.info(e);
						return null;
					}
					toolTip.append(CardFactory.ttAbiltityEnd);
					toolTip.append(CardFactory.ttManacost);
					toolTip.append(StackManager.getHtmlManaCost(card));
					toolTip.append("(");
					toolTip.append(StackManager.getTotalManaCost(card));
					toolTip.append(CardFactory.ttManapaid);
					toolTip.append(StackManager.getHtmlManaPaid(card));
				} else if (StackManager.actionManager.currentAction == WaitActivatedChoice
						.getInstance()) {
					// playable activated abilities of this card
					final List<Ability> list = WaitActivatedChoice.getInstance()
							.abilitiesOf(card);
					final List<Ability> advList = WaitActivatedChoice.getInstance()
							.advancedAbilitiesOf(card);
					if (list != null && !list.isEmpty()) {
						toolTip.append(CardFactory.ttAbility);
						for (Ability ability : list) {
							toolTip.append("<br>&nbsp;&nbsp;");
							toolTip.append(ability.toHtmlString(null));
						}
						toolTip.append(CardFactory.ttAbiltityEnd);
					}

					// playable advanced activated abilities of this card
					if (advList != null && !advList.isEmpty()) {
						toolTip.append(CardFactory.ttAdvancedAability);
						for (Ability ability : advList) {
							toolTip.append("<br>&nbsp;&nbsp;");
							toolTip.append(ability.toHtmlString(null));
						}
						toolTip.append(CardFactory.ttAdvancedAabilityEnd);
					}
				}

				// rule credits
				if (card.database.getRulesCredit() != null) {
					toolTip.append(CardFactory.ttRulesAuthor);
					toolTip.append(card.database.getRulesCredit());
				}
			}
		}
		// append the "this is a copy
		if (card.hasDirtyDataBase()) {
			DatabaseCard orCard = card.getOriginalDatabase();
			toolTip.append(TargetFactory.tooltipDirtyDataBase);
			toolTip.append(orCard.getLocalName());
		}
		// append the "this is [not] a valid target" text
		if (StackManager.isTargetMode()) {
			if (((ChosenTarget) StackManager.actionManager.currentAction)
					.isValidTarget(card)) {
				toolTip.append(TargetFactory.tooltipValidTarget);
			} else {
				toolTip.append(TargetFactory.tooltipInvalidTarget);
			}
		}
		final String id = TargetHelper.getInstance().getMyId(card);
		if (id != null) {
			if (id != TargetHelper.STR_CONTEXT1
			// TODO I am in the context 1, draw a picture
					&& id != TargetHelper.STR_CONTEXT2
					// TODO I am in the context 2, draw a picture
					&& id != TargetHelper.STR_SOURCE) {
				// TODO I am the source, draw a picture
				// I am already targeted
				toolTip.append("<br>");
				toolTip.append(TargetHelper.getInstance().getTargetPictureAsUrl());
			}
		}

		toolTip.append("</html>");
		return toolTip.toString();
	}

	public void mouseClicked(MouseEvent e) {
		card.mouseClicked(e);
	}

	public void mousePressed(MouseEvent e) {
		if (card.getParent() instanceof MZone) {
			card.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			((MZone) card.getParent()).startDragAndDrop(card, e.getPoint());
		}
	}

	public void mouseDragged(MouseEvent e) {
		if (card.getParent() instanceof MZone
				&& ((MZone) card.getParent()).dragAndDropComponent == card) {
			isAutoAlign = false;
			Point mousePoint = ((MZone) card.getParent()).mousePoint;
			Point drag = e.getPoint();
			Point cardLocation = (Point) card.getLocation().clone();
			card.setLocation(cardLocation.x + drag.x - mousePoint.x, cardLocation.y
					+ drag.y - mousePoint.y);
		}
	}

	public void mouseReleased(MouseEvent e) {
		card.setCursor(Cursor.getDefaultCursor());
		card.mouseReleased(e);
	}

	public void mouseExited(MouseEvent e) {
		card.mouseExited(e);
	}

	public void tap(boolean tapped) {
		card.tapped = tapped;
		if (tapped) {
			setPreferredSize(tappedSize);
		} else {
			setPreferredSize(untappedSize);
		}
		setSize(getPreferredSize());
	}

	public void reverse(boolean reversed) {
		card.reversed = reversed;
	}

	public void mouseMoved(MouseEvent e) {
		// nothing to do
	}

	/**
	 * The real card
	 */
	public MCard card;

	private TooltipFilter cardInfoFilter;

	/**
	 * Is this card is aligned to the layout of container.
	 */
	public boolean isAutoAlign;

	/**
	 * Reset all cached data.
	 */
	public void resetCachedData() {
		cardInfoFilter = null;
	}

	/**
	 * Generate a new random angle and update the bounds.
	 * 
	 * @return true if the sizes have been updated.
	 */
	public boolean updateSizes() {
		Dimension oldDimension = (Dimension) getSize().clone();

		// Generate a new random angle
		if (Configuration.getBoolean("randomAngle", Boolean.FALSE).booleanValue()) {
			angle = (new Random().nextDouble() - 0.5) * ROTATE_SCALE;
		} else {
			angle = 0;
		}
		final int cardHeight = CardFactory.cardHeight;
		final int cardWidth = CardFactory.cardWidth;
		if (angle == 0) {
			rotateTransformX = 0;
			rotateTransformY = 0;

			// Update the bounds
			untappedSize = new Dimension(cardWidth, cardHeight);
			tappedSize = new Dimension(cardHeight, cardWidth);
		} else {
			if (angle > 0) {
				rotateTransformX = Math.floor(cardHeight * Math.sin(Math.abs(angle))
						+ 1);
				rotateTransformY = 0;
			} else {
				rotateTransformX = 0;
				rotateTransformY = Math
						.floor(cardWidth * Math.sin(Math.abs(angle)) + 1);
			}

			// Update the bounds
			untappedSize = new Dimension((int) Math.floor(cardWidth * Math.cos(angle)
					+ cardHeight * Math.sin(Math.abs(angle)) + 3), (int) Math
					.floor(cardHeight * Math.cos(angle) + cardWidth
							* Math.sin(Math.abs(angle)) + 3));
			tappedSize = new Dimension((int) Math.floor(cardHeight * Math.cos(angle)
					+ cardWidth * Math.sin(Math.abs(angle)) + 3), (int) Math
					.floor(cardWidth * Math.cos(angle) + cardHeight
							* Math.sin(Math.abs(angle)) + 3));
		}
		final Dimension newSize;
		if (card.tapped) {
			newSize = tappedSize;
		} else {
			newSize = untappedSize;
		}
		if (!newSize.equals(oldDimension)) {
			setSize(newSize);
			setPreferredSize(newSize);
			return true;
		}
		return false;
	}

	/**
	 * Update the layout of this card, and also generate a new random angle.
	 */
	public void updateLayout() {
		// Update the bounds
		if (updateSizes()) {

			// Update the layout
			doLayout();
			Container parent = getParent();
			if (parent != null) {
				parent.doLayout();
				parent = parent.getParent();
				if (parent != null) {
					parent.repaint();
				}
			}
		}
	}

	@Override
	public String toString() {
		return card.toString();
	}

	/**
	 * Update the UI of this card. All cached data are reset.
	 */
	public void updateMUI() {
		cardInfoFilter = null;
	}

	/**
	 * Angle of this card.
	 */
	private double angle = 0;

	/**
	 * The bounds of card when is untapped.
	 */
	public Dimension untappedSize;

	/**
	 * The bounds of card when is tapped.
	 */
	public Dimension tappedSize;

	/**
	 * Translation X to do after the rotation
	 */
	private double rotateTransformX;

	/**
	 * Translation Y to do after the rotation
	 */
	private double rotateTransformY;
}
