package pl.fejku;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.thoughtworks.xstream.XStream;

import pl.fejku.model.Chapter;
import pl.fejku.model.ChapterList;
import pl.fejku.model.ChapterTableModel;
import pl.fejku.model.Manga;
import pl.fejku.model.MangaList;
import pl.fejku.model.MangaTableModel;
import pl.fejku.model.Page;
import pl.fejku.model.Site;
import pl.fejku.model.SiteList;
import pl.fejku.utils.Util;



public class MangaDownloader extends JFrame {

	private static final long serialVersionUID = 5991074200560946978L;
	private static final String siteListXMLFile = "SiteList.xml";
	private static final String mangaListXMLFile = "MangaList.xml";
	
	private JPanel pnlGlowny;
	
	private JScrollPane scrlPnlSite;
	private JList listSite;
	private JButton btnDodajStrone;
	
	private JButton btnPobierzMangii;
	
	private JProgressBar progressBar;
	private JScrollPane scrlPnlManga;
	private JTable tabManga;
	private JTextField txtFilterManga;
	
	private JScrollPane scrPnlChapter;
	private JTable tabChapter;
	
	private void enabelAllControls(boolean enable) {
		listSite.setEnabled(enable);
		btnDodajStrone.setEnabled(enable);
		btnPobierzMangii.setEnabled(enable && !listSite.isSelectionEmpty());
		tabManga.setEnabled(enable);
	}
	
	private void pickSite(MouseEvent e) {
		if (!listSite.isSelectionEmpty()) {
			btnPobierzMangii.setEnabled(true);
		}
		if (e.getClickCount() == 2) {
			fillMangaList();
		}
	}
	
