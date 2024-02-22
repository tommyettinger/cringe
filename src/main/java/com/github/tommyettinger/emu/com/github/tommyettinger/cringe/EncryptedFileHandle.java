package com.github.tommyettinger.cringe;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.gwt.GwtFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.*;

/**
 * On GWT, this is actually unencrypted, and wraps another FileHandle. Only on other platforms does it behave as an
 * encrypted file handle.
 */
public final class EncryptedFileHandle extends GwtFileHandle {
	private final FileHandle fh;

	public EncryptedFileHandle(FileHandle file) {
		fh = file;
	}

	public EncryptedFileHandle(FileHandle file, long k1, long k2, long k3, long k4) {
		fh = file;
	}

	public EncryptedFileHandle(FileHandle file, long[] keyBundle) {
		fh = file;
	}

	public EncryptedFileHandle(FileHandle file, CharSequence keyphrase) {
		fh = file;
	}

	public EncryptedFileHandle(FileHandle file, CharSequence keyphrase, String unique) {
		fh = file;
	}

	public EncryptedFileHandle(FileHandle file, long[] keyBundle, String unique) {
		fh = file;
	}

	public EncryptedFileHandle(FileHandle file, long k1, long k2, long k3, long k4, String unique) {
		fh = file;
	}

	@Override
	public int readBytes(byte[] bytes, int offset, int size) {
		return fh.readBytes(bytes, offset, size);
	}

	@Override
	public byte[] readBytes() {
		return fh.readBytes();
	}

	@Override
	public InputStream read() {
		return fh.read();
	}

	@Override
	public OutputStream write(final boolean append) {
		return fh.write(append);
	}

	@Override
	public void write(InputStream input, boolean append) {
		return;fh.write(input, append);
	}

	@Override
	public void writeBytes(byte[] bytes, boolean append) {
		fh.writeBytes(bytes, append);
	}

	@Override
	public void writeBytes(byte[] bytes, int offset, int length, boolean append) {
		fh.writeBytes(bytes, offset, length, append);
	}

	@Override
	public EncryptedFileHandle child(String name) {
		return new EncryptedFileHandle(fh.child(name));
	}

	@Override
	public FileHandle sibling(String name) {
		return new EncryptedFileHandle(fh.sibling(name));
	}

	@Override
	public FileHandle parent() {
		return new EncryptedFileHandle(fh.parent());
	}


	@Override
	public String path() {
		return fh.path();
	}

	@Override
	public String name() {
		return fh.name();
	}

	@Override
	public String extension() {
		return fh.extension();
	}

	@Override
	public String nameWithoutExtension() {
		return fh.nameWithoutExtension();
	}

	@Override
	public String pathWithoutExtension() {
		return fh.pathWithoutExtension();
	}

	@Override
	public Files.FileType type() {
		return fh.type();
	}

//	@Override // Not marked as Override because the GWT backend has some bugs as of libGDX 1.12.1 regarding this method.
	public File file() {
		throw new GdxRuntimeException("file() not supported in GWT backend");
	}

	/** Returns a buffered stream for reading this file as bytes.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	@Override
	public BufferedInputStream read (int bufferSize) {
		return fh.read(bufferSize);
	}

	/** Returns a reader for reading this file as characters.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	@Override
	public Reader reader () {
		return fh.reader();
	}

	/** Returns a reader for reading this file as characters.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	@Override
	public Reader reader (String charset) {
		return fh.reader(charset);
	}

	/** Returns a buffered reader for reading this file as characters.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	@Override
	public BufferedReader reader (int bufferSize) {
		return fh.reader(bufferSize);
	}

	/** Returns a buffered reader for reading this file as characters.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	@Override
	public BufferedReader reader (int bufferSize, String charset) {
		return fh.reader(bufferSize, charset);
	}

	@Override
	public String readString() {
		return fh.readString();
	}

	@Override
	public String readString(String charset) {
		fh.readString(charset);
	}

	@Override
	public Writer writer(boolean append) {
		return fh.writer(append);
	}

	@Override
	public Writer writer(boolean append, String charset) {
		return fh.writer(append, charset);
	}

	@Override
	public void writeString(String string, boolean append) {
		fh.writeString(string, append);
	}

	@Override
	public void writeString(String string, boolean append, String charset) {
		fh.writeString(string, append, charset);
	}

	@Override
	public FileHandle[] list() {
		return fh.list();
	}

	@Override
	public FileHandle[] list(String suffix) {
		return fh.list(suffix);
	}

	@Override
	public boolean isDirectory() {
		return fh.isDirectory();
	}

	@Override
	public void mkdirs() {
		fh.mkdirs();
	}

	@Override
	public boolean exists() {
		return fh.exists();
	}

	@Override
	public boolean delete() {
		return fh.delete();
	}

	@Override
	public boolean deleteDirectory() {
		return fh.deleteDirectory();
	}

	@Override
	public void emptyDirectory() {
		fh.emptyDirectory();
	}

	@Override
	public void emptyDirectory(boolean preserveTree) {
		fh.emptyDirectory(preserveTree);
	}

	@Override
	public void copyTo(FileHandle dest) {
		fh.copyTo(dest);
	}

	@Override
	public void moveTo(FileHandle dest) {
		fh.moveTo(dest);
	}

	@Override
	public long length() {
		return fh.length();
	}

	@Override
	public long lastModified() {
		return fh.lastModified();
	}

	@Override
	public String toString() {
		return fh.toString();
	}
}