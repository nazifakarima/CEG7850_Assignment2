import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import com.sun.jndi.url.dns.dnsURLContext;

public class ModifiedTreeRandomizedData {

	/**
	 * 
	 */
	double theta = 0.65;
	private final ID3Tree id3Tree;
	ArrayList<Boolean[]> modifiedTestData = new ArrayList<Boolean[]>();
	public ArrayList<Boolean[]> randomizedTrainingData = new ArrayList<Boolean[]>();
	public int[] percentage = { 1, 2, 5, 10, 25, 50, 75, 100 };
	//public int[] percentage = {100};

	public ModifiedTreeRandomizedData(ID3Tree id3Tree) {
		this.id3Tree = id3Tree;
		try {
			randomizedTrainingData = readFileandRandomize("./training.txt");
			putRandomizedDataSetOnFile(randomizedTrainingData,
					"randomizedTrainingData");
			ArrayList<Boolean[]> randomizedTestData = readFileandRandomize("./test.txt");
			putRandomizedDataSetOnFile(randomizedTestData, "randomizedTestData");

			for (int i = 0; i < percentage.length; i++) {
				modifiedTestData = generateTestDataLists(percentage[i]);// 20,50,80
				System.out.println(percentage[i]
						+ "% of training dataset used to build decision tree.");
				HashSet<Integer> attributes = this.id3Tree
						.getAttributeList(modifiedTestData);

				ArrayList<Split> path = new ArrayList<>();
				Node root = buildTree(attributes, path);
				ID3Tree.traverseTree(root, 0, "");
				ArrayList<Boolean> derivedClassValue = new ArrayList<Boolean>();
				StringBuilder toBeWritten = new StringBuilder();
				for (int j = 0; j < randomizedTestData.size(); j++) {
					Boolean[] record = randomizedTestData.get(j);
					derivedClassValue.add(classify(root, record));
					toBeWritten.append(derivedClassValue.get(j));
					toBeWritten.append(System.getProperty("line.separator"));
				}
				String filename = "Modified_data_output_" + percentage[i]
						+ "_percent";
				id3Tree.writeToFile(toBeWritten, filename);
				ArrayList<Boolean> actualClassValue = readFile("actual_class_value");
//				for (int j = 0; j < actualClassValue.size(); j++) {
//					System.out.println("actual class value " + actualClassValue.get(j));
//				}
				Double meanSquareError = ID3Tree.evaluateResults(actualClassValue, derivedClassValue);
				System.out.println(meanSquareError);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<Boolean> readFile(String filename) throws Exception {
		ArrayList<Boolean> data = new ArrayList<>();
		Scanner input = new Scanner(new File(filename));

		while (input.hasNext()) {
			String line = input.nextLine();
			String[] tokens = line.split(System.getProperty("line.separator"));

			// Boolean[] record = new Boolean[tokens.length];
			for (int i = 0; i < tokens.length; i++) {
				String val = tokens[i];
				if (val.equalsIgnoreCase("true")) {
					data.add(true);
				} else {
					data.add(false);
				}
				//data.addAll(tokens[i].trim());
			}
		}
		return data;
	}

	private Boolean classify(Node node, Boolean[] record) {

		if (node.label != null) {
			return node.label;
		} else {
			boolean direction = record[node.attribute];
			if (direction) {
				return classify(node.trueChild, record);
			} else {
				return classify(node.falseChild, record);
			}
		}

	}

	private Node buildTree(HashSet<Integer> attributes, ArrayList<Split> path) {
		Node node = new Node();
		if (attributes.size() == 0) { // no more attributes to split on
			node.label = majorityClass(path);
			return node;
		}
		node.attribute = selectAttribute(attributes, path);
		attributes.remove(node.attribute);

		ArrayList<Split> truePath = (ArrayList<Split>) path.clone();
		truePath.add(new Split(node.attribute, true));
		node.trueChild = buildTree(attributes, truePath);
		ArrayList<Split> falsePath = (ArrayList<Split>) path.clone();
		falsePath.add(new Split(node.attribute, false));
		node.falseChild = buildTree(attributes, falsePath);

		return node;
	}

	private int selectAttribute(HashSet<Integer> attributes,
			ArrayList<Split> path) {// find which attribute to split on for it's
									// child node
		double maxGain = -Double.MIN_VALUE;
		int attribute = -1;
		for (Integer i : attributes) {
			double gain = findGain(i, path);
			if (gain > maxGain) {
				maxGain = gain;
				attribute = i;
			}
		}
		return attribute;
	}

	private double findGain(Integer i, ArrayList<Split> path) {
		double pEWithoutClass = getPEWithoutClass(path);
		double pENotWithoutClass = getPENotWithoutClass(path);
		double gain = entropy(path)
				- ((entropy(path) * pEWithoutClass) + (entropy(path) * pENotWithoutClass));
		if (Double.isNaN(gain))
			return 0.0;
		else
			return gain;
	}

	public double entropy(ArrayList<Split> path) {

		double Qtrue = PStarEWithClass(path);
		double Qfalse = PStarENotWithClass(path);

		double entropy = 0;

		if (Qtrue != 0 && !Double.isNaN(Qtrue)) {
			entropy += Qtrue * (Math.log(Qtrue) / Math.log(2));
		}

		if (Qfalse != 0 && !Double.isNaN(Qfalse)) {
			entropy += Qfalse * (Math.log(Qfalse) / Math.log(2));
		}

		return -entropy;
	}

	private double getPEWithoutClass(ArrayList<Split> path) {
		double pEWithoutClass = ((theta * PStarEWithoutClass(path)) - ((1 - theta) * PStarENotWithoutClass(path)))
				/ (1 - (2 * theta));
		return pEWithoutClass;
	}

	private double getPENotWithoutClass(ArrayList<Split> path) {
		double pENotWithoutClass = ((theta * PStarENotWithoutClass(path)) - ((1 - theta) * PStarEWithoutClass(path)))
				/ (1 - (2 * theta));
		return pENotWithoutClass;
	}

	private double getPEWithClass(ArrayList<Split> path) {
		double pEWithClass = ((theta * PStarEWithClass(path)) - ((1 - theta) * PStarENotWithClass(path)))
				/ (1 - (2 * theta));
		return pEWithClass;
	}

	private double getPENotWithClass(ArrayList<Split> path) {
		double pENotWithClass = ((theta * PStarENotWithClass(path)) - ((1 - theta) * PStarEWithClass(path)))
				/ (1 - (2 * theta));
		return pENotWithClass;
	}

	private Boolean majorityClass(ArrayList<Split> path) {
		return getPEWithClass(path) > getPENotWithClass(path);
	}

	private double PStarEWithoutClass(ArrayList<Split> path) {
		if (path.isEmpty()) {
			return randomizedTrainingData.size();
		}
		double PStarEWithoutClass = 0;
		int countData = 0;
		for (int i = 0; i < randomizedTrainingData.size(); i++) {
			Boolean[] record = randomizedTrainingData.get(i);
			int countPath = 0;
			for (int j = 0; j < path.size(); j++) {
				Split jSplit = path.get(j);
				int att = jSplit.attribute;
				boolean dir = jSplit.direction;
				// boolean recordDir = record[att].equals(1) ? true : false;
				if (record[att] == dir) {
					countPath++;
				}
			}
			if (countPath == path.size()) {
				countData++;
				// PStarEWithoutClass++;
			}
		}
		PStarEWithoutClass = countData / (double) randomizedTrainingData.size();
		return PStarEWithoutClass;

	}

	private double PStarENotWithoutClass(ArrayList<Split> path) {
		if (path.isEmpty()) {
			return 0;
		}
		double PStarENotWithoutClass = 0;
		int countData = 0;
		for (int i = 0; i < randomizedTrainingData.size(); i++) {
			Boolean[] record = randomizedTrainingData.get(i);
			int countPath = 0;
			for (int j = 0; j < path.size(); j++) {
				Split jSplit = path.get(j);
				int att = jSplit.attribute;
				boolean dir = jSplit.direction;
				// boolean recordDir = record[att].equals(1) ? true : false;
				if (!(record[att] == dir)) {
					countPath++;
				}
			}
			if (countPath == path.size()) {
				countData++;
				// PStarENotWithoutClass++;
			}
		}
		PStarENotWithoutClass = countData
				/ (double) randomizedTrainingData.size();
		return PStarENotWithoutClass;

	}

	private double PStarEWithClass(ArrayList<Split> path) {
		double PStarEWithClass = 0;
		int countData = 0;
		if (path.isEmpty()) {
			for (int i = 0; i < randomizedTrainingData.size(); i++) {
				Boolean[] record = randomizedTrainingData.get(i);
				if (record[record.length - 1])
					countData++;
				// PStarEWithClass++;
			}
		} else {
			for (int i = 0; i < randomizedTrainingData.size(); i++) {
				Boolean[] record = randomizedTrainingData.get(i);
				if (record[record.length - 1]) {
					int countPath = 0;
					for (int j = 0; j < path.size(); j++) {
						Split jSplit = path.get(j);
						int att = jSplit.attribute;
						boolean dir = jSplit.direction;
						if (record[att] == dir) {
							countPath++;
						}
					}
					if (countPath == path.size()) {
						countData++;
						// PStarEWithClass++;
					}
				}
			}
		}
		PStarEWithClass = countData / (double) randomizedTrainingData.size();
		return PStarEWithClass;
	}

	private double PStarENotWithClass(ArrayList<Split> path) {
		double PStarENotWithClass = 0;
		int countData = 0;
		if (path.isEmpty()) {
			for (int i = 0; i < randomizedTrainingData.size(); i++) {
				Boolean[] record = randomizedTrainingData.get(i);
				if (!(record[record.length - 1]))
					countData++;
				// PStarENotWithClass++;
			}
		} else {
			for (int i = 0; i < randomizedTrainingData.size(); i++) {
				Boolean[] record = randomizedTrainingData.get(i);
				if (!(record[record.length - 1])) {
					int countPath = 0;
					for (int j = 0; j < path.size(); j++) {
						Split jSplit = path.get(j);
						int att = jSplit.attribute;
						boolean dir = jSplit.direction;
						if (!(record[att] == dir)) {
							countPath++;
						}
					}
					if (countPath == path.size()) {
						countData++;
						// PStarENotWithClass++;
					}
				}
			}
		}
		PStarENotWithClass = countData / (double) randomizedTrainingData.size();
		return PStarENotWithClass;
	}

	private ArrayList<Boolean[]> generateTestDataLists(int percentage) {

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

	private void putRandomizedDataSetOnFile(
			ArrayList<Boolean[]> randomizedData, String fileName) {
		StringBuilder toBeWritten = new StringBuilder();
		for (int i = 0; i < randomizedData.size(); i++) {
			toBeWritten.append(randomizedData.get(i));
			toBeWritten.append(System.getProperty("line.separator"));
		}
		id3Tree.writeToFile(toBeWritten, fileName);
	}

	private Boolean[] randomizeSingleRecord(String[] tokens) {
		Boolean[] modifiedRecord = new Boolean[tokens.length];
		double randomTheta;
		randomTheta = Math.random();
		//System.out.println(randomTheta);
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