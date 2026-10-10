package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;


/**
 * Classe représentant une structure définissant l'organisation des champs dans un tampon binaire.
 * Elle permet de calculer les offsets des champs dans la mémoire.
 *
 * @author Alban (401009)
 */
public class Structure {
    private final int[] offset;
    private final int finalSize;

    /**
     * Enumération des types de champs disponibles, avec leur taille en octets.
     * Chaque type de champ correspond à une taille fixe en mémoire.
     */
    public enum FieldType {
        U8(1), U16(2), S32(4);

        private final int size;

        FieldType(int size) {
            this.size = size;
        }

        /**
         * Retourne la taille du type de champ en octets.
         *
         * @return Taille en octets.
         */
        public int size() {
            return size;
        }
    }

    /**
     * Construit une structure à partir d'un ensemble de champs.
     * Chaque champ doit avoir un index consécutif, commençant à zéro.
     *
     * @param field Tableau de champs définissant la structure.
     * @throws IllegalArgumentException si aucun champ n'est fourni ou si les indices ne sont
     * pas consécutifs.
     */
    public Structure(Field... field) {
        Preconditions.checkArgument(field.length != 0);
        for (int i = 0; i < field.length; i++) {
            Preconditions.checkArgument(field[i].index() == i);
        }
        this.offset = new int[field.length];
        int size = 0;
        for (int i = 0; i < field.length; i++) {
            offset[i] = size;
            size += field[i].type().size();
        }
        this.finalSize = size;
    }

    /**
     * Crée un champ avec un index et un type donné.
     * Cette méthode permet de définir un champ dans la structure en spécifiant
     * son index et son type de données.
     *
     * @param index L'index du champ.
     * @param type Le type du champ.
     * @return Un nouvel objet Field.
     */
    public static Field field(int index, FieldType type) {
        return new Field(index, type);
    }

    /**
     * Retourne la taille totale de la structure en octets.
     * Cette taille correspond à la somme des tailles de tous les champs qui la composent.
     *
     * @return Taille totale de la structure.
     */
    public int totalSize() {
        return finalSize;
    }

    /**
     * Calcule l'offset d'un champ donné pour un élément donné dans la structure.
     * L'offset est calculé en fonction de l'index du champ et de l'index de l'élément
     * dans la structure.
     * L'offset représente la position du champ dans la mémoire.
     *
     * @param fieldIndex L'index du champ.
     * @param elementIndex L'index de l'élément.
     * @return L'offset calculé en octets.
     * @throws IndexOutOfBoundsException si les indices sont hors limites.
     */
    public int offset(int fieldIndex, int elementIndex) {
        if (fieldIndex < 0 || fieldIndex >= offset.length) {
            throw new IndexOutOfBoundsException();
        }
        if (elementIndex < 0) {
            throw new IndexOutOfBoundsException();
        }
        return (elementIndex * this.totalSize()) + offset[fieldIndex];
    }

    /**
     * Représente un champ dans la structure avec un index et un type.
     * Un champ est défini par son index et le type de données qu'il contient.
     */
    public record Field(int index,
                        FieldType type) {
        /**
         * Constructeur de {@link Field}.
         *
         * @param index L'index du champ.
         * @param type Le type du champ.
         * @throws NullPointerException Si le type du champ est nul.
         */
        public Field {
            if (type == null) {
                throw new NullPointerException();
            }
        }
    }
}
