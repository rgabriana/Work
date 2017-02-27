package com.ems.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class GzipResponse extends HttpServletResponseWrapper {
    private HttpServletResponse response;
    private PrintWriter writer;
    private GzipOutputStream stream;

    public GzipResponse(final HttpServletResponse response) {
        super(response);
        this.response = response;
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
        if (stream != null) {
            stream.close();
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        stream.flush();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called!");
        }
        if (stream == null)
            stream = new GzipOutputStream(response);
        return stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer != null)
            return writer;
        if (stream != null) {
            throw new IllegalStateException("getOutputStream() has already been called!");
        }
        stream = new GzipOutputStream(response);
        writer = createPrintWriter(response, stream);
        return writer;
    }

    private static PrintWriter createPrintWriter(final HttpServletResponse response, final OutputStream stream)
            throws UnsupportedEncodingException {
        String encoding = response.getCharacterEncoding();
        if (encoding == null || encoding.length() == 0)
            return new PrintWriter(stream);
        return new PrintWriter(new OutputStreamWriter(stream, encoding));
    }

}
