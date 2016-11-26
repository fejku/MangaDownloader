package pl.fejku.model;

import pl.fejku.MangaDownloaderElement;

public class Site implements MangaDownloaderElement {
	private String name;
	private String address;
	private String arg;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}
	
}
