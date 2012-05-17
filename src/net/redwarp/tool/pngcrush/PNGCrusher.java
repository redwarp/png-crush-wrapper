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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.SwingWorker;

public class PNGCrusher extends SwingWorker<String, String> {
	private File file;
	private boolean bruteForce;
	private static String exePath;
	private static String pngOutPath;

	public PNGCrusher(File file, boolean bruteForce) {
		this.bruteForce = bruteForce;
		this.file = file;
	}

	@Override
	protected String doInBackground() throws Exception {
		pngout();
		return null;
	}

	private static void init() {
		try {
			exePath = Extractor.extractResource("/bin/pngcrush_1_7_15.exe");
		} catch (IOException e) {
			exePath = "";
		}

		try {
			pngOutPath = Extractor
					.extractResource("/bin/pngout_07_02_2011.exe");
		} catch (IOException e) {
			pngOutPath = "";
		}
	}

	static {
		init();
	}

	@SuppressWarnings("unused")
	private void pngcrush() throws Exception {
		if (file.getName().endsWith(".png")) {
			publish("- " + file.getName() + "...");
			String originalName = file.getAbsolutePath();
			File tempFile = File.createTempFile("tempPng",
					Long.toString(System.currentTimeMillis()));
			tempFile.deleteOnExit();
			String outputName = tempFile.getAbsolutePath();
			try {
				String cmd;
				if (bruteForce) {
					cmd = exePath + " -brute " + '"' + originalName + "\" \""
							+ outputName + '"';
				} else {
					cmd = exePath + " " + '"' + originalName + "\" \""
							+ outputName + '"';
				}
				System.out.println(cmd);
				Process proc = Runtime.getRuntime().exec(cmd);

				StreamGobbler outputGobbler = new StreamGobbler(
						proc.getInputStream(), "OUTPUT");
				StreamGobbler errorGobbler = new StreamGobbler(
						proc.getErrorStream(), "ERROR");

				outputGobbler.start();
				errorGobbler.start();

				try {
					int result = proc.waitFor();
					if (result == 0) {
						publish(" done.");
						File outputFile = tempFile;

						URL url = file.toURI().toURL();
						InputStream stream = url.openStream();
						int initialFileSize = stream.available();
						stream.close();

						url = outputFile.toURI().toURL();
						stream = url.openStream();
						int finalFileSize = stream.available();
						stream.close();

						if (finalFileSize != 0
								&& finalFileSize < initialFileSize) {
							if (file.delete()) {
								boolean rename = outputFile.renameTo(file);
								if (rename) {
									float reduction = (100f - 100f
											* (float) finalFileSize
											/ (float) initialFileSize);

									publish(String.format("%.2f", reduction)
											+ " % reduction\n");
								} else {
									publish(" couldn't cleanup\n");
								}
							} else {
								publish(" couldn't cleanup\n");
							}
						} else {
							publish("0 % reduction\n");
							outputFile.delete();
						}
					} else {
						System.out.println("Result : " + result);
						publish(" aborted.\n");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					publish(" aborted.\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
				publish(" aborted.\n");
			}
		}
	}

	private void pngout() throws Exception {
		if (file.getName().endsWith(".png")) {
			publish("- " + file.getName() + "...");
			String originalName = file.getAbsolutePath();
			File tempFile = File.createTempFile("tempPng",
					Long.toString(System.currentTimeMillis()));
			tempFile.deleteOnExit();
			String outputName = tempFile.getAbsolutePath();
			try {
				String cmd;

				cmd = pngOutPath + " " + '"' + originalName + "\" \""
						+ outputName + '"';

				System.out.println(cmd);
				Process proc = Runtime.getRuntime().exec(cmd);

				StreamGobbler outputGobbler = new StreamGobbler(
						proc.getInputStream(), "OUTPUT");
				StreamGobbler errorGobbler = new StreamGobbler(
						proc.getErrorStream(), "ERROR");

				outputGobbler.start();
				errorGobbler.start();

				try {
					int result = proc.waitFor();
					File outputFile = new File(tempFile.getAbsolutePath()
							+ ".png");
					tempFile.delete();
					if (result == 0) {
						URL url = file.toURI().toURL();
						InputStream stream = url.openStream();
						int initialFileSize = stream.available();
						stream.close();

						url = outputFile.toURI().toURL();
						stream = url.openStream();
						int finalFileSize = stream.available();
						stream.close();

						if (finalFileSize != 0
								&& finalFileSize < initialFileSize) {
							if (file.delete()) {
								boolean rename = outputFile.renameTo(file);
								if (rename) {
									float reduction = (100f - 100f
											* (float) finalFileSize
											/ (float) initialFileSize);

									publish(String.format("%.2f", reduction)
											+ " % reduction\n");
								} else {
									publish(" couldn't cleanup\n");
								}
							} else {
								publish(" couldn't cleanup\n");
							}
						} else {
							publish("0 % reduction\n");
							outputFile.delete();
						}
					} else if (result == 2) {
						publish("0 % reduction\n");
						outputFile.delete();
					} else {
						System.out.println("Result : " + result);
						publish(" aborted.\n");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					publish(" aborted.\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
				publish(" aborted.\n");
			}
		}
	}
}
