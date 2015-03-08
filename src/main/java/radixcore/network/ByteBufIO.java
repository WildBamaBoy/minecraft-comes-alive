/*******************************************************************************
 * ByteBufIO.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package radixcore.network;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.io.output.ByteArrayOutputStream;

import radixcore.util.RadixExcept;

public final class ByteBufIO
{
	/**
	 * Writes the provided object to the provided ByteBuf.
	 * 
	 * @param buffer The ByteBuf that the object should be written to.
	 * @param object The object to write to the buffer.
	 */
	public static void writeObject(ByteBuf buffer, Object object)
	{
		writeByteArray(buffer, convertToByteArray(object));
	}

	/**
	 * Reads the next object from the provided ByteBuf.
	 * 
	 * @param buffer The ByteBuf containing the object to be read.
	 * @return Object form of the object read. Must be cast to expected type.
	 */
	public static Object readObject(ByteBuf buffer)
	{
		return convertBytesToObject(readByteArray(buffer));
	}

	/**
	 * Converts the provided object to a byte array that can be written to the buffer.
	 * 
	 * @param obj The object to be converted to a byte array.
	 * @return The object's byte array representation.
	 */
	public static byte[] convertToByteArray(Object obj)
	{
		final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

		try
		{
			final ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
			objectOutput.writeObject(obj);
			objectOutput.close();
		}

		catch (final IOException e)
		{
			e.printStackTrace();
		}

		return byteOutput.toByteArray();
	}

	/**
	 * Converts the provided byte array back into an Object.
	 * 
	 * @param byteArray The byte array to be converted.
	 * @return Object form of the provided byte array. Must be cast to expected type.
	 */
	public static Object convertBytesToObject(byte[] byteArray)
	{
		final ByteArrayInputStream byteInput = new ByteArrayInputStream(byteArray);
		Object returnObject = null;

		try
		{
			final ObjectInputStream objectInput = new ObjectInputStream(byteInput);

			returnObject = objectInput.readObject();
			objectInput.close();
		}

		catch (final IOException e)
		{
			e.printStackTrace();
		}

		catch (final ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		return returnObject;
	}

	/**
	 * Compresses the data in a byte array.
	 * 
	 * @param input The byte array to be compressed.
	 * @return The byte array in its compressed form.
	 */
	public static byte[] compress(byte[] input)
	{
		try
		{
			final Deflater deflater = new Deflater();
			deflater.setLevel(Deflater.BEST_COMPRESSION);
			deflater.setInput(input);

			final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(input.length);
			deflater.finish();

			final byte[] buffer = new byte[1024];

			while (!deflater.finished())
			{
				final int count = deflater.deflate(buffer);
				byteOutput.write(buffer, 0, count);
			}

			byteOutput.close();
			return byteOutput.toByteArray();
		}

		catch (final IOException e)
		{
			RadixExcept.logFatalCatch(e, "Error compressing byte array.");
			return null;
		}
	}

	/**
	 * Decompresses a compressed byte array.
	 * 
	 * @param input The byte array to be decompressed.
	 * @return The byte array in its decompressed, readable form.
	 */
	public static byte[] decompress(byte[] input)
	{
		try
		{
			final Inflater inflater = new Inflater();
			final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(input.length);
			final byte[] buffer = new byte[1024];
			inflater.setInput(input);

			while (!inflater.finished())
			{
				final int count = inflater.inflate(buffer);
				byteOutput.write(buffer, 0, count);
			}

			byteOutput.close();
			return byteOutput.toByteArray();
		}

		catch (final DataFormatException e)
		{
			RadixExcept.logFatalCatch(e, "Error decompressing byte array.");
			return null;
		}

		catch (final IOException e)
		{
			RadixExcept.logFatalCatch(e, "Error decompressing byte array.");
			return null;
		}
	}

	/**
	 * Writes the provided byte array to the buffer. Compresses the provided array and precedes it data with its length as an int. Then the compressed array itself is written.
	 * 
	 * @param buffer ByteBuf that the byte array will be written to.
	 * @param byteArray The byte array that will be written to the ByteBuf.
	 */
	public static void writeByteArray(ByteBuf buffer, byte[] byteArray)
	{
		final byte[] compressedArray = compress(byteArray);
		buffer.writeInt(compressedArray.length);
		buffer.writeBytes(compressedArray);
	}

	/**
	 * Reads the next byte array from the buffer. Gets the array's size by reading the next int, then reads that amount of bytes and returns the decompressed byte array.
	 * 
	 * @param buffer
	 * @return
	 */
	public static byte[] readByteArray(ByteBuf buffer)
	{
		final int arraySize = buffer.readInt();
		return decompress(buffer.readBytes(arraySize).array());
	}
}
