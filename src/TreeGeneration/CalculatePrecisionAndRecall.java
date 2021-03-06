package TreeGeneration;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;


public class CalculatePrecisionAndRecall {

	static String str_path = System.getProperty("user.dir") + File.separator;
	static int int_depthOfTheTree = 7;
	static String str_depthSeparator = "__";
	private static double threshold = 0;

	private static final Logger LOG = Logger.getLogger(CalculatePrecisionAndRecall.class.getCanonicalName());
	private static final Logger log_heuResult = Logger.getLogger("heuResultLogger");
	private static final Logger log_normalized = Logger.getLogger("reportsLogger");

	private static final Map<String, ArrayList<Double>> hmap_subCategoryCount = new HashMap<>();
	private static final HashMap<String, String> hmap_groundTruth = new LinkedHashMap<>();
	private static final LinkedHashMap<String, LinkedHashMap<String, Double>> hmap_testSet = new LinkedHashMap<>();
	private static final Map<String, LinkedList<String>> hmap_groundTruthlist = new LinkedHashMap<>();

	private static final Map<String, LinkedHashMap<String, Double>> hmap_heuResult = new LinkedHashMap<>();
	private static final Map<String, LinkedHashMap<String, Double>> hmap_heuResultNormalized = new LinkedHashMap<>();
	private static final Map<String, LinkedHashMap<String, Double>> hmap_heuResultNormalizedSorted = new LinkedHashMap<>();
	private static final Map<String, LinkedHashMap<String, Double>> hmap_heuResultNormalizedSortedFiltered = new LinkedHashMap<>();

	private static final Map<String, LinkedHashMap<String, Double>> hmap_precisionRecallFmeasure = new LinkedHashMap<>();

	private static final Map<String, Integer> hmap_entityStartingCat = new LinkedHashMap<>();
	

	private static void emptyMaps() {
		hmap_heuResult.clear();
		hmap_heuResultNormalized.clear();
		hmap_heuResultNormalizedSorted.clear();
		hmap_heuResultNormalizedSortedFiltered.clear();
		hmap_precisionRecallFmeasure.clear();
	}

	public static void testNormalization()
	{
		for (Entry<String, String> entry : hmap_groundTruth.entrySet()) {
			String str_entity = entry.getKey();
			LinkedHashMap<String, Double> lhmap_temp = new LinkedHashMap<>();
			HashSet<Double> hset_ValuesToNormalize = new HashSet<>();
			String str_entityNameAndDepth = entry.getKey();

			LinkedList<Double> llist_test = new LinkedList<>(); 
			for (Integer i = 1; i <= int_depthOfTheTree; i++) {

				LinkedHashMap<String, Double> ll_result = hmap_heuResultNormalized.get(str_entity + str_depthSeparator + i.toString());

				if ( hmap_heuResultNormalized.get(str_entity + str_depthSeparator + i.toString())!=null)
				{
					for (Entry<String, Double> entry_CatAndValue : ll_result.entrySet()) 
					{
						llist_test.add(entry_CatAndValue.getValue());
					}


				}

			}
			//			LinkedList<Double> ll_de = new LinkedList<>();
			if (llist_test.size()>0&&!Collections.max(llist_test).equals(1.)) 
			{
				System.out.println(str_entity+ "HATA");
			}
			//			if (llist_test.size()==0) {
			//				
			//				System.out.println(str_entity);
			//				
			//			}

		}
	}

