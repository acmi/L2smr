/*
 * Copyright (c) 2016 acmi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package acmi.l2.clientmod.l2smr.smview;

import acmi.l2.clientmod.io.*;
import acmi.l2.clientmod.unreal.engine.Material;
import gr.zdimensions.jsquish.Squish;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static acmi.l2.clientmod.io.BufferUtil.getCompactInt;
import static acmi.l2.clientmod.util.Util.iterateProperties;
import static gr.zdimensions.jsquish.Squish.decompressImage;

public class ImageUtil {
    public static BufferedImage getImage(byte[] bytes, UnrealPackage up) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        AtomicReference<Format> format = new AtomicReference<>(Format.P8);
        AtomicReference<Palette> palette = new AtomicReference<>(null);
        AtomicInteger width = new AtomicInteger();
        AtomicInteger height = new AtomicInteger();
        iterateProperties(buffer, up, (name, offset, data) -> {
            if (name.equalsIgnoreCase("Format")) {
                format.set(Format.values()[data.get() & 0xFF]);
            } else if (name.equalsIgnoreCase("USize")) {
                width.set(data.getInt());
            } else if (name.equalsIgnoreCase("VSize")) {
                height.set(data.getInt());
            } else if (name.equalsIgnoreCase("Palette")) {
                palette.set(Palette.getRGBA((UnrealPackage.ExportEntry) up.objectReference(getCompactInt(data))));
            }
        });
        DataInput dataInput = DataInput.dataInput(buffer, UnrealPackage.getDefaultCharset());
        new Material().readUnk(dataInput, up.getVersion(), up.getLicense());
        dataInput.readCompactInt();
        dataInput.readInt();
        byte[] data = dataInput.readByteArray();

        switch (format.get()) {
            case P8:
                BufferedImage image = new BufferedImage(width.get(), height.get(), BufferedImage.TYPE_BYTE_INDEXED, getColorModel(palette.get()));
                for (int i = 0; i < height.get(); ++i)
                    System.arraycopy(data, i * width.get(), ((DataBufferByte) image.getRaster().getDataBuffer()).getData(), data.length - width.get() - i * width.get(), width.get());
                return image;
            case DXT1:
            case DXT3:
            case DXT5:
                byte[] decompressed = decompressImage(null, width.get(), height.get(), data, Squish.CompressionType.valueOf(format.get().name()));
                BufferedImage bi = new BufferedImage(width.get(), height.get(), BufferedImage.TYPE_4BYTE_ABGR);
                bi.getRaster().setDataElements(0, 0, width.get(), height.get(), decompressed);
                return bi;
            default:
                return null;
        }
    }

    public static BufferedImage removeAlpha(BufferedImage img) {
        BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < img.getWidth(); x++)
            for (int y = 0; y < img.getHeight(); y++)
                copy.setRGB(x, y, img.getRGB(x, y));
        return copy;
    }

    private enum Format {
        P8,
        RGBA7,
        RGB16,
        DXT1,
        RGB8,
        RGBA8,
        NODATA,
        DXT3,
        DXT5,
        L8,
        G16,
        RRRGGGBBB
    }

    public static class Palette {
        public int exportIndex;
        public final Color[] colors;

        public Palette(int colorCount) {
            this.colors = new Color[colorCount];
        }

        public static Palette getRGBA(UnrealPackage.ExportEntry entry) {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(entry.getObjectRawData()), null);
            if (input.readCompactInt() != entry.getUnrealPackage().nameReference("None")) {
                throw new RuntimeException("Palette with properties");
            } else {
                Palette palette = new Palette(input.readCompactInt());
                readRGBA(palette.colors, input);
                palette.exportIndex = entry.getIndex();
                return palette;
            }
        }

        public static void readRGBA(Color[] colors, DataInput input) {
            for (int i = 0; i < colors.length; ++i) {
                int r = input.readUnsignedByte();
                int g = input.readUnsignedByte();
                int b = input.readUnsignedByte();
                int a = input.readUnsignedByte();
                colors[i] = new Color(r, g, b, a);
            }
        }
    }

    private static IndexColorModel getColorModel(Palette palette) {
        int size = palette.colors.length;
        byte[] r = new byte[size];
        byte[] g = new byte[size];
        byte[] b = new byte[size];
        byte[] a = new byte[size];

        for (int i = 0; i < size; ++i) {
            r[i] = (byte) palette.colors[i].getRed();
            g[i] = (byte) palette.colors[i].getGreen();
            b[i] = (byte) palette.colors[i].getBlue();
            a[i] = (byte) palette.colors[i].getAlpha();
        }

        return new IndexColorModel(8, size, r, g, b, a);
    }
}
