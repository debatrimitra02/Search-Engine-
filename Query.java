import java.util.Scanner;
import java.sql.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.Comparator;
import java.util.Collections;

public class Query{
	
	static Scanner scan = new Scanner(System.in);
	static int corpusSize = 78000;
	static Hashtable<String, Double> searchResultHash = new Hashtable<String, Double>();
	static ArrayList<String> searchResultUrl = new ArrayList<String>();
	
	public static void main(String[] args){
		String userQuery = scan.nextLine();
		String[] queryTokens = userQuery.split(" ");
		DB.setup();
		try{
			if(queryTokens.length == 1){
				String word1 = queryTokens[0];
				searchOneWord(word1);
			}
			else{
				String word1 = queryTokens[0];
				String word2 = queryTokens[1];
				searchTwoWords(word1,word2);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		DB.close();
	}
	
	public static void saveInSearchResult(String url, Double tfidf){
		searchResultHash.put(url, tfidf);
		searchResultUrl.add(url);
	}
	
	public static void sortSearchResult(){
		Comparator<String> urlComparator = new Comparator<String>(){
			public int compare(String str1, String str2){
				double a =  searchResultHash.get(str1);
				double b =  searchResultHash.get(str2);
				try{
					if(a < b)
						return 1;
					else if(a == b)
						return 0;
					else
						return -1;
				}
				catch(Exception e){
					return -1;
				}
			}
		};
		Collections.sort(searchResultUrl, urlComparator);
	}
	
	public static void displayResults(){
		System.out.println("Results Found: " + searchResultUrl.size());
		System.out.println();
		if(searchResultUrl.size() > 5){
			for(int i = 0; i < 5; i++){
				String url = searchResultUrl.get(i);
				System.out.println("URL: " + url);
				System.out.println("TF-IDF: " + searchResultHash.get(url));
				System.out.println();
			}
		}
	}
	
	public static void searchOneWord(String word) throws SQLException{
		searchResultUrl.clear();
		searchResultHash.clear();
		System.out.println("Searching for: " + word);
		String sqlQuery;
		sqlQuery = "SELECT *,count(term) as termCount FROM indexer WHERE term = '" + word + "' GROUP BY url";
		//System.out.println(sqlQuery);
		ResultSet result = DB.statement.executeQuery(sqlQuery);
		result.last();
		int urlSize = result.getRow();
		result.first();

		while(result.next()){
			String term = result.getString("term");
			int position = result.getInt("position");
			String url = result.getString("url");
			int wordCount = result.getInt("termCount");
			double tfidf = (1+ Math.log10(wordCount))*(corpusSize/(Math.log10(urlSize)));
			saveInSearchResult(url, tfidf);
		}
		sortSearchResult();
		displayResults();
	}
	
	public static void searchTwoWords(String word1, String word2) throws SQLException{
		System.out.println("Searching for: " + word1 + " " + word2);
		Hashtable<Integer, ArrayList<String>> table1 = new Hashtable<Integer, ArrayList<String>>();
		Hashtable<Integer, ArrayList<String>> table2 = new Hashtable<Integer, ArrayList<String>>();
		String sqlQuery;
		sqlQuery = "SELECT * FROM indexer WHERE term = '" + word1 + "' or term = '" + word2 + "'";
		//System.out.println(sqlQuery);
		ResultSet result = DB.statement.executeQuery(sqlQuery);
		while(result.next()){
			String term = result.getString("term");
			int position = result.getInt("position");
			String url = result.getString("url");
			if(word1.equals(term)){
				table1 = updateTable(table1, term, position, url);
			}
			else if(word2.equals(term)){
				table2 = updateTable(table2, term, position, url);
			}
		}
		
		findTwoGrams(table1,table2);
		displayResults();
		searchOneWord(word1);
		searchOneWord(word2);
	}
	
	public static Hashtable<Integer, ArrayList<String>> updateTable(Hashtable<Integer, ArrayList<String>> table, String term, Integer position, String url){
		if(! table.containsKey(position)){
			ArrayList<String> newList = new ArrayList<String>();
			newList.add(url);
			table.put(position, newList);
		}
		else{
			ArrayList<String> list = table.get(position);
			list.add(url);
			table.put(position, list);
		}
		return table;
	}
	
	public static void findTwoGrams(Hashtable<Integer, ArrayList<String>> table1, Hashtable<Integer, ArrayList<String>> table2){
		Set<Integer> keySet1 = table1.keySet();
		int pos1 = 0;
		ArrayList<String> valueTable1 = null;
		ArrayList<String> valueTable2 = null;
		String finalURL = null;
		Hashtable<String, Integer> wordCountTable = new Hashtable<String, Integer>();
		for(int key1: keySet1){
			pos1 = key1;
			valueTable1 = table1.get(key1);
			int key2 = ++pos1;
			if(table2.containsKey(key2)){
				valueTable2 = table2.get(key2);
			
				for(int i = 0; i < valueTable1.size(); i++){
					for(int j = 0; j < valueTable2.size(); j++){
						if(valueTable1.get(i).equals(valueTable2.get(j))){
							finalURL = valueTable2.get(j);
							if(wordCountTable.containsKey(finalURL)){
								int urlCount = wordCountTable.get(finalURL);
								urlCount++;
								wordCountTable.put(finalURL, urlCount);
							}
							else{
								wordCountTable.put(finalURL, 1);
							}
						}
					}
				}
			}
		}
		int docsCount = wordCountTable.keySet().size();
		for(String url: wordCountTable.keySet()){
			double tfidf = (1+ Math.log10(wordCountTable.get(url))) * (corpusSize/Math.log10(docsCount));
			saveInSearchResult(url, tfidf);
		}
		System.out.println(Math.log10(docsCount));
		sortSearchResult();
	}
}