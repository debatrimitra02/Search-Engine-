import java.util.Scanner;
import java.sql.*;
import java.io.*;
import java.util.ArrayList;
import java.util.*;
import java.util.Hashtable;
import java.util.Set;
import java.util.Comparator;
import java.util.Collections;
import java.lang.StringBuilder;
import org.apache.commons.lang3.StringUtils;

public class QueryNew{
	
	static Scanner scan = new Scanner(System.in);
	static int corpusSize = 100000;
	static Hashtable<String, Double> searchResultHash = new Hashtable<String, Double>();
	static ArrayList<String> searchResultUrl = new ArrayList<String>();
	
	public static void main(String[] args){
		String userQuery = scan.nextLine();
		DB.setup();
		try{
			searchWords(userQuery);
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
		for(int i = 0; i < searchResultUrl.size() && i < 5 ; i++)
		{
			String url = searchResultUrl.get(i);
			System.out.println("URL: " + url);
			//System.out.println("TF-IDF: " + searchResultHash.get(url));
			System.out.println();
		
		}
	}
	
	public static void searchWords(String full_string) throws SQLException{
		System.out.println("Searching for: " + full_string);
		// HOLDS ALL THE TABLES
		ArrayList<Hashtable<Integer, ArrayList<String>>> tables = new ArrayList<Hashtable<Integer, ArrayList<String>>>();		
		List<String> words = Arrays.asList(full_string.split(" "));
		ArrayList<String> fwords = new ArrayList<String>();
		// creates a table for each word
		for(int i = 0; i < words.size(); i++){
			Hashtable<Integer, ArrayList<String>> table = new Hashtable<Integer, ArrayList<String>>();
			tables.add(table);
		}
		for(int i = 0; i < words.size(); i++){
			fwords.add("'" + words.get(i) + "'");
		}
		String in_str = StringUtils.join(fwords, ",");
		String sqlQuery;
		sqlQuery = "SELECT * FROM indexer WHERE term in ("+ in_str +")";
		//System.out.println(sqlQuery);
		ResultSet result = DB.statement.executeQuery(sqlQuery);
		while(result.next()){
			String term = result.getString("term");
			int position = result.getInt("position");
			String url = result.getString("url");
			// GET THE INDEX OF THE TERM IN THE ARRAYLIST "WORDS"
			int index = 0;
			for(index = 0; index < words.size(); index++){
				if(words.get(index).equals(term))
					break;					
			}
			tables.set(index, updateTable(tables.get(index), term, position, url));
			//System.out.println(tables.get(0));
		}
		
		findGrams(tables);
		displayResults();
		System.exit(0);
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

	public static void findGrams(ArrayList<Hashtable<Integer, ArrayList<String>>> tables){
	
		Set<Integer> keySet1 = tables.get(0).keySet();
		Hashtable<String, Integer> wordCountTable = new Hashtable<String, Integer>();
		for(int key1: keySet1){
			ArrayList<String> foundurls = new ArrayList<String>();
			if(tables.size() == 1){
				foundurls = tables.get(0).get(key1);
			}
			for(int i = 1; i < tables.size(); i++){
				ArrayList<String> commonurls = new ArrayList<String>();
				int pos = key1+1;
				//System.out.println(pos);
				//System.out.println(tables.get(0));
				if(tables.get(i).containsKey(pos)){
					//System.out.println(tables.get(i-1).get(pos-1).get(0));
					ArrayList<String> urlList1 = tables.get(i-1).get(pos-1);
					ArrayList<String> urlList2 = tables.get(i).get(pos);
					commonurls = findcommonurls(urlList1,urlList2);
					//System.out.println(commonurls);
				}
				else{
					continue;
				}
				ArrayList<String> tmpurls = new ArrayList<String>();
				if(i==1)
					tmpurls = commonurls;
				else{
					for(int k = 0;k < commonurls.size(); k++){
						if(foundurls.contains(commonurls.get(k)))
							tmpurls.add(commonurls.get(k));
					}
				}
				foundurls = tmpurls;
			}
			//System.out.println("foundurls: " + foundurls);
			//System.exit(0);
			for(String url: foundurls){
				if(! wordCountTable.containsKey(url)){
					wordCountTable.put(url,1);
				}
				else{
					int count = wordCountTable.get(url);
					count++;
					wordCountTable.put(url,count);
				}
			}
		}
		//System.out.println(wordCountTable);
		int docsCount = wordCountTable.keySet().size();
		for(String url: wordCountTable.keySet()){
			double tfidf = (1+ Math.log10(wordCountTable.get(url))) * (corpusSize/Math.log10(docsCount));
			saveInSearchResult(url, tfidf);
		}
		sortSearchResult();
	}
	
	public static ArrayList<String> findcommonurls(ArrayList<String> list1, ArrayList<String> list2){
		ArrayList<String> finalurls = new ArrayList<String>();
		System.out.println(list1);
		if(list1 == null){
			return finalurls;
		}
		for(int i =0;i < list1.size(); i++){
			for(int j = 0; j < list2.size(); j++){
				if(list1.get(i).equals(list2.get(j)))
					finalurls.add(list2.get(j));
			}
		}
		return finalurls;
	}
}
