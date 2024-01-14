package com.github.tommyettinger.cringe;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.*;

/**
 * A basic way to encrypt a {@link FileHandle} using the <a href="https://en.wikipedia.org/wiki/Speck_(cipher)">Speck
 * Cipher</a>. The supported operations are mostly limited to
 * reading or writing byte arrays, which this always keeps in memory as unencrypted bytes and writes to disk as
 * encrypted bytes. {@link #write(boolean)} and {@link #write(InputStream, boolean)} are not supported. The operations
 * this class supports are sufficient to read and write {@link com.badlogic.gdx.graphics.Pixmap} and
 * {@link com.badlogic.gdx.graphics.Texture} objects with encryption. You can also use
 * {@link #writeString(String, boolean, String)} and {@link #readString(String)} to read and write Strings, but you must
 * be careful to avoid version control (such as Git) changing line endings in encrypted text files. For those, using a
 * file extension like {@code .dat} is a good idea to avoid your data being sometimes changed irreversibly.
 * <br>
 * You may want to use this class to encrypt or decrypt files on platforms that don't have the javax.crypto package,
 * such as GWT (this is probably compatible). That is probably the most valid usage of the class at this point.
 * <br>
 * This uses four {@code long} items as its key, and additionally generates one long nonce from the key and the relative
 * path of the given FileHandle. Don't expect much meaningful security out of this, but this is enough to prevent the
 * average user from just opening up a JAR to look at all the images or read the whole script. Someone determined enough
 * could use a Java agent to replace the writing part of this class with part that writes unencrypted, or just browse
 * the unencrypted data in-memory, so this is very far from bulletproof.
 * <br>
 * Based off <a href="https://gist.github.com/MobiDevelop/6389767">a gist by MobiDevelop</a>.
 */
public final class EncryptedFileHandle extends FileHandle {

	private final transient FileHandle file;
	private final transient long k1, k2, k3, k4, n0;
	private final transient long[] key;

	/**
	 * Creates a EncryptedFileHandle that can write encrypted data to the wrapped FileHandle or read and decrypt
	 * encrypted data from the wrapped FileHandle. The four-part key and the FileHandle's {@link FileHandle#path()}
	 * must all be the same between encryption and decryption for them to refer to the same unencrypted file. This
	 * overload is meant to be used when the path will be the same for reading and writing. If the path may be
	 * different, use {@link #EncryptedFileHandle(FileHandle, long, long, long, long, String)} with either only
	 * the first path or some other source of a unique String.
	 *
	 * @param file the FileHandle to wrap; may be any type, such as {@link Files.FileType#Internal}
	 * @param k1 part 1 of the key; may be any long
	 * @param k2 part 2 of the key; may be any long
	 * @param k3 part 3 of the key; may be any long
	 * @param k4 part 4 of the key; may be any long
	 */
	public EncryptedFileHandle(FileHandle file, long k1, long k2, long k3, long k4) {
		this(file, k1, k2, k3, k4, file.path());
	}

