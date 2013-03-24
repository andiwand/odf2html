package at.andiwand.odf2html.odf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import at.andiwand.commons.io.CharStreamUtil;
import at.andiwand.commons.lwxml.LWXMLEvent;
import at.andiwand.commons.lwxml.reader.LWXMLReader;
import at.andiwand.commons.lwxml.reader.LWXMLStreamReader;
import at.andiwand.commons.util.array.ArrayUtil;


public abstract class OpenDocumentFile {
	
	private static final String MIMETYPE_PATH = "mimetype";
	private static final String MANIFEST_PATH = "META-INF/manifest.xml";
	
	private static final Set<String> UNENCRYPTED_FILES = ArrayUtil
			.toHashSet(new String[] {MIMETYPE_PATH, MANIFEST_PATH});
	
	private String mimetype;
	private Map<String, String> mimetypeMap;
	
	private Map<String, EncryptionParameter> encryptionParameterMap;
	private String password;
	
	public boolean isEncrypted() throws IOException {
		if (encryptionParameterMap == null)
			encryptionParameterMap = Collections
					.unmodifiableMap(EncryptionParameter
							.parseEncryptionParameters(this));
		
		return !encryptionParameterMap.isEmpty();
	}
	
	public boolean isFileEncrypted(String path) throws IOException {
		if (UNENCRYPTED_FILES.contains(path)) return false;
		if (!isEncrypted()) return false;
		
		return encryptionParameterMap.containsKey(path);
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isPasswordValid(String password) throws IOException {
		if (!isEncrypted()) return true;
		
		return OpenDocumentCryptoUtil.validatePassword(password, this);
	}
	
	public EncryptionParameter getEncryptionParameter(String path)
			throws IOException {
		if (!isEncrypted()) return null;
		
		return encryptionParameterMap.get(path);
	}
	
	public Map<String, EncryptionParameter> getEncryptionParameterMap()
			throws IOException {
		if (!isEncrypted()) return null;
		
		return encryptionParameterMap;
	}
	
	public abstract boolean isFile(String name) throws IOException;
	
	public abstract Set<String> getFileNames() throws IOException;
	
	protected abstract InputStream getRawFileStream(String name)
			throws IOException;
	
	public InputStream getFileStream(String path) throws IOException {
		InputStream in = getRawFileStream(path);
		if (!isFileEncrypted(path)) return in;
		
		if (password == null)
			throw new NullPointerException("password cannot be null");
		EncryptionParameter encryptionParameter = getEncryptionParameter(path);
		in = OpenDocumentCryptoUtil.getPlainInputStream(in,
				encryptionParameter, password);
		in = new InflaterInputStream(in, new Inflater(true));
		return in;
	}
	
	public String getFileMimetype(String path) throws IOException {
		if (mimetypeMap == null) mimetypeMap = getFileMimetypeImpl();
		
		return mimetypeMap.get(path);
	}
	
	public Map<String, String> getFileMimetypeImpl() throws IOException {
		Map<String, String> result = new HashMap<String, String>();
		
		LWXMLReader in = new LWXMLStreamReader(getManifest());
		
		String mimetype = null;
		String path = null;
		
		while (true) {
			LWXMLEvent event = in.readEvent();
			if (event == LWXMLEvent.END_DOCUMENT) break;
			
			switch (event) {
			case ATTRIBUTE_NAME:
				String attributeName = in.readValue();
				
				if (attributeName.equals("manifest:media-type")) {
					mimetype = in.readFollowingValue();
				} else if (attributeName.equals("manifest:full-path")) {
					path = in.readFollowingValue();
				}
				
				break;
			case END_ATTRIBUTE_LIST:
				if (mimetype != null) result.put(path, mimetype);
				
				mimetype = null;
				path = null;
				break;
			default:
				break;
			}
		}
		
		in.close();
		
		return result;
	}
	
	public String getMimetype() throws IOException {
		if (mimetype == null) mimetype = getMimetypeImpl();
		
		return mimetype;
	}
	
	private String getMimetypeImpl() throws IOException {
		if (isFile(MIMETYPE_PATH)) {
			InputStream in = getRawFileStream(MIMETYPE_PATH);
			return CharStreamUtil.readAsString(new InputStreamReader(in));
		} else {
			return getFileMimetype("/");
		}
	}
	
	public InputStream getManifest() throws IOException {
		return getRawFileStream(MANIFEST_PATH);
	}
	
	public OpenDocument getAsOpenDocument() throws IOException {
		String mimetype = getMimetype();
		
		if (OpenDocumentText.checkMimetype(mimetype)) {
			return getAsOpenDocumentText();
		} else if (OpenDocumentSpreadsheet.checkMimetype(mimetype)) {
			return getAsOpenDocumentSpreadsheet();
		} else if (OpenDocumentPresentation.checkMimetype(mimetype)) {
			return getAsOpenDocumentPresentation();
		}
		
		throw new IllegalMimeTypeException(mimetype);
	}
	
	public OpenDocumentText getAsOpenDocumentText() throws IOException {
		return new OpenDocumentText(this);
	}
	
	public OpenDocumentSpreadsheet getAsOpenDocumentSpreadsheet()
			throws IOException {
		return new OpenDocumentSpreadsheet(this);
	}
	
	public OpenDocumentPresentation getAsOpenDocumentPresentation()
			throws IOException {
		return new OpenDocumentPresentation(this);
	}
	
}