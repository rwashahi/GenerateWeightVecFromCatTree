package TreeGeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
	
		
		GenerateLevels_1.main();
		//GenerateLevelTrees_2.main();
		
		String pathMainCategories= System.getProperty("user.dir") + File.separator+"MainCategoryFile.txt";
		
	}

}