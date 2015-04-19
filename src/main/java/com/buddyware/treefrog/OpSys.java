package com.buddyware.treefrog;

public class OpSys {

	private static String mOs = System.getProperty("os.name").toLowerCase();

	public static OsType getType() {

		if (win())
			return OsType.OS_WINDOWS;

		if (osx())
			return OsType.OS_OSX;

		if (lin())
			return OsType.OS_LINUX;

		if (sol())
			return OsType.OS_SOLARIS;

		if (nix())
			return OsType.OS_UNIX;

		return OsType.OS_UNDEFINED;
	}

	public static boolean win() {
		return mOs.indexOf("windows") >= 0;
	}

	public static boolean osx() {
		return mOs.indexOf("mac") >= 0;
	}

	public static boolean lin() {
		return mOs.indexOf("linux") >= 0;
	}

	public static boolean nix() {

		return (mOs.indexOf("nix") >= 0 || mOs.indexOf("nux") >= 0 || mOs
				.indexOf("aix") >= 0);
	}

	public static boolean sol() {
		return mOs.indexOf("sunos") >= 0;
	}
}