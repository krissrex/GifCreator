package com.polarbirds.gifcreator.javafx;

import java.io.File;

import javafx.scene.control.ListCell;

public class FileNameCell extends ListCell<File> {

	public FileNameCell() {}
	
	@Override
	protected void updateItem(File item, boolean empty) {
		super.updateItem(item, empty); //Important. Don't skip this.
		
		if (item == null) {
			setText("");
		} else {
			setText(item.getName());
		}
	}
}
