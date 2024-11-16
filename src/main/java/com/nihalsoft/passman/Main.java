package com.nihalsoft.passman;

import com.nihalsoft.passman.data.DataStore;
import com.nihalsoft.passman.task.Task;
import com.nihalsoft.passman.task.TaskFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {

	private Map<String, Object> commands;

	public Main() {
		commands = new HashMap<>();
		for (Command cmd : Command.values()) {
			commands.put(cmd.toString(), null);
		}
	}

	public void run() {

		System.out.println();
		System.out.println("   Password Manager - v1.0");
		System.out.println();
		String fileName = "";
		while (true) {
			try {
				fileName = DataStore.getInstance().getFileName();
				String prompt = System.console().readLine("   PassMan %s> ", fileName == null ? "" : "> " +
						fileName.substring(0, Math.min(fileName.length(), 10)) + " ");
				String[] params = prompt.split(" ");
				if (params.length == 0) {
					continue;
				}

				if (!commands.containsKey(params[0].toUpperCase())) {
					System.out.println();
					throw new Exception("Unknown command: " + params[0]);
				}

				Command cmd = Command.valueOf(params[0].toUpperCase());

				if (cmd.equals(Command.EXIT)) {
					break;
				} else if (cmd.equals(Command.CLEAR)) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					continue;
				}

				System.out.println();
				Task task = TaskFactory.get(params[0]);
				assert task != null;
				task.exec(cmd, Arrays.copyOfRange(params, 1, params.length));

			} catch (Exception e) {
				Util.println("Error: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
			}
			System.out.println();
		}
	}

	public static void main(String[] args) {
		var main = new Main();
		main.run();
	}
}
