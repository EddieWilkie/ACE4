import java.io.File;

public class HTMLFile {
	private File html;

	public HTMLFile(File html) {
		this.html = html;
	}
	//returns the file name of the file
	public String getFileName() {
		return html.getName();
	}
	//returns the html file
	public File getFile() {
		return html;
	}

}
