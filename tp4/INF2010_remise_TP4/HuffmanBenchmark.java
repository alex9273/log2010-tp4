import java.io.File;
import java.util.Arrays;

public class HuffmanBenchmark {

	private static final int WARMUP = 5;
	private static final int RUNS = 15;

	private static double median(long[] values) {
		long[] sorted = values.clone();
		Arrays.sort(sorted);
		return sorted[sorted.length / 2];
	}

	public static void main(String[] args) {
		String dossier = args.length > 0 ? args[0] : "tests";
		File[] fichiers = new File(dossier).listFiles(f -> f.isFile());
		Arrays.sort(fichiers);

		System.out.printf(
				"fichier", "original", "compresse", "k", "profondeur", "PQ", "frequence(us)", "arbre(us)", "codes(us)",
				"encod(ms)", "total(ms)");

		for (File f : fichiers) {
			String tmp = "tmp_sortie.huf";
			for (boolean pq : new boolean[] { true, false }) {
				HuffmanCompression hc = new HuffmanCompression();
				long[] freq = new long[RUNS], arbre = new long[RUNS], codes = new long[RUNS];
				long[] encod = new long[RUNS], total = new long[RUNS];
				HuffmanCompression.Stats s = null;
				for (int i = 0; i < WARMUP + RUNS; i++) {
					hc.compress(f.getPath(), tmp, pq);
					s = hc.getStats();
					if (i >= WARMUP) {
						freq[i - WARMUP] = s.tempsFrequencesNs;
						arbre[i - WARMUP] = s.tempsArbreNs;
						codes[i - WARMUP] = s.tempsCodesNs;
						encod[i - WARMUP] = s.tempsEncodageNs;
						total[i - WARMUP] = s.tempsTotalNs;
					}
				}
				System.out.printf(
						f.getName(), s.tailleOriginale, s.tailleCompressee, s.nbOctetsDifferents,
						s.profondeurArbre, pq ? "avec" : "sans",
						median(freq) / 1e3, median(arbre) / 1e3, median(codes) / 1e3,
						median(encod) / 1e6, median(total) / 1e6);
				System.out.printf(" ",s.tailleArbre, s.tailleDonnees);
			}
			new File(tmp).delete();
		}
	}
}