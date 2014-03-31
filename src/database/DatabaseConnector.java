package database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class DatabaseConnector {
	
	Connection conn1; // An object of type connection
	private static final String SECTION_TEMPLATE_FILE_CONSTANT = "section-template.txt";
	private static final String SECTION_TEMPLATE_DATA_MARKER = "{DATAHERE}";
	private static final String FILE_TYPE_CONSTANT = "UTF-8";
	
	public DatabaseConnector() {
		// Section 1: Load the driver
		try {
			// Load the driver (registers itself)
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception E) {
			System.err.println("Unable to load driver.");
			E.printStackTrace();
		}
		// Section 2. Connect to the database
		String dbUrl = "jdbc:mysql://csdb.cs.iastate.edu:3306/kabernsjDB";
		String user = "kabernsj";
		String password = "kabernsj-84";
		try {
			conn1 = DriverManager.getConnection(dbUrl, user, password);
		} catch (SQLException e) {
			System.err.println("Unable to getConnection");
			e.printStackTrace();
		}
	}
	
	
	public boolean addSection(String referenceNumber, int seatsAvailable) throws SQLException {
		//prepared statement for insert
		PreparedStatement preparedStatement = null;
 
		String insertTableSQL = "INSERT INTO CATALOG"
				+ "(REFERENCE_NUMBER, TOTAL_SEATS, TIMESTAMP) VALUES"
				+ "(?,?,?)";
 
		try {
			preparedStatement = conn1.prepareStatement(insertTableSQL);
 
			preparedStatement.setString(1, referenceNumber);
			preparedStatement.setInt(2, seatsAvailable);
			java.util.Date today = new java.util.Date();
			preparedStatement.setTimestamp(3, new java.sql.Timestamp(today.getTime()));
 
			// execute insert SQL statement
			preparedStatement.executeUpdate();
			return true;
		} catch (SQLException e) {
 
			System.out.println(e.getMessage());
			return false;
 
		} finally {
 
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
		
	}
	
	public boolean deleteSection(String referenceNumber) throws SQLException {
		//prepared statement for deletion
				PreparedStatement preparedStatement = null;
		 
				String insertTableSQL = "DELETE FROM CATALOG"
						+ "WHERE REFERENCE_NUMBER = ?";
		 
				try {
					preparedStatement = conn1.prepareStatement(insertTableSQL);
		 
					preparedStatement.setString(1, referenceNumber);
					java.util.Date today = new java.util.Date();
					preparedStatement.setTimestamp(4, new java.sql.Timestamp(today.getTime()));
		 
					// execute delete SQL statement
					preparedStatement.executeUpdate();
					return true;
				} catch (SQLException e) {
		 
					System.out.println(e.getMessage());
					return false;
		 
				} finally {
		 
					if (preparedStatement != null) {
						preparedStatement.close();
					}
				}
	}
	
	public boolean addSeatData(String referenceNumber, int seatsAvailable) throws SQLException {
		//prepared statement for insert
				PreparedStatement preparedStatement = null;
		 
				String insertTableSQL = "INSERT INTO SEATS"
						+ "(REFERENCE_NUMBER, SEATS_REMAINING, TIMESTAMP) VALUES"
						+ "(?,?,?)";
		 
				try {
					preparedStatement = conn1.prepareStatement(insertTableSQL);
		 
					preparedStatement.setString(1, referenceNumber);
					preparedStatement.setInt(2, seatsAvailable);
					java.util.Date today = new java.util.Date();
					preparedStatement.setTimestamp(3, new java.sql.Timestamp(today.getTime()));
		 
					// execute insert SQL statement
					preparedStatement.executeUpdate();
					updateHTML(referenceNumber);
					return true;
				} catch (SQLException e) {
		 
					System.out.println(e.getMessage());
					return false;
		 
				} finally {
		 
					if (preparedStatement != null) {
						preparedStatement.close();
					}
				}
	}

	
	public boolean updateHTML(String referenceNumber) throws SQLException {
		File file = new File(SECTION_TEMPLATE_FILE_CONSTANT);
		String htmlFile = "";
		String htmlFileName = referenceNumber + ".hmtl";
		Scanner scanner = null;
			try {
				scanner = new Scanner(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			}
			boolean foundOnce = false;
			int mostRecent = 0;
			while(scanner.hasNext()) {
				String next = scanner.nextLine();
				if (next.contains(SECTION_TEMPLATE_DATA_MARKER)) {
					if (!foundOnce) {
						//add data
						PreparedStatement preparedStatement = conn1.prepareStatement("select s.SEATS_REMAINGING, s.TIMESTAMP" + " " + 
                                "from Seats s" + " " + 
                                "where s.REFERENCE_NUMBER = '?' ");
						preparedStatement.setString(1, referenceNumber);
						ResultSet rs1 = preparedStatement.executeQuery();

						//Process the result set 

						int seats;
						htmlFile += "['Date','Seats Remaining'],\n";
						while(rs1.next()) {
						// 	Access and print contents of one tuple
							seats = rs1.getInt ("SEATS_REMAINGING"); // Access by attribute Name
							mostRecent = seats;
							java.sql.Timestamp timestamp = rs1.getTimestamp("TIMESTAMP");
							Date date = new Date(timestamp.getTime());
							DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
							htmlFile += "['" + format.format(date) + "'],['" + seats + "'],\n";
						}
					} else {
						htmlFile += next.replace(SECTION_TEMPLATE_DATA_MARKER, "" + mostRecent) + "\n";
					}
				} else {
					htmlFile += next + "\n";
				}
			}
			scanner.close();
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(htmlFileName, FILE_TYPE_CONSTANT);
			} catch (Exception e) {
				System.out.println("writer create error for server file");
			}
			
			writer.print(htmlFile);
			writer.close();
			return true;
	}
}