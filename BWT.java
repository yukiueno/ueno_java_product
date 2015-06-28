import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;


/**
 * This class contains methods to perform a Burrows-Wheeler transform in both directions
 * as well as an implementation of a Move-To-Front-Coder/Decoder.
 */

public class BWT {

	
	// static variables to avoid stack overflow while sorting
	static private byte[] _input;
	static private int[] _pointers;
	static private int _partIndex, _swapIndex, _swapLength;
	static private byte _partByte, _currentByte;
	static private boolean _cont;
	static private boolean _doSwap;
	

	/**
	 * Constant for "compress" mode.
	 */
	
	static final int COMPRESS = 1;
	
	
	/**
	 * Constant for "decompress" mode.
	 */
	
	static final int DECOMPRESS = 2;
	
	
	/**
	 * Constant for "matrix" mode.
	 */
	
	static final int MATRIX = 3;


	/**
	 * Constant for "frequency count" mode.
	 */
	
	static final int FREQ = 4;


	/**
	 * Swaps two elements in an array of <code>int</code>s.
	 * <p>
	 * The static class variable _pointers contains the array where the swap is performed
	 * </p>
	 *
	 * @param		index1				the first index.
	 * @param		index2				the second index.
	 */
	
	static private void swap(int index1, int index2) {
		int temp = _pointers[index1];
		_pointers[index1] = _pointers[index2];
		_pointers[index2] = temp;
	}
	
	
	/**
	 * Recursively Sorts a BWT matrix using a multi-key quicksort algorithm. This is an adaptation of the string
	 * sorting algorithm from <a href="http://www.cs.princeton.edu/~rs/strings">http://www.cs.princeton.edu/~rs/strings</a>.
	 * <p>
	 * The private class variables _input and _pointers contain the input data and the current pointer array for
	 * sorting.
	 * </p>
	 *
	 * @param		pStart				startIndex (inclusive) in the string pointers array
	 *									(needed for recursive sort).
	 * @param		pLength				length of pointers (starting from pStart) to consider in the
	 *									pointer array (needed for recursive sort).
	 * @param		compIndex			index at which comparison of the string should start
	 *									(needed for recursive sort).
	 */
	
