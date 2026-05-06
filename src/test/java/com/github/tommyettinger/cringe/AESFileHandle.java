package com.github.tommyettinger.cringe;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * An encrypted FileHandle that uses {@link javax.crypto.Cipher} (using AES) to encrypt and decrypt files.
 * This is probably incompatible with every platform except for desktop.
 * Uses the FileHandle's path (or name, if it is an absolute path) to determine its initialization vector.
 * <br>
 * Based off <a href="https://gist.github.com/MobiDevelop/6389767">a gist by MobiDevelop</a>.
 */
@GwtIncompatible
public final class AESFileHandle extends FileHandle {

	private final transient FileHandle fh;

	private final transient byte[] key;
	/**
	 * Creates a EncryptedFileHandle that can write encrypted data to the wrapped FileHandle or read and decrypt
	 * encrypted data from the wrapped FileHandle.
	 *
	 * @param file the FileHandle to wrap; may be any type, such as {@link Files.FileType#Internal}
	 * @param seed any long, which will be used to change the resulting key
	 * @param keyphrase a typically-sentence-to-paragraph-length CharSequence, such as a String, that will be used to generate keys
	 */
	public AESFileHandle(FileHandle file, long seed, CharSequence keyphrase) {
		this.fh = file;
		this.key = expandKeyphrase(seed, keyphrase);
	}

	/**
	 * Creates a EncryptedFileHandle that can write encrypted data to the wrapped FileHandle or read and decrypt
	 * encrypted data from the wrapped FileHandle.
	 *
	 * @param file the FileHandle to wrap; may be any type, such as {@link Files.FileType#Internal}
	 * @param keyphrase a typically-sentence-to-paragraph-length CharSequence, such as a String, that will be used to generate keys
	 */
	public AESFileHandle(FileHandle file, CharSequence keyphrase) {
		this.fh = file;
		this.key = expandKeyphrase(keyphrase);
	}

	/**
	 * Creates a EncryptedFileHandle that can write encrypted data to the wrapped FileHandle or read and decrypt
	 * encrypted data from the wrapped FileHandle.
	 *
	 * @param file the FileHandle to wrap; may be any type, such as {@link Files.FileType#Internal}
	 * @param key a 32-element byte array clone()-ed and used exactly
	 */
	public AESFileHandle(FileHandle file, byte[] key) {
		this.fh = file;
		this.key = key.clone();
	}

	/**
	 * Given a CharSequence key such as a String, this grows that initial key into a 256-bit expanded key (a
	 * {@code byte[32]}).
	 * <br>
	 * This simply returns {@code expandKeyphrase(-1L, keyphrase)}.
	 *
	 * @param keyphrase a typically-sentence-to-paragraph-length CharSequence, such as a String, that will be used to generate keys
	 * @return a 32-item byte array that should, of course, be kept secret to be used cryptographically
	 */
	private static byte[] expandKeyphrase(CharSequence keyphrase) {
		return expandKeyphrase(-1L, keyphrase);
	}

	/**
	 * Given a CharSequence key such as a String, this grows that initial key into a 256-bit expanded key (a
	 * {@code byte[32]}).
	 * @param seed any long, which will be used to change the resulting key
	 * @param keyphrase a typically-sentence-to-paragraph-length CharSequence, such as a String, that will be used to generate keys
	 * @return a 32-item byte array that should, of course, be kept secret to be used cryptographically
	 */
	private static byte[] expandKeyphrase(final long seed, CharSequence keyphrase) {
		if(keyphrase == null) keyphrase = "You should really do a better job at selecting a keyphrase!";
		final byte[] k = new byte[32];
		long sc;
		k[0] = (byte) (sc = Scramblers.scramble(Scramblers.hash64(seed, keyphrase)));
		for (int i = 1; i < k.length; i++) {
			k[i] = (byte)(sc = Scramblers.scramble(Scramblers.hash64(sc^i, keyphrase)));
		}
		return k;
	}

