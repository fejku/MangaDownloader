import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class SavingFile implements Runnable {

	private String path;
	private String fileName;
	private String imageUrl;
	
	public SavingFile(String path, String fileName, String imageUrl) {
		this.path = path;
		this.fileName = fileName;
		this.imageUrl = imageUrl;
	}
	
	@Override
	public void run() {
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

}