	static public void sortByteStrings(int pStart, int pLength, int compIndex) {
		int a, b, c, d;

		// end recursion if we're left with one or no pointer
		if (pLength <= 1) {
			return;
		}
		
		// test if there are only equal elements
		_doSwap = true;
/*		if (pLength > 1) {
			do {
				_currentByte = _input[(_pointers[pStart] + compIndex) % _input.length];
				_cont = true;
				for (_swapIndex = pStart+1; _swapIndex < pLength+pStart; ++_swapIndex) {
					if (_input[(_pointers[_swapIndex] + compIndex) % _input.length] != _currentByte) {
						_cont = false;
						break;
					}
				}
				if (_cont) {
					if (compIndex < (_input.length - 1)) {
						++compIndex;
					} else {
						_cont = false;
						_doSwap = false;	// everything is totally equal, so don't perform any deeper sorting
					}
					//System.out.println("++compIndex");
				}
			} while (_cont);
		}
*/		
		if (_doSwap) {
			// choose the partitioning element randomly
			_partIndex = pStart + (int)(Math.random()*(double)pLength) % pLength;
				// the modulus operator is there to make _absolutely_ sure that we're not out of bounds
			
			// place the partitioning element at the beginning of the array
			swap(pStart, _partIndex);
			_partByte = _input[(_pointers[pStart] + compIndex) % _input.length];
				// the modulus operator provides the necessary wrap-around functionality
			
			// partition the pointer array so that all equal elements are placed on the left and right
			// and the lesser and greater elements around the middle
			a = pStart + 1;
			b = a;
			c = pStart + pLength - 1;
			d = c;
			//byte currentByte;
			while (b <= c) {
				do {
					_currentByte = _input[(_pointers[b] + compIndex) % _input.length];
					if (b <= c && _currentByte <= _partByte) {
						if (_currentByte == _partByte) {
							swap(a, b);
							++a;
						}
						++b;
					}
				} while (b <= c && _currentByte <= _partByte);
				do {
					_currentByte = _input[(_pointers[c] + compIndex) % _input.length];
					if (b <= c && _currentByte >= _partByte) {
						if (_currentByte == _partByte) {
							swap(c, d);
							--d;
						}
						--c;
					}
				} while (b <= c && _currentByte >= _partByte);
				if (b <= c) {	// "equal" cannot happen, it's just here to provide a 1:1 adaptation of the algorithm mentioned above
					swap(b, c);
					++b;
					--c;
				}
			}
			
			// --> debug info
			//byte[] partByteArray = new byte[1];
			//partByteArray[0] = partByte;
			//byte[] sorted = new byte[input.length];
			//for (int i = 0; i < pLength; ++i) {
			//	sorted[i] = input[(pointers[i+pStart] + compIndex) % input.length];
			//}
			//System.out.println(new String(partByteArray) + ": " + new String(sorted));
			// <--

			//System.out.println("deeper");
			
			// swap the left part of elements equal to the partitioning element to the middle
			//int swapIndex;
			_swapLength = a-pStart;
			_swapLength = (_swapLength <= (b-a) ? _swapLength : b-a);
			for (_swapIndex = 0; _swapIndex < _swapLength; ++_swapIndex) {
				swap(pStart+_swapIndex, b-_swapLength+_swapIndex);
			}
			
			// swap the right part of elements equal to the partitioning element to the middle
			_swapLength = pLength - (d+1-pStart);
			_swapLength = (_swapLength <= (d-c) ? _swapLength : d-c);
			for (_swapIndex = 0; _swapIndex < _swapLength; ++_swapIndex) {
				swap(b+_swapIndex, pStart+pLength-_swapLength+_swapIndex);
			}
			
			// sort the lesser elements
			sortByteStrings(pStart, b-a, compIndex);
			
			// sort the equal elements
			if (compIndex < (_input.length - 1)) {
				sortByteStrings(pStart + b-a, (a-pStart) + (pLength-(d+1-pStart)), compIndex+1);
			}
			
			// sort the greater elements
			sortByteStrings(pStart + pLength - (d-c), d-c, compIndex);
			
		}	// if (_doSwap)
		
	}
	
	
	/**
	 * Perfoms a Burrows-Wheeler transform on an input array. The result is written to the
	 * specified output array (which must be of the same size or larger than the input array).
	 * The index of the first input byte in the output array (i.e. the position where the first
	 * byte of the original input is found after resorting it to the output) is returned.
	 * <p>
	 * Attention: The input and output array must not be identical!!
	 * Also, this method does stupid things if the input length is 0!
	 * </p>
	 *
	 * @param		input				the input block.
	 * @param		output				the output block.
	 *
	 * @return		the index of the first input byte in the output array.
	 */
	
	static public int compressBWT(byte[] input, byte[] output) {
		int[] pointers = new int[input.length];
		int i;
		int ret = 0;
		for (i = 0; i < input.length; ++i) {
			pointers[i] = i;
		}
		_input = input;
		_pointers = pointers;
		sortByteStrings(0, input.length, 0);
		for (i = 0; i < input.length; ++i) {
			output[i] = input[(pointers[i]+input.length-1) % input.length];
			//output[i] = input[pointers[i]];
			//System.out.println(pointers[i]);
			if (pointers[i] == 0) {
				ret = i;
			}
		}
		
		return ret;
	}
	
	
	/**
	 * Perfoms a Burrows-Wheeler transform on an input array and outputs the resulting matrix.
	 * This method is only useful if used on text input.
	 * <p>
	 * The result takes the following form:
	 * </p>
	 * <p>
	 * [L column char]: [following characters]
	 * [L column char]: [following characters]
	 * ...
	 * </p>
	 *
	 * @param		input				the input block.
	 * @param		output				the output stream to write the matrix to.
	 * @param		charCount			the number of characters to output after the L-column.
	 *
	 * @return		the index of the first input byte in the output array.
	 *
	 * @exception	IOException			thrown if something goes wrong while writing to the output stream.
	 */
	
