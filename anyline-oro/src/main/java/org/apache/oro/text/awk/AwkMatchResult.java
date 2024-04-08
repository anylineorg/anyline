/*
 * $Id: AwkMatchResult.java,v 1.8 2003/11/07 20:16:24 dfs Exp $
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

import org.apache.oro.text.regex.*;

/**
 * A class used to store and access the results of an AwkPattern match.
 * It is important for you to remember that AwkMatcher does not save
 * parenthesized sub-group information.  Therefore the number of groups
 * saved in an AwkMatchResult will always be 1.
 *
 * @version @version@
 * @since 1.0
 * @see PatternMatcher
 * @see AwkMatcher
 * @see AwkCompiler
 */

final class AwkMatchResult implements MatchResult {
  /**
   * The character offset into the line or stream where the match
   * begins.  Pattern matching methods that look for matches a line at
   * a time should use this field as the offset into the line
   * of the match.  Methods that look for matches independent of line
   * boundaries should use this field as the offset into the entire
   * text stream.
   */
  private int __matchBeginOffset;

  /**
   * The length of the match.  Stored as a convenience to avoid calling
   * the String length().  Since groups  aren't saved, all we need is the
   * length and the offset into the stream.
   */
  private int __length;

  /**
   * The entire string that matched the pattern.
   */
  private String __match;

  /**
   * Default constructor given default access to prevent instantiation
   * outside the package.
   */
  AwkMatchResult(String match, int matchBeginOffset){
    __match            = match;
    __length           = match.length();
    __matchBeginOffset = matchBeginOffset;
  }

  /**
   * Adjusts the relative offset where the match begins to an absolute
   * value.  Only used by AwkMatcher to adjust the offset for stream
   * matches.
   */
  void _incrementMatchBeginOffset(int streamOffset) {
    __matchBeginOffset+=streamOffset;
  }

  /**
   * @return The length of the match.
   */
  public int length(){return __length; }

  /**
   * @return The number of groups contained in the result.  This number
   *         includes the 0th group.  In other words, the result refers
   *         to the number of parenthesized subgroups plus the entire match
   *         itself.  Because Awk doesn't save parenthesized groups, this
   *         always returns 1.
   */
  public int groups(){return 1; }

  /**
   * @param group The pattern subgroup to return.
   * @return A string containing the indicated pattern subgroup.  Group
   *         0 always refers to the entire match.  If a group was never
   *         matched, it returns null.  This is not to be confused with
   *         a group matching the null string, which will return a String
   *         of length 0.
   */
  public String group(int group){return (group == 0 ? __match : null); }

  /**
   * @param group The pattern subgroup.
   * @return The offset into group 0 of the first token in the indicated
   *         pattern subgroup.  If a group was never matched or does
   *         not exist, returns -1.
   */
  public int begin(int group){return (group == 0 ? 0 : -1); }

  /**
   * @param group The pattern subgroup.
   * @return Returns one plus the offset into group 0 of the last token in
   *         the indicated pattern subgroup.  If a group was never matched
   *         or does not exist, returns -1.  A group matching the null
   *         string will return its start offset.
   */
  public int end(int group){return (group == 0 ? __length : -1); }

  /**
   * Returns an offset marking the beginning of the pattern match
   * relative to the beginning of the input.
   * <p>
   * @param group The pattern subgroup.
   * @return The offset of the first token in the indicated
   *         pattern subgroup.  If a group was never matched or does
   *         not exist, returns -1.
   */
  public int beginOffset(int group){
    return (group == 0 ? __matchBeginOffset : -1);
  }

  /**
   * Returns an offset marking the end of the pattern match 
   * relative to the beginning of the input.
   * <p>
   * @param group The pattern subgroup.
   * @return Returns one plus the offset of the last token in
   *         the indicated pattern subgroup.  If a group was never matched
   *         or does not exist, returns -1.  A group matching the null
   *         string will return its start offset.
   */
  public int endOffset(int group){
    return (group == 0 ? __matchBeginOffset + __length : -1);
  }

  /**
   * The same as group(0).
   *
   * @return A string containing the entire match.
   */
  public String toString() {return group(0); }

}