	private void doPickChapter() {
		
		SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
			
			int row = tabChapter.getSelectedRow();
			int column = tabChapter.getSelectedColumn();
			int pageNr = 0;
			
			@Override
			protected Void doInBackground() throws Exception {
				
				Chapter chapter = (Chapter)tabChapter.getModel().getValueAt(row, column);
				
				String mangaName = chapter.getLink().split("/")[4];				
				mangaName = mangaName.replaceAll("_", " ");
				mangaName = StringUtils.capitalize(mangaName);
				mangaName = mangaName.replaceAll("[^a-zA-Z0-9- ]", "");
				String chapterName = chapter.getName().replaceAll("[^a-zA-Z0-9- ]", "");
				String path = mangaName + "/" + chapterName; 

				if (Util.createFolders(path) == false) {
					tabChapter.getModel().setValueAt("<font color=#54af54><b>OK</b></font> ", 
							row, column);
					return null;						
				}	
				
				for(Page page: chapter.getPages()) {
					SwingWorker<Void, String> w = new SwingWorker<Void, String>() {
						boolean isDone = false;
						@Override
						protected Void doInBackground() throws Exception {
							String fileExtansion = page.getImageUrl().substring(page.getImageUrl().lastIndexOf("."));
							String fileName = page.getPageNr() + fileExtansion;
							Util.saveFile(path, fileName, page.getImageUrl());
							publish("");
							return null;
						}

						@Override
						protected void process(List<String> chunks) {
							if (!isDone) {
								pageNr = pageNr + chunks.size();
								tabChapter.getModel().setValueAt(
										"<font color=#6272a4><b>"+pageNr+"/"+chapter.getPageAmount()+"</b></font> ",
										row, column);
							}
						}
						
						@Override
						protected void done() {
							if (pageNr == chapter.getPageAmount()) {
								tabChapter.getModel().setValueAt("<font color=#54af54><b>OK</b></font> ", 
										row, column);
								isDone = true;
							}
						}						
					};
					w.execute();					
				}				
				return null;
			}
		};
		worker.execute();
	}
	
	private void doPickManga() {		
		Manga manga = (Manga)tabManga.getValueAt(tabManga.convertRowIndexToModel(tabManga.getSelectedRow()), 0);
		System.out.println(manga.getLink());
		ChapterList chapterList = new ChapterList();
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				Elements chapterLinks = Util.getDoc(manga.getLink()).select("#chapter_table a");
				for(Element chapterLink: chapterLinks) {
					chapterList.add(new Chapter(chapterLink.text(), chapterLink.attr("href")));
				}										
				return null;
			}

			@Override
			protected void done() {
				tabChapter.setModel(new ChapterTableModel(chapterList.getChapterList()));
			}						
		};
		worker.execute();
	}
	
	private void doPobierzMangii() {	
	
		SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
			int pagesCount;
			XStream xStream;
			MangaList mangaList = new MangaList();
			
			@Override
			protected Void doInBackground() throws Exception {					
				//Lista rozwijalna ze wszystkimi stronami z mang¹
				Element pagesCombobox = Util.getDoc("http://www.mangago.me/list/directory/all/1/").select(".pagination li").last();
				Elements pagesOption = pagesCombobox.getElementsByTag("option");
				
				pagesCount = pagesOption.size();
				progressBar.setMaximum(pagesCount - 1);
				
				for(int i = 0; i < (pagesCount - 1); i++) {
					Element pageOption = pagesOption.get(i);
					//System.out.println("Page " + pagesCount + ": " +pageOption.attr("value"));					
					
					Elements mangaLinks = Util.getDoc(pageOption.attr("value")).select(".title a");
					for(Element mangaLink : mangaLinks) {
						Manga manga = new Manga();
						manga.setName(mangaLink.text());
						manga.setLink(mangaLink.attr("href"));
						
						mangaList.add(manga);
						
						//System.out.println("Manga name: " + mangaLink.text() + ", link:" + mangaLink.attr("href") + "/n");
					}
					publish(i);
				}
				
				return null;
			}

			@Override
			protected void process(List<Integer> chunks) {
				int lastDownloadedPageNr = chunks.get(chunks.size() - 1);
				Util.setProgressTextValue(progressBar, 
						"(" + lastDownloadedPageNr + "/" + pagesCount + ")", 
						lastDownloadedPageNr);
			}

			@Override
			protected void done() {
				Util.clearProgress(progressBar);
				
				xStream = new XStream();
				try {
					xStream.toXML(mangaList, new FileWriter(mangaListXMLFile));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				DefaultListModel<MangaDownloaderElement> mangaListModel = new DefaultListModel<>();
				for(Manga manga : mangaList.getMangaList()) {
					mangaListModel.addElement(manga);
				}
				fillMangaList();
			}		
			
			
		};
		worker.execute();
	}
	
	private void initComponents() {
		pnlGlowny = new JPanel();
		pnlGlowny.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(pnlGlowny);
		pnlGlowny.setLayout(null);
		
		scrlPnlSite = new JScrollPane();
		scrlPnlSite.setBounds(10, 45, 132, 181);
		pnlGlowny.add(scrlPnlSite);
		
		listSite = new JList();
		listSite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pickSite(e);
			}
		});
		listSite.setFont(new Font("Tahoma", Font.PLAIN, 10));
		listSite.setCellRenderer(new MangaDownloaderListCellRenderer());
		scrlPnlSite.setViewportView(listSite);
		
		btnPobierzMangii = new JButton("Pobierz mangii");
		btnPobierzMangii.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnPobierzMangii.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doPobierzMangii();
			}
		});
		btnPobierzMangii.setBounds(152, 11, 200, 23);
		pnlGlowny.add(btnPobierzMangii);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(0, 237, 784, 25);
		pnlGlowny.add(progressBar);
		
		btnDodajStrone = new JButton("Dodaj now\u0105 stron\u0119");
		btnDodajStrone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "To ci¹gle wersja beta ;)");
			}
		});
		btnDodajStrone.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnDodajStrone.setBounds(10, 11, 132, 23);
		pnlGlowny.add(btnDodajStrone);

		scrlPnlManga = new JScrollPane();
		scrlPnlManga.setBounds(152, 45, 200, 150);
		pnlGlowny.add(scrlPnlManga);
		
		tabManga = new JTable();
		tabManga.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					doPickManga();	
				}
			}
		});
		tabManga.setFont(new Font("Tahoma", Font.PLAIN, 10));
		tabManga.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tabManga.setFillsViewportHeight(true);
		tabManga.setTableHeader(null);
		tabManga.setDefaultRenderer(Manga.class, new MangaDownloaderTableCellRenderer());
		tabManga.setShowGrid(false);
		
		scrlPnlManga.setViewportView(tabManga);
		
		txtFilterManga = new JTextField();
		txtFilterManga.setBounds(152, 206, 200, 20);
		pnlGlowny.add(txtFilterManga);
		txtFilterManga.setColumns(10);
		
		scrPnlChapter = new JScrollPane();
		scrPnlChapter.setBounds(362, 45, 200, 181);
		pnlGlowny.add(scrPnlChapter);
		
		tabChapter = new JTable();
		tabChapter.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) {
					doPickChapter();
				}
			}
		});
		tabChapter.setFont(new Font("Tahoma", Font.PLAIN, 10));
		tabChapter.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tabChapter.setFillsViewportHeight(true);
		tabChapter.setTableHeader(null);
		tabChapter.setDefaultRenderer(Chapter.class, new MangaDownloaderTableCellRenderer());
		tabChapter.setShowGrid(false);
		scrPnlChapter.setViewportView(tabChapter);
	}

	public void fillMangaList () {
		XStream xStream = new XStream();
		MangaList mangaList;
		
		Util.setProgressText(progressBar, "Ustawianie listy mang.");
		
		try {
			mangaList = (MangaList)xStream.fromXML(new FileReader(mangaListXMLFile));
		} catch (FileNotFoundException e) {
			//File with mangaList wasn't found, it's OK.
			return;
		}
		
		tabManga.setModel(new MangaTableModel(mangaList.getMangaList()));
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			DefaultListModel<MangaDownloaderElement> mangaListModel;

			@Override
			protected Void doInBackground() throws Exception {
				mangaListModel = new DefaultListModel<>();
				for(Manga manga : mangaList.getMangaList()) {
					mangaListModel.addElement(manga);
				}
				return null;
			}

			@Override
			protected void done() {
				Util.clearProgress(progressBar);
			}					
		};
		worker.execute();
	}
	
	
	public void initSites() {
		XStream xStream = new XStream();
		final SiteList siteList;
		
		enabelAllControls(false);
		Util.setProgressText(progressBar, "Ustawianie listy stron.");
		try {
			siteList = (SiteList)xStream.fromXML(new FileReader(siteListXMLFile));
		} catch (FileNotFoundException e) {
			Util.setProgressText(progressBar, "Brak pliku: " + siteListXMLFile + "!");
			return;
		}
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			DefaultListModel<MangaDownloaderElement> siteListModel;

			@Override
			protected Void doInBackground() throws Exception {
				siteListModel = new DefaultListModel<>();
				for(Site site : siteList.getSiteList()) {
					siteListModel.addElement(site);
				}
				return null;
			}

			@Override
			protected void done() {
				listSite.setModel(siteListModel);
				Util.clearProgress(progressBar);
				enabelAllControls(true);
			}					
		};
		worker.execute();
	}
	
	public MangaDownloader() {		
		initComponents();	
		initSites();
//		
//		mangaAddress = new JTextField();
//		mangaAddress.setText("http://www.mangago.me/read-manga/royal_servant/");
//		mangaAddress.setBounds(89, 11, 233, 20);
//		contentPane.add(mangaAddress);
//		mangaAddress.setColumns(10);
//		
//		JLabel lblAdresMangi = new JLabel("Adres mangi:");
//		lblAdresMangi.setBounds(10, 14, 69, 14);
//		contentPane.add(lblAdresMangi);
//		
//		JScrollPane scrollPane = new JScrollPane();
//		scrollPane.setBounds(209, 42, 132, 165);
//		contentPane.add(scrollPane);		
//		
//		JPanel panel = new JPanel();
//		panel.setBounds(0, 232, 434, 30);
//		contentPane.add(panel);
//		panel.setLayout(null);
//		
//		JLabel lblMessage = new JLabel("");
//		lblMessage.setBounds(10, 9, 414, 12);
//		panel.add(lblMessage);
//		
//		JList list = new JList();
//		list.setFont(UIManager.getFont("TextField.font"));		
//		list.setCellRenderer(new MangaDownloaderListCellRenderer());
//		
//		list.addMouseListener(new MouseAdapter() {
//		    public void mouseClicked(MouseEvent evt) {
//		        JList list = (JList)evt.getSource();
//		        System.out.println(evt.getClickCount());
//		        if (evt.getClickCount() == 2) {	
//		        	Util.changeLabel(lblMessage, Color.BLUE, "Proszê czekaæ trwa pobieranie.");					
//					Chapter chapter = (Chapter)list.getSelectedValue();
//					boolean isFirstPage = true;
//					for(Page page: chapter.getPages()) {
//						String mangaName = mangaAddress.getText().split("/")[4];
//						mangaName = mangaName.replaceAll("_", " ");
//						String path = mangaName + "/" + chapter.getName();
//						path = path.replaceAll("[^a-zA-Z0-9- ]", "");
//						path = StringUtils.capitalize(path);
//						//Pierwsza strona
//						if (isFirstPage) {
//							//Nie utworzono folderu bo ju¿ istnieje
//							if (Util.createFolders(path) == false) {
//								lblMessage.setForeground(Color.RED);
//								lblMessage.setText("Pobrano ju¿ ten chapter.");
//								return;
//							}
//							isFirstPage = false;
//						}
//						String fileExtansion = page.getImageUrl().substring(page.getImageUrl().lastIndexOf("."));
//						String fileName = page.getPageNr() + fileExtansion;
//						Thread thread = new Thread(new SavingFile(path, fileName, page.getImageUrl()));
//						thread.start();
//						//Util.saveFile(path, fileName, page.getImageUrl());
//					}
//					Util.changeLabel(lblMessage, new Color(45, 172, 70), "Pobrano!");
//		        }
//		    }
//		});
//		
//		scrollPane.setViewportView(list);
//		
//		JButton btnPobierz = new JButton("Pobierz");
//		btnPobierz.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				Util.changeLabel(lblMessage, Color.BLUE, "Pobieram chaptery");
//				test();
//				setGui(lblMessage, list);			
//			} 
//		});
//		btnPobierz.setBounds(344, 10, 80, 23);
//		contentPane.add(btnPobierz);
	}
	
	public void test(String link) {
//		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
//			
//			@Override
//			protected Void doInBackground() throws Exception {
//				
//				Elements chapterLinks = Util.getDoc(link).select("#chapter_table a");
//				for(Element chapterLink: chapterLinks) {
//					chapters.add(new Chapter(chapterLink.text(), chapterLink.attr("href")));
//				}
//				
//				
//				return null;
//			}
//		};
	}
	
	public void setGui(JLabel lbl, JList list) {
//		
//		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {			
//			@Override
//			protected Void doInBackground() throws Exception {
//				return null;
//			}
//			
//			@Override
//			protected void done() {
//				MangaGo mangaGo = new MangaGo();
//				Manga manga = mangaGo.getManga(mangaAddress.getText());
//				manga.downloadChapters(lbl, list);
//			}
//		};
//		worker.execute();			
	}
}