	static public int matrixBWT(byte[] input, OutputStream output, int charCount) throws IOException {
		int[] pointers = new int[input.length];
		int i, j;
		int ret = 0;
		for (i = 0; i < input.length; ++i) {
			pointers[i] = i;
		}
		_input = input;
		_pointers = pointers;
		sortByteStrings(0, input.length, 0);
		for (i = 0; i < input.length; ++i) {
			output.write(input[(pointers[i]+input.length-1) % input.length]);
			output.write(": ".getBytes());
			for (j = 0; j < charCount; ++j) {
				output.write(input[(pointers[i] + j) % input.length]);
			}
			output.write("\n".getBytes());
			if (pointers[i] == 0) {
				ret = i;
			}
		}
		
		return ret;
	}
	
	
	/**
	 * Performs a reverse Burrows-Wheeler transform on an array using the specified index
	 * (see forward transform).
	 * <p>
	 * Attention: The input and output arrays must not be identical!!
	 * </p>
	 *
	 * @param		input[]				the result of a BWT.
	 * @param		index				the index resulting from a BWT.
	 * @param		output[]			array to write the original bytes to.
	 */
	
	static public void decompressBWT(byte[] input, int index, byte[] output) {
		int i, j;
		byte nextByte;
		int localIndex, shortcutIndex;
		
		// reconstruct the first column of the matrix
		byte[] firstCol = new byte[input.length];
		System.arraycopy(input, 0, firstCol, 0, input.length);
		System.out.println("[decompress] Sorting first column of BWT matrix ...");
		Arrays.sort(firstCol);
		System.out.println("[decompress] Reconstructing the original input data ...");
		
		// build shurtcut arrays
		int[] count = new int[256];
		int[] byteStart = new int[256];
		int[] shortcut = new int[input.length];
		for (i = 0; i < 256; ++i) {
			count[i] = 0;
			byteStart[i] = -1;
		}
		for (i = 0; i < input.length; ++i) {
			shortcutIndex = (input[i] >= 0 ? (int)input[i] : (int)input[i] + 256);
			shortcut[i] = count[shortcutIndex];
			count[shortcutIndex] += 1;
			shortcutIndex = (firstCol[i] >= 0 ? (int)firstCol[i] : (int)firstCol[i] + 256);
			if (byteStart[shortcutIndex] == -1) {
				byteStart[shortcutIndex] = i;
			}
		}

		// reconstruct the original byte array
		localIndex = index;
		for (i = 0; i < input.length; ++i) {
			nextByte = input[localIndex];
			output[input.length-i-1] = nextByte;
			shortcutIndex = (nextByte >= 0 ? (int)nextByte : (int)nextByte + 256);
			localIndex = byteStart[shortcutIndex] + shortcut[localIndex];
		}
	}
	
	
	/**
	 * Performs a Move-To-Front coding of the input array.
	 *
	 * @param		input				the input data.
	 * @param		output				the coded output data.
	 */
	
	static public void compressMTF(byte[] input, byte[] output) {
		byte[] values = new byte[256];
		int i, j;
		byte currentByte;
		
		// initialize the array of MTF codes
		for (i = 0; i < 256; ++i) {
			values[i] = (byte)(i);
		}
		
		// perform the MTF coding
		for (i = 0; i < input.length; ++i) {
			currentByte = input[i];
			for (j = 0; j < 256; ++j) {
				if (values[j] == currentByte) {
					output[i] = (byte)(j);
					if (j != 0) {
						System.arraycopy(values, 0, values, 1, j);
						values[0] = currentByte;
					}
					break;
				}
			}	// for (j = 0; j < 256; ++j)
		}	// for (i = 0; i < input.length; ++i)
	}
	
	
	/**
	 * Inner helper class.
	 */
	
