package image_char_matching;

import java.util.*;

/**
 * Responsible for matching a specific brightness level (0.0 to 1.0)
 * to the most appropriate ASCII character from a given set.
 * This class maintains a database of characters and their "raw" brightness values.
 * It supports efficient lookups using a TreeMap to handle normalization and nearest-neighbor search.
 */
public class SubImgCharMatcher {

    /**
     * Stores characters grouped by their raw brightness.
     * Key: Raw brightness (0.0 to 1.0) calculated by pixel count.
     * Value: A TreeSet of characters with that specific brightness, sorted by ASCII value.
     */
    private final TreeMap<Double, TreeSet<Character>> brightnessMap;

    /**
     * Constructs a new matcher with the given set of allowed characters.
     * Calculates the brightness for every character in the array and adds it to the database.
     *
     * @param charset An array of characters to be used for ASCII art generation.
     */
    public SubImgCharMatcher(char[] charset) {
        this.brightnessMap = new TreeMap<>();
        for (char c : charset) {
            addChar(c);
        }
    }

    /**
     * Finds the character that best matches the given image brightness.
     * The method performs a linear stretch normalization on the character set
     * so that the darkest character acts as 0.0 and the brightest acts as 1.0.
     *
     * @param brightness The brightness of the sub-image (between 0.0 and 1.0).
     * @return The character with the closest normalized brightness.
     * If there is a tie, the character with the lowest ASCII value is returned.
     * @throws IllegalStateException If the character set is empty.
     */
    public char getCharByImageBrightness(double brightness) {
        if (this.brightnessMap.isEmpty()) {
            throw new IllegalStateException("Character set is empty.");
        }

        // Get the range of raw brightness values in our character set.
        double minBrightness = this.brightnessMap.firstKey();
        double maxBrightness = this.brightnessMap.lastKey();

        // If the min and max brightnesses are the same return the first char at that brightness.
        if (minBrightness == maxBrightness) {
            return this.brightnessMap.get(minBrightness).first();
        }

        // Find the original target raw brightness
        double targetRawBrightness = brightness * (maxBrightness - minBrightness) + minBrightness;

        // Find the closest raw brightnesses in the map
        Double floorKey = brightnessMap.floorKey(targetRawBrightness);
        Double ceilingKey = brightnessMap.ceilingKey(targetRawBrightness);

        // If the target is outside the current range
        if (floorKey == null) return brightnessMap.get(ceilingKey).first();
        if (ceilingKey == null) return brightnessMap.get(floorKey).first();

        // Compare the 2 bounds to find the lower one
        // FIXED: Typo 'floorDistacen' -> 'floorDistance'
        double floorDistance = Math.abs(floorKey - targetRawBrightness);
        double ceilingDistance = Math.abs(ceilingKey - targetRawBrightness);

        return (floorDistance <= ceilingDistance) ? brightnessMap.get(floorKey).first() :
                brightnessMap.get(ceilingKey).first();
    }


    /**
     * Adds a character to the matching database.
     * Calculates its raw brightness and places it into the TreeMap.
     *
     * @param c The character to add.
     */
    public void addChar(char c) {
        double brightnessC = calculateRawBrightness(c);

        // If the brightness does not exist, we create it
        this.brightnessMap.putIfAbsent(brightnessC, new TreeSet<>());
        this.brightnessMap.get(brightnessC).add(c);
    }

    /**
     * Removes a character from the database.
     * @param c The character to remove.
     */
    public void removeChar(char c) {
        double brightnessC = calculateRawBrightness(c);

        if (this.brightnessMap.containsKey(brightnessC)) {
            // FIXED: 'brightness' -> 'brightnessC'
            TreeSet<Character> charsAtBrightness = brightnessMap.get(brightnessC);

            // FIXED: Removed invalid .get() call
            charsAtBrightness.remove(c);

            // If no more char is left, remove the key
            if (charsAtBrightness.isEmpty()) {
                this.brightnessMap.remove(brightnessC);
            }
        }
    }

    /**
     * Helper method to calculate the raw brightness of a character.
     * Counts the number of white pixels in the 16x16 boolean representation
     * and divides by the total number of pixels (256).
     *
     * @param c The character to analyze.
     * @return A value between 0.0 and 1.0 representing raw brightness.
     */
    private double calculateRawBrightness(char c) {
        boolean[][] charImage = CharConverter.convertToBoolArray(c);
        int whitePixelCount = 0;

        for (boolean[] row : charImage) {
            for (boolean pixel : row) {
                if (pixel) {
                    whitePixelCount++;
                }
            }
        }
        return (double) whitePixelCount / (charImage.length * charImage[0].length);
    }
}