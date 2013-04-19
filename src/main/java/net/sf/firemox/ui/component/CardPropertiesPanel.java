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
package net.sf.firemox.ui.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.component.task.TaskAction;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

import org.apache.commons.lang.StringUtils;

import com.l2fprod.common.swing.JCollapsiblePane;
import com.l2fprod.common.swing.JTaskPane;
import com.l2fprod.common.swing.JTaskPaneGroup;

/**
 * Panel containing the card properties. This panel use the lf2prod task
 * component. The layout of this component is defined into the tbs XML
 * definition.<br>
 * For manual modification of TaskPane properties :<br>
 * Please see http://common.l2fprod.com <br>
 * UIManager.put("TaskPane.useGradient", Boolean.TRUE); <br>
 * UIManager.put("TaskPane.backgroundGradientStart", Color.green.darker()); <br>
 * UIManager.put("TaskPane.backgroundGradientEnd",
 * Color.green.darker().darker())<br>;
 * TaskPaneGroup.titleBackgroundGradientStart; <br>
 * TaskPaneGroup.specialTitleForeground; <br>
 * TaskPaneGroup.titleBackgroundGradientEnd; <br>
 * TaskPaneGroup.background; <br>
 * TaskPaneGroup.titleForeground; <br>
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class CardPropertiesPanel extends JPanel {

	/**
	 * Nested element type.
	 */
	public static final int NESTED_ELEMENT = 0;

	/**
	 * Attribute element type.
	 */
	public static final int ATTRIBUTE = 1;

	/**
	 * Create an empty task panel with a main title.
	 */
	public CardPropertiesPanel() {
		super();
		setLayout(new BorderLayout());
		taskPane = new JTaskPane();
		add("Center", new JScrollPane(taskPane));

		/*
		 * taskPaneGroup = new JTaskPaneGroup();
		 * taskPaneGroup.setTitle(LanguageManager.getString("database.title"));
		 * taskPane.add(taskPaneGroup);
		 */
	}

	/**
	 * Read and add from the given input stream the content of this panel.
	 * <p>
	 * <ul>
	 * Structure of Stream : Data[size]
	 * <li>nb nodes [1]</li>
	 * <li>node type i [1]</li>
	 * <li>element/attribute i [...]</li>
	 * </ul>
	 * <p>
	 * <ul>
	 * Structure of Element : Data[size]
	 * <li>element title [1]</li>
	 * <li>nb nodes [1]</li>
	 * <li>node type i [1]</li>
	 * <li>element/attribute i [...]</li>
	 * </ul>
	 * <p>
	 * <ul>
	 * Structure of Attribute : Data[size]
	 * <li>attribute name + '\0' [...]</li>
	 * <li>nb nodes [1]</li>
	 * <li>node type i [1]</li>
	 * <li>element/attribute i [...]</li>
	 * </ul>
	 * <p>
	 * 
	 * @param dbStream
	 *          the source stream.
	 */
	public void init(InputStream dbStream) {
		try {
			fillTaskPane(taskPane, dbStream);
		} catch (Exception e) {
			throw new RuntimeException("Error reading taskPanel properties", e);
		}
		taskPane.revalidate();
	}

	/**
	 * Read and add from the given input stream the content of the given panel.
	 * 
	 * @param parent
	 *          the panel were the read content would be added.
	 * @param dbStream
	 *          the source stream.
	 * @throws Exception
	 *           If some I/O or reflection error occurs.
	 */
	private void fillTaskPane(final JComponent parent, final InputStream dbStream)
			throws IOException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		final int count = dbStream.read();
		for (int i = 0; i < count; i++) {
			final int elementType = dbStream.read();
			switch (elementType) {
			case NESTED_ELEMENT:
				final JTaskPaneGroup nested = new JTaskPaneGroup();
				nested.setTitle(LanguageManagerMDB.getString(MToolKit
						.readString(dbStream)));
				fillTaskPane(nested, dbStream);
				parent.add(nested);
				break;
			case ATTRIBUTE:
				final Action action = (Action) Class.forName(
						new StringBuilder(TaskAction.class.getPackage().getName()).append(
								".").append(
								StringUtils.capitalize(MToolKit.readString(dbStream))).append(
								"Action").toString()).getConstructor(
						new Class[] { InputStream.class }).newInstance(
						new Object[] { dbStream });
				((JTaskPaneGroup) parent).add(action);
				break;
			default:
				throw new RuntimeException("unknown element type : " + elementType);
			}
		}

	}

	/**
	 * Revalidate this task Pane with the given card.
	 * 
	 * @param card
	 *          the card used to revalidate the content of this task Pane.
	 */
	public void revalidate(MCard card) {
		if (card.isVisibleForYou() && card != this.card) {
				final Component[] nesteds = this.taskPane.getComponents();
				for (Object nested : nesteds) {
					if (nested instanceof JTaskPaneGroup) {
						revalidate(card, (JTaskPaneGroup) nested);
					}
				}
			}
	}

	/**
	 * Revalidate the given taskPane with the given card.
	 * 
	 * @param card
	 *          the card used to revalidate the content of the
	 *          <param>taskPaneGroup</param>.
	 * @param taskPaneGroup
	 *          the task pane to revalidate.
	 */
	private void revalidate(MCard card, JTaskPaneGroup taskPaneGroup) {
		final Component[] nesteds = ((Container) ((Container) ((JCollapsiblePane) taskPaneGroup
				.getComponent(0)).getComponent(0)).getComponent(0)).getComponents();
		for (int i = nesteds.length; i-- > 0;) {
			if (nesteds[i] instanceof JTaskPaneGroup) {
				revalidate(card, (JTaskPaneGroup) nesteds[i]);
			} else if (nesteds[i] instanceof JButton) {
				((TaskAction) ((JButton) nesteds[i]).getAction()).revalidate(card);
			}
			((JComponent) nesteds[i]).revalidate();
		}
		this.card = card;
	}

	/**
	 * Return the last card used to display the content of this task pane.
	 * 
	 * @return the last card used to display the content of this task pane.
	 */
	public MCard getCard() {
		return card;
	}

	/**
	 * The task pane root.
	 */
	private JTaskPane taskPane;

	/**
	 * The last card used to display the content of this task pane.
	 */
	private MCard card = null;

}
