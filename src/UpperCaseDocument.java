import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class UpperCaseDocument extends PlainDocument {

	public void insertString(int offset, String str, AttributeSet attSet) throws BadLocationException {
		str = str.toUpperCase();
		super.insertString(offset, str, attSet);
	}

}
