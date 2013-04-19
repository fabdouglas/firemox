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
package fabdouglas;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
public class Starter {

	/**
	 * @param mainClass
	 *          full QName of class to load, must be runnable in version.
	 * @param parameters
	 *          jar parameters to propagate
	 */
	protected void init(final String mainClass, String... parameters) {
		try {

			// adding libraries
			final File[] files = new File("lib").listFiles(new FileFilter() {
				public boolean accept(File f) {
					if (f != null && f.getName().endsWith(".jar")) {
						return true;
					}
					return false;
				}
			});
			if (files == null) {
				System.err.println("Unable to find the 'lib' directory.\n"
						+ "Make sure you are running this from the installation directory");
				System.exit(-1);
				return;
			}
			Arrays.sort(files);
			final List<URL> urls = new ArrayList<URL>(files.length + 1);
			for (int i = 0; i < files.length; i++) {
				URL url = files[i].toURI().toURL();
				if (files[i].getName().indexOf('-') > 0) {
					String matchPath = files[i].getName().substring(0,
							files[i].getName().lastIndexOf('-'));
					if (i > 0 && files[i - 1].getName().contains(matchPath)) {
						urls.remove(i - 1);
					}
				}
				urls.add(url);
			}
			urls.add(0, new File("").getAbsoluteFile().toURI().toURL());
			boot(urls, mainClass, parameters);
		} catch (NullPointerException noURL) {
			noURL.printStackTrace();
		} catch (IOException noConnection) {
			noConnection.printStackTrace();
		}
	}

	/**
	 * @param urls
	 *          URLs defining the classpath for the application
	 * @param mainClass
	 *          name of the class to start as a thread
	 * @param parameters
	 */
	protected void boot(final List<URL> urls, final String mainClass,
			String... parameters) {
		try {
			System.out.println("Use dynamical jar dependencies :");
			for (URL url : urls) {
				System.out.println("\t" + url);
			}
			final URL[] urlsArray = new URL[urls.size()];
			urls.toArray(urlsArray);
			final URLClassLoader loader = new URLClassLoader(urlsArray);
			Thread.currentThread().setContextClassLoader(loader);
			System.out.println("Launching " + mainClass + "...");
			loader.loadClass(mainClass).getMethod("main",
					new Class[] { new String[] {}.getClass() }).invoke(null,
					new Object[] { parameters });
		} catch (ClassNotFoundException obeyYourParents) {
			obeyYourParents.printStackTrace();
		} catch (IllegalAccessException eatMoreFruit) {
			eatMoreFruit.printStackTrace();
		} catch (ClassCastException stupidProgrammer) {
			stupidProgrammer.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException cherchePasTaDejaTord) {
			cherchePasTaDejaTord.printStackTrace();
		} catch (InvocationTargetException perfideEstLeMaitre) {
			perfideEstLeMaitre.printStackTrace();
		} catch (NoSuchMethodException plusYenaMoinsYaDeRiz) {
			plusYenaMoinsYaDeRiz.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {
		new Starter().init("net.sf.firemox.Magic", args);
	}
}
