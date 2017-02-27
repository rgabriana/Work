package com.ems.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class GzipOutputStream extends ServletOutputStream {
    private static final int BUFFER_SIZE = 1024 * 4;
    private HttpServletResponse response;
    private ByteArrayOutputStream byteArrayOS;
    private GZIPOutputStream gzipOS;
    private boolean isClosed;
    private int compressedSize;
    private int uncompressedSize;

    public GzipOutputStream(final HttpServletResponse response) throws IOException {
        this.response = response;
        byteArrayOS = new ByteArrayOutputStream(BUFFER_SIZE);
        gzipOS = new GZIPOutputStream(byteArrayOS, BUFFER_SIZE);
    }

    private void checkClosed(final String method) throws IOException {
        if (!isClosed)
            return;
        throw new IOException(method + "() Failed because the output stream is closed.");
    }

    @Override
    public void close() throws IOException {
        if (isClosed)
            return;
        gzipOS.finish();
        byte[] bytes = byteArrayOS.toByteArray();
        compressedSize = bytes.length;
        gzipOS.close();
        byteArrayOS.close(); // has no effect, but for consistency

        writeToResponse(bytes);
        isClosed = true;
    }

    private void writeToResponse(final byte[] bytes) throws IOException {
        response.addHeader("Content-Length", String.valueOf(compressedSize));
        response.addHeader("Content-Encoding", "gzip");

        ServletOutputStream out = response.getOutputStream();
        out.write(bytes);
        out.flush();
        out.close();
    }

    @Override
    public void flush() throws IOException {
        checkClosed("flush");
        gzipOS.flush();
    }

    @Override
    public void write(final int b) throws IOException {
        checkClosed("write");
        gzipOS.write(b);
        uncompressedSize++;
    }

    @Override
    public void write(final byte b[], final int off, final int len) throws IOException {
        checkClosed("write");
        gzipOS.write(b, off, len);
        uncompressedSize += len;
    }

    @Override
    public void write(final byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Get compressedSize*100 / uncompressedSize, meaningful only after calling close() on this object.
     * 
     * @return compressedSize*100 / uncompressedSize
     */
    public int getCompressionPercent() {
        long p = (compressedSize * 100L) / uncompressedSize;
        return (int) p;
    }

    /**
     * Get the size of compressed response, meaningful only after calling close() on this object.
     * 
     * @return the size of the compressed response in bytes.
     */
    public int getCompressedSize() {
        return compressedSize;
    }

    /**
     * Get the size of the original uncompressed response, meaningful only after calling close() on this object.
     * 
     * @return the size of the response in bytes.
     */
    public int getUncompressedSize() {
        return uncompressedSize;
    }
}
