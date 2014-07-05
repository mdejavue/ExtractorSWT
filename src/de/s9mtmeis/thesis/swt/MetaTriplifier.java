package de.s9mtmeis.thesis.swt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;

public class MetaTriplifier {

	private final static String HOST_P = "<http://s9mtmeis.de/cc/host>";
	private final static String TIME_P = "<http://s9mtmeis.de/cc/time>";
	
	// Gets input and output file and booleans from Checkbox selection
	public static void triplify(String input, String output, boolean time, boolean url) {
		BufferedWriter bw;
		BufferedReader br;
		try {
			// Initialize streams
			br = new BufferedReader(new FileReader(input));
			bw = new BufferedWriter(new FileWriter(output + ".tmp"), 1024*8);
			String line;
			String[] split = null;
			// keep track of all subjects inside one mapper record
			ArrayList<String> subjects = new ArrayList<String>();
			
			// split first line of any new mapper record
			while ((line = br.readLine()) != null) {
				
				if (line.startsWith("NEW_MAPPER_ENTITY")) {
					// clear the list of subjects
					subjects.clear();
					split = line.split("::");
					bw.append("\n");
				}
				else {
					String subject = line.trim().split("\\s+",2)[0];
					// check if subject is unique
					if (subject.length() > 3 && !subjects.contains(subject)) {				
						subjects.add(subject);
						// add triple for url
						if (url)
							bw.append(subject + " " + HOST_P + " \"" + new URI(split[1]).getHost() + "\" .\n");
						// add triple for timestamp
						if (time)
							bw.append(subject + " " + TIME_P + " \"" + split[2] + "\" .\n");
					}
					bw.append(line +"\n");
				}
			}			
			
			br.close();
			bw.close();
		}
		catch (Exception e) { e.printStackTrace(); }
	}
}
