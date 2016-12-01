package pl.fejku;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.thoughtworks.xstream.XStream;

import pl.fejku.model.Chapter;
import pl.fejku.model.Manga;
import pl.fejku.model.MangaList;
import pl.fejku.model.MangaTableModel;
import pl.fejku.model.Site;
import pl.fejku.model.SiteList;
import pl.fejku.model.SiteTableModel;
import pl.fejku.utils.DoubleClickMouseAdapter;
import pl.fejku.utils.Util;



public class MangaDownloader extends JFrame {

	private static final long serialVersionUID = 5991074200560946978L;
	private static final String siteListXMLFile = "SiteList.xml";
	private static final String mangaListXMLFile = "MangaList.xml";
	
	private JPanel pnlGlowny;
	
	private JScrollPane scrlPnlSite;
	private JTable tabSite;
	private JButton btnDodajStrone;
	
	private JButton btnPobierzMangii;
	
	private JScrollPane scrlPnlManga;
	private JTable tabManga;
	private JTextField txtFilterManga;
	
	private JScrollPane scrPnlChapter;
	private JTable tabChapter;
	
	private SiteTableModel siteTableModel;
	private MangaTableModel mangaTableModel;
	private TableRowSorter sorter;
	
	private void enabelAllControls(boolean enable) {
		tabSite.setEnabled(enable);
		btnDodajStrone.setEnabled(enable);
		btnPobierzMangii.setEnabled(enable && (tabSite.getSelectedRow() > -1));
		tabManga.setEnabled(enable);
	}
	
	private void pickSite() {
			if (tabSite.getSelectedRow() > -1) {
				btnPobierzMangii.setEnabled(true);
			}
			fillMangaList();
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
	
	
	private void doPickChapter(int row) {		
		DownloadChapterWorker downloadChapterWorker = new DownloadChapterWorker(tabChapter.getModel());
		downloadChapterWorker.setRow(row);
		downloadChapterWorker.execute();
	}
	
	private void doDownloadSelectedChapters() {
		for(int selectedChapterNr : tabChapter.getSelectedRows()){
			DownloadChaptersWorker downloadChaptersWorker = new DownloadChaptersWorker(selectedChapterNr);
			downloadChaptersWorker.setChapterModel(tabChapter.getModel());
			downloadChaptersWorker.execute();
		}
	}
	
	private void doPickManga() {		
		GetChaptersFromMangaWorker chapterWorker = new GetChaptersFromMangaWorker();
		chapterWorker.setManga((Manga)tabManga.getValueAt(tabManga.getSelectedRow(), 0));
		chapterWorker.setTabChapter(tabChapter);
		chapterWorker.execute();
	}
	
	private void doPobierzMangii(int row) {
		
		btnPobierzMangii.setEnabled(false);

		GetMangaFromSiteWorker mangaWorker = new GetMangaFromSiteWorker();
		mangaWorker.setRow(row);
		mangaWorker.setTabSite(tabSite);
		mangaWorker.setMangaModel(mangaTableModel);
		mangaWorker.setMangaListXMLFile(mangaListXMLFile);
		mangaWorker.execute();
	}
	
	public void fillMangaList () {
		XStream xStream = new XStream();
		MangaList mangaList;
		
		try {
			mangaList = (MangaList)xStream.fromXML(new FileReader(mangaListXMLFile));
		} catch (FileNotFoundException e) {
			//PRZENIESC to z catch do if-a
			doPobierzMangii(tabSite.getSelectedRow());
			return;
		}
		
		mangaTableModel.setMangaList(mangaList.getMangaList());
		
		btnPobierzMangii.setEnabled(true);
	}
	
	
	public void initSites() {
		XStream xStream = new XStream();
		SiteList siteList;
		
		enabelAllControls(false);
		try {
			siteList = (SiteList)xStream.fromXML(new FileReader(siteListXMLFile));
		} catch (FileNotFoundException e) {
			System.out.println("Brak pliku: " + siteListXMLFile + "!");
			return;
		}
		
		siteTableModel.setSiteList(siteList.getSiteList());

		enabelAllControls(true);
	}
	
	private void initComponents() {
		pnlGlowny = new JPanel() {
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
		scrlPnlSite.setOpaque(false);
		scrlPnlSite.getViewport().setOpaque(false);		
		pnlGlowny.add(scrlPnlSite);
		
		tabSite = new JTable();
		tabSite.addMouseListener(new DoubleClickMouseAdapter() {
			@Override
			public void doubleClick() {
				pickSite();
			}
		});
		tabSite.setFont(new Font("Tahoma", Font.PLAIN, 10));
		tabSite.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tabSite.setFillsViewportHeight(true);
		tabSite.setTableHeader(null);
		tabSite.setDefaultRenderer(Site.class, new MangaDownloaderTableCellRenderer());
		tabSite.setShowGrid(false);		
		siteTableModel = new SiteTableModel();
		tabSite.setModel(siteTableModel);
		tabSite.setOpaque(false);	
		scrlPnlSite.setViewportView(tabSite);
		
		btnPobierzMangii = new JButton("Pobierz mangii");
		btnPobierzMangii.setContentAreaFilled(false);
		btnPobierzMangii.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnPobierzMangii.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doPobierzMangii(tabSite.getSelectedRow());
			}
		});
		btnPobierzMangii.setBounds(152, 11, 300, 23);
		pnlGlowny.add(btnPobierzMangii);
		
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
		tabManga.addMouseListener(new DoubleClickMouseAdapter() {
			@Override
			public void doubleClick() {
				doPickManga();
			}
		});
		tabManga.setFont(new Font("Tahoma", Font.PLAIN, 10));
		tabManga.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tabManga.setFillsViewportHeight(true);
		tabManga.setTableHeader(null);
		tabManga.setDefaultRenderer(Manga.class, new MangaDownloaderTableCellRenderer());
		tabManga.setShowGrid(false);		
		mangaTableModel = new MangaTableModel();
		tabManga.setModel(mangaTableModel);
		sorter = new TableRowSorter(mangaTableModel);
		tabManga.setRowSorter(sorter);
	    tabManga.setOpaque(false);	
	    scrlPnlManga.setViewportView(tabManga);    
		
		txtFilterManga = new JTextField();
		txtFilterManga.getDocument().addDocumentListener(new DocumentListener() {
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
		});
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
					doDownloadSelectedChapters();					
				}
			}
		});
		tabChapter.addMouseListener(new DoubleClickMouseAdapter() {			
			@Override
			public void doubleClick() {
				doPickChapter(tabChapter.getSelectedRow());
			}
		});
		tabChapter.setFont(new Font("Tahoma", Font.PLAIN, 10));
		tabChapter.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tabChapter.setFillsViewportHeight(true);
		tabChapter.setTableHeader(null);
		tabChapter.setDefaultRenderer(Chapter.class, new MangaDownloaderTableCellRenderer());
		tabChapter.setShowGrid(false);
		tabChapter.setOpaque(false);
		scrPnlChapter.setViewportView(tabChapter);
	}
	
	public MangaDownloader() {		
		initComponents();	
		initSites();
	}
}
