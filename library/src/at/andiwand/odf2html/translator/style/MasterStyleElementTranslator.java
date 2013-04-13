package at.andiwand.odf2html.translator.style;

import java.util.LinkedHashSet;
import java.util.Set;

import at.andiwand.commons.util.array.ArrayUtil;


public class MasterStyleElementTranslator extends DefaultStyleElementTranslator {
	
	private static final String PAGE_LAYOUT_NAME = "style:page-layout-name";
	
	private static final Set<String> DEFAULT_PARENT_ATTRIBUTES = ArrayUtil
			.toCollection(new LinkedHashSet<String>(1), PAGE_LAYOUT_NAME);
	
	public MasterStyleElementTranslator() {
		super(DEFAULT_PARENT_ATTRIBUTES);
	}
	
}