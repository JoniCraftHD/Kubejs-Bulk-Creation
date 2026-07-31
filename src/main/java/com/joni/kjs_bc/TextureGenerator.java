package com.joni.kjs_bc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TextureGenerator {


    private static BufferedImage loadTexture(String resourceId) throws IOException {
        String[] split = resourceId.split(":", 2);
        String namespace = split.length > 1 ? split[0] : "minecraft";
        String path = split.length > 1 ? split[1] : split[0];

        String resourcePath = "assets/" + namespace + "/textures/" + path + ".png";


        InputStream stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath);

        if (stream == null) {
            stream = TextureGenerator.class.getClassLoader().getResourceAsStream(resourcePath);
        }

        if (stream != null) {
            BufferedImage img = ImageIO.read(stream);
            stream.close();
            if (img != null) return img;
        }


        File looseFile = new File("kubejs/assets/" + namespace + "/textures/" + path + ".png");
        if (looseFile.exists()) {
            BufferedImage img = ImageIO.read(looseFile);
            if (img != null) return img;
        }

        throw new IOException("Textur nicht gefunden (weder im Classpath noch lose): " + resourcePath
                + " (auch geprüft: " + looseFile.getPath() + ")");
    }



    private static BufferedImage tintImage(BufferedImage overlay, String hexColor) {
        int color = Integer.parseInt(hexColor.replace("#", ""), 16);
        int rTint = (color >> 16) & 0xFF;
        int gTint = (color >> 8) & 0xFF;
        int bTint = color & 0xFF;

        int width = overlay.getWidth();
        int height = overlay.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = overlay.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int newR = (r * rTint) / 255;
                int newG = (g * gTint) / 255;
                int newB = (b * bTint) / 255;

                int newArgb = (a << 24) | (newR << 16) | (newG << 8) | newB;
                result.setRGB(x, y, newArgb);
            }
        }
        return result;
    }


    private static BufferedImage compositeImages(BufferedImage base, BufferedImage overlay) {
        int width = base.getWidth();
        int height = base.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        java.awt.Graphics2D g2d = result.createGraphics();
        g2d.drawImage(base, 0, 0, null);
        g2d.drawImage(overlay, 0, 0, null);
        g2d.dispose();

        return result;
    }


    public static void generateCompositeTexture(String baseTextureId, String overlayTextureId,
                                                String hexColor, File outputFile) {
        try {
            BufferedImage base = loadTexture(baseTextureId);
            BufferedImage overlay = loadTexture(overlayTextureId);
            BufferedImage tintedOverlay = tintImage(overlay, hexColor);
            BufferedImage composite = compositeImages(base, tintedOverlay);

            outputFile.getParentFile().mkdirs();
            ImageIO.write(composite, "PNG", outputFile);

            System.out.println("[BulkCreation] Textur generiert: " + outputFile.getPath());
        } catch (IOException e) {
            System.err.println("[BulkCreation] Fehler beim Generieren der Textur: " + baseTextureId + " + " + overlayTextureId);
            e.printStackTrace();
        }
    }
}