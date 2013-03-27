package org.zonedabone.commandsigns.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.bukkit.command.CommandSender;
import org.zonedabone.commandsigns.CommandSigns;

import com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager;

public class Updater {

    public volatile Version currentVersion, newestVersion;

    public volatile boolean newAvailable = false;

    public volatile boolean awaitingRestart = false;

    private CommandSigns plugin;

    private final String downloadLocation = "http://dev.bukkit.org/media/files/";
    private URL downloadUrl;

    private final String version = "https://raw.github.com/zonedabone/CommandSigns/master/VERSION";

    public Updater(CommandSigns plugin) {
        this.plugin = plugin;
    }
    
	private class AllowAllTrustManager extends X509ExtendedTrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1,
				String arg2, String arg3) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1,
				String arg2, String arg3) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		/**
		 * Returns a URL connection stream for HTTPS. This method removes
		 * TrustManager intervention.
		 * 
		 * @param url
		 * @return
		 * @throws IOException
		 */
		public HttpsURLConnection getHTTPSConnection(URL url)
				throws IOException {
			HttpsURLConnection connection;
			connection = (HttpsURLConnection) url.openConnection();

			// Set the AllowAll trust manager
			try {
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, new TrustManager[] { this }, null);
				connection.setSSLSocketFactory(sslContext.getSocketFactory());
			} catch (Exception e) {
				plugin.getLogger()
						.warning(
								"HTTPS connection error in updater - "
										+ e.getMessage());
			}

			return connection;
		}
	}

	public class Checker implements Runnable {

		@Override
		public void run() {
			try {
				currentVersion = new Version(plugin.getDescription()
						.getVersion());

				URL url = new URL(version);

				URLConnection connection;
				if (url.getProtocol() == "HTTPS") {
					connection = new AllowAllTrustManager()
							.getHTTPSConnection(url);
				} else {
					connection = url.openConnection();
				}

				/*
				 * Line 1: Version Line Line 2: Random numbers for Bukkit
				 * download URL
				 */
				BufferedReader in = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				newestVersion = new Version(in.readLine());
				downloadUrl = new URL(downloadLocation + in.readLine()
						+ "/CommandSigns.jar");

				if (currentVersion.compareTo(newestVersion) < 0) {
					newAvailable = true;

					// Auto update if set
					if (plugin.config.getBoolean("updater.auto-install") == true)
						new UpdaterThread(plugin.getServer().getConsoleSender())
								.start();
				}
			} catch (Exception e) {
				plugin.getLogger().warning(
						"Unable to check for updates - " + e.getMessage());
			}
		}
	}

	public class UpdaterThread extends Thread {

		private CommandSender sender;

		public UpdaterThread(CommandSender sender) {
			this.sender = sender;
		}

		@Override
		public void run() {
			if (newAvailable && !awaitingRestart) {
				try {
					long startTime = System.currentTimeMillis();

					URLConnection connection;
					if (downloadUrl.getProtocol() == "HTTPS") {
						connection = new AllowAllTrustManager()
								.getHTTPSConnection(downloadUrl);
					} else {
						connection = downloadUrl.openConnection();
					}

					InputStream reader = connection.getInputStream();
					File f = plugin.getUpdateFile();
					f.getParentFile().mkdirs();
					FileOutputStream writer = new FileOutputStream(f);
					byte[] buffer = new byte[153600];
					int totalBytesRead = 0;
					int bytesRead = 0;

					while ((bytesRead = reader.read(buffer)) > 0) {
						writer.write(buffer, 0, bytesRead);
						buffer = new byte[153600];
						totalBytesRead += bytesRead;
					}
					long endTime = System.currentTimeMillis();
					plugin.messenger.sendMessage(sender, "update.finish",
							new String[] { "SIZE", "TIME" }, new String[] {
									"" + totalBytesRead / 1000,
									"" + ((double) (endTime - startTime))
											/ 1000 });
					writer.close();
					reader.close();

					newAvailable = false;
					awaitingRestart = true;
				} catch (MalformedURLException e) {
					plugin.messenger.sendMessage(sender, "update.fetch_error",
							new String[] { "ERROR" },
							new String[] { e.getMessage() });
				} catch (IOException e) {
					plugin.messenger.sendMessage(sender, "update.fetch_error",
							new String[] { "ERROR" },
							new String[] { e.getMessage() });
				}
			} else if (awaitingRestart) {
				plugin.messenger.sendMessage(sender,
						"update.already_downloaded");
			} else {
				plugin.messenger.sendMessage(sender, "update.up_to_date");
			}
		}
	}

	public class Version implements Comparable<Version> {

		private List<Integer> parts = new ArrayList<Integer>();

		public Version(String versionString) {
			try {
				String[] stringParts = versionString.split("\\.");
				for (String part : stringParts) {
					parts.add(Integer.parseInt(part));
				}
			} catch (Exception ex) {
				plugin.getLogger().warning(
						"Unable to decode version string " + versionString);
			}
		}

		public boolean add(int subversion) {
			return parts.add(subversion);
		}

		@Override
		public int compareTo(Version anotherVersion) {
			try {
				// Add .0s to the shortest version string to make lengths the
				// same
				for (int i = this.length(); i < anotherVersion.length(); i++)
					this.add(0);
				for (int i = anotherVersion.length(); i < this.length(); i++)
					anotherVersion.add(0);

				int max = this.length();

				// Compare each integer in the string and stop at the first
				// difference
				int i = 0;
				while (i < max && this.get(i) == anotherVersion.get(i)) {
					i++;
				}

				if (i >= max)
					return 0;
				else if (this.get(i) < anotherVersion.get(i))
					return -1;
				else
					return 1;
			} catch (Exception ex) {
				plugin.getLogger().warning("Unable to compare versions");
				return 0;
			}
		}

		public Integer get(int index) {
			return parts.get(index);
		}

		public List<Integer> getParts() {
			return parts;
		}

		public int length() {
			return parts.size();
		}

		public Integer remove() {
			return parts.remove(parts.size() - 1);
		}

		public String toString() {
			String versionString = "";
			boolean first = true;
			for (Integer part : parts) {
				if (!first)
					versionString += ".";
				versionString += String.format("%d", part);
				first = false;
			}
			return versionString;
		}
	}
}