	public static void main(String str_fileNameGroundTruthList, String str_fileNameTestSet, Double db_threshold,
			GlobalVariables.HeuristicType heu) {
		emptyMaps();
		ReadSubCategoryNumber();
		threshold = db_threshold;
		InitializeGroundTruthAndList(str_fileNameGroundTruthList);

		// printMap(log_normalized, hmap_groundTruth,heu);
		// printMap(log_normalized, hmap_groundTruthlist,heu);

		InitializeTestSet(str_fileNameTestSet);
		//printMap(log_normalized, hmap_testSet,heu);

		callHeuristic(heu);// hmap_heuResult
		//printMap(log_normalized, hmap_heuResult, heu);
		callNormalization();// hmap_heuResultNormalized
		//printMap(log_normalized, hmap_heuResultNormalized, heu);
		//testNormalization();
		//System.out.println("Finished Test");
		//printMap(log_normalized, hmap_heuResultNormalized, heu);
		SortHeuristicResults();// hmap_heuResultNormalizedSorted
		//printMap(log_normalized, hmap_heuResultNormalizedSorted, heu);

		filterHeuResults();// hmap_heuResultNormalizedSortedFiltered
		//printMap(log_normalized, hmap_heuResultNormalizedSortedFiltered, heu);

		// printMap(log_normalized, hmap_heuResultNormalizedSortedFiltered,heu);

		// System.out.println("Number Of Entities Before Filtering"+
		// hmap_heuResultNormalizedSorted.size());

		/*
		 * 1)Heu 2)Normalization 3)Sort 4)Filter = hmap_finalResult
		 */
		// compareResultsWithGroundTruth(hmap_heuResultNormalizedSortedFiltered);
		callCalculatePrecisionAndRecall();
	}

	public static void callNormalization() {
		//Entity And Depth Based
		//		for (Entry<String, LinkedHashMap<String, Double>> entry :hmap_heuResult.entrySet()) 
		//		{
		//			if (entry.getValue().size()>0) 
		//			{
		//				hmap_heuResultNormalized.put(entry.getKey(), NormalizeMap(entry.getValue()));
		//			}
		//			else
		//				hmap_heuResultNormalized.put(entry.getKey(), new LinkedHashMap<>());
		//		}

		//Entity Based
		// Just to get entity Names
		for (Entry<String, String> entry : hmap_groundTruth.entrySet()) {
			String str_entity = entry.getKey();
			LinkedHashMap<String, Double> lhmap_temp = new LinkedHashMap<>();
			HashSet<Double> hset_ValuesToNormalize = new HashSet<>();
			String str_entityNameAndDepth = entry.getKey();

			for (Integer i = 1; i <= int_depthOfTheTree; i++) {
				LinkedHashMap<String, Double> ll_result = hmap_heuResult
						.get(str_entity + str_depthSeparator + i.toString());

				for (Entry<String, Double> entry_CatAndValue : ll_result.entrySet()) {
					hset_ValuesToNormalize.add(entry_CatAndValue.getValue());
				}
			}

			if (hset_ValuesToNormalize.size() > 0) {
				Map<Double, Double> hmap_NormalizationMap = NormalizeHashSet(hset_ValuesToNormalize);

				for (Integer i = int_depthOfTheTree; i > 0; i--) {
					LinkedHashMap<String, Double> ll_result = hmap_heuResult
							.get(str_entity + str_depthSeparator + i.toString());
					lhmap_temp = new LinkedHashMap<>();
					for (Entry<String, Double> entry_CatAndValue : ll_result.entrySet()) {


						str_entityNameAndDepth = entry_CatAndValue.getKey();
						lhmap_temp.put(entry_CatAndValue.getKey(),
								hmap_NormalizationMap.get(entry_CatAndValue.getValue()));
					}
					hmap_heuResultNormalized.put(str_entity + str_depthSeparator + i.toString(), lhmap_temp);

				}


			}
			else
			{
				for (Integer i = int_depthOfTheTree; i > 0; i--) {
					hmap_heuResultNormalized.put(str_entity + str_depthSeparator + i.toString(), lhmap_temp);
				}

			}

		}
	}

