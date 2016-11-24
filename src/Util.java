import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.swing.JLabel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Util {
	public static Document getDoc(String pageUrl) {
		Document doc = null;
		try {
			doc = Jsoup.connect(pageUrl)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
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
}
