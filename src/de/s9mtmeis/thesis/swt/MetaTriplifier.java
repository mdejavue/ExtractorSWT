package de.s9mtmeis.thesis.swt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class MetaTriplifier {

	private final static String URL_P = "<http://s9mtdev.de/cc/url>";
	private final static String TIME_P = "<http://s9mtdev.de/cc/time>";
	
	public static void triplify(String input, String output, boolean time, boolean url) {
		BufferedWriter bw;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(input));
			bw = new BufferedWriter(new FileWriter(output + ".tmp"), 1024*8);
			String line;
			String[] split = null;
			ArrayList<String> subjects = new ArrayList<String>();
			
			
			while ((line = br.readLine()) != null) {
				
				if (line.startsWith("NEW_MAPPER_ENTITY")) {
					subjects.clear();
					split = line.split("::");
					bw.append("\n");
				}
				else {
					String subject = line.trim().split("\\s+",2)[0];
					if (subject.length() > 3 && !subjects.contains(subject)) {				
						subjects.add(subject);
						if (url)
							bw.append(subject + " " + URL_P + " \"" + split[1] + "\" .\n");
						
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