	/**
     * Creates a EncryptedFileHandle that can write encrypted data to the wrapped FileHandle or read and decrypt
	 * encrypted data from the wrapped FileHandle. The four-part key and the {@code unique} String must all be the same
	 * between encryption and decryption for them to refer to the same unencrypted file. This overload is meant to be
	 * used when reading and writing to different paths; the unique String should be generated from only one of the
	 * paths, if you generate it from a path at all.
	 *
	 * @param file the FileHandle to wrap; may be any type, such as {@link Files.FileType#Internal}
	 * @param k1 part 1 of the key; may be any long
	 * @param k2 part 2 of the key; may be any long
	 * @param k3 part 3 of the key; may be any long
	 * @param k4 part 4 of the key; may be any long
	 * @param unique any String that is likely to be unique for a given key, such as the path to the wrapped file
	 */
	public EncryptedFileHandle(FileHandle file, long k1, long k2, long k3, long k4, String unique) {
		this.file = file;
		this.k1 = k1;
		this.k2 = k2;
		this.k3 = k3;
		this.k4 = k4;
		this.key = expandKey(k1, k2, k3, k4);
		this.n0 = k1 + k2 ^ k3 - (k4 ^ Scramblers.hash64(k1 ^ k2 ^ k3 ^ k4, unique));
	}
	/**
	 * Given a 256-bit key as four long values, this grows that initial key into a 2176-bit expanded key (a
	 * {@code long[34]}). This uses 34 rounds of the primary algorithm used by Speck.
	 * @param k1 a secret long; part of the key
	 * @param k2 a secret long; part of the key
	 * @param k3 a secret long; part of the key
	 * @param k4 a secret long; part of the key
	 * @return a 34-item long array that should, of course, be kept secret to be used cryptographically
	 */
	private static long[] expandKey(long k1, long k2, long k3, long k4) {
		long tk0 = k4, tk1 = k3, tk2 = k2, tk3 = k1;
		long[] k = new long[34];
		k[0] = k4;
		// corresponds to 34 rounds
		for (int i = 0, c = 0; i < 11; i++) {
			tk1 = (tk1 << 56 | tk1 >>> 8) + tk0 ^ c;
			tk0 = (tk0 << 3 | tk0 >>> 61) ^ tk1;
			++c;
			k[c] = tk0;

			tk2 = (tk2 << 56 | tk2 >>> 8) + tk0 ^ c;
			tk0 = (tk0 << 3 | tk0 >>> 61) ^ tk2;
			++c;
			k[c] = tk0;

			tk3 = (tk3 << 56 | tk3 >>> 8) + tk0 ^ c;
			tk0 = (tk0 << 3 | tk0 >>> 61) ^ tk3;
			++c;
			k[c] = tk0;
		}
		return k;
	}

	private static void xorIntoBytes(byte[] bytes, int index, long data) {
		if(bytes.length > index) {
			switch (bytes.length - index) {
				default: bytes[index + 7] ^= (byte) data;
				case 7:  bytes[index + 6] ^= (byte) (data >>> 8);
				case 6:  bytes[index + 5] ^= (byte) (data >>> 16);
				case 5:  bytes[index + 4] ^= (byte) (data >>> 24);
				case 4:  bytes[index + 3] ^= (byte) (data >>> 32);
				case 3:  bytes[index + 2] ^= (byte) (data >>> 40);
				case 2:  bytes[index + 1] ^= (byte) (data >>> 48);
				case 1:  bytes[index    ] ^= (byte) (data >>> 56);
			}
		}
	}

	private static void intoBytes(byte[] bytes, int index, long data) {
		if(bytes.length > index) {
			switch (bytes.length - index) {
				default: bytes[index + 7] = (byte) data;
				case 7:  bytes[index + 6] = (byte) (data >>> 8);
				case 6:  bytes[index + 5] = (byte) (data >>> 16);
				case 5:  bytes[index + 4] = (byte) (data >>> 24);
				case 4:  bytes[index + 3] = (byte) (data >>> 32);
				case 3:  bytes[index + 2] = (byte) (data >>> 40);
				case 2:  bytes[index + 1] = (byte) (data >>> 48);
				case 1:  bytes[index    ] = (byte) (data >>> 56);
			}
		}
	}

	/**
	 * An internal encryption step. Runs 34 rounds of the cipher over 16 bytes then writes the result into the
	 * ciphertext.
	 * @param iv0 the first half of the previous result, or the first IV if there was no previous result
	 * @param iv1 the last half of the previous result, or the second IV if there was no previous result
	 * @param ciphertext the ciphertext array, as byte items that will be written to
	 * @param cipherOffset the index to start writing to in ciphertext
	 */
	private void encryptStep(long iv0, long iv1, byte[] ciphertext, int cipherOffset) {
		long b0 = iv0, b1 = iv1;
		long[] key = this.key;

		for (int i = 0; i < 34; i++) {
			b1 = (b1 << 56 | b1 >>> 8) + b0 ^ key[i];
			b0 = (b0 << 3 | b0 >>> 61) ^ b1;
		}
		intoBytes(ciphertext, cipherOffset, b1);
		intoBytes(ciphertext, cipherOffset + 8, b0);
	}

