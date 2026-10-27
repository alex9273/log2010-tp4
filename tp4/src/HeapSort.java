import java.util.Arrays;
import java.util.Random;

public class HeapSort {

	private enum SortingType {
		ASCENDING,
		DESCENDING
	}
	
	static void heapSort(int[] arrayToSort, SortingType sort) {
		Monceau monceau = new Monceau(arrayToSort);
		if(sort == SortingType.ASCENDING){
			for(int i = 0; i < arrayToSort.length; i++)
				arrayToSort[i] = monceau.poll();
		}
		else{
			for(int i = arrayToSort.length - 1; i >= 0; i--)
				arrayToSort[i] = monceau.poll();
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
