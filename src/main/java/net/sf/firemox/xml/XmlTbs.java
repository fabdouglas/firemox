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
package net.sf.firemox.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.Pair;
import net.sf.firemox.tools.ReferencePager;
import net.sf.firemox.xml.XmlParser.Node;
import net.sf.firemox.xml.tbs.Tbs;

import org.apache.commons.io.filefilter.FileFilterUtils;

/**
 * This is the main class used to convert a turn based system described in a XML
 * form to a binary form for further interpretation.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 * @since 0.82 static-modifiers supported in TBS rules
 */
public final class XmlTbs {

	/**
	 * Creates a new instance of XmlTbs.<br>
	 */
	private XmlTbs() {
		super();
	}

	/**
	 * Writes a list of actions described in the given XML node to the given
	 * OutputStream (which must be a FileOutputStream) prefixing the stream with
	 * an integer containing the number of written actions and returns this number
	 * of written actions.
	 * 
	 * @param node
	 *          the node containing list of actions
	 * @param out0
	 *          the OutputStream to write the given actions to
	 * @throws IOException
	 *           if an I/O error occurs if an illegal access error occurs if a
	 *           security error occurs if an illegal argument is given
	 * @return the amount of written action.
	 */
	public static int writeActionList(Node node, OutputStream out0)
			throws IOException {
		assert out0 instanceof FileOutputStream;
		boolean oldDefaultOnMeTag = XmlTools.defaultOnMeTag;
		XmlTools.defaultOnMeTag = true;
		FileOutputStream out = (FileOutputStream) out0;
		int nbCost = 0;
		currentActionIndex = 0;
		if (node == null) {
			out.write(0);
		} else {
			// first, evaluate the amount of actions to write
			final long position = out.getChannel().position();
			out.write(0); // this byte will be replaced later
			nbCost += writeActionListNoNb(node, out);
			long tmpPosition = out.getChannel().position();
			out.getChannel().position(position);
			out.write(nbCost);
			out.getChannel().position(tmpPosition);
		}
		XmlTools.defaultOnMeTag = oldDefaultOnMeTag;
		return nbCost;
	}

	/**
	 * Writes a list of actions described in the given XML node into the given
	 * OutputStream as a sequence and returns the number of written actions.
	 * 
	 * @param node
	 *          the node containing list of actions
	 * @param out
	 *          the OutputStream to write the actions to
	 * @return the amount of written action.
	 * @throws IOException
	 *           if an I/O error occurs if an illegal access error occurs if a
	 *           security error occurs if an illegal argument is given
	 */
	public static int writeActionListNoNb(Node node, OutputStream out)
			throws IOException {
		int nbCost = 0;
		for (Object obj : node) {
			if (obj instanceof Node) {
				Node child = (Node) obj;
				final int written = XmlAction.getAction(child.getTag()).buildMdb(child,
						out);
				currentActionIndex += written;
				nbCost += written;
			}
		}
		return nbCost;
	}

	/**
	 * Writes the card identified by the given card XML description file name in
	 * the given directory to the given OutputStream.
	 * 
	 * @param currentDir
	 *          the directory path to find the card XML description file name
	 * @param out
	 *          the OutputStream to write the identified card to
	 * @param cardFile
	 *          the card XML description file name
	 */
	private static void writeCard(String currentDir, OutputStream out,
			String cardFile) {
		try {
			XmlConfiguration.parseCard(cardFile, currentDir, out);
		} catch (Throwable e) {
			if (e instanceof StackOverflowError) {
				throw new InternalError(
						"StackOverflowError writting card, may be you use some recursive actions reference");
			}
			throw new InternalError(e.getMessage());
		}
	}

