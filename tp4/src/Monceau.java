public class Monceau {
    private static final int ROOT = 1; // la case 0 n'est jamais utilisée
    private Integer[] tab;
    private int length; // nombre d'éléments; ils occupent tab[1..length]

    public Monceau() {
        tab = new Integer[10];
        length = 0;
    }

    public int peek() {
        if( isEmpty( ) )
            return 0;
        return tab[ROOT];
    }

    public int poll() {
        if( isEmpty( ) )
            return 0;
        int minItem = peek();
        tab[ ROOT ] = tab[ length ];
        tab[ length-- ] = null;
        if( !isEmpty( ) )
            percolateDown( ROOT );
        return minItem;
    }

    public void add(Integer n) {
        if(length == tab.length - 1)
            doubleArraySize();

        int hole = ++length;
        for( ; hole > ROOT && n.compareTo( tab[ hole / 2 ] ) < 0; hole /= 2)
            tab[ hole ] = tab[ hole / 2 ];
        tab[ hole ] = n;
    }

    public boolean isEmpty() {
        return length == 0;
    }

    public int size(){
        return length;
    }

    private void doubleArraySize(){
        Integer[] temp = new Integer[tab.length * 2];
        for(int i = ROOT; i <= length; i++){
            temp[i] = tab[i];
        }
        tab = temp;
    }

    private void percolateDown( int hole )
    {
        int child;
        int tmp = tab[ hole ];
        for( ; hole * 2 <= length; hole = child ){
            child = hole * 2; //Considérer fils de gauche
            if( child != length && tab[ child + 1 ].compareTo( tab[ child ] ) < 0 ) //et fils droit<fils gauche
                child++; //Considérer fils droit
            if( tab[ child ].compareTo( tmp ) < 0 )//fils considéré< élément à percoler
                tab[ hole ] = tab[ child ];//Remonter le fils courrent de un niveau
            else
                break; //sortir de la boucle. L'élément à percoler sera inséré à position hole
        }
        tab[ hole ] = tmp; // Insérer l'élément à percoler à la position hole
    }
}