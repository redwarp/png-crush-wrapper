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

import javax.swing.SwingWorker;

public class PNGCrusher extends SwingWorker<String, String> {
	private File file;
	private boolean bruteForce;
	private static String exePath;

	public PNGCrusher(File file, boolean bruteForce) {
		this.bruteForce = bruteForce;
		this.file = file;
	}

	@Override
	protected String doInBackground() throws Exception {
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

						url = tempFile.toURI().toURL();
						stream = url.openStream();
						int finalFileSize = stream.available();
						stream.close();

						if (finalFileSize != 0 && finalFileSize < initialFileSize) {
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
							tempFile.delete();
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
		return null;
	}

	private static void init() {
		File exe;
		try {
			final ProtectionDomain domain;
			final CodeSource source;
			final URL url;
			final URI uri;

			domain = Main.class.getProtectionDomain();
			source = domain.getCodeSource();
			url = source.getLocation();
			uri = url.toURI();
			System.out.println(uri);
			exe = new File(uri);
			if (exe.isDirectory()) {
				exePath = PNGCrusher.class.getResource("/bin/pngcrush_1_7_15.exe")
						.getPath();
			} else {
				final ZipFile zipFile = new ZipFile(exe);

				final File tempFile;
				final ZipEntry entry;
				final InputStream zipStream;
				OutputStream fileStream;
				String fileName = "pngcrush";

				tempFile = File.createTempFile(fileName,
						Long.toString(System.currentTimeMillis()));
				tempFile.deleteOnExit();
				entry = zipFile.getEntry("bin/pngcrush_1_7_15.exe");

				if (entry == null) {
					throw new FileNotFoundException(
							"cannot find file: pngcrusher in archive: "
									+ zipFile.getName());
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
				exePath = tempFile.getAbsolutePath();
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
	}

	static {
		init();
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
