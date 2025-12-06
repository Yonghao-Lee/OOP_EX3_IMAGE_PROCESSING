package ascii_art;

import image.Image;
import image.ImageProcessing;
import image_char_matching.SubImgCharMatcher;

/**
 * Responsible for running the ASCII Art algorithm on a specific image.
 * This class handles the padding, division, brightness calculation, and character matching.
 */
public class AsciiArtAlgorithm {
    private final Image image;
    private final int resolution;
    private final SubImgCharMatcher matcher;

    // We use static fields to cache the results of the PREVIOUS run.
    // Because a new instance of this class is created for every run.
    // static fields are the only way to remember data between runs.
    private static Image lastImage = null;
    private static int lastResolution = 0;
    private static double[][] cachedBrightnessMatrix = null;

    /**
     * Constructor.
     * @param image The image to convert.
     * @param resolution The number of characters per row.
     * @param matcher The matcher object (already initialized with a charset).
     */
    public AsciiArtAlgorithm(Image image, int resolution, SubImgCharMatcher matcher) {
        this.image = image;
        this.resolution = resolution;
        this.matcher = matcher;
    }

    /**
     * Runs the algorithm.
     * @return A 2D array of characters representing the ASCII image.
     */
    public char[][] run() {
        Image paddedImage = ImageProcessing.padImage(image);

        int charsInRow = resolution;

        int subImageSize = paddedImage.getWidth() / resolution;
        int charsInCol = paddedImage.getHeight() / subImageSize;

        char[][] outputMatrix = new char[charsInCol][charsInRow];

        // If the image and resolution are identical to the last run, skip image processing.
        if (image == lastImage && resolution == lastResolution && cachedBrightnessMatrix != null) {

            // Use cached brightness values
            for (int r = 0; r < charsInCol; r++) {
                for (int c = 0; c < charsInRow; c++) {
                    double brightness = cachedBrightnessMatrix[r][c];
                    outputMatrix[r][c] = matcher.getCharByImageBrightness(brightness);
                }
            }

        } else {
            // Full Calculation

            // Divide the image into sub-images
            Image[][] subImages = ImageProcessing.divideImage(paddedImage, resolution);

            // Initialize the cache for next time
            cachedBrightnessMatrix = new double[subImages.length][subImages[0].length];
            lastImage = image;
            lastResolution = resolution;

            // Loop through sub-images, calculate brightness, and match char
            for (int r = 0; r < subImages.length; r++) {
                for (int c = 0; c < subImages[0].length; c++) {
                    // Calculate brightness
                    double brightness = ImageProcessing.calculateImageBrightness(subImages[r][c]);

                    // Save to cache
                    cachedBrightnessMatrix[r][c] = brightness;

                    // Find matching character
                    outputMatrix[r][c] = matcher.getCharByImageBrightness(brightness);
                }
            }
        }

        return outputMatrix;
    }
}