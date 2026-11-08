import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class HuffmanCompression {	

	private static final int NB_SYMBOLES = 256;

	public static class Stats {
		public boolean avecFilePriorite;
		public long tailleOriginale;
		public long tailleCompressee;
		public long tailleArbre;
		public long tailleDonnees;
		public int nbOctetsDifferents;
		public int profondeurArbre;
		public long tempsFrequencesNs;
		public long tempsArbreNs;
		public long tempsCodesNs;
		public long tempsEncodageNs;
		public long tempsTotalNs;
	}
 
	private Stats stats = new Stats();
 
	public Stats getStats() {
		return stats;
	}
	
	private class HuffmanNode implements Comparable<HuffmanNode> {
		
		private final byte value;
		private final int frequency;
		private final int minValue;
		private final HuffmanNode left;
		private final HuffmanNode right;
 
		public HuffmanNode(byte value, int frequency) {
			this.value = value;
			this.frequency = frequency;
			this.minValue = value & 0xFF;
			this.left = null;
			this.right = null;
		}
		
		public HuffmanNode(HuffmanNode left, HuffmanNode right) {
			this.value = 0;
			this.frequency = left.frequency + right.frequency;
			this.minValue = Math.min(left.minValue, right.minValue);
			this.left = left;
			this.right = right;
		}

		public HuffmanNode() {
			this.value = 0;
			this.frequency = 0;
			this.minValue = 0;
			this.left = null;
			this.right = null;
		}

		public HuffmanNode(HuffmanNode left, HuffmanNode right, boolean lecture) {
			this.value = 0;
			this.frequency = 0;
			this.minValue = 0;
			this.left = left;
			this.right = right;
		}
 
		public boolean isLeaf() {
			return left == null && right == null;
		}

		@Override
		public int compareTo(HuffmanNode other) {
			if (frequency != other.frequency)
				return Integer.compare(frequency, other.frequency);
			return Integer.compare(minValue, other.minValue);
		}
	}
	
	private int computeMaxDepth(HuffmanNode node) {
		if (node == null || node.isLeaf())
			return 0;
		return 1 + Math.max(computeMaxDepth(node.left), computeMaxDepth(node.right));
	}
	
	private void generateHuffmanCodes(HuffmanNode node, String prefix, String[] codes) {
		if (node.isLeaf()) {
			codes[node.value & 0xFF] = prefix.isEmpty() ? "0" : prefix;
			return;
		}
		generateHuffmanCodes(node.left, prefix + "0", codes);
		generateHuffmanCodes(node.right, prefix + "1", codes);
	}

	
	private BitStreamWriter huffmanWriter;
	private BitStreamReader huffmanReader;
	
	private void recursiveWriteTree(HuffmanNode node) throws IOException {
		if (node.isLeaf()) {
			huffmanWriter.writeBit((byte) 1);
			huffmanWriter.writeByte(node.value);
		} 
		else {
			huffmanWriter.writeBit((byte) 0);
			recursiveWriteTree(node.left);
			recursiveWriteTree(node.right);
		}
	}
	
	private HuffmanNode recursiveReadTree() throws IOException {
		if (huffmanReader.readBit() == 1) {
			return new HuffmanNode(huffmanReader.readByte(), 0);
		}
		HuffmanNode left = recursiveReadTree();
		HuffmanNode right = recursiveReadTree();
		return new HuffmanNode(left, right, true);
	}
	
	
	private void writeHuffmanCompressedFile(HuffmanNode tree, String compressedMessage, String outputFilePath) {
		try {
			huffmanWriter = new BitStreamWriter(outputFilePath);
			recursiveWriteTree(tree);
			huffmanWriter.writeSize(compressedMessage.length());
			for (int i = 0; i < compressedMessage.length(); i++)
				huffmanWriter.writeBit((byte) (compressedMessage.charAt(i) == '1' ? 1 : 0));
			huffmanWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] readHuffmanCompressedFile(String inputFilePath) {
		try {
			huffmanReader = new BitStreamReader(inputFilePath);

			HuffmanNode tree = recursiveReadTree();
			int nbBits = huffmanReader.readSize();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			HuffmanNode current = tree;
			for (int i = 0; i < nbBits; i++) {
				if (!tree.isLeaf())
					current = (huffmanReader.readBit() == 0) ? current.left : current.right;
				else
					huffmanReader.readBit();
				if (current.isLeaf()) {
					out.write(current.value);
					current = tree;
				}
			}

			huffmanReader.close();
			return out.toByteArray();

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private int[] getFrequencies(String inputFilePath) throws IOException {
		int[] frequencies = new int[NB_SYMBOLES];
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputFilePath))) {
			int b;
			while ((b = in.read()) != -1)
				frequencies[b]++;
		}
		return frequencies;
	}
	
	public void printFrequencies(String inputFilePath) throws IOException {
		int[] frequencies = getFrequencies(inputFilePath);
		for (int i = 0; i < frequencies.length; i++)
			if (frequencies[i] > 0)
				System.out.println(i + "\t" + frequencies[i]);
	}

	private int[] getFrequencies(byte[] data) {
		int[] frequencies = new int[NB_SYMBOLES];
		for (byte b : data)
			frequencies[b & 0xFF]++;
		return frequencies;
	}
	
	private HuffmanNode generateHuffmanTreeWithPriorityQueue(int[] frequencies) {
		PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();
		for (int i = 0; i < frequencies.length; i++)
			if (frequencies[i] > 0)
				queue.add(new HuffmanNode((byte) i, frequencies[i]));
 
		if (queue.isEmpty())
			return new HuffmanNode((byte) 0, 0);
 
		while (queue.size() > 1) {
			HuffmanNode a = queue.poll();
			HuffmanNode b = queue.poll();
			queue.add(new HuffmanNode(a, b));
		}
		return queue.poll();
	}
	
	private HuffmanNode generateHuffmanTreeWithoutPriorityQueue(int[] frequencies) {
		ArrayList<HuffmanNode> nodes = new ArrayList<>();
		for (int i = 0; i < frequencies.length; i++)
			if (frequencies[i] > 0)
				nodes.add(new HuffmanNode((byte) i, frequencies[i]));
 
		if (nodes.isEmpty())
			return new HuffmanNode((byte) 0, 0);
 
		while (nodes.size() > 1) {
			HuffmanNode a = removeMinimum(nodes);
			HuffmanNode b = removeMinimum(nodes);
			nodes.add(new HuffmanNode(a, b));
		}
		return nodes.get(0);
	}
 
	private int countLeaves(int[] frequencies) {
		int count = 0;
		for (int f : frequencies)
			if (f > 0)
				count++;
		return Math.max(count, 1);
	}

	private HuffmanNode removeMinimum(ArrayList<HuffmanNode> nodes) {
		int minIndex = 0;
		for (int i = 1; i < nodes.size(); i++)
			if (nodes.get(i).compareTo(nodes.get(minIndex)) < 0)
				minIndex = i;
		HuffmanNode min = nodes.get(minIndex);
		HuffmanNode last = nodes.remove(nodes.size() - 1);
		if (minIndex < nodes.size())
			nodes.set(minIndex, last);
		return min;
	}
	
	public void compress(String inputFilePath, String outputFilePath) {
		compress(inputFilePath, outputFilePath, true);
	}
	
	public void compress(String inputFilePath, String outputFilePath, boolean usePriorityQueue) {
		try {
			byte[] data = Files.readAllBytes(Paths.get(inputFilePath));

			long t0 = System.nanoTime();
			int[] frequencies = getFrequencies(data);
			long t1 = System.nanoTime();
			HuffmanNode tree = usePriorityQueue ? generateHuffmanTreeWithPriorityQueue(frequencies) : generateHuffmanTreeWithoutPriorityQueue(frequencies);
			long t2 = System.nanoTime();
			String[] codes = new String[NB_SYMBOLES];
			generateHuffmanCodes(tree, "", codes);
			long t3 = System.nanoTime();
			StringBuilder message = new StringBuilder();
			for (byte b : data)message.append(codes[b & 0xFF]);
			long t4 = System.nanoTime();

			String compressedMessage = message.toString();
			writeHuffmanCompressedFile(tree, compressedMessage, outputFilePath);

			int k = countLeaves(frequencies);
			stats = new Stats();
			stats.avecFilePriorite = usePriorityQueue;
			stats.tailleOriginale = data.length;
			stats.tailleCompressee = Files.size(Paths.get(outputFilePath));
			stats.tailleArbre = (10L * k - 1 + 7) / 8;
			stats.tailleDonnees = (compressedMessage.length() + 7L) / 8;
			stats.nbOctetsDifferents = k;
			stats.profondeurArbre = computeMaxDepth(tree);
			stats.tempsFrequencesNs = t1 - t0;
			stats.tempsArbreNs = t2 - t1;
			stats.tempsCodesNs = t3 - t2;
			stats.tempsEncodageNs = t4 - t3;
			stats.tempsTotalNs = t4 - t0;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void decompress(String inputFilePath, String outputFilePath) {
		byte[] data = readHuffmanCompressedFile(inputFilePath);
		if (data == null)
			return;
		try {
			Files.write(Paths.get(outputFilePath), data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		HuffmanCompression hc = new HuffmanCompression();
 
		if (args.length < 2) {
			System.out.println("Usage : java HuffmanCompression <c|cs|d> <entrée> <sortie> ou f <entrée>");
			System.out.println("  c  = compresser avec file de priorité, cs = sans file de priorité, d = décompresser");
			return;
		}
		switch (args[0]) {
			case "c":  hc.compress(args[1], args[2], true);  break;
			case "cs": hc.compress(args[1], args[2], false); break;
			case "d":  hc.decompress(args[1], args[2]);
			break;
			case "f":
				try {
					hc.printFrequencies(args[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			default:   System.out.println("Mode inconnu : " + args[0]);
		}
	}
	
}