	/**
	 * One of the main ways here to encrypt a "plaintext" byte array and get back a coded "ciphertext" byte array.
	 * This takes four {@code long}s as its key (256-bits), and also requires one unique (never used again) long as
	 * the nonce. How you generate keys is up to you, but the keys must be kept secret for encryption to stay secure.
	 * To generate nonce, secrecy isn't as important as uniqueness; calling DistinctRandom.nextLong() even many
	 * times will never return the same long unless nonce are requested for years from one generator, so it is a good
	 * option to produce nonce data. The rest of the arguments are about the data being encrypted. The plaintext is the
	 * byte array to encrypt; it will not be modified here. The plainOffset is which index in plaintext to start reading
	 * from. The ciphertext is the byte array that will be written to, and should usually be empty at the start (though
	 * it doesn't need to be). The ciphertext does not need to be padded. The cipheroffset is
	 * which index to start writing to in ciphertext. Lastly, the textLength is how many byte items to encrypt from
	 * plaintext; this can be less than plaintext's length.
	 *
	 * @param plaintext the byte array to encrypt; will not be modified
	 * @param plainOffset which index to start reading from plaintext
	 * @param ciphertext the byte array to write encrypted data to; will be modified
	 * @param cipherOffset which index to start writing to in ciphertext
	 * @param textLength how many byte items to read and encrypt from plaintext
	 * @return ciphertext, after modifications
	 */
	private byte[] encrypt(byte[] plaintext, int plainOffset, byte[] ciphertext, int cipherOffset, int textLength) {
		IvParameterSpec iv = new IvParameterSpec(
				(fh.type() == Files.FileType.Absolute ? fh.name() : fh.path()).getBytes(StandardCharsets.UTF_8));
		SecretKeySpec sKeySpec = new SecretKeySpec(key, "AES");

		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);
			cipher.doFinal(plaintext, plainOffset, textLength, ciphertext, cipherOffset);
		} catch (GeneralSecurityException e) {
			throw new GdxRuntimeException(e);
		}
		return ciphertext;
	}

	/**
	 * Encrypts a "plaintext" byte array in-place, making it a coded "ciphertext" byte array.
	 * This requires one unique (never used again) long as the nonce.
	 * To generate nonce, secrecy isn't as important as uniqueness; calling DistinctRandom.nextLong() even many
	 * times will never return the same long unless nonce are requested for years from one generator, so it is a good
	 * option to produce nonce data. The rest of the arguments are about the data being encrypted. The plaintext is the
	 * byte array to encrypt; it will be modified here. The plainOffset is which index in plaintext to start reading
	 * from and writing to. Lastly, the textLength is how many byte items to encrypt from
	 * plaintext; this can be less than plaintext's length.
	 *
	 * @param plaintext the byte array to encrypt in-place; will be modified
	 * @param plainOffset which index to start reading from and writing to in plaintext
	 * @param textLength how many byte items to read and encrypt from plaintext
	 */
	private byte[] encryptInPlace(byte[] plaintext, int plainOffset, int textLength) {
		IvParameterSpec iv = new IvParameterSpec(
				(fh.type() == Files.FileType.Absolute ? fh.name() : fh.path()).getBytes(StandardCharsets.UTF_8));
		SecretKeySpec sKeySpec = new SecretKeySpec(key, "AES");

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);
			return cipher.doFinal(plaintext, plainOffset, textLength);
        } catch (GeneralSecurityException e) {
            return null;
        }
    }

	/**
	 * Decrypts a coded "ciphertext" byte array and changes it in-place to a "plaintext" byte array.
	 * This takes four {@code long}s as its key (256-bits), and also requires one unique (never used again) long as
	 * the nonce. How you generate keys is up to you, but the keys must be kept secret for encryption to stay secure.
	 * To generate nonce, secrecy isn't as important as uniqueness; calling DistinctRandom.nextLong() even many
	 * times will never return the same long unless nonce are requested for years from one generator, so it is a good
	 * option to produce nonce data. The rest of the arguments are about the data being decrypted. The ciphertext is the
	 * byte array that contains coded data, and should have been encrypted by
	 * {@link #encryptInPlace(byte[], int, int)}. The ciphertext will be modified
	 * in-place to become the plaintext. The cipheroffset is which index to start reading from in ciphertext. Lastly,
	 * the textLength is how many byte items to decrypt from ciphertext; this can be less than ciphertext's length.
	 *
	 * @param ciphertext the byte array to read encrypted data from; will be modified in-place
	 * @param cipherOffset which index to start reading from and writing to in ciphertext
	 * @param textLength how many byte items to read and decrypt from ciphertext
	 */
	private byte[] decryptInPlace(byte[] ciphertext, int cipherOffset, int textLength) {
		IvParameterSpec iv = new IvParameterSpec(
				(fh.type() == Files.FileType.Absolute ? fh.name() : fh.path()).getBytes(StandardCharsets.UTF_8));
		SecretKeySpec sKeySpec = new SecretKeySpec(key, "AES");

		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);
			return cipher.doFinal(ciphertext, cipherOffset, textLength);
		} catch (GeneralSecurityException e) {
			return null;
		}

	}

	@Override
	public int readBytes(byte[] bytes, int offset, int size) {
		int ret = fh.readBytes(bytes, offset, size);
		decryptInPlace(bytes, offset, size);
		return ret;
	}

	@Override
	public byte[] readBytes() {
		byte[] bytes = fh.readBytes();
		return decryptInPlace(bytes, 0, bytes.length);
	}

	@Override
	public InputStream read() {
		try (InputStream is = new ByteArrayInputStream(readBytes())) {
			return is;
		} catch (Exception ex) {
			throw new GdxRuntimeException(ex);
		}
	}

	@Override
	public OutputStream write(final boolean append) {
		IvParameterSpec iv = new IvParameterSpec(
				(fh.type() == Files.FileType.Absolute ? fh.name() : fh.path()).getBytes(StandardCharsets.UTF_8));
		SecretKeySpec sKeySpec = new SecretKeySpec(key, "AES");

		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);
			return new CipherOutputStream(fh.write(append), cipher);
		} catch (GeneralSecurityException e) {
			return null;
		}
	}

	@Override
	public void write(InputStream input, boolean append) {
		OutputStream output = null;
		try {
			output = write(append);
			StreamUtils.copyStream(input, output);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error stream writing to file: " + fh + " (" + fh.type() + ")", ex);
		} finally {
			StreamUtils.closeQuietly(input);
			StreamUtils.closeQuietly(output);
		}
	}

	@Override
	public void writeBytes(byte[] bytes, boolean append) {
		fh.writeBytes(encrypt(bytes, 0, new byte[bytes.length], 0, bytes.length), append);
	}

	@Override
	public void writeBytes(byte[] bytes, int offset, int length, boolean append) {
		fh.writeBytes(encrypt(bytes, offset, new byte[length], 0, length), 0, length, append);
	}

	@Override
	public AESFileHandle child(String name) {
		return new AESFileHandle(fh.child(name), key);
	}

	@Override
	public AESFileHandle sibling(String name) {
		return new AESFileHandle(fh.sibling(name), key);
	}

	@Override
	public AESFileHandle parent() {
		return new AESFileHandle(fh.parent(), key);
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

	@Override
	public File file() {
		return fh.file();
	}

	/** Returns a buffered stream for reading this file as bytes.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	@Override
	public BufferedInputStream read (int bufferSize) {
		return new BufferedInputStream(read(), bufferSize);
	}

	/** Returns a reader for reading this file as characters.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	@Override
	public Reader reader () {
		return new InputStreamReader(read());
	}

	/** Returns a reader for reading this file as characters.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	@Override
	public Reader reader (String charset) {
		try {
			return new InputStreamReader(read(), charset);
		} catch (UnsupportedEncodingException e) {
			throw new GdxRuntimeException("Encoding '" + charset + "' not supported", e);
		}
	}

	/** Returns a buffered reader for reading this file as characters.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	@Override
	public BufferedReader reader (int bufferSize) {
		return new BufferedReader(reader(), bufferSize);
	}

	/** Returns a buffered reader for reading this file as characters.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	@Override
	public BufferedReader reader (int bufferSize, String charset) {
		return new BufferedReader(reader(charset), bufferSize);
	}

	@Override
	public String readString() {
		return new String(readBytes());
	}

	@Override
	public String readString(String charset) {
		try {
			return new String(readBytes(), charset);
		} catch (UnsupportedEncodingException e) {
			throw new GdxRuntimeException("Error (incorrect encoding) reading file " + fh);
		}
	}

	@Override
	public Writer writer(boolean append) {
		return writer(append, null);
	}

	@Override
	public Writer writer(boolean append, String charset) {
		if (fh.type() == Files.FileType.Classpath) throw new GdxRuntimeException("Cannot write to a classpath file: " + fh);
		if (fh.type() == Files.FileType.Internal) throw new GdxRuntimeException("Cannot write to an internal file: " + fh);
		parent().mkdirs();
		try {
			OutputStream output = write(append);
			if (charset == null)
				return new OutputStreamWriter(output);
			else
				return new OutputStreamWriter(output, charset);
		} catch (Exception ex) {
			if (file().isDirectory())
				throw new GdxRuntimeException("Cannot open a stream to a directory: " + fh + " (" + fh.type() + ")", ex);
			throw new GdxRuntimeException("Error writing file: " + fh + " (" + fh.type() + ")", ex);
		}
	}

	@Override
	public void writeString(String string, boolean append) {
		final byte[] bytes = string.getBytes();
		fh.writeBytes(encryptInPlace(bytes, 0, bytes.length), append);
	}

	@Override
	public void writeString(String string, boolean append, String charset) {
		try {
			final byte[] bytes = string.getBytes(charset);
			fh.writeBytes(encryptInPlace(bytes, 0, bytes.length), append);
		} catch (UnsupportedEncodingException e) {
			throw new GdxRuntimeException("Error (incorrect encoding) writing file " + fh);
		}
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
	public FileHandle[] list(FilenameFilter filter) {
		return fh.list(filter);
	}

	@Override
	public FileHandle[] list(FileFilter filter) {
		return fh.list(filter);
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