	// NormalizeHashSet();
	// for(Entry<String, LinkedHashMap<String, Double>> entry:
	// hmap_heuResult.entrySet())
	// {
	// LinkedHashMap<String, Double> lhmap_temp = new LinkedHashMap<>();
	//
	// String str_entityNameAndDepth = entry.getKey();
	//
	// LinkedHashMap<String, Double> hmap_CatAndValue = entry.getValue();
	//
	// for(Entry<String, Double> entry_CatAndValue:
	// hmap_CatAndValue.entrySet())
	// {
	// lhmap_temp.put(entry_CatAndValue.getKey(),
	// hmap_NormalizationMap.get(entry_CatAndValue.getValue()));
	// }
	// hmap_heuResultNormalized.put(str_entityNameAndDepth, lhmap_temp);
	//
	// }
	private void countEntityThatContainsCategory(String str_categoryName)
	{
		int int_entityCount = 0;
		str_categoryName = str_categoryName.toLowerCase();
		for (Entry<String, String> entry : hmap_groundTruth.entrySet()) 
		{

			String str_entityName = entry.getKey();

			for (Integer i = 1; i <= int_depthOfTheTree; i++) 
			{

				String str_entNameAndDepth =str_entityName+str_depthSeparator+ i.toString();
				if (hmap_testSet.get(str_entNameAndDepth).containsKey(str_categoryName)) 
				{
					int_entityCount++;
					break;
				}
			}
		}
	}
	public static void callCalculatePrecisionAndRecall() {

		for (Entry<String, LinkedHashMap<String, Double>> entry : hmap_heuResultNormalizedSortedFiltered.entrySet()) {
			String str_entityNameAndDepth = entry.getKey();
			String str_depth = str_entityNameAndDepth.substring(
					str_entityNameAndDepth.indexOf(str_depthSeparator) + str_depthSeparator.length(),
					str_entityNameAndDepth.length());
			String str_entityName = str_entityNameAndDepth.substring(0,
					str_entityNameAndDepth.indexOf(str_depthSeparator));
			hmap_precisionRecallFmeasure.put(str_entityNameAndDepth,
					CalculatePrecisionRecallFmeasure(str_entityName, str_depth));
		}

		String str_Pre = "=SPLIT(\"";
		String str_Rec = "=SPLIT(\"";
		String str_Fsco = "=SPLIT(\"";

		for (Integer int_depth = int_depthOfTheTree; int_depth > 0; int_depth--)
			// for (Integer int_depth = 1; int_depth <= int_depthOfTheTree ;
			// int_depth++)
		{
			int int_NumberOfEntities = 0;
			Double[] arr_Pre = new Double[int_depthOfTheTree];
			Arrays.fill(arr_Pre, 0.);
			Double[] arr_Rec = new Double[int_depthOfTheTree];
			Arrays.fill(arr_Rec, 0.);
			Double[] arr_Fsco = new Double[int_depthOfTheTree];
			Arrays.fill(arr_Fsco, 0.);

			for (Entry<String, LinkedHashMap<String, Double>> entry : hmap_precisionRecallFmeasure.entrySet()) {
				String str_entityNameAndDepth = entry.getKey();

				if (Integer.parseInt(str_entityNameAndDepth.substring(
						str_entityNameAndDepth.indexOf(str_depthSeparator) + str_depthSeparator.length(),
						str_entityNameAndDepth.length())) == int_depth) {
					int_NumberOfEntities++;
					LinkedHashMap<String, Double> hmap_preRcalFsco = entry.getValue();

					arr_Pre[int_depth - 1] += hmap_preRcalFsco.get("Precision");
					arr_Rec[int_depth - 1] += hmap_preRcalFsco.get("Recall");

				}
			}

			Locale.setDefault(Locale.US);
			DecimalFormat df = new DecimalFormat("0.00000");

			final String averagePrecision = df.format(arr_Pre[int_depth - 1] / int_NumberOfEntities);
			final String averageRecall = df.format(arr_Rec[int_depth - 1] / int_NumberOfEntities);

			double averageFScore=0;
			if (Double.parseDouble(averageRecall)+Double.parseDouble(averagePrecision)!=0) 
			{
				averageFScore = 2 * Double.parseDouble(averagePrecision)*Double.parseDouble(averageRecall) / (Double.parseDouble(averagePrecision)+Double.parseDouble(averageRecall));
			}
			str_Pre = str_Pre + " ," + averagePrecision;

			str_Rec = str_Rec + " ," + averageRecall;
			str_Fsco = str_Fsco + " ," + df.format(averageFScore);
			// System.out.println("Depth "+ int_depth + " Precision " +
			// df.format(arr_Pre[int_depth-1]/int_NumberOfEntities));
			// System.out.println("Depth "+ int_depth + " Recall " +
			// df.format(arr_Rec[int_depth-1]/int_NumberOfEntities));
			// System.out.println("Depth "+ int_depth + " Fscore " +
			// df.format(arr_Fsco[int_depth-1]/int_NumberOfEntities));

		}
		str_Pre += "\",\",\")";
		str_Rec += "\",\",\")";
		str_Fsco += "\",\",\")";

		System.out.println(str_Pre.replace("=SPLIT(\" ,", "=SPLIT(\""));
		System.out.println(str_Rec.replace("=SPLIT(\" ,", "=SPLIT(\""));
		System.out.println(str_Fsco.replace("=SPLIT(\" ,", "=SPLIT(\""));

	}

