package hr.fer.su.mgc.matlab;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class MatlabEngineWin {

	public static String runScript(File scriptName) {
		String output = "", error = "";
		try {
			String commandToRun = "matlab -nodisplay < " + scriptName;
			System.out.println(commandToRun);
			Process p = Runtime.getRuntime().exec(commandToRun);

			String s;

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			// read the output from the command
			System.out
					.println("\nHere is the standard output of the command:\n");
			while ((s = stdInput.readLine()) != null) {
				output += s + "\n";
				System.out.println(s);
			}

			// read any errors from the attempted command
			System.out
					.println("\nHere is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				error += s + "\n";
				System.out.println(s);
			}

		} catch (Exception e) {
			System.out.println("exception happened – here’s what I know: ");
			e.printStackTrace();
			System.exit(-1);
		}
		return output + error;
	}
}
