/*
 * $Id: MalformedPerl5PatternException.java,v 1.7 2003/11/07 20:16:25 dfs Exp $
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


package org.apache.oro.text.perl;

import org.apache.oro.text.MalformedCachePatternException;

/**
 * An exception used to indicate errors in Perl style regular expressions.
 * It is derived from RuntimeException, and therefore does not have to be
 * caught.  You should generally make an effort to catch
 * MalformedPerl5PatternException whenever you use dynamically generated
 * patterns (from user input or some other source).  Static expressions
 * represented as strings in your source code don't require exception
 * handling because as you write and test run your program you will
 * correct any errors in those expressions when you run into an uncaught
 * MalformedPerl5PatternException.  By the time you complete your
 * project, those static expressions will be guaranteed to be correct.
 * However, pieces of code with expressions that you cannot guarantee to
 * be correct should catch MalformedPerl5PatternException to ensure
 * reliability.
 *
 * @version @version@
 * @since 1.0
 * @see org.apache.oro.text.regex.MalformedPatternException
 */
public final class MalformedPerl5PatternException
       extends MalformedCachePatternException
{
  /**
   * Simply calls the corresponding constructor of its superclass.
   */
  public MalformedPerl5PatternException() {
    super();
  }

  /**
   * Simply calls the corresponding constructor of its superclass.
   * <p>
   * @param message  A message indicating the nature of the error.
   */
  public MalformedPerl5PatternException(String message) {
    super(message);                                
  }

}

