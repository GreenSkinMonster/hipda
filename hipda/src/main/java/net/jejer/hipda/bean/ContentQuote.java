package net.jejer.hipda.bean;

public class ContentQuote extends ContentAbs {
	private String mQuote;

	public ContentQuote(String text) {
		mQuote = text;
	}

	@Override
	public String getContent() {
		return "『" + mQuote + "』";
	}

	@Override
	public String getCopyText() {
		return "『" + mQuote + "』";
	}

}
