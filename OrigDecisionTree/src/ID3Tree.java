import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

public class ID3Tree {
	public static ArrayList<Boolean[]> testData = new ArrayList<Boolean[]>();
	public static int[] percentage = { 10, 25, 50, 75, 100 };

	public static ArrayList<Boolean[]> readFile(String filename)
			throws Exception {

		ArrayList<Boolean[]> data = new ArrayList<>();
		Scanner input = new Scanner(new File(filename));

		while (input.hasNext()) {
			String line = input.nextLine();
			String[] tokens = line.split("[,]");

			Boolean[] record = new Boolean[tokens.length];
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].trim().equals("1")) {
					record[i] = true;
				} else {
					record[i] = false;
				}
			}
			data.add(record);
		}

		input.close();

		return data;
	}

	public static ArrayList<Boolean[]> getTrueRows(ArrayList<Boolean[]> data,
			int attribute) {// rows in the remaining database, on which this
							// specific attribute=true
		ArrayList<Boolean[]> trueRows = new ArrayList<>();

		for (Boolean[] row : data) {
			if (row[attribute]) {
				trueRows.add(row);
			}
		}

		return trueRows;
	}

	public static ArrayList<Boolean[]> getFalseRows(ArrayList<Boolean[]> data,
			int attribute) {
		ArrayList<Boolean[]> falseRows = new ArrayList<>();

		for (Boolean[] row : data) {
			if (!row[attribute]) {
				falseRows.add(row);
			}
		}

		return falseRows;
	}

	public static boolean allOneClass(ArrayList<Boolean[]> data) {
		return (numTrue(data) == data.size() || numFalse(data) == data.size());
	}

	public static boolean majorityClass(ArrayList<Boolean[]> data) {
		return numTrue(data) > numFalse(data);
	}

	public static int numTrue(ArrayList<Boolean[]> data) {
		int count = 0;
		for (Boolean[] row : data) {
			if (row[row.length - 1]) {
				count++;
			}
		}
		return count;
	}

	public static int numFalse(ArrayList<Boolean[]> data) {
		int count = 0;
		for (Boolean[] row : data) {
			if (!row[row.length - 1]) {
				count++;
			}
		}
		return count;
	}

	public static double entropy(ArrayList<Boolean[]> data) {

		double Qtrue = numTrue(data) / (double) data.size();
		double Qfalse = numFalse(data) / (double) data.size();

		double entropy = 0;

		if (Qtrue != 0 && !Double.isNaN(Qtrue)) {
			entropy += Qtrue * (Math.log(Qtrue) / Math.log(2));
		}

		if (Qfalse != 0 && !Double.isNaN(Qfalse)) {
			entropy += Qfalse * (Math.log(Qfalse) / Math.log(2));
		}

		return -entropy;
	}

	public static double findGain(ArrayList<Boolean[]> data, int attribute) {

		ArrayList<Boolean[]> trueRows = getTrueRows(data, attribute);
		ArrayList<Boolean[]> falseRows = getFalseRows(data, attribute);

		double gain = entropy(data)
				- ((trueRows.size() / (double) data.size()) * entropy(trueRows) + (falseRows
						.size() / (double) data.size()) * entropy(falseRows));

		if (Double.isNaN(gain))
			return 0.0;
		else
			return gain;
	}

	public static int selectAttribute(ArrayList<Boolean[]> data,
			HashSet<Integer> attributes) {
		double maxGain = -Double.MIN_VALUE;
		int attribute = -1;

		for (Integer i : attributes) {
			double gain = findGain(data, i);
			if (gain > maxGain) {
				maxGain = gain;
				attribute = i;
			}
		}

		return attribute;
	}

	public static Node buildTree(ArrayList<Boolean[]> data,
			HashSet<Integer> attributes) {
		Node node = new Node();

		if (allOneClass(data)) {
			node.label = majorityClass(data);
			return node;
		}

		if (attributes.size() == 0) { // no more attributes to split on
			node.label = majorityClass(data);
			return node;
		}

		node.attribute = selectAttribute(data, attributes);
		attributes.remove(node.attribute);

		ArrayList<Boolean[]> trueRows = getTrueRows(data, node.attribute);
		if (trueRows.size() == 0) {
			node.trueChild = new Node();
			node.trueChild.label = majorityClass(data);
		} else {
			node.trueChild = buildTree(trueRows, attributes);
		}

		ArrayList<Boolean[]> falseRows = getFalseRows(data, node.attribute);
		if (falseRows.size() == 0) {
			node.falseChild = new Node();
			node.falseChild.label = majorityClass(data);
		} else {
			node.falseChild = buildTree(falseRows, attributes);
		}

		return node;
	}

	public static boolean classify(Node node, Boolean[] row) {
		if (node.label != null) {
			return node.label;
		} else {
			boolean direction = row[node.attribute];
			if (direction) {
				return classify(node.trueChild, row);
			} else {
				return classify(node.falseChild, row);
			}
		}
	}

	public static void traverseTree(Node node, int tab, String dir) {
		if (node.label != null) {
			for (int i = 0; i < tab; i++) {
				System.out.print("\t");
			}
			System.out.println(dir + "the final answer is " + node.label);
		} else {
			for (int i = 0; i < tab; i++) {
				System.out.print("\t");
			}
			System.out.println(dir + "split on " + node.attribute);
			traverseTree(node.trueChild, tab + 1, "if true: ");
			traverseTree(node.falseChild, tab + 1, "if false: ");
		}
	}

	public static void main(String[] args) throws Exception {

		testData = readFile("./test.txt");
		ArrayList<Boolean[]> trainingData = readFile("./training.txt");
		ArrayList<Boolean> actualClassValue = new ArrayList<Boolean>();
		// System.out.println("for 100% test data" + testData.size());
		for (int i = 0; i < trainingData.size(); i++) {
			Boolean[] record = trainingData.get(i);
			actualClassValue.add(record[record.length - 1]);
		}

		// ArrayList<Node> trees = new ArrayList<Node>();
		// HashSet<Integer> attributes = getAttributeList(testData);
		// Node root = buildTree(testData, attributes);
		// traverseTree(root, 0, "");
		// trees.add(root);

		for (int i = 0; i < percentage.length; i++) {
			ArrayList<Boolean[]> modifiedTestData = generateTestDataLists(percentage[i]);// 20,50,80
			System.out.println("Unmodified dataset size: " + modifiedTestData.size());
			HashSet<Integer> attributes = getAttributeList(modifiedTestData);
			Node root = buildTree(modifiedTestData, attributes);
			traverseTree(root, 0, "");
			// trees.add(root);
			ArrayList<Boolean> derivedClassValue = new ArrayList<Boolean>();
			for (int j = 0; j < trainingData.size(); j++) {
				Boolean[] record = trainingData.get(j);
				// actualClassValue.add(record[record.length - 1]);
				///System.out.println(Arrays.toString(record));
				// System.out.println("With complete data");
				derivedClassValue.add(classify(root, record));
				///System.out.println(classify(root, record));

				// System.out.println("With half data");
				// System.out.println(classify(trees.get(1), record));
			}
			Double meanSquareError = compareResults(actualClassValue, derivedClassValue);
			System.out.println(meanSquareError);
		}

	}

	private static Double compareResults(ArrayList<Boolean> actualClassValue,
			ArrayList<Boolean> derivedClassValue) {
		int countActualTrue = 0;
		int countDerivedTrue = 0;
		for (int i = 0; i < actualClassValue.size(); i++) {
			if (actualClassValue.get(i)) {
				countActualTrue++;
			}
			if (derivedClassValue.get(i)) {
				countDerivedTrue++;
			}
		}
		Double msq = (double) ((countActualTrue-countDerivedTrue) * (countActualTrue-countDerivedTrue));
		return msq;

	}

	private static HashSet<Integer> getAttributeList(
			ArrayList<Boolean[]> modifiedTestData) {
		HashSet<Integer> attributes;
		attributes = new HashSet<>();
		for (int i = 0; i < modifiedTestData.get(0).length - 1; i++) {
			attributes.add(i);
		}
		return attributes;
	}

	private static ArrayList<Boolean[]> generateTestDataLists(int percentage) {

		ArrayList<Boolean[]> data = new ArrayList<Boolean[]>();
		int size = testData.size();
		int newSize = (int) ((size * percentage) / 100);
		// System.out.println("for 50% test data full data" + testData.size()+
		// newSize);

		for (int i = 0; i < newSize; i++) {
			Boolean[] record = testData.get(i);
			// System.out.println("Building half tree" +
			// Arrays.toString(record));
			data.add(record);
		}
		return data;
	}

	private static void writeToFile(StringBuilder toBeWritten, String filename) {
		FileOutputStream fop = null;
		File file;
		String toWriteInFile = new String(toBeWritten);
		try {

			file = new File(filename);
			fop = new FileOutputStream(file);

			// if file doesnt exists, then create it
			// if (!file.exists()) {
			file.createNewFile();
			// }

			// get the content in bytes
			byte[] contentInBytes = toWriteInFile.getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