	private static class Block {
		private int _length;
		private byte _type;
		private byte _value;
		private Block(int length, byte type, byte value) {
			_length = length;
			_type = type;
			_value = value;
		}
	}
	
	
	/**
	 * Performs a run-length encoding of the input array. Don't call with an empty input array!
	 * <p>
	 * The output may be longer than the input! The input must not be longer than 2 GB.
	 * This algorithm performs rather poor regarding compression efficiency.
	 * </p>
	 *
	 * @param		input				the input data to encode.
	 *
	 * @return		the encoded data.
	 */
	
	static byte[] compressRLE(byte[] input) {
		// inner class 
		Vector blocks = new Vector();
		int size = 0;
		int numSame = 0;
		byte currentByte = 0;
		boolean rleFound;
		int rleIndex = 0;
		int i, j, k, x;
		Block block;
		
		// devide the input into RLE/non-RLE blocks
		for (i = 0; i < input.length;) {
			// search the next RLE-Block
			rleFound = false;
			for (j = i; j <= input.length; ++j) {
				if (j == i) {
					currentByte = input[j];
					numSame = 1;
				} else if (j == input.length) {
					if (numSame >= 100) {
						rleIndex = j-numSame;
						rleFound = true;
					}
				} else {
					if (input[j] == currentByte) {
						++numSame;
					} else {
						if (numSame >= 100) {
							rleIndex = j-numSame;
							rleFound = true;
							break;
						} else {
							currentByte = input[j];
							numSame = 1;
						}
					}
				}
			}
			
			// put the block(s) found into the vectors
			if (rleFound) {
				if (rleIndex == i) {
					blocks.addElement(new Block(numSame, (byte)1, currentByte));
					size += 6;
				} else {
					blocks.addElement(new Block(rleIndex-i, (byte)0, (byte)0));
					blocks.addElement(new Block(numSame, (byte)1, currentByte));
					size += 11+rleIndex-i;
				}
				i = rleIndex+numSame;
			} else {
				blocks.addElement(new Block(input.length-i, (byte)0, (byte)0));
				size += 5+input.length-i;
				break;
			}
		}
	
		// perform the RLE
		byte[] result = new byte[size];
		j = 0;
		k = 0;
		for (i = 0; i < blocks.size(); ++i) {
			block = (Block)blocks.elementAt(i);
			size = block._length;
			result[j++] = block._type;
			if (block._type == 1) {
				result[j++] = block._value;
				k += size;
			}
			result[j++] = (byte)(size >>> 24);
			result[j++] = (byte)(size >>> 16);
			result[j++] = (byte)(size >>> 8);
			result[j++] = (byte)size;
			if (block._type == 0) {
				for (x = 0; x < size; ++x) {
					result[j++] = input[k++];
				}
			}
		}
		
		return result;
	}
	
	
	/**
	 * Performs a reverse RLE on the input data.
	 *
	 * @param		input				the input data.
	 *
	 * @return		the decoded data.
	 */
	
	static byte[] decompressRLE(byte[] input) {
		int size, totalSize = 0;
		byte type, value = 0;
		int i, j, k;
		
		// calculate the length of the result
		for (i = 0; i < input.length;) {
			type = input[i++];
			if (type == 1) {
				++i;
			}
			size = 0;
			size = (((int)input[i++]) & 0xFF) << 24;
			size |= (((int)input[i++]) & 0xFF) << 16;
			size |= (((int)input[i++]) & 0xFF) << 8;
			size |= (((int)input[i++]) & 0xFF);
			totalSize += size;
			if (type == 0) {
				i += size;
			}
		}
		
		// decode the input data
		byte[] result = new byte[totalSize];
		k = 0;
		for (i = 0; i < input.length;) {
			type = input[i++];
			if (type == 1) {
				value = input[i++];
			}
			size = 0;
			size = (((int)input[i++]) & 0xFF) << 24;
			size |= (((int)input[i++]) & 0xFF) << 16;
			size |= (((int)input[i++]) & 0xFF) << 8;
			size |= (((int)input[i++]) & 0xFF);
			if (type == 0) {
				for (j = 0; j < size; ++j) {
					result[k++] = input[i++];
				}
			} else {
				for (j = 0; j < size; ++j) {
					result[k++] = value;
				}
			}
		}
		
		return result;
	}
	
	
	/**
	 * Performs a Move-To-Front decoding of the input array.
	 *
	 * @param		input				the input data.
	 * @param		output				the decoded output data.
	 */
	
