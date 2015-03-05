import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class ModifiedTreeRandomizedData {

	/**
	 * 
	 */
	double theta = 0.8;
	private final ID3Tree id3Tree;
	public ArrayList<Boolean[]> randomizedTrainingData = new ArrayList<Boolean[]>();
	//public int[] percentage = { 1, 2, 5, 10, 25, 50, 75, 100 };
	public int[] percentage = {100};

	public ModifiedTreeRandomizedData(ID3Tree id3Tree) {
		this.id3Tree = id3Tree;
		try {
			randomizedTrainingData = readFileandRandomize("./training.txt");
			putRandomizedDataSetOnFile(randomizedTrainingData,
					"randomizedTrainingData");
			ArrayList<Boolean[]> randomizedTestData = readFileandRandomize("./test.txt");
			putRandomizedDataSetOnFile(randomizedTestData, "randomizedTestData");
			
			
//			ArrayList<Boolean> actualClassValue = new ArrayList<Boolean>();
//			for (int i = 0; i < randomizedTestData.size(); i++) {
//				Boolean[] record = randomizedTestData.get(i);
//				actualClassValue.add(record[record.length - 1]);
//			}
			
			
			for (int i = 0; i < percentage.length; i++) {
				ArrayList<Boolean[]> modifiedTestData = generateTestDataLists(percentage[i]);// 20,50,80
				System.out.println(percentage[i]+ "% of training dataset used to build decision tree.");
				HashSet<Integer> attributes = this.id3Tree
						.getAttributeList(modifiedTestData);
				Node root = buildTree(modifiedTestData, attributes);
				ID3Tree.traverseTree(root, 0, "");
				// trees.add(root);
				ArrayList<Boolean> derivedClassValue = new ArrayList<Boolean>();
				StringBuilder toBeWritten = new StringBuilder();
				for (int j = 0; j < randomizedTestData.size(); j++) {
					Boolean[] record = randomizedTestData.get(j);
					derivedClassValue.add(classify(root, record));
					toBeWritten.append(derivedClassValue.get(j));
					toBeWritten.append(System.getProperty("line.separator"));
				}
				String filename = "unmodified_data_output_" + percentage[i]
						+ "_percent";
				id3Tree.writeToFile(toBeWritten, filename);
				///Double meanSquareError = ID3Tree.evaluateResults(actualClassValue, derivedClassValue);
				///System.out.println(meanSquareError);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void putRandomizedDataSetOnFile(
			ArrayList<Boolean[]> randomizedData, String fileName) {
		StringBuilder toBeWritten = new StringBuilder();
		for (int i = 0; i < randomizedData.size(); i++) {
			toBeWritten.append(randomizedData.get(i));
			toBeWritten.append(System.getProperty("line.separator"));
		}
		id3Tree.writeToFile(toBeWritten, fileName);
	}

	private Boolean classify(Node root, Boolean[] record) {
		// TODO Auto-generated method stub
		return null;
	}

	private Node buildTree(ArrayList<Boolean[]> modifiedTestData,
			HashSet<Integer> attributes) {

		return null;
	}

	public ArrayList<Boolean[]> generateTestDataLists(int percentage) {

		ArrayList<Boolean[]> data = new ArrayList<Boolean[]>();
		int size = randomizedTrainingData.size();
		int newSize = (int) ((size * percentage) / 100);
		// System.out.println("for 50% test data full data" +
		// testData.size()+
		// newSize);

		for (int i = 0; i < newSize; i++) {
			Boolean[] record = randomizedTrainingData.get(i);
			// System.out.println("Building half tree" +
			// Arrays.toString(record));
			data.add(record);
		}
		return data;
	}

	private ArrayList<Boolean[]> readFileandRandomize(String filename)
			throws Exception {

		ArrayList<Boolean[]> data = new ArrayList<>();
		Scanner input = new Scanner(new File(filename));
		while (input.hasNext()) {
			String line = input.nextLine();
			String[] tokens = line.split("[,]");
			Boolean[] record = new Boolean[tokens.length];
			record = randomizeSingleRecord(tokens);
			data.add(record);
		}
		input.close();
		return data;
	}

	private Boolean[] randomizeSingleRecord(String[] tokens) {
		Boolean[] modifiedRecord = new Boolean[tokens.length];
		double randomTheta;
		randomTheta = Math.random();
		if (randomTheta <= theta) {
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].trim().equals("1")) {
					modifiedRecord[i] = true;
				} else {
					modifiedRecord[i] = false;
				}
			}
		} else {
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].trim().equals("1")) {
					modifiedRecord[i] = false;
				} else {
					modifiedRecord[i] = true;
				}
			}
		}
		return modifiedRecord;
	}
}