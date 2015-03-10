import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class ID3Tree {

	public static void main(String[] args) throws Exception {
		ID3Tree id3Tree = new ID3Tree();
		System.out.println("Showing results for unmodified data.");
		ID3Tree.UnmodifiedTreeUnmodifiedData uTUD = id3Tree.new UnmodifiedTreeUnmodifiedData();
		System.out.println("Showing results for randomized data.");
		ModifiedTreeRandomizedData mTRD = new ModifiedTreeRandomizedData(
				id3Tree);
	}

	// start of class UnmodifiedTreeUnmodifiedData
	public class UnmodifiedTreeUnmodifiedData {
		public ArrayList<Boolean[]> trainingData = new ArrayList<Boolean[]>();
		public int[] percentage = { 1, 2, 5, 10, 25, 50, 75, 100 };

		public UnmodifiedTreeUnmodifiedData() {

			try {
				trainingData = readFile("./training.txt");
				ArrayList<Boolean[]> testData = readFile("./test.txt");
				ArrayList<Boolean> actualClassValue = new ArrayList<Boolean>();
				populateAndSave(testData, actualClassValue);
				
				for (int i = 0; i < percentage.length; i++) {
					ArrayList<Boolean[]> modifiedTestData = generateTestDataLists(percentage[i]);// 20,50,80
					System.out
							.println(percentage[i]
									+ "% of training dataset used to build decision tree.");
					HashSet<Integer> attributes = getAttributeList(modifiedTestData);
					Node root = buildTree(modifiedTestData, attributes);
					traverseTree(root, 0, "");
					ArrayList<Boolean> derivedClassValue = new ArrayList<Boolean>();
					StringBuilder toBeWritten = new StringBuilder();
					for (int j = 0; j < testData.size(); j++) {
						Boolean[] record = testData.get(j);
						derivedClassValue.add(classify(root, record));
						toBeWritten.append(derivedClassValue.get(j));
						toBeWritten
								.append(System.getProperty("line.separator"));
					}
					String filename = "unmodified_data_output_" + percentage[i]
							+ "_percent";
					writeToFile(toBeWritten, filename);
					Double meanSquareError = evaluateResults(actualClassValue,
							derivedClassValue);
					System.out.println(meanSquareError);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void populateAndSave(ArrayList<Boolean[]> testData,
				ArrayList<Boolean> actualClassValue) {
			for (int i = 0; i < testData.size(); i++) {
				Boolean[] record = testData.get(i);
				actualClassValue.add(record[record.length - 1]);
			}
			StringBuilder toBeWritten = new StringBuilder();
			for (int j = 0; j < actualClassValue.size(); j++) {
				toBeWritten.append(actualClassValue.get(j));
				toBeWritten.append(System.getProperty("line.separator"));
			}
			String filename = "actual_class_value";
			writeToFile(toBeWritten, filename);
		}

		public ArrayList<Boolean[]> generateTestDataLists(int percentage) {

			ArrayList<Boolean[]> data = new ArrayList<Boolean[]>();
			int size = trainingData.size();
			int newSize = (int) ((size * percentage) / 100);
			// System.out.println("for 50% test data full data" +
			// testData.size()+
			// newSize);

			for (int i = 0; i < newSize; i++) {
				Boolean[] record = trainingData.get(i);
				// System.out.println("Building half tree" +
				// Arrays.toString(record));
				data.add(record);
			}
			return data;
		}

		public ArrayList<Boolean[]> getTrueRows(ArrayList<Boolean[]> data,
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

		public ArrayList<Boolean[]> getFalseRows(ArrayList<Boolean[]> data,
				int attribute) {
			ArrayList<Boolean[]> falseRows = new ArrayList<>();

			for (Boolean[] row : data) {
				if (!row[attribute]) {
					falseRows.add(row);
				}
			}

			return falseRows;
		}

		public double findGain(ArrayList<Boolean[]> data, int attribute) {

			ArrayList<Boolean[]> trueRows = getTrueRows(data, attribute);
			ArrayList<Boolean[]> falseRows = getFalseRows(data, attribute);

			double gain = entropy(data)
					- ((trueRows.size() / (double) data.size())
							* entropy(trueRows) + (falseRows.size() / (double) data
							.size()) * entropy(falseRows));// should (falseRows...) be subtrated?

			if (Double.isNaN(gain))
				return 0.0;
			else
				return gain;
		}

		public int selectAttribute(ArrayList<Boolean[]> data,
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

		public Node buildTree(ArrayList<Boolean[]> data,
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

		public boolean allOneClass(ArrayList<Boolean[]> data) {
			return (numTrue(data) == data.size() || numFalse(data) == data
					.size());
		}

		public boolean majorityClass(ArrayList<Boolean[]> data) {
			return numTrue(data) > numFalse(data);
		}

		public int numTrue(ArrayList<Boolean[]> data) {
			int count = 0;
			for (Boolean[] row : data) {
				if (row[row.length - 1]) {
					count++;
				}
			}
			return count;
		}

		public int numFalse(ArrayList<Boolean[]> data) {
			int count = 0;
			for (Boolean[] row : data) {
				if (!row[row.length - 1]) {
					count++;
				}
			}
			return count;
		}

		public double entropy(ArrayList<Boolean[]> data) {

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

		public boolean classify(Node node, Boolean[] row) {
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
	}

	// end of class UnmodifiedTreeUnmodifiedData
	

	public static ArrayList<Boolean[]> readFile(String filename) throws Exception {

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

	public HashSet<Integer> getAttributeList(ArrayList<Boolean[]> resizedData) {
		HashSet<Integer> attributes;
		attributes = new HashSet<>();
		for (int i = 0; i < resizedData.get(0).length - 1; i++) {
			attributes.add(i);
		}
		return attributes;
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

	public static Double evaluateResults(ArrayList<Boolean> actualClassValue,
			ArrayList<Boolean> derivedClassValue) {
		double countActualTrue = 0.0;
		double countDerivedTrue = 0.0;
		for (int i = 0; i < actualClassValue.size(); i++) {
			if (actualClassValue.get(i)) {
				countActualTrue++;
			}
			if (derivedClassValue.get(i)) {
				countDerivedTrue++;
			}
		}
		countActualTrue /= 100.00;
		countDerivedTrue /= 100.00;
		Double msq = (double) ((countActualTrue - countDerivedTrue) * (countActualTrue - countDerivedTrue));
		return msq;

	}

	public void writeToFile(StringBuilder toBeWritten, String filename) {
		FileOutputStream fop = null;
		File file;
		String toWriteInFile = new String(toBeWritten);
		try {

			file = new File(filename);
			fop = new FileOutputStream(filename, false);

			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

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
