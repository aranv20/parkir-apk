package com.example.parkirfirebase.printqr;

public class PrinterCommands {

    // Escapes
    public static final byte[] ESC_ALIGN_LEFT = new byte[]{0x1B, 0x61, 0x00};
    public static final byte[] ESC_ALIGN_CENTER = new byte[]{0x1B, 0x61, 0x01};
    public static final byte[] ESC_ALIGN_RIGHT = new byte[]{0x1B, 0x61, 0x02};

    // Bit image mode
    public static final byte[] SELECT_BIT_IMAGE_MODE = new byte[]{0x1B, 0x2A, 0x21};
    public static final byte[] SET_LINE_SPACING_24 = new byte[]{0x1B, 0x33, 24};

    // Feed line
    public static final byte[] FEED_LINE = new byte[]{0x0A};

    // ... Add more commands as needed
}
