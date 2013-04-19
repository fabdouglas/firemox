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
package net.sf.firemox.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.firemox.annotation.XmlTestElement;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.PairStringInt;
import net.sf.firemox.xml.XmlParser.Node;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class XmlConfiguration {

	/**
	 * Constructor.
	 * 
	 * @param xmlFile
	 *          the XML source file.
	 * @param workingDir
	 *          the working directory.
	 * @param output
	 *          the output stream of MDB file.
	 * @exception SAXException
	 *              parsing error.
	 * @exception IOException
	 *              writing error.
	 */
	private XmlConfiguration(String xmlFile, String workingDir,
			OutputStream output) throws SAXException, IOException {
		initParser();
		synchronized (parser) {
			config = parser.parse(MToolKit.getResourceAsStream(xmlFile));
		}
		try {
			if (XmlTools.aliasMap == null) {
				XmlTools.aliasMap = new HashMap<String, Integer>(newIncludeInstance());
			}
			String tagName = config.getTag();
			config.addAttribute(new XmlParser.Attribute("workingDir", workingDir));
			config.addAttribute(new XmlParser.Attribute("xmlFile", xmlFile.replace(
					'\\', '/')));
			XmlTbs.getTbsComponent(tagName).buildMdb(config, output);
		} catch (Exception e) {
			error("Component error with '" + config.getTag() + "'\n" + config);
			e.printStackTrace();
		}
	}

	/**
	 * Build and return a parsed instance of given rules file.
	 * 
	 * @param rulesFile
	 *          the XML card file.
	 * @param workingDir
	 *          the working directory.
	 * @param output
	 *          the output stream of MD file.
	 * @return a parsed instance of given rules file.
	 * @exception SAXException
	 *              parsing error.
	 * @exception IOException
	 *              writing error.
	 */
	public static XmlConfiguration parseRules(String rulesFile,
			String workingDir, OutputStream output) throws SAXException, IOException {
		currentCard = null;
		return new XmlConfiguration(rulesFile, workingDir, output);
	}

	/**
	 * Build and return a parsed instance of given card file.
	 * 
	 * @param cardFile
	 *          the XML card file.
	 * @param workingDir
	 *          the working directory.
	 * @param output
	 *          the output stream of MDB file.
	 * @return a parsed instance of given card file.
	 * @exception SAXException
	 *              parsing error.
	 * @exception IOException
	 *              writing error.
	 */
	public static XmlConfiguration parseCard(String cardFile, String workingDir,
			OutputStream output) throws SAXException, IOException {
		currentCard = FilenameUtils.getName(cardFile);
		return new XmlConfiguration(cardFile, workingDir, output);
	}

	/**
	 * @throws SAXException
	 */
	private static synchronized void initParser() throws SAXException {
		if (parser != null) {
			return;
		}
		parser = new XmlParser(XmlConfiguration.getOptions().isXsdValidation());
	}

	/**
	 * Add an error.
	 * 
	 * @param message
	 *          error message.
	 */
	public static void error(String message) {
		error(message, false);
	}

	/**
	 * Add an error.
	 * 
	 * @param message
	 *          error message.
	 * @param dump
	 *          if <code>true</code>, the stack trace is printed.
	 */
	public static void error(String message, boolean dump) {
		if (currentCard != null) {
			System.out.println(currentCard);
			currentCard = null;
		}
		XmlConfiguration.error++;
		System.out.print("\tERROR ");
		System.out.println(message);
		if (dump) {
			new RuntimeException().printStackTrace(System.out);
		}
	}

	/**
	 * Add an error.
	 * 
	 * @param message
	 *          error message.
	 */
	public static void info(String message) {
		System.out.println(message);
	}

	/**
	 * Add an error.
	 * 
	 * @param message
	 *          error message.
	 */
	public static void warning(String message) {
		if (currentCard != null) {
			System.out.println(currentCard);
			currentCard = null;
		}
		XmlConfiguration.warning++;
		System.out.print("\tWARNING ");
		System.out.println(message);
	}

	/**
	 * Add an uncompleted counter.
	 */
	public static void uncompleted() {
		if (currentCard != null) {
			System.out.println(currentCard);
			currentCard = null;
		}
		XmlConfiguration.uncompleted++;
		System.out.println("\t... uncompleted card");
	}

	/**
	 * Add a fatal error message.
	 * 
	 * @param message
	 *          error message.
	 */
	public static void fatal(String message) {
		XmlConfiguration.error++;
		throw new RuntimeException(message);
	}

	/**
	 * The current parsed card.
	 */
	private static String currentCard;

	private static Options options;

	/**
	 * Return the method name corresponding to the specified TAG.
	 * 
	 * @param tagName
	 * @return the method name corresponding to the specified TAG.
	 */
	static XmlToMDB getXmlClass(String tagName, Map<String, XmlToMDB> instances,
			Class<?> nameSpaceCall) {
		if (!nameSpaceCall.getSimpleName().startsWith("Xml"))
			throw new InternalError("Caller should be an Xml class : "
					+ nameSpaceCall);

		XmlToMDB nodeClass = instances.get(tagName);
		if (nodeClass != null) {
			return nodeClass;
		}

		String simpleClassName = StringUtils
				.capitalize(tagName.replaceAll("-", ""));
		String packageName = nameSpaceCall.getPackage().getName();
		String namespace = nameSpaceCall.getSimpleName().substring(3).toLowerCase();
		String className = packageName + "." + namespace + "." + simpleClassName;
		XmlToMDB result;
		try {
			result = (XmlToMDB) Class.forName(className).newInstance();
		} catch (Throwable e) {
			Class<?> mdbClass = null;
			simpleClassName = WordUtils.capitalize(tagName.replaceAll("-", " "))
					.replaceAll(" ", "");
			try {
				result = (XmlToMDB) Class.forName(
						packageName + "." + namespace + "." + simpleClassName)
						.newInstance();
			} catch (Throwable e1) {
				try {
					className = StringUtils.chomp(packageName, ".xml") + "." + namespace
							+ "." + simpleClassName;
					mdbClass = Class.forName(className);
					if (!mdbClass.isAnnotationPresent(XmlTestElement.class)) {
						result = (XmlToMDB) mdbClass.newInstance();
					} else {
						result = getAnnotedBuilder(mdbClass, tagName, packageName,
								namespace);
					}
				} catch (Throwable ei2) {
					error("Unsupported " + namespace + " '" + tagName + "'");
					result = DummyBuilder.instance();
				}
			}
		}
		instances.put(tagName, result);
		return result;
	}

	/**
	 * @param mdbClass
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	private static XmlToMDB getAnnotedBuilder(Class<?> mdbClass, String tagName,
			String packageName, String namespace) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (!mdbClass.isAnnotationPresent(XmlTestElement.class)) {
			error("Unsupported annoted " + namespace + " '" + tagName + "'");
			return DummyBuilder.instance();
		}
		XmlTestElement xmlElement = mdbClass.getAnnotation(XmlTestElement.class);
		XmlAnnoted annotedInstance = (XmlAnnoted) Class.forName(
				packageName + "." + namespace + ".XmlAnnoted").newInstance();
		annotedInstance.setXmlElement(xmlElement);
		annotedInstance.setAnnotedClass(mdbClass);
		return annotedInstance;
	}

	/**
	 * Create a new object and configure it. A new object is created and
	 * configured.
	 * 
	 * @return The newly created configured object.
	 */
	private Map<String, Integer> newIncludeInstance() {
		// initialize export lists
		EXPORTED_PROPERTIES.clear();
		EXPORTED_TYPES.clear();
		EXPORTED_PHASES.clear();
		PROPERTY_PICTURES.clear();

		Node aliases = XmlTools.getExternalizableNode(config, "aliases");
		Map<String, Integer> aliasMap = new HashMap<String, Integer>();
		// add the no-care value
		aliasMap.put("nocare", IdConst.NO_CARE);
		for (Node alias : aliases.getNodes("alias")) {
			aliasMap.put(alias.getAttribute("name"), Integer.parseInt(alias
					.getAttribute("value")));
			final String exportation = alias.getAttribute("export");
			if ("properties".equals(exportation)) {
				EXPORTED_PROPERTIES.add(new PairStringInt(alias.getAttribute("name"),
						Integer.parseInt(alias.getAttribute("value"))));
				// property-picture association
				final String picture = alias.getAttribute("picture");
				if (picture != null) {
					PROPERTY_PICTURES.put(Integer.parseInt(alias.getAttribute("value")),
							picture);
				}
			} else if ("damage-types".equals(exportation)) {
				EXPORTED_DAMAGE_TYPES.add(new PairStringInt(alias.getAttribute("name"),
						Integer.parseInt(alias.getAttribute("value"))));
			} else if ("types".equals(exportation)) {
				EXPORTED_TYPES.add(new PairStringInt(alias.getAttribute("name"),
						Integer.parseInt(alias.getAttribute("value"))));
			} else if ("phases".equals(exportation)) {
				EXPORTED_PHASES.add(new PairStringInt(alias.getAttribute("name"),
						Integer.parseInt(alias.getAttribute("value"))));
			}
		}
		return aliasMap;
	}

	/**
	 * <ul>
	 * 2 modes:
	 * <li>Update the a MDB for specified TBS against the XML files (main file,
	 * cards and fragments). Arguments are : TBS_NAME</li>
	 * <li>Rebuild completely the MDB for specified TBS. Arguments are : -full
	 * TBS_NAME</li>
	 * </ul>
	 * 
	 * @param args
	 *          main arguments.
	 */
	public static void main(String... args) {
		options = new Options();
		final CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			// Display help
			info(e.getMessage());
			parser.setUsageWidth(80);
			parser.printUsage(System.out);
			System.exit(-1);
			return;
		}

		if (options.isVersion()) {
			// Display version
			info("Version is " + IdConst.VERSION);
			System.exit(-1);
			return;
		}

		if (options.isHelp()) {
			// Display help
			parser.setUsageWidth(80);
			parser.printUsage(System.out);
			System.exit(-1);
			return;
		}

		warning = 0;
		uncompleted = 0;
		error = 0;
		long start = System.currentTimeMillis();
		XmlTools.initHashMaps();
		MToolKit.tbsName = options.getMdb();
		String xmlFile = MToolKit.getFile(
				IdConst.TBS_DIR + "/" + MToolKit.tbsName + ".xml", false)
				.getAbsolutePath();
		try {
			if (options.isForce()) {
				final File recycledDir = MToolKit.getTbsFile("recycled");
				if (!recycledDir.exists() || !recycledDir.isDirectory()) {
					recycledDir.mkdir();
				}

				parseRules(xmlFile, MToolKit.getTbsFile("recycled").getAbsolutePath(),
						new FileOutputStream(MToolKit.getTbsFile(MToolKit.tbsName + ".mdb",
								false)));
			} else {
				// Check the up-to-date state of MDB
				final File file = MToolKit.getFile(IdConst.TBS_DIR + "/"
						+ MToolKit.tbsName + "/" + MToolKit.tbsName + ".mdb");
				final long lastModifiedMdb;
				if (file == null) {
					lastModifiedMdb = 0;
				} else {
					lastModifiedMdb = file.lastModified();
				}
				boolean update = false;
				// Check the up-to-date state of MDB against the main XML file
				if (MToolKit.getFile(xmlFile).lastModified() > lastModifiedMdb) {
					// The main XML file is newer than MDB
					System.out.println("MDB is out of date, " + xmlFile + " is newer");
					update = true;
				} else {
					final File fragmentDir = MToolKit.getTbsFile("");
					for (File frament : fragmentDir
							.listFiles((FilenameFilter) FileFilterUtils.andFileFilter(
									FileFilterUtils.suffixFileFilter("xml"), FileFilterUtils
											.prefixFileFilter("fragment-")))) {
						if (frament.lastModified() > lastModifiedMdb) {
							// One card is newer than MDB
							System.out
									.println("MDB is out of date, at least one fragment found : "
											+ frament.getName());
							update = true;
							break;
						}
					}
					if (!update) {
						// Check the up-to-date state of MDB against the cards
						final File recycledDir = MToolKit.getTbsFile("recycled");
						if (!recycledDir.exists() || !recycledDir.isDirectory()) {
							recycledDir.mkdir();
						}
						if (recycledDir.lastModified() > lastModifiedMdb) {
							// The recycled XML file is newer than MDB
							System.out
									.println("MDB is out of date, the recycled directory is new");
							update = true;
						} else {
							for (File card : recycledDir
									.listFiles((FilenameFilter) FileFilterUtils.andFileFilter(
											FileFilterUtils.suffixFileFilter("xml"), FileFilterUtils
													.notFileFilter(FileFilterUtils
															.suffixFileFilter(IdConst.FILE_DATABASE_SAVED))))) {
								if (card.lastModified() > lastModifiedMdb) {
									// One card is newer than MDB
									System.out
											.println("MDB is out of date, at least one new card found : "
													+ card);
									update = true;
									break;
								}
							}
						}
					}
				}
				if (!update) {
					return;
				}
				// Need to update the whole MDB
				parseRules(xmlFile, MToolKit.getTbsFile("recycled").getAbsolutePath(),
						new FileOutputStream(MToolKit.getTbsFile(MToolKit.tbsName + ".mdb",
								false)));
			}
		} catch (SAXParseException e) {
			// Ignore this error
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (warning > 0) {
			System.out
					.println("\t" + warning + " warning" + (warning > 1 ? "s" : ""));
		}
		if (error > 0) {
			System.out.println("\t" + error + " error" + (error > 1 ? "s" : ""));
			System.out.println("Some cards have not been built correctly. Fix them.");
		} else {
			System.out.println("\tSuccessfull build");
		}
		System.out.println("\tTime : " + (System.currentTimeMillis() - start)
				/ 1000 + " s");
	}

	/**
	 * 
	 */
	private static XmlParser parser;

	/**
	 * 
	 */
	private XmlParser.Node config;

	/**
	 * Exported properties
	 */
	public static final List<PairStringInt> EXPORTED_PROPERTIES = new ArrayList<PairStringInt>();

	/**
	 * Exported properties
	 */
	public static final Map<Integer, String> PROPERTY_PICTURES = new HashMap<Integer, String>();

	/**
	 * Exported types
	 */
	public static final List<PairStringInt> EXPORTED_TYPES = new ArrayList<PairStringInt>();

	/**
	 * Exported phases
	 */
	public static final List<PairStringInt> EXPORTED_PHASES = new ArrayList<PairStringInt>();

	/**
	 * Exported damage types
	 */
	public static final List<PairStringInt> EXPORTED_DAMAGE_TYPES = new ArrayList<PairStringInt>();

	/**
	 * Found errors
	 */
	private static int error;

	/**
	 * Found warnings
	 */
	private static int warning;

	/**
	 * Found uncompleted cards
	 */
	static int uncompleted;

	/**
	 * Indicates the debug data are saved in the MDB.
	 * 
	 * @return true if the debug data are saved in the MDB.
	 */
	public static boolean isDebugEnable() {
		return true; // TODO use configuration to enable/disable debug on MDB
	}

	/**
	 * Return <code>true</code> if there is one or more errors.
	 * 
	 * @return <code>true</code> if there is one or more errors.
	 */
	public static boolean hasError() {
		return error != 0;
	}

	/**
	 * Return options of builder.
	 * 
	 * @return options.
	 */
	public static Options getOptions() {
		return options;
	}

	/**
	 * Return the no-Pay-Mana option.
	 * 
	 * @return the no-Pay-Mana option.
	 */
	public static boolean isNoPayMana() {
		return options.isNoPayMana();
	}
}
