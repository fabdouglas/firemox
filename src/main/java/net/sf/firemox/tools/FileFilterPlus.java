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
package net.sf.firemox.tools;

import java.io.File;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

/**
 * A convenience implementation of FileFilter that filters out all files except
 * for those type extensions that it knows about. Extensions are of the type
 * ".foo", which is typically found on Windows and Unix boxes, but not on
 * Macinthosh. Case is ignored. Example - create a new filter that filerts out
 * all files but gif ,png and jpg image files: JFileChooser chooser = new
 * JFileChooser(); FileFilterPlus filter = new FileFilterPlus( new String{"gif",
 * "jpg", "png"}, "JPEG & GIF Images") chooser.addChoosableFileFilter(filter);
 * chooser.showOpenDialog(this);
 * 
 * @version 1.15 11/01/03
 * @author Jeff Dinkins
 * @author Fabrice Daugan (has only changed the originaly name of this class)
 */
public class FileFilterPlus extends FileFilter implements java.io.FileFilter {

	private Dictionary<String, FileFilter> filters = null;

	private String description = null;

	private String fullDescription = null;

	private boolean useExtensionsInDescription = true;

	/**
	 * Creates a file filter. If no filters are added, then all files are
	 * accepted.
	 */
	public FileFilterPlus() {
		this.filters = new Hashtable<String, FileFilter>();
	}

	/**
	 * Creates a file filter that accepts files with the given extension. Example:
	 * new FileFilterPlus("jpg");
	 * 
	 * @param extension
	 *          the filter extension.
	 * @see #addExtension(String)
	 */
	public FileFilterPlus(String extension) {
		this(extension, null);
	}

	/**
	 * Creates a file filter that accepts the given file type. Example: new
	 * FileFilterPlus("jpg", "JPEG Image Images"); Note that the "." before the
	 * extension is not needed. If provided, it will be ignored.
	 * 
	 * @param extension
	 *          the filter extension.
	 * @param description
	 *          the filter description.
	 * @see #addExtension(String)
	 */
	public FileFilterPlus(String extension, String description) {
		this();
		if (extension != null) {
			addExtension(extension);
		}
		if (description != null) {
			setDescription(description);
		}
	}

	/**
	 * Creates a file filter from the given string array. Example: new
	 * FileFilterPlus(String {"gif", "jpg"}); Note that the "." before the
	 * extension is not needed adn will be ignored.
	 * 
	 * @param filters
	 *          set of allowed filters.
	 * @see #addExtension(String)
	 */
	public FileFilterPlus(String[] filters) {
		this(filters, null);
	}

	/**
	 * Creates a file filter from the given string array and description. Example:
	 * new FileFilterPlus(String {"gif", "jpg"}, "Gif and JPG Images"); Note that
	 * the "." before the extension is not needed and will be ignored.
	 * 
	 * @param filters
	 *          set of allowed filters.
	 * @param description
	 *          file filter description.
	 * @see #addExtension(String)
	 */
	public FileFilterPlus(String[] filters, String description) {
		this();
		for (String filter : filters) {
			// add filters one by one
			addExtension(filter);
		}
		if (description != null) {
			setDescription(description);
		}
	}

	/**
	 * Return true if this file should be shown in the directory pane, false if it
	 * shouldn't. Files that begin with "." are ignored.
	 * 
	 * @param f
	 *          is the file to verify
	 * @return true if this file should be shown in the directory pane, false if
	 *         it shouldn't.
	 * @see #getExtension(File)
	 */
	@Override
	public boolean accept(File f) {
		if (f != null) {
			if (f.isDirectory()) {
				return filters.get(".") != null;
			}
			String extension = getExtension(f);
			if (extension != null && filters.get(extension.toLowerCase()) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the extension part of the file's name .
	 * 
	 * @param fiile
	 *          the fulle file.
	 * @return the extension portion of the file's name .
	 */
	public String getExtension(File fiile) {
		if (fiile != null) {
			String filename = fiile.getName();
			int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1) {
				return filename.substring(i + 1).toLowerCase();
			}
		}
		return null;
	}

	/**
	 * Adds a filetype "dot" extension to filter against. For example: the
	 * following code will create a filter that filters out all files except those
	 * that end in ".jpg" and ".tif": FileFilterPlus filter = new
	 * ExampleFileFilter(); filter.addExtension("jpg");
	 * filter.addExtension("tif"); Note that the "." before the extension is not
	 * needed and will be ignored.
	 * 
	 * @param extension
	 *          the filter extension.
	 */
	public void addExtension(String extension) {
		if (filters == null) {
			filters = new Hashtable<String, FileFilter>(5);
		}
		filters.put(extension.toLowerCase(), this);
		fullDescription = null;
	}

	@Override
	public String getDescription() {
		if (fullDescription == null) {
			if (description == null || isExtensionListInDescription()) {
				fullDescription = description == null ? "(" : description + " (";
				// build the description from the extension list
				Enumeration<String> extensions = filters.keys();
				if (extensions != null) {
					fullDescription += "." + extensions.nextElement();
					while (extensions.hasMoreElements()) {
						fullDescription += ", ." + extensions.nextElement();
					}
				}
				fullDescription += ")";
			} else {
				fullDescription = description;
			}
		}
		return fullDescription;
	}

	/**
	 * Sets the human readable description of this filter. For example:
	 * filter.setDescription("Gif and JPG Images");
	 * 
	 * @param description
	 *          the filter description.
	 * @see #setDescription(String)
	 * @see #setExtensionListInDescription(boolean)
	 * @see #isExtensionListInDescription()
	 */
	public void setDescription(String description) {
		this.description = description;
		fullDescription = null;
	}

	/**
	 * Determines whether the extension list (.jpg, .gif, etc) should show up in
	 * the human readable description. Only relevent if a description was provided
	 * in the constructor or using setDescription();
	 * 
	 * @param b ?
	 * @see FileFilter#getDescription()
	 * @see #setDescription(String)
	 * @see #isExtensionListInDescription()
	 */
	public void setExtensionListInDescription(boolean b) {
		useExtensionsInDescription = b;
		fullDescription = null;
	}

	/**
	 * Returns whether the extension list (.jpg, .gif, etc) should show up in the
	 * human readable description. Only relevent if a description was provided in
	 * the constructor or using setDescription();
	 * 
	 * @return true if use extensions in description
	 * @see FileFilter#getDescription()
	 * @see #setDescription(String)
	 * @see #setExtensionListInDescription(boolean)
	 */
	public boolean isExtensionListInDescription() {
		return useExtensionsInDescription;
	}
}