package pl.fejku.utils;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class DoubleClickMouseAdapter extends MouseAdapter {

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2)
			doubleClick();
	}

	public abstract void doubleClick(); 
}