	/**
	 * An internal encryption step. Runs 34 rounds of the cipher over 16 bytes then XORs the result with the
	 * plaintext.
	 * @param iv0 the first IV
	 * @param iv1 the second IV
	 * @param plaintext the plaintext array, as byte items; will be modified
	 * @param plainOffset the index to start writing to (with XOR) in plaintext
	 */
	private void encryptStepWithXor(long iv0, long iv1, byte[] plaintext, int plainOffset) {
		long b0 = iv0, b1 = iv1;
		long[] key = this.key;

		for (int i = 0; i < 34; i++) {
			b1 = (b1 << 56 | b1 >>> 8) + b0 ^ key[i];
			b0 = (b0 << 3 | b0 >>> 61) ^ b1;
		}
		xorIntoBytes(plaintext, plainOffset, b1);
		xorIntoBytes(plaintext, plainOffset + 8, b0);
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
	 * @param nonce a long that must never be reused as nonce again under the same key; needed to decrypt
	 * @param plaintext the byte array to encrypt; will not be modified
	 * @param plainOffset which index to start reading from plaintext
	 * @param ciphertext the byte array to write encrypted data to; will be modified
	 * @param cipherOffset which index to start writing to in ciphertext
	 * @param textLength how many byte items to read and encrypt from plaintext
	 * @return ciphertext, after modifications
	 */
	private byte[] encrypt(long nonce, byte[] plaintext, int plainOffset, byte[] ciphertext, int cipherOffset, int textLength) {
		int blocks = textLength + 15 >>> 4, i = 0;
		long counter = 0L;
		do {
			encryptStep(nonce, counter++, ciphertext, cipherOffset);

			for (int x = 0, n = Math.min(16, Math.min(ciphertext.length - cipherOffset, plaintext.length - plainOffset));
				 x < n;
				 x++, cipherOffset++, plainOffset++) {
				ciphertext[cipherOffset] ^= plaintext[plainOffset];
			}

			i++;
		} while(i < blocks);
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
	 * @param nonce a long that must never be reused as nonce again under the same key; needed to decrypt
	 * @param plaintext the byte array to encrypt in-place; will be modified
	 * @param plainOffset which index to start reading from and writing to in plaintext
	 * @param textLength how many byte items to read and encrypt from plaintext
	 */
	private byte[] encryptInPlace(long nonce, byte[] plaintext, int plainOffset, int textLength) {
		int blocks = textLength + 15 >>> 4, i = 0;
		long counter = 0L;
		do {
			encryptStepWithXor(nonce, counter++, plaintext, plainOffset);
			plainOffset+=16;
			i++;
		} while(i < blocks);
		return plaintext; // now it is encrypted
	}

	/**
	 * Decrypts a coded "ciphertext" byte array and changes it in-place to a "plaintext" byte array.
	 * This takes four {@code long}s as its key (256-bits), and also requires one unique (never used again) long as
	 * the nonce. How you generate keys is up to you, but the keys must be kept secret for encryption to stay secure.
	 * To generate nonce, secrecy isn't as important as uniqueness; calling DistinctRandom.nextLong() even many
	 * times will never return the same long unless nonce are requested for years from one generator, so it is a good
	 * option to produce nonce data. The rest of the arguments are about the data being decrypted. The ciphertext is the
	 * byte array that contains coded data, and should have been encrypted by
	 * {@link #encryptInPlace(long, byte[], int, int)}. The ciphertext will be modified
	 * in-place to become the plaintext. The cipheroffset is which index to start reading from in ciphertext. Lastly,
	 * the textLength is how many byte items to decrypt from ciphertext; this can be less than ciphertext's length.
	 *
	 * @param nonce a long that was used as nonce to encrypt this specific data
	 * @param ciphertext the byte array to read encrypted data from; will be modified in-place
	 * @param cipherOffset which index to start reading from and writing to in ciphertext
	 * @param textLength how many byte items to read and decrypt from ciphertext
	 */
	private byte[] decryptInPlace(long nonce, byte[] ciphertext, int cipherOffset, int textLength) {
		return encryptInPlace(nonce, ciphertext, cipherOffset, textLength);
	}

	public int readBytes(byte[] bytes, int offset, int size) {
		int ret = file.readBytes(bytes, offset, size);
		decryptInPlace(n0, bytes, offset, size);
		return ret;
	}

	@Override
	public byte[] readBytes() {
		byte[] bytes = file.readBytes();
		return decryptInPlace(n0, bytes, 0, bytes.length);
	}

	public InputStream read() {
		try (InputStream is = new ByteArrayInputStream(readBytes())) {
			return is;
		} catch (IOException ex) {
			throw new GdxRuntimeException(ex);
		}
	}

	@Override
	public OutputStream write(boolean append) {
		throw new UnsupportedOperationException("EncryptedFileHandle cannot be used to obtain an OutputStream.");
	}

	@Override
	public void write(InputStream input, boolean append) {
		throw new UnsupportedOperationException("EncryptedFileHandle cannot be used to emit an InputStream.");
	}

	@Override
	public void writeBytes(byte[] bytes, boolean append) {
		file.writeBytes(encrypt(n0, bytes, 0, new byte[bytes.length], 0, bytes.length), append);
	}

	@Override
	public void writeBytes(byte[] bytes, int offset, int length, boolean append) {
		file.writeBytes(encrypt(n0, bytes, offset, new byte[length], 0, length), 0, length, append);
	}

	@Override
	public FileHandle child(String name) {
		return new EncryptedFileHandle(file.child(name), k1, k2, k3, k4);
	}

	@Override
	public FileHandle sibling(String name) {
		return new EncryptedFileHandle(file.sibling(name), k1, k2, k3, k4);
	}

	@Override
	public FileHandle parent() {
		return new EncryptedFileHandle(file.parent(), k1, k2, k3, k4);
	}


	@Override
	public String path() {
		return file.path();
	}

	@Override
	public String name() {
		return file.name();
	}

	@Override
	public String extension() {
		return file.extension();
	}

	@Override
	public String nameWithoutExtension() {
		return file.nameWithoutExtension();
	}

	@Override
	public String pathWithoutExtension() {
		return file.pathWithoutExtension();
	}

	@Override
	public Files.FileType type() {
		return file.type();
	}

	@Override
	public File file() {
		return file.file();
	}

	@Override
	public BufferedInputStream read(int bufferSize) {
		throw new UnsupportedOperationException("EncryptedFileHandle.read(int) is unsupported.");
	}

	@Override
	public Reader reader() {
		throw new UnsupportedOperationException("EncryptedFileHandle.reader() is unsupported.");
	}

	@Override
	public Reader reader(String charset) {
		throw new UnsupportedOperationException("EncryptedFileHandle.reader(String) is unsupported.");
	}

	@Override
	public BufferedReader reader(int bufferSize) {
		throw new UnsupportedOperationException("EncryptedFileHandle.reader(int) is unsupported.");
	}

	@Override
	public BufferedReader reader(int bufferSize, String charset) {
		throw new UnsupportedOperationException("EncryptedFileHandle.reader(int, String) is unsupported.");
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
			throw new GdxRuntimeException("Error (incorrect encoding) reading file " + file);
		}
	}

	@Override
	public Writer writer(boolean append) {
		throw new UnsupportedOperationException("EncryptedFileHandle.writer(boolean) is unsupported.");
	}

	@Override
	public Writer writer(boolean append, String charset) {
		throw new UnsupportedOperationException("EncryptedFileHandle.writer(boolean, String) is unsupported.");
	}

	@Override
	public void writeString(String string, boolean append) {
		final byte[] bytes = string.getBytes();
		file.writeBytes(encryptInPlace(n0, bytes, 0, bytes.length), append);
	}

	@Override
	public void writeString(String string, boolean append, String charset) {
		try {
			final byte[] bytes = string.getBytes(charset);
			file.writeBytes(encryptInPlace(n0, bytes, 0, bytes.length), append);
		} catch (UnsupportedEncodingException e) {
			throw new GdxRuntimeException("Error (incorrect encoding) writing file " + file);
		}
	}

	@Override
	public FileHandle[] list() {
		return file.list();
	}

	@Override
	public FileHandle[] list(String suffix) {
		return file.list(suffix);
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public void mkdirs() {
		file.mkdirs();
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public boolean delete() {
		return file.delete();
	}

	@Override
	public boolean deleteDirectory() {
		return file.deleteDirectory();
	}

	@Override
	public void emptyDirectory() {
		file.emptyDirectory();
	}

	@Override
	public void emptyDirectory(boolean preserveTree) {
		file.emptyDirectory(preserveTree);
	}

	@Override
	public void copyTo(FileHandle dest) {
		file.copyTo(dest);
	}

	@Override
	public void moveTo(FileHandle dest) {
		file.moveTo(dest);
	}

	@Override
	public long length() {
		return file.length();
	}

	@Override
	public long lastModified() {
		return file.lastModified();
	}

	@Override
	public String toString() {
		return file.toString();
	}
}