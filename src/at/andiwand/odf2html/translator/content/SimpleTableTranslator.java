package at.andiwand.odf2html.translator.content;

import java.io.IOException;
import java.io.Writer;

import at.andiwand.odf2html.translator.StyleScriptUtil;
import at.andiwand.odf2html.translator.context.TranslationContext;

public class SimpleTableTranslator extends SimpleTableElementTranslator {

    public SimpleTableTranslator() {
	super("table");

	addNewAttribute("border", "0");
	addNewAttribute("cellspacing", "0");
	addNewAttribute("cellpadding", "0");
    }

    @Override
    public void generateStyle(Writer out, TranslationContext context)
	    throws IOException {
	StyleScriptUtil
		.pipeStyleResource(SpreadsheetTableTranslator.class, out);
    }

}