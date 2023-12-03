/*
 * $Id: PatternCache.java,v 1.7 2003/11/07 20:16:24 dfs Exp $
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

import org.apache.oro.text.regex.*;

/**
 * An interface defining the basic functions of a regular expression
 * cache.
 * <p>
 * A PatternCache is an object that takes care of compiling, storing, and
 * retrieving regular expressions so that the programmer does not have to
 * explicitly manage these operation himself.  The main benefit derived
 * is the ease of use from only having to express regular expressions
 * by their String representations.
 *
 * @version @version@
 * @since 1.0
 * @see MalformedCachePatternException
 */

public interface PatternCache {

  /**
   * Adds a pattern to the cache and returns the compiled pattern.  This
   * method is in principle almost identical to
   * {@link #getPattern(String)} except for the fact that
   * it throws a MalformedPatternException if an expression cannot be
   * compiled.
   * <p>
   * addPattern() is meant to be used when you expressly intend to add
   * an expression to a cache and is useful for front-loading a cache
   * with expressions before use.  If the expression added does not
   * already exist in the cache, it is compiled, added to the cache,
   * and returned.  If the compiled expression is already in the cache, it
   * is simply returned.
   * <p>
   * The expected behavior of this method should be to start replacing
   * patterns in the cache only after the cache has been filled to capacity.
   * <p>
   * @param expression  The regular expression to add to the cache.
   * @return The Pattern corresponding to the String representation of the
   *         regular expression.
   * @exception MalformedPatternException  If there is an error in compiling
   *         the regular expression.
   */
  public Pattern addPattern(String expression)
       throws MalformedPatternException;


  /**
   * Adds a pattern to the cache and returns the compiled pattern.  This
   * method is in principle almost identical to
   * {@link #getPattern(String)} except for the fact that
   * it throws a MalformedPatternException if an expression cannot be
   * compiled.
   * <p>
   * addPattern() is meant to be used when you expressly intend to add
   * an expression to the cache and is useful for front-loading a cache
   * with expressions before use.  If the expression added does not
   * already exist in the cache, it is compiled, added to the cache,
   * and returned.  If the compiled expression is already in the cache, it
   * is simply returned.
   * <p>
   * The expected behavior of this method should be to start replacing
   * patterns in the cache only after the cache has been filled to capacity.
   * <p>
   * @param expression  The regular expression to add to the cache.
   * @param options The compilation options to use when compiling the
   *        expression.
   * @return The Pattern corresponding to the String representation of the
   *         regular expression.
   * @exception MalformedPatternException  If there is an error in compiling
   *         the regular expression.
   */
  public Pattern addPattern(String expression, int options)
       throws MalformedPatternException;


  /**
   * This method fetches a pattern from the cache.  It is nearly identical
   * to {@link #addPattern addPattern()} except that it doesn't
   * throw a MalformedPatternException.  If the pattern is not in the
   * cache, it is compiled, placed in the cache, and returned.  If
   * the pattern cannot be compiled successfully, the implementation must
   * throw an exception derived from MalformedCachePatternException.
   * Note that this exception is derived from RuntimeException, which means
   * you are NOT forced to catch it by the compiler.  Please refer to
   * {@link MalformedCachePatternException} for a discussion of when you
   * should and shouldn't catch this exception.
   * <p>
   * @param expression  The regular expression to fetch from the cache in
   *        compiled form.
   * @return The Pattern corresponding to the String representation of the
   *         regular expression.
   * @exception MalformedCachePatternException  If there is an error in
   *     compiling the regular expression.
   */
  public Pattern getPattern(String expression)
       throws MalformedCachePatternException;


  /**
   * This method fetches a pattern from the cache.  It is nearly identical
   * to {@link #addPattern addPattern()} except that it doesn't
   * throw a MalformedPatternException.  If the pattern is not in the
   * cache, it is compiled, placed in the cache, and returned.  If
   * the pattern cannot be compiled successfully, it
   * throws a MalformedCachePatternException.
   * Note that this exception is derived from RuntimeException, which means
   * you are NOT forced to catch it by the compiler.  Please refer to
   * {@link MalformedCachePatternException} for a discussion of when you
   * should and shouldn't catch this exception.
   * <p>
   * @param expression  The regular expression to fetch from the cache in
   *        compiled form.
   * @param options The compilation options to use when compiling the
   *        expression.
   * @return The Pattern corresponding to the String representation of the
   *         regular expression.
   * @exception MalformedCachePatternException  If there is an error in
   *     compiling the regular expression.
   */
  public Pattern getPattern(String expression, int options)
       throws MalformedCachePatternException;


  /**
   * Returns the number of elements in the cache, not to be confused with
   * the {@link #capacity()} which returns the number
   * of elements that can be held in the cache at one time.
   * <p>
   * @return  The current size of the cache (i.e., the number of elements
   *          currently cached).
   */
  public int size();


  /**
   * Returns the maximum number of patterns that can be cached at one time.
   * <p>
   * @return The maximum number of patterns that can be cached at one time.
   */
  public int capacity();
}

