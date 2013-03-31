package at.andiwand.odf2html.odf;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import at.andiwand.commons.lwxml.LWXMLUtil;


public class OpenDocumentPresentation extends OpenDocument {
	
	public static final String MIMETYPE = "application/vnd.oasis.opendocument.presentation";
	
	private static final String PAGE_NAME_ATTRIBUTE = "draw:name";
	
	public static boolean checkMimetype(String mimetype) {
		return mimetype.startsWith(MIMETYPE);
	}
	
	private int pageCount = -1;
	private List<String> pageNames;
	
	public OpenDocumentPresentation(OpenDocumentFile openDocumentFile)
			throws IOException {
		super(openDocumentFile);
	}
	
	public int getPageCount() throws IOException {
		if (pageCount == -1) getPageNames();
		
		return pageCount;
	}
	
	// TODO: improve with path/query (0.00000000001% necessary)
	public List<String> getPageNames() throws IOException {
		if (pageNames == null) {
			pageNames = Collections.unmodifiableList(LWXMLUtil
					.getAllAttributeValue(getContent(), PAGE_NAME_ATTRIBUTE));
			
			if (pageCount == -1) pageCount = pageNames.size();
		}
		
		return pageNames;
	}
	
	@Override
	protected boolean isMimetypeValid(String mimetype) {
		return checkMimetype(mimetype);
	}
	
}