import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;


public class ID3Tree {
	
	public static ArrayList<Boolean[]> readFile(String filename) throws Exception {
		
		ArrayList<Boolean[]> data = new ArrayList<>();
		
		Scanner input = new Scanner(new File(filename));
		
		while (input.hasNext()) {
			String line = input.nextLine();
			String[] tokens = line.split("[,]");
			
			Boolean[] record = new Boolean[tokens.length];
			for (int i=0; i<tokens.length; i++) {
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
	
	public static ArrayList<Boolean[]> getTrueRows(ArrayList<Boolean[]> data, int attribute) {// rows in the remaining database, on which this specific attribute=true
		ArrayList<Boolean[]> trueRows = new ArrayList<>();
		
		for (Boolean[] row: data) {
			if (row[attribute]) {
				trueRows.add(row);
			}
		}
		
		return trueRows;
	}
	
	public static ArrayList<Boolean[]> getFalseRows(ArrayList<Boolean[]> data, int attribute) {
		ArrayList<Boolean[]> falseRows = new ArrayList<>();
		
		for (Boolean[] row: data) {
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
		for (Boolean[] row: data) {
			if (row[row.length-1]) {
				count++;
			}
		}
		return count;
	}
	
	public static int numFalse(ArrayList<Boolean[]> data) {
		int count = 0;
		for (Boolean[] row: data) {
			if (!row[row.length-1]) {
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
		
		double gain = entropy(data) - 
				((trueRows.size() / (double) data.size()) * entropy(trueRows) + 
				 (falseRows.size() / (double) data.size()) * entropy(falseRows));
		
		if (Double.isNaN(gain)) 
			return 0.0;
		else 
			return gain;
	}
	 
	public static int selectAttribute(ArrayList<Boolean[]> data, HashSet<Integer> attributes) {
		double maxGain = -Double.MIN_VALUE;
		int attribute = -1;
		
		for (Integer i: attributes) {
			double gain = findGain(data, i);
			if (gain > maxGain) {
				maxGain = gain;
				attribute = i;
			}
		}
		
		return attribute;
	}
	
	public static Node buildTree(ArrayList<Boolean[]> data, HashSet<Integer> attributes) {
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
			for (int i=0; i<tab; i++) {
				System.out.print("\t");
			}
			System.out.println(dir + "the final answer is " + node.label);
		} else {
			for (int i=0; i<tab; i++) {
				System.out.print("\t");
			}
			System.out.println(dir + "split on " + node.attribute);
			traverseTree(node.trueChild, tab+1, "if true: ");
			traverseTree(node.falseChild, tab+1, "if false: ");
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		ArrayList<Boolean[]> trainingData = readFile("./training.txt");
		
		HashSet<Integer> attributes = new HashSet<>();
		for (int i=0; i<trainingData.get(0).length-1; i++) {
			attributes.add(i);
		}
		
		Node root = buildTree(trainingData, attributes);
		
		traverseTree(root, 0, "");
		
		//Boolean[] record = {false, true, true};
		//Boolean[] record = {true, false, false};
		//Boolean[] record = {false, false, true};
		//Boolean[] record = {true, true, false};
		ArrayList<Boolean[]> testData = readFile("./test.txt");
		for (int i = 0; i < testData.size(); i++) {
			Boolean[] record = testData.get(i);
			System.out.println(Arrays.toString(record));
			System.out.println(classify(root, record));
		}
		
	}

}
