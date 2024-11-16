package com.nihalsoft.passman.task;

import com.nihalsoft.passman.Command;

public interface Task {

	void exec(Command command, String[] args) throws Exception;

}
