/*
 * $Id: AwkStreamInput.java,v 1.7 2003/11/07 20:16:24 dfs Exp $
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation", "Jakarta-Oro" 
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache" 
 *    or "Jakarta-Oro", nor may "Apache" or "Jakarta-Oro" appear in their 
 *    name, without prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.oro.text.awk;

import java.io.*;
import org.apache.oro.text.regex.*;

/**
 * The AwkStreamInput class is used to look for pattern matches in an
 * input stream (actually a java.io.Reader instance) in conjunction with
 * the AwkMatcher class.  It is called
 * AwkStreamInput instead of AwkInputStream to stress that it is a form
 * of streamed input for the AwkMatcher class to use rather than a subclass of
 * InputStream.
 * AwkStreamInput performs special internal buffering to accelerate
 * pattern searches through a stream.  You can determine the size of this
 * buffer and how it grows by using the appropriate constructor.
 * <p>
 * If you want to perform line by line
 * matches on an input stream, you should use a DataInput or BufferedReader
 * instance in conjunction
 * with one of the PatternMatcher methods taking a String, char[], or
 * PatternMatcherInput as an argument.  The DataInput and BufferedReader
 * readLine() methods will likely be implemented as native methods and
 * therefore more efficient than supporting line by line searching within
 * AwkStreamInput.
 * <p>
 * In the future the programmer will be able to set this class to save
 * all the input it sees so that it can be accessed later.  This will avoid
 * having to read a stream more than once for whatever reason.
 *
 * @version @version@
 * @since 1.0
 * @see AwkMatcher
 */
public final class AwkStreamInput {
  static final int _DEFAULT_BUFFER_INCREMENT = 2048;
  private Reader __searchStream;
  private int __bufferIncrementUnit;
  boolean _endOfStreamReached;
  // The offset into the stream corresponding to buffer[0]
  int _bufferSize, _bufferOffset, _currentOffset;
  char[] _buffer;

  /**
   * We use this default contructor only within the package to create a dummy
   * AwkStreamInput instance.
   */
  AwkStreamInput() {
    _currentOffset = 0;
  }


  /**
   * Creates an AwkStreamInput instance bound to a Reader with a
   * specified initial buffer size and default buffer increment.
   * <p>
   * @param input  The InputStream to associate with the AwkStreamInput
   *        instance.
   * @param bufferIncrement  The initial buffer size and the default buffer
   *      increment to use when the input buffer has to be increased in
   *      size.
   */
  public AwkStreamInput(Reader input, int bufferIncrement) {
    __searchStream = input;
    __bufferIncrementUnit = bufferIncrement;
    _buffer = new char[bufferIncrement];
    _bufferOffset = _bufferSize  =  _currentOffset = 0;
    _endOfStreamReached = false;
  }


  /**
   * Creates an AwkStreamInput instance bound to a Reader with an
   * initial buffer size and default buffer increment of 2048 bytes.
   * <p>
   * @param input  The InputStream to associate with the AwkStreamInput
   *        instance.
   */
  public AwkStreamInput(Reader input) {
    this(input, _DEFAULT_BUFFER_INCREMENT);
  }

  // Only called when buffer overflows
  int _reallocate(int initialOffset) throws IOException {
    int offset, bytesRead;
    char[] tmpBuffer;

    if(_endOfStreamReached)
      return _bufferSize;

    offset    = _bufferSize - initialOffset;
    tmpBuffer = new char[offset + __bufferIncrementUnit];

    bytesRead =
      __searchStream.read(tmpBuffer, offset, __bufferIncrementUnit);

    if(bytesRead <= 0){
      _endOfStreamReached = true;
      /* bytesRead should never equal zero, but if it does, we don't
	 want to continue to try and read, running the risk of entering
	 an infinite loop.  Throw an IOException instead, because this
	 really IS an exception. */
      if(bytesRead == 0)
	throw new IOException("read from input stream returned 0 bytes.");
      return _bufferSize;
    } else {
      _bufferOffset += initialOffset;
      _bufferSize = offset + bytesRead;

      System.arraycopy(_buffer, initialOffset, tmpBuffer, 0, offset);
      _buffer = tmpBuffer;
    }

    return offset;
  }

  boolean read() throws IOException {
    _bufferOffset+=_bufferSize;
    _bufferSize = __searchStream.read(_buffer);
    _endOfStreamReached = (_bufferSize == -1);
    return (!_endOfStreamReached);
  }

  public boolean endOfStream() { return _endOfStreamReached; }

}
