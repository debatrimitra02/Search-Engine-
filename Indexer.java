import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class Indexer{

	static String filesFolder = "../Indexer/files";
	static int batchCount = 0;
	static int batch = 1;

	public static void main(String[] args){
		
		ArrayList<String> fileNames = getFileNames();
		//DB.setup();
		int fileCount = 1;
		for(String fileName : fileNames){
			String filePath = filesFolder + "/" + fileName;
			try{
				System.out.println("\nReading file number: " + fileCount);
				fileCount++;
				indexFile(filePath);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		//DB.close();
	}
	
	public static void indexFile(String filePath) throws IOException, SQLException{
		System.out.println("Scanning file: " + filePath);
		File file = new File(filePath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = null;
		reader.readLine();
		String url = reader.readLine().replaceAll(",","");
		if(url.contains(".csv") || url.contains(".data")){
			System.out.println("Skipping file: " + filePath + " URL: " + url);
			return;
		}
		batchCount++;
		if(batchCount > 50000){
			batch++;
			batchCount = 0;
		}
		String csvName = "batch" + batch + ".txt";
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(csvName, true)));
		int position = 0;
		while((line = reader.readLine()) != null){
			line = line.replaceAll("[^a-zA-Z\\- ]","").toLowerCase();
			line = StopWords.cleanLine(line);
			String[] tokens = line.split("\\s+");
			for(String token: tokens){
				if(token.isEmpty()){
					continue;
				}
				if(token.length() > 100 || url.length() > 100)
					continue;
				position++;
			//	DB.getStatement().executeUpdate("INSERT INTO indexer(term, position, url) VALUES('"+token+"','"+position+"','"+url+"')");
			
				String ln = token + "," + position +"," + url;
				out.println(ln);
				
			}
		}
		out.close();
	}
	
	public static ArrayList<String> getFileNames(){
		File folder = new File(filesFolder);
		ArrayList<String> files = new ArrayList<String>();
		for(File file : folder.listFiles()){
			if(file.isFile())
				files.add(file.getName());
		}
		return files;
	}
}