	/**
	 * Reads from the tbs directory the existing XML files, parse them, add them
	 * to the mdb file in the right order
	 * 
	 * @param mdbName
	 *          the mdb name used to find the directory containing the XML files.
	 * @param out
	 *          the outputStream where the cards would written in.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void updateMdb(String mdbName, OutputStream out)
			throws IOException {
		final String incomingDir = IdConst.TBS_DIR + "/" + mdbName + "/recycled";

		// Save the first card's code offset
		final long referenceOffset = ((FileOutputStream) out).getChannel()
				.position();
		MToolKit.writeInt24(out, 0);

		// initialize the card pager
		cardPager = new ReferencePager();

		// list the available cards
		final String[] cards = MToolKit.getFile(incomingDir).list(
				FileFilterUtils.andFileFilter(FileFilterUtils.suffixFileFilter("xml"),
						FileFilterUtils.notFileFilter(FileFilterUtils
								.suffixFileFilter(IdConst.FILE_DATABASE_SAVED))));
		final Pair<String, String>[] sortedPair = new Pair[cards.length];
		for (int i = sortedPair.length; i-- > 0;) {
			sortedPair[i] = new Pair<String, String>(cards[i].toLowerCase(), cards[i]);
		}
		Arrays.sort(sortedPair);

		System.out.println("\t" + sortedPair.length + " founds ...");
		for (Pair<String, String> pair : sortedPair) {
			final File file = MToolKit.getFile(incomingDir + "/" + pair.value);
			try {
				referencedAbilities = null;
				referencedTest = null;
				referencedActions = null;
				referencedNonMacroActions = null;
				writeCard(incomingDir, out, file.getAbsolutePath());
			} catch (InternalError e) {
				XmlConfiguration.error(e.getMessage());
			}
		}

		// Save the first card's code offset
		final long curentOffset = ((FileOutputStream) out).getChannel().position();
		((FileOutputStream) out).getChannel().position(referenceOffset);
		MToolKit.writeInt24(out, (int) curentOffset);
		((FileOutputStream) out).getChannel().position(curentOffset);

		// Write the card references
		cardPager.write(out);

		System.out.println("Build completed");
		System.out.println("\t" + sortedPair.length + " proceeded card"
				+ (sortedPair.length > 1 ? "s" : ""));
		System.out.println("\t" + XmlConfiguration.uncompleted
				+ " uncompleted card" + (XmlConfiguration.uncompleted > 1 ? "s" : ""));
		System.out.println("\tRate : "
				+ (sortedPair.length - XmlConfiguration.uncompleted) * 100.
				/ sortedPair.length + "%");
	}

	/**
	 * Returns a suitable XmlToMDB implementation for the given tbs component
	 * name.
	 * 
	 * @param component
	 *          the name of the tbs component
	 * @return a suitable XmlToMDB implementation for the given tbs component name
	 */
	public static XmlToMDB getTbsComponent(String component) {
		return XmlConfiguration.getXmlClass(component, instances, XmlTbs.class);
	}

	/**
	 * Map of already instanciated XmlToMDB implementations indexed with their
	 * corresponding component name.
	 */
	private static Map<String, XmlToMDB> instances = new HashMap<String, XmlToMDB>();

	/**
	 * Returns the referenced ability. First search into the declared abilities
	 * within the current card, then within the declared ability within the rules
	 * definition.
	 * 
	 * @param abilityName
	 *          the ability name
	 * @return the ability referenced by the given ability name as a node
	 */
	public static Node getReferencedAbility(String abilityName) {
		if (abilityName == null) {
			XmlConfiguration.error("ability reference name missing");
		} else {
			if (XmlTbs.referencedAbilities != null
					&& XmlTbs.referencedAbilities.get(abilityName) != null) {
				return XmlTbs.referencedAbilities.get(abilityName);
			}
			if (Tbs.referencedAbilities != null
					&& Tbs.referencedAbilities.get(abilityName) != null) {
				return Tbs.referencedAbilities.get(abilityName);
			}
			XmlConfiguration.error("Could not find referenced ability '"
					+ abilityName + "'");
		}
		return new Node(null, abilityName, null);
	}

