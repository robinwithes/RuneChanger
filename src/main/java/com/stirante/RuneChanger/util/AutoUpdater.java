package com.stirante.RuneChanger.util;

import com.google.gson.Gson;
import com.stirante.RuneChanger.gui.Constants;
import com.stirante.RuneChanger.model.github.Asset;
import com.stirante.RuneChanger.model.github.Release;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.FileUtils;

public class AutoUpdater {
	public static final String LATEST_RELEASE_URL = "https://api.github.com/repos/stirante/RuneChanger/releases/latest";
	private String downloadLink;
	public String latestReleaseVersion;

	public AutoUpdater() {
	}

	private boolean checkVersion() throws IOException {
		URL url = new URL("https://api.github.com/repos/stirante/RuneChanger/releases/latest");
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		InputStream in = conn.getInputStream();
		Release latest = (Release)(new Gson()).fromJson(new InputStreamReader(in), Release.class);
		in.close();
		this.downloadLink = ((Asset)latest.assets.get(0)).browserDownloadUrl;
		this.latestReleaseVersion = latest.tagName;
		return latest.tagName.equalsIgnoreCase("1.8");
	}

	private boolean updatePopup() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Update Available");
		alert.setHeaderText("A update is available, would you like RuneChanger to update to version " + this.latestReleaseVersion + "?");
		alert.setContentText((String)null);
		Optional<ButtonType> option = alert.showAndWait();
		if (option.get() == null) {
			return false;
		} else if (option.get() == ButtonType.OK) {
			return true;
		} else {
			return option.get() == ButtonType.CANCEL ? false : false;
		}
	}

	private void downloadUpdate() {
		try {
			HttpURLConnection connection;
			URL url = new URL(this.downloadLink);
			connection = (HttpURLConnection)url.openConnection();
			connection.connect();
			if (connection.getResponseCode() != 200) {
				return;
			}

			connection.disconnect();
			File newJar = new File("RuneChanger-" + this.latestReleaseVersion + ".jar");
			File jarDir = new File(System.getProperty("user.dir") + "\\RuneChanger-" + Constants.VERSION_STRING + ".jar");
			if (jarDir.exists()) {
				System.out.println(jarDir); //TODO temp
				System.out.println("Detected old jar and scheduled it for deletion.");
				Runtime.getRuntime().exec("cmd /c ping localhost -n 6 > nul && del " + jarDir.getAbsolutePath());
			}

			System.out.println("Downloading");
			FileUtils.copyURLToFile(url, newJar, 7500, 10000);
			System.out.println("Done downloading new jar");
			Process proc = Runtime.getRuntime().exec("java -jar " + newJar.getAbsolutePath());
			System.exit(0);
		} catch (Exception var6) {
			var6.printStackTrace();
		}

	}

	public void checkForUpdate() throws IOException {
		System.out.println("User is running latest release: " + this.checkVersion());
		System.out.println("User is running version: 1.8\nLatest version: " + this.latestReleaseVersion);
		if (!this.checkVersion() && this.updatePopup()) {
			this.downloadUpdate();
		}

	}

	public static void main(String[] args) throws IOException {
		AutoUpdater updater = new AutoUpdater();
		updater.checkVersion();
		updater.downloadUpdate();
	}
}
