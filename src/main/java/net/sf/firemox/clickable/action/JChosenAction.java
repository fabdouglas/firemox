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
package net.sf.firemox.clickable.action;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;

import javax.swing.ToolTipManager;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.handler.ChosenAction;
import net.sf.firemox.clickable.Clickable;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.ui.MagicUIComponents;

/**
 * An implementation of a two-state button.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class JChosenAction extends Clickable {

	private static final String CACHING_STR = "caching...";

	/**
	 * The picture corresponding to the "completed" status of an action
	 */
	private static transient Image completedImage = null;

	/**
	 * The picture corresponding to the "is completing" status of an action
	 */
	private static transient Image completingImage = null;

	/**
	 * The picture corresponding to the "uncompleted" status of an action
	 */
	private transient static Image uncompletedImage = null;

	/**
	 * The context associated to this playable action
	 */
	transient ActionContextWrapper context;

	/**
	 * @param context
	 *          The associated action context.
	 */
	public JChosenAction(ActionContextWrapper context) {
		super();
		this.context = context;
		context.actionUI = this;
		if (completedImage == null) {
			try {
				completedImage = Picture.loadImage(IdConst.IMAGES_DIR
						+ "completedaction.gif");
				uncompletedImage = Picture.loadImage(IdConst.IMAGES_DIR
						+ "uncompletedaction.gif");
				completingImage = Picture.loadImage(IdConst.IMAGES_DIR
						+ "completingaction.gif");

			} catch (MalformedURLException e) {
				// IGNORING
				Log.debug(e);
			}
		}
		addMouseListener(this);
		setMaximumSize(new Dimension(2000, 25));
		context.refreshText(StackManager.currentAbility, StackManager.getInstance()
				.getAbilityContext());
		putClientProperty(TOOL_TIP_TEXT_KEY, CACHING_STR);
		ToolTipManager.sharedInstance().registerComponent(this);
		repaint();
	}

	@Override
	public void setToolTipText(String text) {
		putClientProperty(TOOL_TIP_TEXT_KEY, text);
	}

	@Override
	public String getToolTipText() {
		String ttCache = (String) getClientProperty(TOOL_TIP_TEXT_KEY);
		if (ttCache == CACHING_STR) {
			ttCache = "<html>"
					+ context.toHtmlString(StackManager.currentAbility, StackManager
							.getInstance().getAbilityContext());
			setToolTipText("<html>" + ttCache + ChosenCostPanel.ttClick);
		}
		return ttCache;
	}

	@Override
	public void disHighLight() {
		// disable this action
		((ChosenAction) context.action).disactivate(context, StackManager
				.getInstance().getAbilityContext(), StackManager.currentAbility);
		super.disHighLight();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!isHighLighted && StackManager.actionManager.clickOn(this)) {
			// first disable
			Log.debug("clickOn(Mouse):" + context.action);
			sendClickToOpponent();
			StackManager.actionManager.succeedClickOn(this);
		}
	}

	/**
	 * Set this action as selected. This triggers this action to be chosen and
	 * also been played.
	 * 
	 * @return true if the stack resolution can continue.
	 */
	public boolean setSelected() {
		StackManager.actionManager.currentAction = context.action;
		StackManager.actionManager.currentIdAction = context.contextID;
		context.refreshText(StackManager.currentAbility, StackManager.getInstance()
				.getAbilityContext());
		highLight(Color.ORANGE);
		return ((ChosenAction) context.action).choose(context, StackManager
				.getInstance().getAbilityContext(), StackManager.currentAbility);
	}

	@Override
	public void sendClickToOpponent() {
		MagicUIComponents.chosenCostPanel.sendClickToOpponent(this);
	}

	@Override
	public void paint(Graphics g) {
		// paint action with the "completed" status
		super.paint(g);
		if (isCompleted()) {
			g.setColor(Color.GREEN.darker());
			g.fill3DRect(0, 0, getWidth() - 1, getHeight() - 1, true);
			g.drawImage(completedImage, 5, 5, this);
		} else if (isHighLighted) {
			g.setColor(Color.ORANGE);
			g.fill3DRect(0, 0, getWidth() - 1, getHeight() - 1, true);
			g.drawImage(completingImage, 5, 5, null);
		} else {
			g.setColor(Color.RED.darker());
			g.fill3DRect(0, 0, getWidth() - 1, getHeight() - 1, true);
			g.drawImage(uncompletedImage, 5, 5, this);
		}

		// draw the HTML text of this action
		if (context.isRefreshText()) {
			context.toHtmlString(StackManager.currentAbility, StackManager
					.getInstance().getAbilityContext());
		}
		g.setColor(Color.BLACK);
		((View) getClientProperty(BasicHTML.propertyKey)).paint(g,
				getSharedRectangle());
	}

	/**
	 * Return the shared Rectangle object used for Painting operation of
	 * JChosenAction.
	 * 
	 * @return the shared Rectangle object used for Painting operation of
	 *         JChosenAction.
	 */
	private Rectangle getSharedRectangle() {
		if (sharedRectangle == null) {
			sharedRectangle = new Rectangle(20, 5, getWidth() - 23, getHeight() - 2);
		}
		return sharedRectangle;
	}

	/**
	 * The shared Rectangle object used for Painting operation of JChosenAction
	 * component.
	 */
	private static Rectangle sharedRectangle = null;

	/**
	 * Is this action is completed?
	 * 
	 * @return true if this action is completed.
	 */
	public boolean isCompleted() {
		return context.isCompleted();
	}
}
