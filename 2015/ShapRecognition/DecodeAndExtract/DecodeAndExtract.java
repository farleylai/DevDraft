import java.util.*;
import java.lang.*;
import java.io.*;
import java.nio.*;

class DecodeAndExtract
{
    private static class BMPReader {
        public static final int OFF_PIX_ARRAY = 0xA;
        public static final int OFF_PIX_WIDTH = 0x12;
        public static final int OFF_PIX_HEIGHT = 0x16;
        public static final int OFF_PIX_BITS = 0x1C;

        private String mData;
        private int mWidth;
        private int mHeight;
        private int mBitsPerPixel;
        private int mPadding;
        private int mOffset;
        private char[][] mPixels;

        private int position(int offset) {
            return 2 * offset;
        }

        private int position(int offset, int size) {
            return 2 * (offset + size);
        }

        public BMPReader(InputStream in) throws Exception {
            // Parse header
            mData = new BufferedReader(new InputStreamReader(in)).readLine();
            debug("%s\n", mData);
            mWidth = readInt(OFF_PIX_WIDTH);
            mHeight = readInt(OFF_PIX_HEIGHT);
            mBitsPerPixel = readShort(OFF_PIX_BITS);
            mOffset = readInt(OFF_PIX_ARRAY);
            mPixels = new char[mHeight][mWidth];
            int trailingBits = mBitsPerPixel * mWidth % 32;
            if(trailingBits == 0 || trailingBits > 24) mPadding = 0;
            else if(trailingBits <= 8) mPadding = 3;
            else if(trailingBits <= 16) mPadding = 2;
            else mPadding = 1;
            debug("BMP Header: w=%d, h=%d, bits=%d, padding=%d, offset=%d\n", mWidth, mHeight, mBitsPerPixel, mPadding, mOffset);

            // Store pixels
            int trailingByte = mBitsPerPixel * mWidth % 8 == 0 ? 0 : 1; 
            int widthInBytes = mBitsPerPixel * mWidth / 8 + trailingByte + mPadding;
            ByteBuffer buffer = ByteBuffer.allocate(widthInBytes);
            //debug("widthInBytes=%d\n", widthInBytes);
            for(int h = 0; h < mHeight; h++) {
                int offset = mOffset + (mHeight - 1 - h) * widthInBytes;
                //debug("row offset=%d\n", offset);
                buffer.clear();
                for(int i = 0; i < widthInBytes; i++)
                    buffer.put(readByte(offset + i));
                buffer.flip();
                switch(mBitsPerPixel) {
                    case 1:
                        BitSet bits = BitSet.valueOf(buffer.array());
                        for(int w = 0; w < mWidth; w++) {
                            int bit = w / 8 * 8 +  (7 - w % 8);
                            mPixels[h][w] = bits.get(bit) ? 'W' : 'B';
                            //debug("pixel(%d,%d)=%c\n", w, h, mPixels[h][w]);
                        }
                        break;
                    case 8:
                        for(int w = 0; w < mWidth; w++) 
                            mPixels[h][w] = buffer.get() < 128 ? 'B' : 'W';
                        break;
                    case 24:
                        for(int w = 0; w < mWidth; w++)
                            mPixels[h][w] = (buffer.get() + buffer.get() + buffer.get()) / 3 < 128 ? 'B' : 'W';
                        break;
                }
            }
        }

        private int readInt(int offset) {
            String hex = mData.substring(position(offset), position(offset, 4));
            int value = Integer.parseInt(hex, 16);
            value = ((value & 0x000000FF) << 24) | ((value & 0x0000FF00) << 8) | ((value & 0x00FF0000) >>> 8) | ((value & 0xFF000000) >>> 24);
            //debug("int=%d(0x%s) at offset: %d(%X), from: %d, to: %d\n", value, hex, offset, offset, position(offset), position(offset, 4));
            return value;
        }

        private short readShort(int offset) {
            String hex = mData.substring(position(offset), position(offset, 2));
            int value = Integer.parseInt(hex, 16);
            value = ((value & 0x00FF) << 8) | ((value & 0xFF00) >>> 8);
            //debug("short=%d(0x%s) at offset: %d(%X), from: %d, to: %d\n", value, hex, offset, offset, position(offset), position(offset, 2));
            return (short)value;
        }

        private byte readByte(int offset) {
            String hex = mData.substring(position(offset), position(offset, 1));
            int value = Integer.parseInt(hex, 16);
            //debug("byte=%d(0x%s) at offset: %d(%X), from: %d, to: %d\n", value, hex, offset, offset, position(offset), position(offset, 1));
            return (byte)value;
        }

        public char readPixel(int w, int h) {
            return mPixels[h][w];
        }

        public int getWidth() { return mWidth; }
        public int getHeight() { return mHeight; }
    }

    private static boolean DEBUG = false;
    private static void debug(String fmt, Object... args) {
        if(DEBUG)
            System.err.printf(fmt, args);
    }

    public static void main (String[] args) throws Exception {
        BMPReader reader = new BMPReader(System.in);
        System.out.printf("%d %d\n", reader.getWidth(), reader.getHeight());
        for(int h = 0; h < reader.getHeight(); h++) {
            for(int w = 0; w < reader.getWidth(); w++) 
                System.out.print(reader.readPixel(w, h));
            System.out.println();
        }
    }
}
