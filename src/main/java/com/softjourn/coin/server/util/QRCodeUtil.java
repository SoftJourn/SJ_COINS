package com.softjourn.coin.server.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.codec.binary.Hex;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class QRCodeUtil {

    public static final String IMAGE_TYPE = "png";

    public static final int IMAGE_SIZE = 250;

    /**
     * Generate QR code from data bytes interpreted as hex string.
     * @param data data bytes
     * @return png image bytes
     */
    public static byte[] genQRCode(byte[] data) {
        try {

            String hexText = Hex.encodeHexString(data);

            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(hexText, BarcodeFormat.QR_CODE, IMAGE_SIZE, IMAGE_SIZE, hintMap);
            int width = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, width);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ImageIO.write(image, IMAGE_TYPE, buffer);
            return buffer.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error on creating QR code.", e);
        }
    }
}