	public static double GetAverageArray(Double[] arr) {
		double sum = 0;
		double size = arr.length;

		for (int i = 0; i < arr.length; i++) {
			sum += arr[i];
		}

		return sum / size;
	}

	public static LinkedHashMap<String, Double> CalculatePrecisionRecallFmeasure(String str_entity, String str_depth) {
		LinkedHashMap<String, Double> hmap_preRCallFmea = new LinkedHashMap<>();

		double db_relevantElements, db_selectedElements,

		int_truePositive = 0;
		if (hmap_groundTruthlist.get(str_entity) == null) {
			LOG.error("entity does not exist "+str_entity);
		}
		db_relevantElements = hmap_groundTruthlist.get(str_entity).size();

		double precision = 0.0, recall = 0.0, Fscore = 0.0;

		final LinkedHashMap<String, Double> lhmap_depthElements = hmap_heuResultNormalizedSortedFiltered
				.get(str_entity + str_depthSeparator + str_depth);
		final LinkedList<String> llist_groundTruth = hmap_groundTruthlist.get(str_entity);

		db_selectedElements = lhmap_depthElements.size();

		for (Entry<String, Double> entry : lhmap_depthElements.entrySet()) {
			String str_Cat = entry.getKey();

			if (llist_groundTruth.contains(str_Cat)) {
				int_truePositive += 1;
			}
			// int_truePositive=0;
		}

		if (int_truePositive != 0) 
		{
			precision = int_truePositive / db_selectedElements;
			recall = int_truePositive / db_relevantElements;
			Fscore = 2 * ((precision * recall) / (precision + recall));

			hmap_preRCallFmea.put("Precision", precision);
			hmap_preRCallFmea.put("Recall", recall);
			hmap_preRCallFmea.put("Fscore", Fscore);
		} 
		else {
			hmap_preRCallFmea.put("Precision", 0.);
			hmap_preRCallFmea.put("Recall", 0.);
			hmap_preRCallFmea.put("Fscore", 0.);
		}

		//System.out.println(str_entity+" "+ precision + " "+ recall +" "+ Fscore);
		return hmap_preRCallFmea;
	}

