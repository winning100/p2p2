import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class Translator {
	String DEFAULT_CONFIG_FILE = "config.txt";
	File config;
	public Translator(String config_file) {
		config = new File(config_file);
	}
	public Translator() {
		config = new File(DEFAULT_CONFIG_FILE);
	}
	public String getAddress(int id) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(this.config));
		String line = br.readLine();
		while(line != null) {
			String[] elem = line.split(" ");
			if (Integer.parseInt(elem[0].trim()) == id)
			{
				br.close();
				return elem[1];
			}
			line = br.readLine();
		}
		br.close();
		return null;
	}
}
