package com.sitech.crmpd.idmm2.client.jms.message;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年5月5日 上午10:14:49
 */
public class DefaultByteMessage extends DefaultMessage implements BytesMessage {

	/**
	 * @see javax.jms.BytesMessage#getBodyLength()
	 */
	@Override
	public long getBodyLength() throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.BytesMessage#readBoolean()
	 */
	@Override
	public boolean readBoolean() throws JMSException {
		return false;
	}

	/**
	 * @see javax.jms.BytesMessage#readByte()
	 */
	@Override
	public byte readByte() throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.BytesMessage#readUnsignedByte()
	 */
	@Override
	public int readUnsignedByte() throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.BytesMessage#readShort()
	 */
	@Override
	public short readShort() throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.BytesMessage#readUnsignedShort()
	 */
	@Override
	public int readUnsignedShort() throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.BytesMessage#readChar()
	 */
	@Override
	public char readChar() throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.BytesMessage#readInt()
	 */
	@Override
	public int readInt() throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.BytesMessage#readLong()
	 */
	@Override
	public long readLong() throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.BytesMessage#readFloat()
	 */
	@Override
	public float readFloat() throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.BytesMessage#readDouble()
	 */
	@Override
	public double readDouble() throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.BytesMessage#readUTF()
	 */
	@Override
	public String readUTF() throws JMSException {
		return null;
	}

	/**
	 * @see javax.jms.BytesMessage#readBytes(byte[])
	 */
	@Override
	public int readBytes(byte[] value) throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.BytesMessage#readBytes(byte[], int)
	 */
	@Override
	public int readBytes(byte[] value, int length) throws JMSException {
		return 0;
	}

	/**
	 * @see javax.jms.BytesMessage#writeBoolean(boolean)
	 */
	@Override
	public void writeBoolean(boolean value) throws JMSException {
	}

	/**
	 * @see javax.jms.BytesMessage#writeByte(byte)
	 */
	@Override
	public void writeByte(byte value) throws JMSException {
	}

	/**
	 * @see javax.jms.BytesMessage#writeShort(short)
	 */
	@Override
	public void writeShort(short value) throws JMSException {
	}

	/**
	 * @see javax.jms.BytesMessage#writeChar(char)
	 */
	@Override
	public void writeChar(char value) throws JMSException {
	}

	/**
	 * @see javax.jms.BytesMessage#writeInt(int)
	 */
	@Override
	public void writeInt(int value) throws JMSException {
	}

	/**
	 * @see javax.jms.BytesMessage#writeLong(long)
	 */
	@Override
	public void writeLong(long value) throws JMSException {
	}

	/**
	 * @see javax.jms.BytesMessage#writeFloat(float)
	 */
	@Override
	public void writeFloat(float value) throws JMSException {
	}

	/**
	 * @see javax.jms.BytesMessage#writeDouble(double)
	 */
	@Override
	public void writeDouble(double value) throws JMSException {
	}

	/**
	 * @see javax.jms.BytesMessage#writeUTF(java.lang.String)
	 */
	@Override
	public void writeUTF(String value) throws JMSException {
	}

	/**
	 * @see javax.jms.BytesMessage#writeBytes(byte[])
	 */
	@Override
	public void writeBytes(byte[] value) throws JMSException {
	}

	/**
	 * @see javax.jms.BytesMessage#writeBytes(byte[], int, int)
	 */
	@Override
	public void writeBytes(byte[] value, int offset, int length) throws JMSException {
	}

	/**
	 * @see javax.jms.BytesMessage#writeObject(java.lang.Object)
	 */
	@Override
	public void writeObject(Object value) throws JMSException {
	}

	/**
	 * @see javax.jms.BytesMessage#reset()
	 */
	@Override
	public void reset() throws JMSException {
	}

}
