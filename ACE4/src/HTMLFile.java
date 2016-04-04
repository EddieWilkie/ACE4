import java.io.File;

public class HTMLFile {
	private File html;

	public HTMLFile(File html) {
		this.html = html;
	}

	public String getFileName() {
		return html.getName();
	}

	public File getFile() {
		return html;
	}

}
