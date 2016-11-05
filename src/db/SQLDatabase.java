package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * Database object containing the SQL database connection and information regarding how to interact with it
 * @author Nick Schillaci, Brandon Dixon
 *
 */
public class SQLDatabase {

	private String databaseFileName;
	private Connection c = null;
	private Statement stmt = null;
	
	/**
	 * Create SQLDatabase object and initialize connection to the database file
	 */
	public SQLDatabase() {
		databaseFileName = "database.db"; //will be serialized and saved when we allow changing the database name
	    this.initConnection();
	}
	
	/**
	 * Initialize connection to the SQL Database
	 */
	public void initConnection() {
		try {
    		Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + databaseFileName);
			c.setAutoCommit(false);
			this.initTable();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes connection to the SQLDatabase
	 */
	public void closeConnection() {
		try {
	    	c.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * Ensures that the database file has the proper SQL table for storing terms.
	 * If a file is not empty, but the table is invalid or missing, the database is considered corrupted and should be removed for the program to recreate it 
	 * @throws SQLException
	 */
	public void initTable() throws SQLException {
		try {
			BufferedReader br = new BufferedReader(new FileReader(databaseFileName));
			if (br.readLine() == null) {
				stmt = c.createStatement();
		    	String sql = "CREATE TABLE TERMS " +
		    				 "(TERM TEXT, " +
		    				 "SCORE INTEGER NOT NULL," +
		    				 "FREQUENCY INTEGER NOT NULL," +
		    				 "AVGPROB REAL NOT NULL)";
		    	stmt.executeUpdate(sql);
		    	stmt.close();
				c.commit();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Performs a SQL INSERT operation on the database
	 * @param String term to insert
	 * @param int score to assign to the term
	 * @throws SQLException
	 */
	public void addTerm(int term, int score) throws SQLException {
    	stmt = c.createStatement();
    	String sql = "INSERT INTO TERMS (TERM,SCORE,FREQUENCY,AVGPROB) " +
    				 "VALUES (\'" + term + "\', " + score + ", " + 0 + ", " + 0 + ");";
    	stmt.executeUpdate(sql);
    	stmt.close();
		c.commit();
	}
	
	/**
	 * Performs a SQL DELETE operation on the database
	 * @param String term to remove
	 * @throws SQLException
	 */
	public void removeTerm(int term) throws SQLException {
		stmt = c.createStatement();
		String sql = "DELETE FROM TERMS WHERE TERM=\'" + term + "\';";
		stmt.executeUpdate(sql);
		stmt.close();
		c.commit();
	}
	
	/**
	 * Performs a SQL DELETE operation on the database (removes all terms). Maintains the table and columns
	 * (SQLite doesn't support DELETE * or TRUNCATE)
	 * @throws SQLException
	 */
	public void removeAll() throws SQLException {
		stmt = c.createStatement();
		String sql = "DELETE FROM TERMS;";
		stmt.executeUpdate(sql);
		stmt.close();
		c.commit();
	}
	
	/**
	 * Performs a SQL UPDATE operation on the database to change a term's score
	 * @param String term to change score of
	 * @param int score to change previous score to
	 * @throws SQLException
	 */
	public void changeScore(int term, int score) throws SQLException {
		stmt = c.createStatement();
		String sql = "UPDATE TERMS SET SCORE = " + score + " WHERE TERM='" + term + "';";
		stmt.executeUpdate(sql);
		stmt.close();
		c.commit();
	}
	
	/**
	 * Performs a SQL SELECT operation to query the database for all terms and scores
	 * @return HashMap<%TERM%,%SCORE%> of all terms in the database
	 * @throws SQLException 
	 */
	public HashMap<Integer,Integer> getTerms() throws SQLException {
		HashMap<Integer,Integer> terms = new HashMap<Integer,Integer>();
		stmt = c.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM TERMS;");
		while(rs.next()) { //loop through entries in the database
			terms.put(rs.getInt("TERM"), rs.getInt("SCORE"));
		}
		rs.close();
		stmt.close();
		c.commit();
		return terms;
	}
	
	/**
	 * Get the frequency of a specified term
	 * @param term
	 * @return frequency of the term
	 * @throws SQLException
	 */
	public int getFrequency(int term) throws SQLException {
		int freq = 0;
		stmt = c.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM TERMS WHERE TERM='" + term + "';");
		while(rs.next()) {
			freq = rs.getInt("FREQUENCY");
		}
		rs.close();
		stmt.close();
		c.commit();
		return freq;
	}
	
	/**
	 * Increment the frequency of a term to report that an instance of it has been found
	 * @param int term to increment frequency of
	 * @throws SQLException
	 */
	public void incrementFrequency(int term) throws SQLException {
		int freq = this.getFrequency(term);
		stmt = c.createStatement();
		String sql = "UPDATE TERMS SET FREQUENCY = " + (++freq) + " WHERE TERM='" + term + "';";
		stmt.executeUpdate(sql);
		stmt.close();
		c.commit();
	}
	
	/**
	 * Get the average probability of a specified term
	 * @param term to get probability of
	 * @return probability of the term
	 * @throws SQLException
	 */
	public double getAverageProbability(int term) throws SQLException {
		double prob = 0;
		stmt = c.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM TERMS WHERE TERM='" + term + "';");
		while(rs.next()) {
			prob = rs.getDouble("AVGPROB");
		}
		rs.close();
		stmt.close();
		c.commit();
		return prob;
	}
	
	/**
	 * Set the new average probability of a specified term after calculation
	 * @param term to set probability for
	 * @param prob new average probability to set for the term
	 * @throws SQLException
	 */
	public void setAverageProbability(int term, double prob) throws SQLException {
		stmt = c.createStatement();
		String sql = "UPDATE TERMS SET AVGPROB = " + prob + " WHERE TERM='" + term + "';";
		stmt.executeUpdate(sql);
		stmt.close();
		c.commit();
	}
	
	/**
	 * @return the file name of the database
	 */
	public String getDatabaseFileName() {
		return databaseFileName;
	}
	
	/**
	 * Set the file name of the database and re-initialize connection to it
	 * @param String filename of the new SQLite database
	 */
	public void setDatabaseFileName(String filename) {
		databaseFileName = filename;
		this.initConnection();
	}
	
}
