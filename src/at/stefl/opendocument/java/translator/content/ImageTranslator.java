package at.stefl.opendocument.java.translator.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import at.stefl.commons.codec.Base64OutputStream;
import at.stefl.commons.codec.Base64Settings;
import at.stefl.commons.io.ByteStreamUtil;
import at.stefl.commons.lwxml.LWXMLUtil;
import at.stefl.commons.lwxml.reader.LWXMLPushbackReader;
import at.stefl.commons.lwxml.writer.LWXMLWriter;
import at.stefl.opendocument.java.translator.context.TranslationContext;
import at.stefl.opendocument.java.util.FileCache;
import at.stefl.svm.tosvg.SVGTranslator;

// TODO: skip empty images
public class ImageTranslator extends
	DefaultElementTranslator<TranslationContext> {

    private static final String PATH_ATTRIBUTE_NAME = "xlink:href";

    private static final String OBJECT_REPLACEMENT_STRING = "ObjectReplacement";

    private static final String ALT_FAILED = "Image not found or unsupported: ";

    private final ByteStreamUtil streamUtil = new ByteStreamUtil();

    public ImageTranslator() {
	super("img");
    }

    @Override
    public void translateAttributeList(LWXMLPushbackReader in, LWXMLWriter out,
	    TranslationContext context) throws IOException {
	String name = LWXMLUtil.parseSingleAttribute(in, PATH_ATTRIBUTE_NAME);
	// TODO: log
	if (name == null)
	    return;
	// TODO: move to OpenDocumentFile and improve
	name = name.replaceAll("\\./", "");

	out.writeAttribute("style", "width: 100%; heigth: 100%");

	out.writeAttribute("alt", ALT_FAILED + name);

	out.writeAttribute("src", "");
	if (context.getDocumentFile().isFile(name)) {
	    writeSource(name, out, context);
	} else {
	    out.write(name);
	}
    }

    @Override
    public void translateChildren(LWXMLPushbackReader in, LWXMLWriter out,
	    TranslationContext context) throws IOException {
	LWXMLUtil.flushBranch(in);

	out.writeEndEmptyElement();
    }

    @Override
    public void translateEndElement(LWXMLPushbackReader in, LWXMLWriter out,
	    TranslationContext context) throws IOException {
	// TODO: log
    }

    private void writeSource(String name, Writer out, TranslationContext context)
	    throws IOException {
	switch (context.getSettings().getImageStoreMode()) {
	case CACHE:
	    writeSourceCached(name, out, context);
	    break;
	case INLINE:
	    writeSourceInline(name, out, context);
	    break;
	default:
	    throw new UnsupportedOperationException();
	}
    }

    private void writeSourceCached(String name, Writer out,
	    TranslationContext context) throws IOException {
	FileCache cache = context.getSettings().getCache();
	String imageName = new File(name).getName();

	// TODO: improve
	if (name.contains(OBJECT_REPLACEMENT_STRING)) {
	    imageName += ".svg";
	}

	if (!cache.exists(imageName)) {
	    File file = cache.create(imageName);
	    InputStream fileIn = context.getDocumentFile().getFileStream(name);
	    OutputStream fileOut = new FileOutputStream(file);

	    try {
		// TODO: improve
		if (name.contains(OBJECT_REPLACEMENT_STRING)) {
		    SVGTranslator.TRANSLATOR.translate(fileIn, fileOut);
		} else {
		    streamUtil.writeStream(fileIn, fileOut);
		}
	    } finally {
		fileOut.close();
		fileIn.close();
	    }
	}

	out.write(cache.getURI(imageName).toString());
    }

    private void writeSourceInline(String name, Writer out,
	    TranslationContext context) throws IOException {
	String mimetype = context.getDocumentFile().getFileMimetype(name);
	// TODO: improve
	if (name.contains(OBJECT_REPLACEMENT_STRING)) {
	    mimetype = "image/svg+xml";
	}
	if (mimetype == null) {
	    // TODO: log
	    // TODO: "null" ?
	    out.write("null");
	    return;
	}

	out.write("data:");
	out.write(mimetype);
	out.write(";base64,");

	InputStream imgIn = context.getDocumentFile().getFileStream(name);
	OutputStream imgOut = new Base64OutputStream(out,
		Base64Settings.ORIGINAL);

	try {
	    // TODO: improve
	    if (name.contains(OBJECT_REPLACEMENT_STRING)) {
		SVGTranslator.TRANSLATOR.translate(imgIn, imgOut);
	    } else {
		streamUtil.writeStream(imgIn, imgOut);
	    }
	} finally {
	    imgIn.close();
	}
    }

}