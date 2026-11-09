import java.util.Arrays;
import java.util.Random;

public class HeapSort {

	private enum SortingType {
		ASCENDING,
		DESCENDING
	}


	// heapSort est O(N log N)
	// O(N log N) pour insérer la liste dans le monceau
	// O(N log N) pour la remettre dans la liste à la fin
	// O(N log N) + O(N log N) = O(N log N)

	// la complexité spatiale est O(N), puisque le monceau copie tous les éléments
	static void heapSort(int[] arrayToSort, SortingType sort) {
		Monceau<Integer> monceau = new Monceau<>(arrayToSort.length);
		for (int val : arrayToSort) // O(N): O(1) * n éléments de la liste
			monceau.add(val); // O(log N): add est O(log N) puisqu'il parcour la hauteur de l'arbre

		if(sort == SortingType.ASCENDING){
			for(int i = 0; i < arrayToSort.length; i++) // O(N): O(1) * n éléments de la liste
				arrayToSort[i] = monceau.poll(); // O(log N): poll est O(log N) puisqu'il parcour la hauteur de l'arbre
		}
		else{
			for(int i = arrayToSort.length - 1; i >= 0; i--) // O(N): O(1) * n éléments de la liste
				arrayToSort[i] = monceau.poll(); // O(log N): poll est O(log N) puisqu'il parcour la hauteur de l'arbre
		}
	}
	
	public static void main(String[] args) {
		
		// DONE: Faite le triage par monceau d'un tableau d'entier.

		Random random = new Random();
		int[] original = new int[20];
		for (int i = 0; i < original.length; i++) {
			original[i] = random.nextInt(100);
		}
		System.out.println("Avant :       " + Arrays.toString(original));


		int[] croissant = original.clone();
		int[] decroissant = original.clone();

		heapSort(croissant, SortingType.ASCENDING);
		heapSort(decroissant, SortingType.DESCENDING);

		System.out.println("Croissant :   " + Arrays.toString(croissant));
		System.out.println("Décroissant : " + Arrays.toString(decroissant));
	}
}
