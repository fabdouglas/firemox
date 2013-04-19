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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.database.Proxy;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.layout.FlowLayoutVertical;

import com.l2fprod.common.swing.JButtonBar;
import com.l2fprod.common.swing.plaf.blue.BlueishButtonBarUI;

/**
 * The settings forms is built of a button bar and many tabs associated to.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class Settings extends YesNo {

	/**
	 * Creates a new instance of Settings <br>
	 */
	public Settings() {
		super(LanguageManager.getString("wiz_settings.title"), LanguageManager
				.getString("wiz_settings.description"), "wiz_settings.gif",
				LanguageManager.getString(LABEL_OK), LanguageManager
						.getString(LABEL_CANCEL), 550, 500);
	}

	/**
	 * Create a list containing the givent proxy list. This list is added to the
	 * given container. The built list is also associated to 2 buttons (up/down)
	 * added to the same container.
	 * 
	 * @param listTitle
	 *          the title of this list. Must be an existing key.
	 * @param proxies
	 *          the proxy list to fill in the list.
	 * @param container
	 *          the container of list and buttons.
	 * @return the list model of the built list.
	 */
	private DefaultListModel createListPanel(String listTitle, Proxy[] proxies,
			JPanel container) {
		final DefaultListModel listModel = new DefaultListModel();
		for (Proxy proxy : proxies) {
			listModel.addElement(proxy);
		}
		final DataBaseList list = new DataBaseList(listModel);
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(LanguageManager.getString(listTitle)),
				BorderLayout.NORTH);
		panel.add(list);
		final JPanel buttonPanel = new JPanel(new FlowLayoutVertical());
		buttonPanel.add(list.upButton);
		buttonPanel.add(list.downButton);
		buttonPanel.add(new JPanel());
		panel.add(BorderLayout.EAST, buttonPanel);
		panel.setPreferredSize(new Dimension(100, 100));
		container.add(panel);
		return (DefaultListModel) list.getModel();
	}

	/**
	 * 
	 */
	private static class DataBaseList extends JList implements
			ListSelectionListener, ActionListener {
		/**
		 * Create a new instance of this class.
		 * 
		 * @param listModel
		 */
		protected DataBaseList(DefaultListModel listModel) {
			super(listModel);
			setBorder(new EtchedBorder());
			upButton = new JButton(LanguageManager
					.getString("wiz_settings.database.button.up"), UIHelper
					.getIcon("up.gif"));
			upButton.setMnemonic(upButton.getText().charAt(0));
			upButton.setToolTipText("wiz_settings.database.button.up.tooltip");
			upButton.setHorizontalTextPosition(SwingConstants.RIGHT);
			upButton.setHorizontalAlignment(SwingConstants.LEFT);
			upButton.addActionListener(this);
			downButton = new JButton(LanguageManager
					.getString("wiz_settings.database.button.down"), UIHelper
					.getIcon("down.gif"));
			downButton.setMnemonic(downButton.getText().charAt(0));
			downButton.setToolTipText("wiz_settings.database.button.down.tooltip");
			downButton.setHorizontalTextPosition(SwingConstants.RIGHT);
			downButton.setHorizontalAlignment(SwingConstants.LEFT);
			downButton.addActionListener(this);
			setVisibleRowCount(10);
			addListSelectionListener(this);
			setSelectedIndex(0);
		}

		public void actionPerformed(ActionEvent event) {
			final int index = getSelectedIndex();
			final Object element = getModel().getElementAt(index);
			((DefaultListModel) getModel()).removeElementAt(index);
			int nextIndex = index;
			if (event.getSource() == upButton) {
				nextIndex--;
			} else if (event.getSource() == downButton) {
				nextIndex++;
			}
			((DefaultListModel) getModel()).add(nextIndex, element);
			setSelectedIndex(nextIndex);
			revalidate();
		}

		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				// Update buttons state
				if (getSelectedIndex() == -1) {
					// No selection, disable up/down source.upButton.setEnabled(false);
					upButton.setEnabled(false);
					downButton.setEnabled(false);
				} else {
					upButton.setEnabled(getSelectedIndex() != 0);
					downButton.setEnabled(getSelectedIndex() != getModel().getSize() - 1);
				}
			}
		}

		/**
		 * The button giving an higher priority to a database.
		 */
		protected JButton upButton;

		/**
		 * The button giving a lower priority to a database.
		 */
		protected JButton downButton;
	}

	@Override
	public void setVisible(boolean visible) {
		settingsPanel = new JPanel(new BorderLayout());
		final JPanel dataBasePanel = makeComponent(LanguageManager
				.getString("wiz_settings.database.title"));

		final JPanel subpanel = new JPanel();
		subpanel.setBorder(new EtchedBorder());
		subpanel.setLayout(new BoxLayout(subpanel, BoxLayout.X_AXIS));
		pictureProxies = createListPanel("wiz_settings.database.proxy.picture",
				DatabaseFactory.pictureProxies, subpanel);
		databaseProxies = createListPanel("wiz_settings.database.proxy.data",
				DatabaseFactory.dataProxies, subpanel);
		dataBasePanel.add(subpanel);
		final JButtonBar toolbar = new JButtonBar(SwingConstants.VERTICAL);
		toolbar.setUI(new BlueishButtonBarUI());
		final ButtonGroup group = new ButtonGroup();
		settingsPanel.add(BorderLayout.WEST, toolbar);
		addButton(LanguageManager.getString("wiz_settings.database.button"),
				"wiz_database.gif", dataBasePanel, toolbar, group);
		addButton("TODO", "cards.gif", makeComponent("Todo"), toolbar, group);
		gameParamPanel.add(BorderLayout.CENTER, settingsPanel);
		super.setVisible(visible);
	}

	/**
	 * Make a tiled component displayable with a button bar in our wizard form.
	 * 
	 * @param title
	 *          the component title displayed on the top of this tab.
	 * @return the built component.
	 */
	private JPanel makeComponent(String title) {
		final JPanel panel = new JPanel(new BorderLayout());
		final JLabel top = new JLabel(title);
		top.setFont(top.getFont().deriveFont(Font.BOLD));
		top.setOpaque(true);
		top.setBackground(panel.getBackground().brighter());
		panel.add(BorderLayout.NORTH, top);
		panel.setPreferredSize(new Dimension(400, 300));
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		return panel;
	}

	/**
	 * Show a configuration component. The current component will be hidden.
	 * 
	 * @param component
	 *          the component to show.
	 */
	protected void show(Component component) {
		if (currentComponent != null) {
			settingsPanel.remove(currentComponent);
		}
		currentComponent = component;
		settingsPanel.add(BorderLayout.CENTER, currentComponent);
		settingsPanel.revalidate();
		settingsPanel.repaint();
	}

	/**
	 * Add a button to the wizard.
	 * 
	 * @param title
	 *          the button title
	 * @param iconUrl
	 *          the icon url to display in the button
	 * @param component
	 *          the associated component to this new button
	 * @param bar
	 *          the button bar containing the other buttons
	 * @param group
	 *          the group this new button would be added to.
	 */
	private void addButton(String title, String iconUrl,
			final Component component, JButtonBar bar, ButtonGroup group) {
		Action action = new AbstractAction(title, UIHelper.getIcon(iconUrl)) {
			public void actionPerformed(ActionEvent e) {
				show(component);
			}
		};

		JToggleButton button = new JToggleButton(action);
		button.setPreferredSize(new Dimension(64, 90));
		bar.add(button);

		group.add(button);

		if (group.getSelection() == null) {
			button.setSelected(true);
			show(component);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == okBtn) {
			// Update database orders : data
			StringBuilder order = new StringBuilder();
			for (int i = 0; i < databaseProxies.getSize(); i++) {
				DatabaseFactory.dataProxies[i] = (Proxy) databaseProxies.get(i);
				order.append(DatabaseFactory.dataProxies[i].getXmlName());
				order.append("|");
			}
			Configuration.setProperty("database.dataOrder", order);

			// Update database orders : pictures
			order = new StringBuilder();
			for (int i = 0; i < pictureProxies.getSize(); i++) {
				DatabaseFactory.pictureProxies[i] = (Proxy) pictureProxies.get(i);
				order.append(DatabaseFactory.pictureProxies[i].getXmlName());
				order.append("|");
			}
			Configuration.setProperty("database.pictureOrder", order);

		}
		super.actionPerformed(event);
	}

	/**
	 * The panel containing the tabs to display. The current component displayed
	 * is <code>currentComponent</code>.
	 */
	private JPanel settingsPanel;

	/**
	 * The current panel displayed in the wizard. Used to add/reove a tab from
	 * <code>settingsPanel</code>.
	 */
	private Component currentComponent;

	private DefaultListModel databaseProxies;

	private DefaultListModel pictureProxies;
}
