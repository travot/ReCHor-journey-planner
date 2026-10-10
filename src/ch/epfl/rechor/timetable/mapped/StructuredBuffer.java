package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

import java.nio.ByteBuffer;
/**
 * Classe représentant un tampon binaire structuré, permettant l'accès aux données selon une
 * structure définie. Ce tampon assure que les données sont correctement alignées selon
 * la structure fournie. Elle permet de lire des valeurs spécifiques à partir du tampon en utilisant
 * les informations de la structure pour obtenir les offsets des champs.
 * Chaque champ de la structure est accessible en utilisant son index et l'index de l'élément dans
 * le tampon.
 * La classe offre des méthodes pour récupérer les valeurs des différents types de champs :
 * U8, U16, et S32.
 *
 * @author Alban (401009)
 */
public class StructuredBuffer {

    private final Structure structure;
    private final ByteBuffer buffer;
    private final int numElements;

    /**
     * Construit un tampon structuré avec une structure donnée et un tampon binaire.
     * Cette méthode initialise un tampon structuré en vérifiant que le tampon binaire et
     * la structure sont valides.
     * De plus, elle s'assure que la taille du tampon est un multiple exact de la taille
     * de la structure,
     * garantissant ainsi que tous les éléments dans le tampon sont correctement alignés.
     *
     * @param structure La structure définissant l'organisation des données dans le tampon.
     * @param buffer Le tampon binaire contenant les données.
     * @throws IllegalArgumentException Si l'un des arguments est null ou si la taille du tampon
     * n'est pas un multiple de la taille de la structure.
     */
    public StructuredBuffer(Structure structure, ByteBuffer buffer) {
        Preconditions.checkArgument(buffer != null);
        Preconditions.checkArgument(structure != null);
        int structureSize = structure.totalSize();
        this.structure = structure;
        this.buffer = buffer;
        Preconditions.checkArgument(buffer.remaining() % structureSize == 0);
        this.numElements = buffer.remaining() / structureSize;
    }

    /**
     * Retourne le nombre total d'éléments stockés dans le tampon.
     * Ce nombre correspond à la quantité d'éléments de la structure qui peuvent être
     * lus dans le tampon.
     *
     * @return Le nombre d'éléments dans le tampon.
     */
    public int size(){
        return numElements;
    }

    /**
     * Retourne une valeur non signée de 8 bits à l'index spécifié.
     * Cette méthode permet de lire une valeur de type U8 à partir du tampon structuré.
     *
     * @param fieldIndex L'index du champ dans la structure. Un champ de type U8 doit être spécifié
     * @param elementIndex L'index de l'élément dans le tampon. Cet index détermine
     *                     quel élément de la structure
     *                    à cet emplacement dans le tampon sera lu.
     * @return La valeur non signée de 8 bits présente à l'index spécifié.
     */
    public int getU8(int fieldIndex, int elementIndex) {
        return Byte.toUnsignedInt(buffer.get(structure.offset(fieldIndex, elementIndex)));
    }

    /**
     * Retourne une valeur non signée de 16 bits à l'index spécifié.
     * Cette méthode permet de lire une valeur de type U16 à partir du tampon structuré.
     *
     * @param fieldIndex L'index du champ dans la structure. Un champ de type U16 doit
     *                   être spécifié.
     * @param elementIndex L'index de l'élément dans le tampon.
     * @return La valeur non signée de 16 bits présente à l'index spécifié.
     */
    public int getU16(int fieldIndex, int elementIndex) {
        return Short.toUnsignedInt(buffer.getShort(structure.offset(fieldIndex, elementIndex)));
    }

    /**
     * Retourne une valeur signée de 32 bits à l'index spécifié.
     * Cette méthode permet de lire une valeur de type S32 à partir du tampon structuré.
     *
     * @param fieldIndex L'index du champ dans la structure. Un champ de type S32 doit être spécifié
     * @param elementIndex L'index de l'élément dans le tampon.
     * @return La valeur signée de 32 bits présente à l'index spécifié.
     */
    public int getS32(int fieldIndex, int elementIndex) {
        return buffer.getInt(structure.offset(fieldIndex, elementIndex));
    }
}