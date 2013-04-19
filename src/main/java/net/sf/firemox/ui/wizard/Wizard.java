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
package net.sf.firemox.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.sf.firemox.action.BackgroundMessaging;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 * @since 0.86 JDialog icon updated
 * @since 0.86 Auto-mnemonic
 */
public abstract class Wizard extends JDialog implements ActionListener,
		KeyListener {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param ability
	 *          ability to associate to this ability. If this ability has an
	 *          associated picture, it will be used instead of given picture.
	 *          Ability's name is also used to fill the title. This ability will
	 *          be used to restart this wizard in case of Background button is
	 *          used.
	 * @param title
	 *          the title of this wizard.
	 * @param description
	 *          the description appended to the title of this wizard. This content
	 *          will be displayed as Html.
	 * @param iconName
	 *          the icon's name to display on the top right place.
	 * @param width
	 *          the preferred width.
	 * @param height
	 *          the preferred height.
	 */
	public Wizard(Ability ability, String title, String description,
			String iconName, int width, int height) {
		super(MagicUIComponents.magicForm, StringUtils.capitalize(title), true);
		getRootPane().setPreferredSize(new Dimension(width, 300));
		getRootPane().setMinimumSize(new Dimension(width, height));
		setSize(new Dimension(width, height));

		// center
		gameParamPanel = new JPanel(null);
		gameParamPanel.setLayout(new BoxLayout(gameParamPanel, BoxLayout.Y_AXIS));
		if (ability == null)
			getContentPane().add(
					new WizardTitle(new WizardImageIcon((Image) null, iconName),
							description), BorderLayout.NORTH);
		else
			getContentPane().add(
					new WizardTitle(new WizardImageIcon(ability.getCard(), iconName),
							description), BorderLayout.NORTH);
		getContentPane().add(gameParamPanel, BorderLayout.CENTER);
		getContentPane().add(new JPanel(), BorderLayout.EAST);

		// bottom
		final JPanel abstractButtonPanel = new JPanel(new BorderLayout());
		this.buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		this.ability = ability;
		abstractButtonPanel.setBorder(null);
		abstractButtonPanel.add(new JSeparator(), BorderLayout.NORTH);
		abstractButtonPanel.add(buttonPanel, BorderLayout.CENTER);
		abstractButtonPanel.add(wizardInfo, BorderLayout.SOUTH);
		getContentPane().add(abstractButtonPanel, BorderLayout.SOUTH);
		setLocationRelativeTo(null);
	}

	/**
	 * Set the attached context to this wizard. It will be used to restart this
	 * wizard in case of background button is used.
	 * 
	 * @param context
	 *          the attached context to this wizard.
	 */
	public void setContext(ContextEventListener context) {
		this.context = context;
	}

	/**
	 * Set the attached context to this wizard. It will be used to restart this
	 * wizard in case of background button is used.
	 * 
	 * @param action
	 *          the action's name and content will be used in the wizard title and
	 *          also message text.
	 */
	public void setAction(BackgroundMessaging action) {
		this.action = action;
		if (action != null) {
			if (backgroundButton != null) {
				buttonPanel.remove(backgroundButton);
			}
			backgroundButton = new JButton(LanguageManager
					.getString("wiz_background"));
			backgroundButton.setMnemonic(backgroundButton.getText().charAt(0));
			backgroundButton.setToolTipText(LanguageManager
					.getString("wiz_background.tooltip"));
			backgroundButton.addActionListener(this);
		}
		buttonPanel.add(backgroundButton);
	}

	/**
	 * triggers the OK button
	 * 
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == backgroundButton) {
			// put in background this wizard
			optionAnswer = BACKGROUND_OPTION;
			MagicUIComponents.backgroundBtn.startButton(context, ability, action,
					this);
			setVisible(false);
			MagicUIComponents.skipButton.setEnabled(false);
		}
	}

	/**
	 * Check the validity of this wizard.
	 * 
	 * @return true if this wizard can be validated.
	 */
	protected abstract boolean checkValidity();

	/**
	 * Add a component to be listened.
	 * 
	 * @param component
	 *          component to add
	 */
	protected final void addCheckValidity(Component component) {
		component.addKeyListener(this);
	}

	public void keyTyped(KeyEvent e) {
		// Ignore this event
	}

	public void keyPressed(KeyEvent e) {
		// Ignore this event
	}

	public void keyReleased(KeyEvent e) {
		wizardInfo.noNewMessage = true;
		if (checkValidity() && wizardInfo.noNewMessage) {
			wizardInfo.reset();
		}
	}

	@Override
	public void setVisible(boolean visible) {
		try {
			if (visible) {
				MagicUIComponents.magicForm.setIconImage(Picture
						.loadImage(IdConst.IMAGES_DIR + "mp_wiz.gif"));
			} else {
				MagicUIComponents.magicForm.setIconImage(Picture
						.loadImage(IdConst.IMAGES_DIR + "mp.gif"));
			}
		} catch (Exception e) {
			// IGNORING
		}
		super.setVisible(visible);
	}

	/**
	 * @param optionAnswer
	 */
	protected void validAnswer(int optionAnswer) {
		Wizard.optionAnswer = optionAnswer;
		if (isModal()) {
			setVisible(false);
			dispose();
		} else if (action != null) {
			// notify the listener
			action.replayAction(context, ability, this);
		}
	}

	/**
	 * @see javax.swing.JOptionPane#CANCEL_OPTION
	 * @see javax.swing.JOptionPane#OK_OPTION
	 */
	public static int optionAnswer;

	/**
	 * Optional integer answer
	 */
	public static int indexAnswer;

	/**
	 * The background button code.
	 */
	public static final int BACKGROUND_OPTION = -2;

	/**
	 * The panel containing the "cancel" button
	 */
	protected final JPanel buttonPanel;

	/**
	 * The panel containing all fields related to the connection.
	 */
	protected final JPanel gameParamPanel;

	/**
	 * Wizard info of this wizard.
	 */
	protected final WizardInfo wizardInfo = new WizardInfo();

	private JButton backgroundButton;

	private ContextEventListener context;

	private final Ability ability;

	private BackgroundMessaging action;
}
