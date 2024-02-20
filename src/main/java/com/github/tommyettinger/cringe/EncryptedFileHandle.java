package com.github.tommyettinger.cringe;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.*;

/**
 * A basic way to encrypt a {@link FileHandle} using the <a href="https://en.wikipedia.org/wiki/Speck_(cipher)">Speck
 * Cipher</a>. The supported operations are mostly limited to
 * reading or writing byte arrays, which this always keeps in memory as unencrypted bytes and writes to disk as
 * encrypted bytes. All FileHandle operations are supported on at least some platforms. This can read and write
 * {@link com.badlogic.gdx.graphics.Pixmap} and {@link com.badlogic.gdx.graphics.Texture} objects with encryption. You
 * can also use {@link #writeString(String, boolean, String)} and {@link #readString(String)} to read and write Strings,
 * but you must be careful to avoid version control (such as Git) changing line endings in encrypted text files. For
 * those, using a file extension like {@code .dat} can help avoid your data being sometimes changed irreversibly.
 * <br>
 * You may want to use this class to encrypt or decrypt files on platforms that don't have the javax.crypto package,
 * such as GWT. This is maybe technically compatible with GWT, but the libGDX Preloader generally makes using this code
 * impossible on GWT. Text files that get encrypted will use line-ending characters as ordinary gibberish characters in
 * the encoded output, but if a server does any filtering on line-endings, those characters will change and the encoding
 * will break. This can be solved by setting text files to the 'b' (binary) file type in assets.txt, but there are other
 * problems with images (and, in all likelihood, any other file types). Images are read in as binary during some of the
 * process, but they also seem to be checked for validity and, if valid, added to a map of loaded files. The encrypted
 * images this produces are not valid when read as PNG, JPG, or any other image format. So, sigh, GWT won't work yet.
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

	private final transient FileHandle fh;
	// nonce value
	private final transient long n0;
	// expanded, currently length 34
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
	 * encrypted data from the wrapped FileHandle. The long array keyBundle and the FileHandle's
	 * {@link FileHandle#path()} must all be the same between encryption and decryption for them to refer to the same
	 * unencrypted file. This overload is meant to be used when the path will be the same for reading and writing. If
	 * the path may be different, use {@link #EncryptedFileHandle(FileHandle, long[], String)} with
	 * either only the first path or some other source of a unique String.
	 *
	 * @param file the FileHandle to wrap; may be any type, such as {@link Files.FileType#Internal}
	 * @param keyBundle a long array that should be at least length 34; if not, the remaining items are generated
	 */
	public EncryptedFileHandle(FileHandle file, long[] keyBundle) {
		this(file, keyBundle, file.path());
	}

	/**
     * Creates a EncryptedFileHandle that can write encrypted data to the wrapped FileHandle or read and decrypt
	 * encrypted data from the wrapped FileHandle. The four-part key and the {@code unique} String must all be the same
	 * between encryption and decryption for them to refer to the same unencrypted file. This overload is meant to be
	 * used when reading and writing to different paths; the unique String should be generated from only one of the
	 * paths, if you generate it from a path at all.
	 *
	 * @param file the FileHandle to wrap; may be any type, such as {@link Files.FileType#Internal}
	 * @param keyBundle a long array that should be at least length 34; if not, the remaining items are generated
	 * @param unique any String that is likely to be unique for a given key, such as the path to the wrapped file
	 */
	public EncryptedFileHandle(FileHandle file, long[] keyBundle, String unique) {
		this.fh = file;
		this.key = expandKey(keyBundle);
		this.n0 = Scramblers.hash64(Scramblers.scramble(this.key[0] ^ this.key[this.key.length-1]), unique);
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
		this.fh = file;
		this.key = expandKey(k1, k2, k3, k4);
		this.n0 = Scramblers.hash64(Scramblers.scramble(this.key[0] ^ this.key[this.key.length-1]), unique);
	}

	/**
	 * Given a long array that is usually an existing expanded key, this either copies that key if it is at least length
	 * 34, or grows that initial key into a 2176-bit expanded key (a {@code long[34]}).
	 * This doesn't reuse Speck, but it still only uses ARX operations if it has to grow the key.
	 * @param keyBundle a long array that should be at least length 34; if not, the remaining items are generated
	 * @return a 34-item long array that should, of course, be kept secret to be used cryptographically
	 */
	private static long[] expandKey(long[] keyBundle) {
		long[] k = new long[34];
		int length;
		if(keyBundle == null) {
			length = 1;
			k[0] = 123456789L;
		} else {
			length = keyBundle.length;
			System.arraycopy(keyBundle, 0, k, 0, Math.min(length, k.length));
		}
		if(length < 34) {
			long stateA = length;
			long stateB = length;
			long stateC = length;
			long stateD = length;
			long stateE = length;
			for (int i = length; i < k.length; i++) {
				final long fa = stateA;
				final long fb = stateB;
				final long fc = stateC;
				final long fd = stateD;
				final long fe = stateE;
				stateA = fa + 0x9E3779B97F4A7C15L;
				stateB = fa ^ fe;
				stateC = fb + fd;
				stateD = (fc << 52 | fc >>> 12);
				stateE = fb + fc;
				k[i] = length + stateB;
			}
		}
		return k;
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

	@Override
	public int readBytes(byte[] bytes, int offset, int size) {
		int ret = fh.readBytes(bytes, offset, size);
		decryptInPlace(n0, bytes, offset, size);
		return ret;
	}

	@Override
	public byte[] readBytes() {
		byte[] bytes = fh.readBytes();
		return decryptInPlace(n0, bytes, 0, bytes.length);
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
		return new FilterOutputStream(fh.write(append)){
			private long bytesWritten = append ? fh.length() : 0L;
			private final byte[] text = new byte[16];
			private boolean consumedBlock = true;

			@Override
			public void write(int b) throws IOException {
				if(consumedBlock)
					encryptStep(n0, bytesWritten & -16, text, 0);
				super.write(b ^ text[(int)(bytesWritten++ & 15)]);
				consumedBlock = ((bytesWritten & 15) == 0);
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				while (len >= 16) {
					if(consumedBlock) {
						System.arraycopy(b, off, text, 0, 16);
						encryptStepWithXor(n0, bytesWritten & -16, text, 0);
						bytesWritten += 16;
						super.write(text, 0, 16);
					} else {
						long n = (bytesWritten & -16) + 16;
						int start = (int)(bytesWritten & 15);
						for (int i = start; bytesWritten < n; bytesWritten++, i++) {
							text[i] ^= b[off+i];
						}
						super.write(text, start, 16 - start);
					}
					len -= 16;
					consumedBlock = true;
				}
				if(len > 0){
					if(consumedBlock) {
						System.arraycopy(b, off, text, 0, len);
						encryptStepWithXor(n0, bytesWritten & -16, text, 0);
						bytesWritten += len;
						super.write(text, 0, len);
					} else {
						long n = (bytesWritten & -16) + len;
						int start = (int)(bytesWritten & 15);
						for (int i = start; bytesWritten < n; bytesWritten++, i++) {
							text[i] ^= b[off+i];
						}
						super.write(text, start, len - start);
					}
				}
			}
		};
	}

	@Override
	public void write(InputStream input, boolean append) {
		OutputStream output = null;
		try {
			output = write(append);
			StreamUtils.copyStream(input, output);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error stream writing to file: " + fh + " (" + type + ")", ex);
		} finally {
			StreamUtils.closeQuietly(input);
			StreamUtils.closeQuietly(output);
		}
	}

	@Override
	public void writeBytes(byte[] bytes, boolean append) {
		fh.writeBytes(encrypt(n0, bytes, 0, new byte[bytes.length], 0, bytes.length), append);
	}

	@Override
	public void writeBytes(byte[] bytes, int offset, int length, boolean append) {
		fh.writeBytes(encrypt(n0, bytes, offset, new byte[length], 0, length), 0, length, append);
	}

	@Override
	public FileHandle child(String name) {
		return new EncryptedFileHandle(fh.child(name), key);
	}

	@Override
	public FileHandle sibling(String name) {
		return new EncryptedFileHandle(fh.sibling(name), key);
	}

	@Override
	public FileHandle parent() {
		return new EncryptedFileHandle(fh.parent(), key);
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
		if (type == Files.FileType.Classpath) throw new GdxRuntimeException("Cannot write to a classpath file: " + fh);
		if (type == Files.FileType.Internal) throw new GdxRuntimeException("Cannot write to an internal file: " + fh);
		parent().mkdirs();
		try {
			OutputStream output = write(append);
			if (charset == null)
				return new OutputStreamWriter(output);
			else
				return new OutputStreamWriter(output, charset);
		} catch (Exception ex) {
			if (file().isDirectory())
				throw new GdxRuntimeException("Cannot open a stream to a directory: " + fh + " (" + type + ")", ex);
			throw new GdxRuntimeException("Error writing file: " + fh + " (" + type + ")", ex);
		}
	}

	@Override
	public void writeString(String string, boolean append) {
		final byte[] bytes = string.getBytes();
		fh.writeBytes(encryptInPlace(n0, bytes, 0, bytes.length), append);
	}

	@Override
	public void writeString(String string, boolean append, String charset) {
		try {
			final byte[] bytes = string.getBytes(charset);
			fh.writeBytes(encryptInPlace(n0, bytes, 0, bytes.length), append);
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