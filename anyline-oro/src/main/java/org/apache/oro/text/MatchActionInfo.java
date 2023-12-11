/*
 * $Id: MatchActionInfo.java,v 1.8 2003/11/07 20:16:24 dfs Exp $
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


package org.apache.oro.text;
 
import java.util.*;
import java.io.*;

import org.apache.oro.text.regex.*;

/**
 * This class is used to provide information regarding a match found by
 * MatchActionProcessor to a MatchAction callback implementation.
 *
 * @version @version@
 * @since 1.0
 * @see MatchAction
 * @see MatchActionProcessor
 */
public final class MatchActionInfo {
  /** The line number of the matching line */
  public int lineNumber;

  /** 
   * The String representation of the matching line with the trailing
   * newline truncated.
   */
  public String line;

  /** 
   * The char[] representation of the matching line with the trailing
   * newline truncated.
   */
  public char[] charLine;

  /**
   * The field separator used by the MatchActionProcessor.  This will be
   * set to null by a MatchActionProcessor instance if no field separator
   * was specified before match processing began.
   */
  public Pattern fieldSeparator;

  /**
   * A List of Strings containing the fields of the line that were
   * separated out by the fieldSeparator.  If no field separator was
   * specified, this variable will be set to null.
   */
  public List fields;

  /** The PatternMatcher used to find the match. */
  public PatternMatcher matcher;

  /**
   * The pattern found in the line of input.  If a MatchAction callback
   * is registered with a null pattern (meaning the callback should be
   * applied to every line of input), this value will be null.
   */
  public Pattern pattern;

  /** 
   * The first match found in the line of input.    If a MatchAction callback
   * is registered with a null pattern (meaning the callback should be
   * applied to every line of input), this value will be null.
   */
  public MatchResult match;

  /** The output stream passed to the MatchActionProcessor.  */
  public PrintWriter output;

  /**
   * The input stream passed to the MatchActionProcessor from which the
   * matching line was read.
   */
  public BufferedReader input;
}

