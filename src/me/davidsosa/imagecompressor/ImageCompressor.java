/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 David Sosa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.davidsosa.imagecompressor;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Contains utility methods for compressing a single image or all images in a
 * directory.
 *
 * @author David Sosa
 */
public class ImageCompressor {

    /**
     * <p>
     * Compresses all jpeg or png images in the specified folder path.
     * </p>
     * <br />
     * <b>WARNING</b>
     * <br />
     * <p>
     * All images in the folder will be replaced by their compressed version if
     * replaceOriginals == true
     * </p>
     *
     * @param parentPath path of the folder containing all images to compress
     * @param quality quality of the compression, from 0.0 (highest compression,
     * lowest image quality) to 1.0 (lowest compression, highest image quality)
     * @param replaceOriginals flag to indicate if the original images are to be
     * replaced by the compressed versions
     * @throws java.io.IOException
     */
    public void compressImagesInDirectory(String parentPath, float quality, boolean replaceOriginals) throws IOException {
        File directory = new File(parentPath);
        System.out.println("Path: " + directory.getAbsolutePath());
        System.out.println();
        FileFilter filter = new ImageFileFilter();
        File[] files = directory.listFiles(filter);
        for (File file : files) {
            try {
                compressImage(file, quality, replaceOriginals);
            } catch (IOException ex) {
                System.err.println("Could not compress image " + file.getName());
                System.err.println();
            }
        }
    }

    /**
     * Compress a single jpg or png image file with the specified quality
     *
     * @param imageFile the jpg or png image file to compress
     * @param quality quality of the compression, from 0.0 (highest compression,
     * lowest image quality) to 1.0 (lowest compression, highest image quality)
     * @param replaceOriginal if true, replaces the original image by the
     * compressed version, if false, creates a new file for the compressed
     * version with '-compressed' appended to its name
     * @return a File object with an added suffix '-compressed' right before the
     * jpg extension as the original
     * @throws IOException if the image file cannot be found
     */
    public File compressImage(File imageFile, float quality, boolean replaceOriginal) throws IOException {
        // Get a ImageWriter for jpeg format.
        Iterator<ImageWriter> writers;
        String extension = imageFile.getName().substring(imageFile.getName().length() - 3);

        writers = ImageIO.getImageWritersBySuffix(extension);

        if (!writers.hasNext()) {
            throw new IllegalStateException("No writers found");
        }

        ImageWriter writer = writers.next();
        // Create the ImageWriteParam to compress the image.
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            if (extension.equalsIgnoreCase("bmp")) {
                param.setCompressionType(param.getCompressionTypes()[0]);
            }
            param.setCompressionQuality(quality);
        }
        // The output will be another file, replacing the original
        BufferedImage image = ImageIO.read(imageFile);
        String name = imageFile.getName();

        if (!replaceOriginal) {
            name = name.replace("." + extension, "-compressed." + extension);
        }

        File compressedFile = new File(imageFile.getParent() + "/" + name);
        try (FileOutputStream fos = new FileOutputStream(compressedFile);
                ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {
            writer.setOutput(ios);

            System.out.println("Compressing image " + imageFile.getName() + "...");
            writer.write(image);
            if (replaceOriginal) {
                imageFile.delete();
            }
            System.out.println("Success!");
            System.out.println("Compressed version saved as: " + compressedFile.getAbsolutePath());
            System.out.println();
        }
        return compressedFile;
    }

    /**
     * Filter jpg and png files.
     */
    public static class ImageFileFilter implements FileFilter {

        @Override
        public boolean accept(File pathName) {
            return (pathName.getName().endsWith(".jpg")
                    || pathName.getName().endsWith(".png")
                    || pathName.getName().endsWith(".bmp")
                    || pathName.getName().endsWith(".JPG")
                    || pathName.getName().endsWith(".PNG")
                    || pathName.getName().endsWith(".BMP"))
                    && (!pathName.getName().endsWith("-compressed.jpg")
                    && !pathName.getName().endsWith("-compressed.png")
                    && !pathName.getName().endsWith("-compressed.bmp")
                    && !pathName.getName().endsWith("-compressed.JPG")
                    && !pathName.getName().endsWith("-compressed.PNG")
                    && !pathName.getName().endsWith("-compressed.BMP"));
        }
    }
}
