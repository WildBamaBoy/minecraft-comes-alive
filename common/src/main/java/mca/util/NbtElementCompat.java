package mca.util;

import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtNull;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;

public interface NbtElementCompat {
    /**
     * The numeric ID of an NBT null value. Is {@value}.
     *
     * @see NbtNull
     */
    byte NULL_TYPE = 0;
    /**
     * The numeric ID of an NBT byte value. Is {@value}.
     *
     * @see NbtByte
     */
    byte BYTE_TYPE = 1;
    /**
     * The numeric ID of an NBT short value. Is {@value}.
     *
     * @see NbtShort
     */
    byte SHORT_TYPE = 2;
    /**
     * The numeric ID of an NBT integer value. Is {@value}.
     *
     * @see NbtInt
     */
    byte INT_TYPE = 3;
    /**
     * The numeric ID of an NBT long value. Is {@value}.
     *
     * @see NbtLong
     */
    byte LONG_TYPE = 4;
    /**
     * The numeric ID of an NBT float value. Is {@value}.
     *
     * @see NbtFloat
     */
    byte FLOAT_TYPE = 5;
    /**
     * The numeric ID of an NBT double value. Is {@value}.
     *
     * @see NbtDouble
     */
    byte DOUBLE_TYPE = 6;
    /**
     * The numeric ID of an NBT byte array value. Is {@value}.
     *
     * @see NbtByteArray
     */
    byte BYTE_ARRAY_TYPE = 7;
    /**
     * The numeric ID of an NBT string value. Is {@value}.
     *
     * @see NbtString
     */
    byte STRING_TYPE = 8;
    /**
     * The numeric ID of an NBT list value. Is {@value}.
     *
     * @see NbtList
     */
    byte LIST_TYPE = 9;
    /**
     * The numeric ID of an NBT compound value. Is {@value}.
     *
     * @see NbtCompound
     */
    byte COMPOUND_TYPE = 10;
    /**
     * The numeric ID of an NBT integer array value. Is {@value}.
     *
     * @see NbtIntArray
     */
    byte INT_ARRAY_TYPE = 11;
    /**
     * The numeric ID of an NBT long array value. Is {@value}.
     *
     * @see NbtLongArray
     */
    byte LONG_ARRAY_TYPE = 12;
    /**
     * A wildcard NBT numeric ID that can be used for <i>checking</i> whether an NBT element is an {@link AbstractNbtNumber}. Is {@value}.
     *
     * @see NbtCompound#getType(String)
     * @see NbtCompound#contains(String, int)
     */
    byte NUMBER_TYPE = 99;
}
