package at.andiwand.odf2html.translator.content;

import at.andiwand.odf2html.translator.context.TextTranslationContext;

public class TextContentTranslator extends
	DefaultTextContentTranslator<TextTranslationContext> {

    public TextContentTranslator() {
	ParagraphTranslator paragraphTranslator = new ParagraphTranslator("p",
		true);
	addElementTranslator("text:p", paragraphTranslator);
	addElementTranslator("text:h", paragraphTranslator);

	BookmarkTranslator bookmarkTranslator = new BookmarkTranslator();
	addElementTranslator(BookmarkTranslator.START, bookmarkTranslator);
	addElementTranslator(BookmarkTranslator.END, bookmarkTranslator);
    }

}