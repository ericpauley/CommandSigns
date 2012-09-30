package org.zonedabone.commandsigns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.bukkit.command.CommandSender;

import com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager;

public class CommandSignsUpdater {
    
    private CommandSigns plugin;
    private String upstream = "https://raw.github.com/zonedabone/CommandSigns/master/";
    private String downloadRoot = "http://cloud.github.com/downloads/zonedabone/CommandSigns/";
    public Version currentVersion, newestVersion;
    public boolean newAvailable = false;
    
    public CommandSignsUpdater(CommandSigns plugin) {
        this.plugin = plugin;
    }
    
    public class Checker implements Runnable {
        
        @Override
        public void run() {
            try {	
                currentVersion = new Version(plugin.getDescription().getVersion());
                
                URL url = new URL(upstream + "VERSION");
                HttpsURLConnection connection;
                connection = (HttpsURLConnection)url.openConnection();
                
                if (upstream.startsWith("https")) {
                	// Special handling for HTTPS URLs if the user has insufficient Cert Keys
                    SSLContext sslContext = SSLContext.getInstance("TLS");
         			sslContext.init(null, new TrustManager[] { new AllowAllTrustManager() }, null);
                    connection.setSSLSocketFactory(sslContext.getSocketFactory());
                }
                
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                newestVersion = new Version(in.readLine());
                
                if (currentVersion.compareTo(newestVersion) < 0)
                    newAvailable = true;
            } catch (Exception e) {
                plugin.getLogger().warning("Unable to check for updates - " + e.getMessage());
            }
        }
    }
    
    public class Updater extends Thread {
        
        private CommandSender sender;
        
        public Updater(CommandSender sender) {
            this.sender = sender;
        }
        
        @Override
        public void run() {
            try {
                long startTime = System.currentTimeMillis();
                URL url = new URL(downloadRoot + "CommandSigns-" + newestVersion + ".jar");
                url.openConnection();
                InputStream reader = url.openStream();
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
                Messaging.sendMessage(sender, "update.finish", "s", "" + (((totalBytesRead)) / 1000), "t", "" + (((double) (endTime - startTime)) / 1000));
                writer.close();
                reader.close();
            } catch (MalformedURLException e) {
                Messaging.sendMessage(sender, "update.fetch_error", "e", e.getMessage());
            } catch (IOException e) {
                Messaging.sendMessage(sender, "update.fetch_error", "e", e.getMessage());
            }
        }
    }
    
    public class AllowAllTrustManager extends X509ExtendedTrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1,
				String arg2, String arg3) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1,
				String arg2, String arg3) throws CertificateException {
		}
    }
    
    public class Version implements Comparable<Version> {
        
        private List<Integer> parts = new ArrayList<Integer>();
        
        public boolean add(int subversion) {
        	return parts.add(subversion);
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
        
        public Version(String versionString) {
            try {
                String[] stringParts = versionString.split("\\.");
                for (String part : stringParts) {
                    parts.add(Integer.parseInt(part));
                }
            } catch (Exception ex) {
                plugin.getLogger().warning("Unable to decode version string " + versionString);
            }
        }
        
        @Override
        public int compareTo(Version anotherVersion) {
            try {
            	// Add .0s to the shortest version string to make lengths the same
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
    }
}