	/**
	 * Returns the referenced attachment declared within the rules definition.
	 * 
	 * @param attachmentName
	 *          the attachment name
	 * @return the attachment referenced by the given attachment name as a xml
	 *         node
	 */
	public static XmlParser.Node getReferencedAttachment(String attachmentName) {
		if (attachmentName == null) {
			XmlConfiguration.error("attachment reference name missing = 'null'");
			Thread.dumpStack();
		} else {
			if (Tbs.referencedAttachments != null
					&& Tbs.referencedAttachments.get(attachmentName) != null) {
				return Tbs.referencedAttachments.get(attachmentName);
			}
			XmlConfiguration.error("Could not find referenced attachment '"
					+ attachmentName + "'");
		}
		return null;
	}

	/**
	 * Returns the referenced action(s). First search into the declared action(s)
	 * within the current card, then within the declared action(s) within the
	 * rules definition.
	 * 
	 * @param actionName
	 *          the action name
	 * @return the action(s) referenced by the given action name as a list
	 */
	public static List<Node> getReferencedActions(String actionName) {
		if (actionName == null) {
			XmlConfiguration.error("action reference name missing = 'null'");
			Thread.dumpStack();
		} else {
			if (XmlTbs.referencedActions != null
					&& XmlTbs.referencedActions.get(actionName) != null) {
				return XmlTbs.referencedActions.get(actionName);
			}
			if (Tbs.referencedActions != null
					&& Tbs.referencedActions.get(actionName) != null) {
				return Tbs.referencedActions.get(actionName);
			}
			XmlConfiguration.error("Could not find referenced action '" + actionName
					+ "'");
		}
		return new ArrayList<Node>();
	}

	/**
	 * Indicates if the specified referenced action accept macro or not.
	 * 
	 * @param referenceName
	 *          the referenced action name
	 * @return true if the specified referenced action accept macro, false either
	 */
	public static boolean isNotMacro(String referenceName) {
		return referencedNonMacroActions != null
				&& referencedNonMacroActions.contains(referenceName)
				|| Tbs.referencedNonMacroActions != null
				&& !Tbs.referencedNonMacroActions.contains(referenceName);
	}

	/**
	 * Returns the referenced test. First search into the declared test within the
	 * current card, then within the declared test within the rules definition.
	 * 
	 * @param testName
	 *          the test name
	 * @return the test referenced by the given test name as a node
	 */
	public static Node getReferencedTest(String testName) {
		if (testName == null) {
			XmlConfiguration.error("test reference name missing");
		} else {
			if (XmlTbs.referencedTest != null
					&& XmlTbs.referencedTest.get(testName) != null) {
				return XmlTbs.referencedTest.get(testName);
			}
			if (Tbs.referencedTest != null
					&& Tbs.referencedTest.get(testName) != null) {
				return Tbs.referencedTest.get(testName);
			}
			XmlConfiguration.error("Could not find referenced test '" + testName
					+ "'");
		}
		return new Node(null, testName, null);
	}

	/**
	 * Available ability references of this Card.
	 */
	public static Map<String, Node> referencedAbilities = null;

	/**
	 * Available test references of this Card.
	 */
	public static Map<String, Node> referencedTest = null;

	/**
	 * Available actions references of this Card.
	 */
	public static Map<String, List<Node>> referencedActions = null;

	/**
	 * Available actions references that do not accept macro.
	 */
	public static Set<String> referencedNonMacroActions = null;

	/**
	 * Nb written action in the current 'writeActionlist' method.
	 */
	public static int currentActionIndex;

	/**
	 * Is the last action has benn written in effects part of ability?
	 */
	public static boolean currentInEffect;

	/**
	 * The card reference pager.
	 */
	public static ReferencePager cardPager;

	static void clean() {
		instances.clear();
		referencedActions = null;
		referencedNonMacroActions = null;
		referencedTest = null;
		referencedAbilities = null;
	}
}
