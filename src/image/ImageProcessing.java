/**
 * A utility class providing static methods for image processing operations.
 * This includes padding images to power-of-2 dimensions, dividing images into sub-squares,
 * and calculating brightness values.
 */
public class ImageProcessing {
    private ImageProcessing() {
        throw new IllegalStateException("Utility class, do not instantiate");
    }

    /**
     * Pads the image with white pixels so its dimensions are powers of 2.
     * @param image The original image.
     * @return A new, padded Image object.
     */

    public static Image padImage(Image image) {
        int originalHeight = image.getHeight();
        int originalWidth = image.getWidth();

        // Compute the new dimensions to be powers of 2
        int newHeight = nextPowerOfTwo(originalHeight);
        int newWidth = nextPowerOfTwo(originalWidth);

        // If already powers of 2, we leave them as they are
        if (newHeight == originalHeight && newWidth == originalWidth){
            return image;
        }

        // create a new pixel array filled with white pixels that is of power of 2
        Color[][] newPixelArray = new Color[newHeight][newWidth];
        for(int i = 0; i < newHeight; i++) {
            for(int j = 0; j < newWidth; j++) {
                newPixelArray[i][j] = Color.WHITE; // white pixel

            }
        }

        // Find the offsets
        int heightPadding = newHeight - originalHeight;
        int widthPadding = newWidth - originalWidth;

        int startH = heightPadding / 2;
        int startW = widthPadding / 2;

        // Copy the original image pixels into the center of the new pixel array
        for (int i = 0; i < originalHeight; i++) {
            for (int j = 0; j < originalWidth; j++) {
                newPixelArray[i + startH][j + startW] = image.getPixel(i, j);
            }
        }

        return new Image(newPixelArray, newWidth, newHeight);
    }

    /**
     * Divides the image into square sub-images based on the resolution.
     * @param image The (padded) image to divide.
     * @param resolution The number of sub-images per row.
     * @return An array of Image objects representing the sub-squares.
     */

    /**
     * Divides the image into square sub-images based on the resolution.
     * @param image The (padded) image to divide.
     * @param resolution The number of sub-images per row.
     * @return An array of Image objects representing the sub-squares.
     */
    public static Image[][] divideImage(Image image, int resolution) {
        int subImageSize = image.getWidth() / resolution;
        int rows = image.getHeight() / subImageSize;
        int cols = resolution;

        Image[][] subImages = new Image[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Make a sub-array for the current square
                Color[][] subImagePixels = new Color[subImageSize][subImageSize];

                // Copy the pixels to the squares
                for (int row = 0; row < subImageSize; row++) {
                    for (int col = 0; col < subImageSize; col++) {
                        int originalRow = i * subImageSize + row;
                        int originalCol = j * subImageSize + col;
                        subImagePixels[row][col] = image.getPixel(originalRow, originalCol);
                    }
                }

                subImages[i][j] = new Image(subImagePixels, subImageSize, subImageSize);
            }
        }
        return subImages;
    }

    /**
     * Calculates the brightness of a given image (or sub-image).
     * @param image The image to analyze.
     * @return A brightness value between 0.0 and 1.0.
     */
    public static double calculateImageBrightness(Image image) {
        double totalGrey = 0;
        int height = image.getHeight();
        int width = image.getWidth();

        for (int i = 0; i < height; i ++) {
            for (int j = 0; j < width; j++) {
                Color color = image.getPixel(i,j);
                double greyPixel = color.getRed() * 0.2126 + color.getGreen() * 0.7152 + color.getBlue() * 0.0722;
                totalGrey += greyPixel;
            }
        }

        return (totalGrey / (height * width)) / 255.0;
    }

    private static int nextPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }
}