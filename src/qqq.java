import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.Color;

public class qqq extends JFrame {

	private JPanel contentPane;
	private JTextField mangaAddress;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					qqq frame = new qqq();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public qqq() {
		ImageIcon img = new ImageIcon("op_small.ico");
		setIconImage(img.getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		mangaAddress = new JTextField();
		mangaAddress.setText("http://www.mangago.me/read-manga/royal_servant/");
		mangaAddress.setBounds(89, 11, 233, 20);
		contentPane.add(mangaAddress);
		mangaAddress.setColumns(10);
		
		JLabel lblAdresMangi = new JLabel("Adres mangi:");
		lblAdresMangi.setBounds(10, 14, 69, 14);
		contentPane.add(lblAdresMangi);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(20, 39, 132, 165);
		contentPane.add(scrollPane);		
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 232, 434, 30);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblMessage = new JLabel("");
		lblMessage.setBounds(10, 9, 414, 12);
		panel.add(lblMessage);
		
		JList list = new JList();
		list.setFont(UIManager.getFont("TextField.font"));		
		list.setCellRenderer(new MangaDownloaderListCellRenderer());
		
		list.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        JList list = (JList)evt.getSource();
		        System.out.println(evt.getClickCount());
		        if (evt.getClickCount() == 2) {	
					lblMessage.setForeground(Color.BLUE);
					lblMessage.setText("Proszê czekaæ trwa pobieranie.");
					Chapter chapter = (Chapter)list.getSelectedValue();
					boolean isFirstPage = true;
					for(Page page: chapter.getPages()) {
						String mangaName = mangaAddress.getText().split("/")[4];
						String path = mangaName + "/" + chapter.getName();
						//Pierwsza strona
						if (isFirstPage) {
							//Nie utworzono folderu bo ju¿ istnieje
							if (Util.createFolders(path) == false) {
								lblMessage.setForeground(Color.RED);
								lblMessage.setText("Pobrano ju¿ ten chapter.");
								return;
							}
							isFirstPage = false;
						}
						String fileExtansion = page.getImageUrl().substring(page.getImageUrl().lastIndexOf("."));
						String fileName = page.getPageNr() + fileExtansion;
						Thread thread = new Thread(new SavingFile(path, fileName, page.getImageUrl()));
						thread.start();
						//Util.saveFile(path, fileName, page.getImageUrl());
					}
					lblMessage.setForeground(Color.GREEN);
					lblMessage.setText("Pobrano!");
		        }
		    }
		});
		
		scrollPane.setViewportView(list);
		
		JButton btnPobierz = new JButton("Pobierz");
		btnPobierz.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lblMessage.setForeground(Color.BLUE);
				lblMessage.setText("Pobieram chaptery");
				setGui(lblMessage, list);			
			} 
		});
		btnPobierz.setBounds(344, 10, 80, 23);
		contentPane.add(btnPobierz);
	}
	
	public void setGui(JLabel lbl, JList list) {
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {			
			@Override
			protected Void doInBackground() throws Exception {
				return null;
			}
			
			@Override
			protected void done() {
				MangaGo mangaGo = new MangaGo();
				Manga manga = mangaGo.getManga(mangaAddress.getText());
				manga.downloadChapters(lbl, list);
			}
		};
		worker.execute();			
	};
}
