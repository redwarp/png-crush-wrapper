/*
 * Copryright (C) 2012 Redwarp
 * 
 * This file is part of PNGCrush Wrapper.
 * PNGCrush Wrapper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * PNGCrush Wrapper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with PNGCrush Wrapper.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.redwarp.tool.pngcrush;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Extractor {
	public static String extractResource(String path) throws IOException{
		try {
			final ProtectionDomain domain;
			final CodeSource source;
			final URL url;
			final URI uri;

			domain = Main.class.getProtectionDomain();
			source = domain.getCodeSource();
			url = source.getLocation();
			uri = url.toURI();
			File exe = new File(uri);
			if (exe.isDirectory()) {
				return Extractor.class.getResource(path).getPath();
			} else {
				final ZipFile zipFile = new ZipFile(exe);

				final File tempFile;
				final ZipEntry entry;
				final InputStream zipStream;
				OutputStream fileStream;
				String fileName = "resource";

				tempFile = File.createTempFile(fileName,
						Long.toString(System.currentTimeMillis()));
				tempFile.deleteOnExit();
				if(path.startsWith("/")){
					path = path.substring(1);
				}
				entry = zipFile.getEntry(path);

				if (entry == null) {
					throw new FileNotFoundException(
							"cannot find file in archive: " + zipFile.getName());
				}

				zipStream = zipFile.getInputStream(entry);
				fileStream = null;

				try {
					final byte[] buf;
					int i;

					fileStream = new FileOutputStream(tempFile);
					buf = new byte[1024];
					i = 0;

					while ((i = zipStream.read(buf)) != -1) {
						fileStream.write(buf, 0, i);
					}
				} finally {
					close(zipStream);
					close(fileStream);
				}
				tempFile.setExecutable(true);
				return tempFile.getAbsolutePath();
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new IOException("Couldn't get resource + "+path);
	}

	private static void close(final Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
