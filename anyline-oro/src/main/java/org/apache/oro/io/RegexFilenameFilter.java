/*
 * $Id: RegexFilenameFilter.java,v 1.9 2003/11/07 20:16:23 dfs Exp $
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


package org.apache.oro.io;

import java.io.*;

import org.apache.oro.text.regex.*;
import org.apache.oro.text.*;

/**
 * RegexFilenameFilter is the base class for a set of FilenameFilter
 * implementations that filter based on a regular expression.
 *
 * @version @version@
 * @since 1.0
 * @see Perl5FilenameFilter
 * @see AwkFilenameFilter
 * @see GlobFilenameFilter
 */
public abstract class RegexFilenameFilter implements FilenameFilter,
						     FileFilter
 {
  PatternCache   _cache;
  PatternMatcher _matcher;
  Pattern _pattern;

  RegexFilenameFilter(PatternCache cache, PatternMatcher matcher, String regex)
  {
    _cache   = cache;
    _matcher = matcher;
    setFilterExpression(regex);
  }

  RegexFilenameFilter(PatternCache cache, PatternMatcher matcher,
		      String regex, int options)
  {
    _cache   = cache;
    _matcher = matcher;
    setFilterExpression(regex, options);
  }

  RegexFilenameFilter(PatternCache cache, PatternMatcher matcher) {
    this(cache, matcher, "");
  }

  /**
   * Set the regular expression on which to filter.
   * <p>
   * @param regex  The regular expression on which to filter.
   * @exception MalformedCachePatternException  If there is an error in
   *     compiling the regular expression.  This need not be caught if
   *     you are using a hard-coded expression that you know is correct.
   *     But for robustness and reliability you should catch this exception
   *     for dynamically entered expressions determined at runtime.
   */
  public void setFilterExpression(String regex)
       throws MalformedCachePatternException
  {
    _pattern = _cache.getPattern(regex);
  }

  /**
   * Set the regular expression on which to filter along with any
   * special options to use when compiling the expression.
   * <p>
   * @param regex  The regular expression on which to filter.
   * @param options A set of compilation options specific to the regular
   *        expression grammar being used.
   * @exception MalformedCachePatternException  If there is an error in
   *     compiling the regular expression.  This need not be caught if
   *     you are using a hard-coded expression that you know is correct.
   *     But for robustness and reliability you should catch this exception
   *     for dynamically entered expressions determined at runtime.
   */
  public void setFilterExpression(String regex, int options)
       throws MalformedCachePatternException
  {
    _pattern = _cache.getPattern(regex, options);
  }

  /**
   * Filters a filename.  Tests if the filename EXACTLY matches the pattern
   * contained by the filter.  The directory argument is not examined.
   * Conforms to the java.io.FilenameFilter interface.
   * <p>
   * @param dir  The directory containing the file.
   * @param filename  The name of the file.
   * @return True if the filename EXACTLY matches the pattern, false if not.
   */
  public boolean accept(File dir, String filename) {
    synchronized(_matcher) {
      return _matcher.matches(filename, _pattern);
    }
  }

  /**
   * Filters a filename.  Tests if the filename EXACTLY matches the pattern
   * contained by the filter.  The filename is defined as pathname.getName().
   * Conforms to the java.io.FileFilter interface.
   * <p>
   * @param pathname  The file pathname.
   * @return True if the filename EXACTLY matches the pattern, false if not.
   */
  public boolean accept(File pathname) {
    synchronized(_matcher) {
      return _matcher.matches(pathname.getName(), _pattern);
    }
  }
}