	public static void InitializeGroundTruthAndList(String fileName) {
		try (BufferedReader br = new BufferedReader(new FileReader(str_path + fileName));) {

			String str_entity = null, str_mainCat = null;

			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				line = line.toLowerCase();
				if (line == null) {
					// System.out.println("--------------------------------------");
				}
				LinkedList<String> ll_goalSet = new LinkedList<>();
				String[] str_split = line.split("\t");
				for (int i = 0; i < str_split.length; i++) {
					str_entity = str_split[0];
					str_mainCat = str_split[1];

					if (i != 0) {
						ll_goalSet.add(str_split[i]);
					}
				}

				hmap_groundTruth.put(str_entity, str_mainCat);
				hmap_groundTruthlist.put(str_entity, ll_goalSet);
				if (hmap_groundTruth.size() > 100) {
					// System.out.println("--------------------------------------");
				}
			}

			for (Entry<String, LinkedList<String>> entry : hmap_groundTruthlist.entrySet()) {
				str_entity = entry.getKey();
				LinkedList<String> str_categories = entry.getValue();

				// System.out.println(str_entity+ " "+ str_categories);
			}
		} catch (IOException e) {

			System.out.println(e.getMessage());
			e.printStackTrace();

		}
	}

	public static void InitializeTestSet(String fileName) {
		String str_entityName = null;
		String str_catName = null;
		Integer int_count_= 0;
		try (BufferedReader br = new BufferedReader(new FileReader(str_path + fileName));) {
			String line = null;
			int depth = int_depthOfTheTree;

			ArrayList<String> arrList_paths = new ArrayList<>();

			ArrayList<Integer> numberOfPaths = new ArrayList<>();
			LinkedHashMap<String, Double> hmap_catAndValue = new LinkedHashMap<>();
			while ((line = br.readLine()) != null) {
				line = line.toLowerCase();
				if (line.contains(",") && !line.contains("\",\"")) {

					str_entityName = line.split(",")[0].toLowerCase();
					str_catName = line.split(",")[1].toLowerCase();
					// hmap_groundTruth.put(str_entityName, str_catName);

				} else if (line.length() < 1) {
					hmap_testSet.put(str_entityName + "__" + depth, hmap_catAndValue);
					// System.out.println("WWW "+str_entityName + "__" + depth +
					// " " +hmap_catAndValue);
					hmap_catAndValue = new LinkedHashMap<>();
					depth--;
					numberOfPaths.clear();
					arrList_paths.clear();
				} 
				else {
					if (line.contains(":")) {

						hmap_catAndValue.put(line.substring(0, line.indexOf(":")),
								Double.parseDouble(line.substring(line.indexOf(":") + 1, line.length())));
					} else if (line.contains("-"))
					{
						int_count_++;
					}
				}
				if (depth == 0) {
					depth = int_depthOfTheTree;
					hmap_entityStartingCat.put(str_entityName, int_count_);
					int_count_=0;
				}
			}
		} catch (IOException e) {

			e.printStackTrace();

		}

		for (Integer i = 1; i <= 7; i++) {
			for (Entry<String, String> entry : hmap_groundTruth.entrySet()) {
				if (!hmap_testSet.containsKey(entry.getKey() + str_depthSeparator + i.toString())) {
					System.out.println(entry);
				}
			}
		}

	}

	public static void SortHeuristicResults() {
		for (Entry<String, LinkedHashMap<String, Double>> entry : hmap_heuResultNormalized.entrySet()) {
			final LinkedHashMap<String, Double> temp = sortByValue(entry.getValue());
			hmap_heuResultNormalizedSorted.put(entry.getKey(), temp);
		}
	}

	public static void callHeuristic(GlobalVariables.HeuristicType enum_heuType) {
		for (Entry<String, LinkedHashMap<String, Double>> entry : hmap_testSet.entrySet()) {
			String str_entityNameAndDepth = entry.getKey();
			String str_entityName = str_entityNameAndDepth.substring(0, str_entityNameAndDepth.indexOf(str_depthSeparator));
			String str_depth = str_entityNameAndDepth.substring(
					str_entityNameAndDepth.indexOf(str_depthSeparator) + str_depthSeparator.length(), str_entityNameAndDepth.length());
			LinkedHashMap<String, Double> hmap_Values = (LinkedHashMap<String, Double>) entry.getValue();
			LinkedHashMap<String, Double> lhmap_Results = (LinkedHashMap<String, Double>) entry.getValue();
			for (Entry<String, Double> entry_hmapValues : hmap_Values.entrySet()) {
				String str_catName = entry_hmapValues.getKey();
				Double db_value = entry_hmapValues.getValue();
				Double db_heuValue = 0.0;
//				if (enum_heuType.equals(GlobalVariables.HeuristicType.HEURISTIC_NO))
//				{
//					db_heuValue = Heuristic_NanHeuristic(db_value);
//				}
//				else 
					if (enum_heuType.equals(GlobalVariables.HeuristicType.HEURISTIC_NUMBEROFPATHS)) {
					db_heuValue = Heuristic_NumberOfPaths(db_value);
				} else if (enum_heuType.equals(GlobalVariables.HeuristicType.HEURISTIC_NUMBEROFPATHSANDDEPTH)) {
					db_heuValue = Heuristic_NumberOfPathsAndDepth(db_value, Integer.parseInt(str_depth));
				}
				else if(enum_heuType.equals(GlobalVariables.HeuristicType.HEURISTIC_FIRSTFINDFIRSTDEPTH))
				{
					db_heuValue = Heuristic_FirstPathsAndDepth(str_entityName,db_value, Integer.parseInt(str_depth)) ;
				}
				// db_heuValue =
				// Heuristic_NumberOfPathsDepthSubCat(db_value,Integer.parseInt(str_depth),
				// str_catName);
				// }
				lhmap_Results.put(str_catName, db_heuValue);
			}
			hmap_heuResult.put(str_entityNameAndDepth, lhmap_Results);
		}
		// System.err.println("ZZZZZZZZZZZ
		// "+hmap_tempResults.containsKey("Gustav_Mahler__1"));
	}

	public static void filterHeuResults() {
		int int_catNumberBeforeFilter = 0;
		int int_catNumberFiltered = 0;

		for (Entry<String, LinkedHashMap<String, Double>> entry : hmap_heuResultNormalizedSorted.entrySet()) {
			String str_entityName = entry.getKey();

			LinkedHashMap<String, Double> hmap_Values = (LinkedHashMap<String, Double>) entry.getValue();
			LinkedHashMap<String, Double> hmap_catAndValFiletered = new LinkedHashMap<>();

			for (Entry<String, Double> entry_hmapValues : hmap_Values.entrySet()) {
				String str_catName = entry_hmapValues.getKey();
				Double db_value = entry_hmapValues.getValue();
				int_catNumberBeforeFilter++;
				if (db_value >= threshold) {
					hmap_catAndValFiletered.put(str_catName, db_value);
				} else {
					int_catNumberFiltered++;
				}
			}
			hmap_heuResultNormalizedSortedFiltered.put(str_entityName, hmap_catAndValFiletered);
		}
		//		 System.out.println();
		//		 System.out.println("Thershold"+ threshold );
		//		 System.out.println("Total Category Number Before Filtering:"+
		//		 int_catNumberBeforeFilter );
		//		 System.out.println("Total Category Number After Filtering:"+
		//		 (int_catNumberBeforeFilter-int_catNumberFiltered) );
		//		 System.out.println();
	}

	private static double Heuristic_NanHeuristic(double db_Value) {
		return 1.0;
	}

	private static double Heuristic_NumberOfPaths(double db_Value) {
		return db_Value;
	}

	private static double Heuristic_NumberOfPathsAndDepth(double db_Value, int int_depth) {
		return (double) (db_Value / (double) int_depth);
	}

	private static double Heuristic_FirstPathsAndDepth(String str_entity,double db_Value, int int_depth) 
	{
		int int_entitystartingDepth= hmap_entityStartingCat.get(str_entity);
		if (!(int_entitystartingDepth>=0)) 
		{
			LOG.error("Entity not found hmap_entityStartingCat"+ str_entity);
		}
		if (int_depth<=int_entitystartingDepth) 
		{
			return 0;
		}
		return (double) (db_Value / (double) (int_depth-int_entitystartingDepth));
	}
	private static double Heuristic_NumberOfPathsDepthSubCat(double db_Value, int int_depth, String str_cat) {
		return (double) db_Value / (double) ((hmap_subCategoryCount.get(str_cat).get(int_depth - 1) * int_depth));
	}

	private static void compareResultsWithGroundTruth(Map<String, LinkedHashMap<String, Double>> hmap_heuResult) {
		int[] arr_FoundDepth = new int[int_depthOfTheTree];
		int count_Cat = 0;
		int count_NotFoundCat = 0;

		for (Entry<String, String> entry : hmap_groundTruth.entrySet()) {
			String str_entity = entry.getKey();
			String str_category = entry.getValue();

			boolean changed = false;
			for (int i = 0; i < 7; i++) {
				Integer int_index = i + 1;
				LinkedHashMap<String, Double> ll_result = hmap_heuResult
						.get(str_entity + str_depthSeparator + int_index.toString());
				if (str_entity.equals("wright_brothers") && int_index == 7) {
					// System.out.println("yes");
				}

				if (ll_result == null) {
					continue;
				}
				if (ll_result.size() > 0) {
					// String str_resCat= ll_result.keySet().iterator().next();
					///////////////////////////////////////////////////////
					final Double maxNumber = ll_result.values().iterator().next();
					final List<String> firstElements = ll_result.entrySet().stream()
							.filter(p -> p.getValue() >= maxNumber).map(p -> p.getKey()).collect(Collectors.toList());
					// System.err.println(str_entity+"\t"+int_index+"\t"+str_category
					// +"\t"+firstElements+"\t"+firstElements.contains(str_category));
					// System.out.println(str_entity+" "+firstElements);
					if (firstElements.contains(str_category))
						///////////////////////////////////////////////////////
						// if (str_resCat.equals(str_category))
					{
						if (int_index == 7) {
							// System.out.println(entry.getValue()+" "+
							// entry.getKey());
						}
						// System.out.println(entry.getValue()+" "+
						// entry.getKey());
						changed = true;
						// System.out.println(int_index+" "+str_entity+"
						// "+str_category);
						arr_FoundDepth[i] += 1;
						// System.out.println(str_entity+" "+firstElements+" "+
						// firstElements+" "+arr_FoundDepth[i]);
						count_Cat++;
						break;
					}
				}

			}
			if (!changed) {
				// System.out.println("XXXXXXXXXXX "+str_entity);
				count_NotFoundCat++;
			}

		}
		// System.out.println("=SPLIT(\"" + formatResult[i] + "\",\",\")");
		String str_formated = "=SPLIT(\"";
		for (int i = arr_FoundDepth.length - 1; i >= 0; i--) {

			str_formated = str_formated + " ," + arr_FoundDepth[i];
			// System.out.println(str_formated);
		}

		str_formated += "\",\",\")";
		System.out.println(str_formated.replace("SPLIT(\" ,", "str_formated=SPLIT(\""));
		// System.out.println(str_formated.replace("=SPLIT(\" ,", "=SPLIT(\""));

		// System.out.println("Entity Count: "+ hmap_groundTruth.size());
		// System.out.println("Total Found Category Number: "+ count_Cat );
		// System.out.println("Total NOT Found Category Number: "+
		// count_NotFoundCat );
	}

	// public static void NormalizeHashSet()
	// {
	//
	// double min = Collections.min(hset_ValuesToNormalize);
	// double max =Collections.max(hset_ValuesToNormalize);
	//
	// if (min==max)
	// {
	// hmap_NormalizationMap.put(min, 1.);
	// return;
	// }
	//
	// for (Double db_val: hset_ValuesToNormalize)
	// {
	// hmap_NormalizationMap.put(db_val, ((double) ((double) (db_val - min) /
	// (double) (max - min))));
	// }
	// //Double[] array = hmap_NormalizationMap.values().stream().toArray(x ->
	// new Double[x]);
	// //System.err.println("Median :"+median(array));
	// }


	public static LinkedHashMap<String, Double> NormalizeMap(Map<String, Double> hmap_ValuesToNormalize) {

		Map<String, Double> hmap_NormalizationMap = new LinkedHashMap<>();
		double max=1;
		try 
		{
			max = Collections.max(hmap_ValuesToNormalize.values());
		} 
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
		}

		for (Entry<String, Double> entry_CatAndVal : hmap_ValuesToNormalize.entrySet()) 
		{
			hmap_NormalizationMap.put(entry_CatAndVal.getKey(), ((double) ((double) entry_CatAndVal.getValue() / (double) max)));
		}
		return (LinkedHashMap<String, Double>) hmap_NormalizationMap;
	}

	public static Map<Double, Double> NormalizeHashSet(HashSet<Double> hset_ValuesToNormalize) {
		Map<Double, Double> hmap_NormalizationMap = new HashMap<>();
		double min = Collections.min(hset_ValuesToNormalize);
		double max = Collections.max(hset_ValuesToNormalize);

		if (min == max) {
			hmap_NormalizationMap.put(min, 1.);
			return hmap_NormalizationMap;
		}

		for (Double db_val : hset_ValuesToNormalize) {
			hmap_NormalizationMap.put(db_val, ((double) ((double) db_val / (double) max)));
		}
		return hmap_NormalizationMap;
		// Double[] array = hmap_NormalizationMap.values().stream().toArray(x ->
		// new Double[x]);
		// System.err.println("Median :"+median(array));
	}

	public static double median(Double[] m) {
		int middle = m.length / 2;
		if (m.length % 2 == 1) {
			return m[middle];
		} else {
			return (m[middle - 1] + m[middle]) / 2.0;
		}
	}

	public static void printMap(Logger log, Map<String, LinkedHashMap<String, Double>> hmapHeuresultnormalized, GlobalVariables.HeuristicType heu) {

		for (Entry<String, LinkedHashMap<String, Double>> entry : hmapHeuresultnormalized.entrySet()) 
		{

			if (!entry.getValue().isEmpty())
			{
				System.out.println(entry.getKey() + " = " + entry.getValue());
			}

		}

		// System.out.println("----------------------------"+mp.);
		// log.info(heu);
		//		Iterator it = mp.entrySet().iterator();
		//		while (it.hasNext()) {
		//			Map.Entry pair = (Map.Entry) it.next();
		//			// //System.out.println(pair.getKey() + " = " + pair.getValue());
		//			 //log.info(pair.getKey() + " = " + pair.getValue());
		//			if (pair.getValue().equals("{}"))
		//			{
		//				System.out.println("Yes");
		//			}
		//			System.out.println(pair.getKey() + " = " + pair.getValue());
		//		}
		//		// log.info("----------------------------");
		//		// log.info("");
	}

	private static LinkedHashMap<String, Double> sortByValue(Map<String, Double> unsortMap) {
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
		LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	public static void ReadSubCategoryNumber() {
		String[] subCount = null;

		int[] int_subCount;
		double[] double_subCount;
		BufferedReader brC;
		ArrayList<Double> arrListTemp;
		try {
			brC = new BufferedReader(new FileReader(str_path + "SubCategory_Count.csv"));
			String lineCategory = null;

			while ((lineCategory = brC.readLine()) != null) {

				lineCategory = lineCategory.toLowerCase();
				arrListTemp = new ArrayList<>();
				// System.out.println(lineCategory);
				subCount = (lineCategory.substring(lineCategory.indexOf(":,") + 2, lineCategory.length()).split(","));
				int_subCount = Arrays.stream(subCount).mapToInt(Integer::parseInt).toArray();

				for (int i = 0; i < int_subCount.length; i++) {
					arrListTemp.add((double) int_subCount[i]);
				}
				hmap_subCategoryCount.put(lineCategory.substring(0, lineCategory.indexOf(":")), arrListTemp);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}