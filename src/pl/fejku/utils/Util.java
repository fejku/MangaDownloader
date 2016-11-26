package pl.fejku.utils;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Util {
	public static Document getDoc(String pageUrl) {
		Document doc = null;
		try {
			doc = Jsoup.connect(pageUrl)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
					.timeout(0)
					.get();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return doc;
	}
	
	public static void saveFile(String path, String fileName, String imageUrl) {
		try {
			URL website = new URL(imageUrl);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(path + "/" + fileName);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (MalformedURLException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean createFolders(String path) {
		File folder = new File(path);
		return folder.mkdirs();
	}
	
	public static void changeLabel(JLabel lbl, Color color, String text) {
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {			
			@Override
			protected Void doInBackground() throws Exception {
				return null;
			}
			
			@Override
			protected void done() {
				lbl.setForeground(color);
				lbl.setText(text);
			}
		};
		worker.execute();	
	}

	public static void clearProgress(JProgressBar progressBar) {
		progressBar.setValue(0);
		progressBar.setStringPainted(false);
	}
	
	public static void setProgressText(JProgressBar progressBar, String text) {
		progressBar.setStringPainted(true);
		progressBar.setString(text);
	}
	
	public static void setProgressTextValue(JProgressBar progressBar, String text, int value) {
		progressBar.setStringPainted(true);
		progressBar.setString(text);
		progressBar.setValue(value);
	}
	
	public static void setProgressValue(JProgressBar progressBar, int value) {
		progressBar.setStringPainted(true);
		progressBar.setValue(value);
	}
}
