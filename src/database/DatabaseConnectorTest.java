package database;

import java.util.Scanner;

import org.junit.Test;

public class DatabaseConnectorTest {

	@SuppressWarnings("resource")
	@Test
	public void test() {
		String string = "line 1 \nline 2";
		Scanner scanner = new Scanner(string);
		String ret = scanner.nextLine() + "\n" + scanner.nextLine();
		System.out.println(ret);
	}

}
