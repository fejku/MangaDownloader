package pl.fejku;

import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Main {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					final MangaDownloader mangaDownloader = new MangaDownloader();
					mangaDownloader.setIconImage(new ImageIcon("./images/icon.png").getImage());
					mangaDownloader.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					mangaDownloader.setBounds(100, 100, 800, 600);
					mangaDownloader.setTitle("MangaDownloader");
					mangaDownloader.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
