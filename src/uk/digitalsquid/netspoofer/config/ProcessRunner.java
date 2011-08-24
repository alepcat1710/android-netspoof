/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer - change and mess with webpages and the internet on
 * other people's computers
 * Copyright (C) 2011 Will Shackleton
 *
 * Network Spoofer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Network Spoofer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Network Spoofer, in the file COPYING.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package uk.digitalsquid.netspoofer.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.os.Build;
import android.util.Log;

public final class ProcessRunner implements LogConf {
	private ProcessRunner() {}
	
	public static final int runProcess(String... args) throws IOException {
		return runProcess(null, args);
	}

	public static final int runProcess(Map<String, String> env, String... args) throws IOException {
		Process proc;
		if(Build.VERSION.SDK_INT >= 9) { // 2.2 doesn't like this method
			ProcessBuilder pb = new ProcessBuilder(args);
			if(env != null) pb.environment().putAll(env);
			proc = pb.start();
		} else {
			if(env != null) {
				Map<String, String> systemEnv = System.getenv(); // We also must include this
				Map<String, String> combinedEnv = new HashMap<String, String>();
				combinedEnv.putAll(env);
				combinedEnv.putAll(systemEnv);
				
				String[] envArray = new String[combinedEnv.size()];
				int i = 0;
				for(String key : combinedEnv.keySet()) {
					envArray[i++] = String.format("%s=%s", key, combinedEnv.get(key));
				}
				proc = Runtime.getRuntime().exec(args, envArray);
			} else {
				proc = Runtime.getRuntime().exec(args);
			}
		}
		BufferedReader cout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		BufferedReader cerr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		boolean running = true;
		while(running) {
			while(cout.ready()) {
				String msg = cout.readLine();
				Log.d(TAG, "cout: " + msg);
			}
			while(cerr.ready()) {
				String msg = cerr.readLine();
				Log.d(TAG, "cerr: " + msg);
			}
			try {
				proc.exitValue();
				running = false;
				cout.close();
				cerr.close();
				return proc.exitValue();
			}
			catch (IllegalThreadStateException e) { } // Not done
			try { Thread.sleep(50); } catch (InterruptedException e) { }
		}
		return 0;
	}
}