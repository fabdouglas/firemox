/*
 *   Magic-Project is a turn based strategy simulator
 *   Copyright (C) 2003-2006 Fabrice Daugan
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
package net.sf.firemox;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Jon Peck http://jonpeck.com (adapted from
 *         http://www.javaworld.com/javaworld/javatips/jw-javatip113.html)
 */
public final class Tools {

	/**
	 * Private constructor to prevent this class to be instanciated.
	 */
	private Tools() {
		// nothing to do
	}

	/**
	 * list Classes inside a given package
	 * @param dir 
	 * 
	 * @param pckgname
	 *          String name of a Package, EG "java.lang"
	 * @return Class[] classes inside the root of the given package
	 * @throws ClassNotFoundException
	 *           if the Package is invalid
	 */
	public static Class<?>[] getClasses(String dir, String pckgname)
			throws ClassNotFoundException {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		// Get a File object for the package
		File directory = null;
		try {
			// Thread.currentThread().getContextClassLoader()
			// .getResource("magicproject/Magic.class")
			directory = new File(Thread.currentThread().getContextClassLoader()
					.getResource(
							((dir == null || dir.length() == 0) ? "" : dir + "/")
									+ pckgname.replace('.', '/')).getFile());
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(pckgname
					+ " does not appear to be a valid package");
		}
		if (directory.exists()) {
			// Get the list of the files contained in the package
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				// we are only interested in .class files
				if (files[i].endsWith(".class")) {
					// removes the .class extension
					classes.add(Class.forName(pckgname + '.'
							+ files[i].substring(0, files[i].length() - 6)));
				}
			}
		} else {
			throw new ClassNotFoundException(pckgname
					+ " does not appear to be a valid package");
		}
		return classes.toArray(new Class[classes.size()]);
	}
}