	static public void decompressMTF(byte[] input, byte[] output) {
		byte[] values = new byte[256];
		int i, j;
		byte currentByte;
		int position;
		
		// initialize the array of MTF codes
		for (i = 0; i < 256; ++i) {
			values[i] = (byte)(i);
		}
		
		// perform the MTF decoding
		for (i = 0; i < input.length; ++i) {
			currentByte = input[i];
			position = (currentByte >= 0 ? (int)currentByte : (int)currentByte + 256);
			currentByte = values[position];
			output[i] = currentByte;
			if (position != 0) {
				System.arraycopy(values, 0, values, 1, position);
				values[0] = currentByte;
			}
		}
	}
	
	
	/**
	 * Applies a combined huffman/run-length encoding. The output is prefixed with
	 * the length of the input stream.
	 *
	 * @param		input				an array with input data.
	 * @param		outputStream		a stream the output is written to.
	 *
	 * @exception	IOException			thrown if something goes wrong while writing out the files.
	 */

	static public void pack(byte[] input, OutputStream output) throws IOException {
		long[] count = new long[512];	// 0-255: ordinary bytes; 256-511: number of zeroes
		long[] count2 = new long[256];	// codes after a run of zeroes
		int i;
		int index;
		int zeroCount;
		boolean count2used = false;
		
		// initialisations
		BitWriter bitWriter = new BitWriter(output);
		for (i = 0; i < 512; ++i) {
			count[i] = 0;
		}
		for (i = 0; i < 256; ++i) {
			count2[i] = 0;
		}
		zeroCount = 0;
		
		// count the bytes / runs of zeroes
		for (i = 0; i < input.length; ++i) {
			if (input[i] == 0) {
				if (zeroCount < 255) {
					++zeroCount;	// zeroCount ranges from 1 to 255
				} else {
					count[256] += 1;	// 255 and more to come ...
					zeroCount = 1;
				}
			} else {
				index = (input[i] >= 0 ? (int)input[i] : 256 + (int)input[i]);
				if (zeroCount > 0) {
					count[256+zeroCount] += 1;
					zeroCount = 0;
					count2[index] += 1;
					count2used = true;
				} else {
					count[index] += 1;
				}
			}
		}
		if (zeroCount > 0) {
			count[256+zeroCount] += 1;
		}
		
		// build the huffman tress
		Huffman huffman = new Huffman();
		huffman.buildTree(count);
		Huffman huffman2 = null;
		if (count2used) {
			huffman2 = new Huffman();
			huffman2.buildTree(count2);
		}
		
		// write the output file
		try {
			
			// write out the original length
			byte[] lengthBytes = new byte[4];
			int length = input.length;
			lengthBytes[0] = (byte)(length >>> 24);
			lengthBytes[1] = (byte)(length >>> 16);
			lengthBytes[2] = (byte)(length >>> 8);
			lengthBytes[3] = (byte)length;
			output.write(lengthBytes);
			
			// write out the huffman trees
			huffman.writeTree(bitWriter);
			if (count2used) {
				bitWriter.write(1);
				huffman2.writeTree(bitWriter);
			} else {
				bitWriter.write(0);
			}
		
			// iterate over the input bytes
			zeroCount = 0;
			for (i = 0; i < input.length; ++i) {
				if (input[i] == 0) {
					if (zeroCount < 255) {
						++zeroCount;	// zeroCount ranges from 1 to 255
					} else {
						huffman.encode(256, bitWriter);
						zeroCount = 1;
					}
				} else {
					index = (input[i] >= 0 ? (int)input[i] : 256 + (int)input[i]);
					if (zeroCount > 0) {
						huffman.encode(256+zeroCount, bitWriter);
						zeroCount = 0;
						huffman2.encode(index, bitWriter);
					} else {
						huffman.encode(index, bitWriter);
					}
				}
			}
			if (zeroCount > 0) {
				huffman.encode(256+zeroCount, bitWriter);
			}

			// flush the bit buffer
			bitWriter.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Applies a combined huffman/run-length decoding. The input is expected to be prefixed with
	 * the original length.
	 *
	 * @param		input				the input stream to read from.
	 *
	 * @exception	IOException			thrown if something goes wrong while reading.
	 */

	static public byte[] unpack(InputStream input) throws IOException {
		// initialisations
		BitReader bitReader = new BitReader(input);
		int length = 0;
		byte[] result = null;
		boolean useTree2;
		int value, zeroCount;
		int i, j;
		
		// read the input data
		try {
			// read the original length and create a byte array of that length
			byte[] lengthBytes = new byte[4];
			input.read(lengthBytes);
			length = (((int)lengthBytes[0]) & 0xFF) << 24;
			length |= (((int)lengthBytes[1]) & 0xFF) << 16;
			length |= (((int)lengthBytes[2]) & 0xFF) << 8;
			length |= (((int)lengthBytes[3]) & 0xFF);
			result = new byte[length];
			
			// read the huffman tress
			Huffman huffman = new Huffman();
			Huffman huffman2 = null;
			huffman.readTree(bitReader);
			if (bitReader.read() != 0) {
				huffman2 = new Huffman();
				huffman2.readTree(bitReader);
			}
		
			// iterate over the input bytes
			useTree2 = false;
			i = 0;
			while (i < length) {
				if (useTree2) {
					value = huffman2.decode(bitReader);
					useTree2 = false;
					result[i] = (byte)value;
					++i;
				} else {
					value = huffman.decode(bitReader);
					if (value < 256) {
						result[i] = (byte)value;
						++i;
					} else {
						if (value == 256) {
							zeroCount = 255;
						} else {
							zeroCount = value-256;
							useTree2 = true;
						}
						for (j = 0; j < zeroCount; ++j) {
							result[i] = 0;
							++i;
						}
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	

	/**
	 * Outputs the most frequently occuring bytes in the specified array.
	 *
	 * @param		input				the input array.
	 */

	static public void outputFreq(byte[] input) {
		int[] count = new int[256];
		int maxCount, i;
		int maxByte, j;
		
		// initialize the count array
		for (i = 0; i < 256; ++i) {
			count[i] = 0;
		}
		
		// count the byte occurences
		for (i = 0; i < input.length; ++i) {
			count[(input[i] >= 0 ? (int)input[i] : 256 + (int)input[i])] += 1;
		}
		
		// output the ten most frequently occuring bytes
		for (j = 0; j < 10; ++j) {
			maxByte = -1;
			maxCount = 0;
			for (i = 0; i < 256; ++i) {
				if (count[i] > maxCount) {
					maxCount = count[i];
					maxByte = i;
				}
			}
			if (maxByte != -1) {
				count[maxByte] = 0;
				System.out.println("" + maxByte + ": " + maxCount);
			} else {
				break;
			}
		}
		
		// output the sum of the rest of the bytes
		maxCount = 0;
		for (i = 0; i < 256; ++i) {
			maxCount += count[i];
		}
		System.out.println("rest: " + maxCount);
	}

	
	/**
	 * Main method.
	 */
	
	static public void main(String[] args) {
		boolean forward = false;
		boolean compress = false;
		String inFile = null;
		String outFile = null;
		int mode = 0;
		
		// extract the command line args
		try {
			if (args[0].equals("-compress")) {
				mode = COMPRESS;
			} else if (args[0].equals("-decompress")) {
				mode = DECOMPRESS;
			} else if (args[0].equals("-matrix")) {
				mode = MATRIX;
			} else if (args[0].equals("-freq")) {
				mode = FREQ;
			} else {
				throw new IllegalArgumentException();
			}
			inFile = args[1];
			if (mode != FREQ) {
				outFile = args[2];
			}
		} catch (Exception e) {
			System.out.println("Usage: BWT <-compress|-decompress|-matrix|-freq>");
			System.out.println("           <input file> <output file>");
			System.out.println("");
			System.out.println("  -matrix: output some columns of the BWT matrix");
			System.out.println("  -countFreq: output byte frequencies before and after MTF");
			System.out.println("              (no output file required); no RLE performed");
			System.out.println("");
			System.exit(0);
		}
		
		// read the input file
		File inputFile = null;
		FileInputStream inputStream = null;
		File outputFile = null;
		FileOutputStream outputStream = null;
		byte[] workArray1 = null;
		byte[] workArray2 = null;
		byte[] indexBytes = new byte[4];
		int index = 0;
		try {

			inputFile = new File(inFile);
			inputStream = new FileInputStream(inputFile);
			if (mode != FREQ) {
				outputFile = new File(outFile);
				outputFile.delete();
				outputStream = new FileOutputStream(outputFile);
			}

			if (mode == COMPRESS) {

				// read the original data
				System.out.println("[compress] Reading input file ...");
				workArray1 = new byte[(int)(inputFile.length())];
				inputStream.read(workArray1);

				// perform RLE coding to prevent BWT from polluting the stack
				System.out.println("[compress] Performing RLE coding ...");
				workArray2 = compressRLE(workArray1);
				
				// perform the forward BWT
				System.out.println("[compress] Performing BWT ...");
				workArray1 = new byte[workArray2.length];
				index = compressBWT(workArray2, workArray1);

				// perform MTF coding
				System.out.println("[compress] Performing MTF coding ...");
				compressMTF(workArray1, workArray2);
				
				// write the BWT index
				indexBytes[0] = (byte)(index >>> 24);
				indexBytes[1] = (byte)(index >>> 16);
				indexBytes[2] = (byte)(index >>> 8);
				indexBytes[3] = (byte)index;
				outputStream.write(indexBytes);
				
				// write the compressed file
				System.out.println("[compress] Packing and writing output file ...");
				pack(workArray2, outputStream);
			
			} else if (mode == DECOMPRESS) {

				// read the BWT index
				inputStream.read(indexBytes);
				index = (((int)indexBytes[0]) & 0xFF) << 24;
				index |= (((int)indexBytes[1]) & 0xFF) << 16;
				index |= (((int)indexBytes[2]) & 0xFF) << 8;
				index |= (((int)indexBytes[3]) & 0xFF);
				
				// unpack the input data
				System.out.println("[decompress] Reading and unpacking input file ...");
				workArray1 = unpack(inputStream);
				workArray2 = new byte[workArray1.length];

				// perform MTF decoding
				System.out.println("[decompress] Performing MTF decoding ...");
				decompressMTF(workArray1, workArray2);
				
				// perform reverse BWT
				System.out.println("[decompress] Performing reverse BWT ...");
				decompressBWT(workArray2, index, workArray1);
				
				// perform reverse RLE
				System.out.println("[decompress] Performing RLE decoding ...");
				workArray2 = decompressRLE(workArray1);
				
				// write the decompressed file
				System.out.println("[decompress] Writing output file ...");
				outputStream.write(workArray2);
				
			} else if (mode == MATRIX) {
				
				// read the original data
				System.out.println("[matrix] Reading input file ...");
				workArray1 = new byte[(int)(inputFile.length())];
				inputStream.read(workArray1);
				
				// perform the forward BWT and write out the matrix
				System.out.println("[matrix] Performing BWT and writing matrix file ...");
				workArray2 = new byte[workArray1.length];
				index = matrixBWT(workArray1, outputStream, 20);
			
			} else if (mode == FREQ) {

				// read the original data
				System.out.println("[freq] Reading input file ...");
				workArray1 = new byte[(int)(inputFile.length())];
				inputStream.read(workArray1);

				// output the frequencies
				outputFreq(workArray1);
				
				// perform the forward BWT
				System.out.println("[compress] Performing BWT ...");
				workArray2 = new byte[workArray1.length];
				index = compressBWT(workArray1, workArray2);

				// perform MTF coding
				System.out.println("[compress] Performing MTF coding ...");
				compressMTF(workArray2, workArray1);
				
				// output the frequencies again
				outputFreq(workArray1);
			}

			inputStream.close();
			if (mode != FREQ) {
				outputStream.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
