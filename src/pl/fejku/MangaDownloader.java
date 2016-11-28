package pl.fejku;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
	
	private MangaTableModel mangaTableModel;
	private TableRowSorter sorter;
	
	private void enabelAllControls(boolean enable) {
		listSite.setEnabled(enable);
		btnDodajStrone.setEnabled(enable);
		btnPobierzMangii.setEnabled(enable && !listSite.isSelectionEmpty());
		tabManga.setEnabled(enable);
	}
	
	private void pickSite(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (!listSite.isSelectionEmpty()) {
				btnPobierzMangii.setEnabled(true);
			}
			fillMangaList();
		}
	}
	
	private void newMangaTabFilter()
	{	    
	    sorter.setRowFilter(new RowFilter<TableModel,Integer>() {
		    @Override
		    public boolean include(RowFilter.Entry<? extends TableModel,? extends Integer> row) {
		        Manga manga = (Manga)row.getValue(0);
		        return manga.getName().toLowerCase().startsWith(txtFilterManga.getText().toLowerCase());
		    }
		});
	}
	
	
	private void doPickChapter(int row, int column) {
		
		SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
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
		Manga manga = (Manga)tabManga.getValueAt(tabManga.getSelectedRow(), 0);
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
		int row = tabManga.getSelectedRow();
		int column = tabManga.getSelectedColumn();
		
//		tabManga.getModel().setValueAt("<font color=#54af54><b>OK</b></font> ", 
//				row, column);
		
		btnPobierzMangii.setEnabled(false);
		
		SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
			int pagesCount;
			int downloadedPageAmount;
			XStream xStream;
			MangaList mangaList = new MangaList();
			
			@Override
			protected Void doInBackground() throws Exception {					
				//Lista rozwijalna ze wszystkimi stronami z mang¹
				Element pagesCombobox = Util.getDoc("http://www.mangago.me/list/directory/all/1/").select(".pagination li").last();
				Elements pagesOption = pagesCombobox.getElementsByTag("option");
				
				pagesCount = pagesOption.size();
				progressBar.setMaximum(pagesCount - 1);
				
				for(Element pageOption : pagesOption) {
					SwingWorker<Void, String> insidePageWorker = new SwingWorker<Void, String>() {
						
						@Override
						protected Void doInBackground() throws Exception {
							Elements mangaLinks = Util.getDoc(pageOption.attr("value")).select(".title a");
							for(Element mangaLink : mangaLinks) {
								Manga manga = new Manga();
								manga.setName(mangaLink.text());
								manga.setLink(mangaLink.attr("href"));
								
								mangaList.add(manga);								
							}						
							publish("");
							return null;
						}

						@Override
						protected void process(List<String> chunks) {
							downloadedPageAmount += chunks.size();
							System.out.println(downloadedPageAmount+"/"+pagesCount);
						}

						@Override
						protected void done() {
							if (downloadedPageAmount == pagesCount) {
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
						}
					};	
					insidePageWorker.execute();
				}				
				return null;
			}	
		};
		worker.execute();
	}
	
	public void fillMangaList () {
		XStream xStream = new XStream();
		MangaList mangaList;
		
		Util.setProgressText(progressBar, "Ustawianie listy mang.");
		
		try {
			mangaList = (MangaList)xStream.fromXML(new FileReader(mangaListXMLFile));
		} catch (FileNotFoundException e) {
			//PRZENIESC to z catch do if-a
			doPobierzMangii();
			return;
		}
		
		mangaTableModel.setMangaList(mangaList.getMangaList());
		
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
				btnPobierzMangii.setEnabled(true);
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
	
	private void initComponents() {
		pnlGlowny = new JPanel() 
			{
				final Image image = new ImageIcon("./images/butterflies.jpg").getImage();
				@Override
				  protected void paintComponent(Graphics g) {

				    super.paintComponent(g);
				        g.drawImage(image, 0, 0, null);
				}
		};
		pnlGlowny.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(pnlGlowny);
		pnlGlowny.setLayout(null);
		
		scrlPnlSite = new JScrollPane();
		scrlPnlSite.setBounds(10, 45, 132, 481);
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
		btnPobierzMangii.setContentAreaFilled(false);
		btnPobierzMangii.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnPobierzMangii.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doPobierzMangii();
			}
		});
		btnPobierzMangii.setBounds(152, 11, 300, 23);
		pnlGlowny.add(btnPobierzMangii);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(0, 537, 784, 25);
		pnlGlowny.add(progressBar);
		
		btnDodajStrone = new JButton("Dodaj now\u0105 stron\u0119");
		btnDodajStrone.setContentAreaFilled(false);
		btnDodajStrone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "To ci¹gle wersja beta ;)");
			}
		});
		btnDodajStrone.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnDodajStrone.setBounds(10, 11, 132, 23);
		pnlGlowny.add(btnDodajStrone);

		scrlPnlManga = new JScrollPane();
		scrlPnlManga.setBounds(152, 45, 300, 450);
		scrlPnlManga.setOpaque(false);
		scrlPnlManga.getViewport().setOpaque(false);
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
		tabManga.setDefaultRenderer(Manga.class, new MangaDownloaderTableCellRenderer() 
			{{ setOpaque(false); }}
		);
		tabManga.setShowGrid(false);		
		mangaTableModel = new MangaTableModel();
		tabManga.setModel(mangaTableModel);
		sorter = new TableRowSorter(mangaTableModel);
		tabManga.setRowSorter(sorter);
	    tabManga.setOpaque(false);	
	    scrlPnlManga.setViewportView(tabManga);
	    
	    
		
		txtFilterManga = new JTextField();
		txtFilterManga.getDocument().addDocumentListener(
				   new DocumentListener()
				   {
					@Override
					public void insertUpdate(DocumentEvent e) {
						newMangaTabFilter();	
					}
					@Override
					public void removeUpdate(DocumentEvent e) {
						newMangaTabFilter();
					}
					@Override
					public void changedUpdate(DocumentEvent e) {
						newMangaTabFilter();						
					}
				   }
				);
		txtFilterManga.setBounds(152, 506, 300, 20);
		pnlGlowny.add(txtFilterManga);
		txtFilterManga.setColumns(10);
		
		scrPnlChapter = new JScrollPane();
		scrPnlChapter.setBounds(461, 45, 313, 481);
		scrPnlChapter.setOpaque(false);
		scrPnlChapter.getViewport().setOpaque(false);
		pnlGlowny.add(scrPnlChapter);
		
		tabChapter = new JTable();
		tabChapter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					for(int selectedChapterNr : tabChapter.getSelectedRows()){
						SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

							@Override
							protected Void doInBackground() throws Exception {
								doPickChapter(selectedChapterNr, 0);
								return null;
							}
							
						};
						worker.execute();
					}
					
				}
			}
		});
		tabChapter.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) {
					doPickChapter(tabChapter.getSelectedRow(), tabChapter.getSelectedColumn());
				}
			}
		});
		tabChapter.setFont(new Font("Tahoma", Font.PLAIN, 10));
		tabChapter.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tabChapter.setFillsViewportHeight(true);
		tabChapter.setTableHeader(null);
		tabChapter.setDefaultRenderer(Chapter.class, new MangaDownloaderTableCellRenderer()
			{{ setOpaque(false); }}
		);
		tabChapter.setShowGrid(false);
		tabChapter.setOpaque(false);
		scrPnlChapter.setViewportView(tabChapter);
	}
	
	public MangaDownloader() {		
		initComponents();	
		initSites();
	}
}
