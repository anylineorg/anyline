/*
 * $Id: AwkCompiler.java,v 1.10 2003/11/07 20:16:24 dfs Exp $
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
 * The AwkCompiler class is used to create compiled regular expressions
 * conforming to the Awk regular expression syntax.  It generates
 * AwkPattern instances upon compilation to be used in conjunction
 * with an AwkMatcher instance.  AwkMatcher finds true leftmost-longest
 * matches, so you must take care with how you formulate your regular
 * expression to avoid matching more than you really want.
 * <p>
 * The supported regular expression syntax is a superset of traditional AWK,
 * but NOT to be confused with GNU AWK or other AWK variants.  Additionally,
 * this AWK implementation is DFA-based and only supports 8-bit ASCII.
 * Consequently, these classes can perform very fast pattern matches in
 * most cases.
 * <p>
 * This is the traditional Awk syntax that is supported:
 * <ul>
 * <li> Alternatives separated by |
 * <li> Quantified atoms
 * <dl compact>
 *      <dt> *     <dd> Match 0 or more times.
 *      <dt> +     <dd> Match 1 or more times.
 *      <dt> ?     <dd> Match 0 or 1 times.
 * </dl>
 * <li> Atoms
 * <ul>
 *     <li> regular expression within parentheses
 *     <li> a . matches everything including newline
 *     <li> a ^ is a null token matching the beginning of a string
 *          but has no relation to newlines (and is only valid at the
 *          beginning of a regex; this differs from traditional awk
 *          for the sake of efficiency in Java).
 *     <li> a $ is a null token matching the end of a string but has
 *          no relation to newlines (and is only valid at the
 *          end of a regex; this differs from traditional awk for the
 *          sake of efficiency in Java).
 *     <li> Character classes (e.g., [abcd]) and ranges (e.g. [a-z])
 *     <ul>
 *         <li> Special backslashed characters work within a character class
 *     </ul>
 *     <li> Special backslashed characters
 *     <dl compact>
 *         <dt> \b <dd> backspace
 *         <dt> \n <dd> newline
 *         <dt> \r <dd> carriage return
 *         <dt> \t <dd> tab
 *         <dt> \f <dd> formfeed
 *         <dt> \xnn <dd> hexadecimal representation of character
 *         <dt> \nn or \nnn <dd> octal representation of character
 *         <dt> Any other backslashed character matches itself
 *     </dl>
 * </ul></ul>
 * <p>
 * This is the extended syntax that is supported:
 * <ul>
 * <li> Quantified atoms
 * <dl compact>
 *      <dt> {n,m} <dd> Match at least n but not more than m times.
 *	<dt> {n,}  <dd> Match at least n times.
 *      <dt> {n}   <dd> Match exactly n times.  
 * </dl>
 * <li> Atoms
 * <ul>
 *     <li> Special backslashed characters
 *     <dl compact>
 *         <dt> \d <dd> digit [0-9]
 *         <dt> \D <dd> non-digit [^0-9]
 *         <dt> \w <dd> word character [0-9a-z_A-Z]
 *         <dt> \W <dd> a non-word character [^0-9a-z_A-Z]
 *         <dt> \s <dd> a whitespace character [ \t\n\r\f]
 *         <dt> \S <dd> a non-whitespace character [^ \t\n\r\f]
 *         <dt> \cD <dd> matches the corresponding control character
 *         <dt> \0 <dd> matches null character
 *     </dl>
 * </ul></ul>
 *
 * @version @version@
 * @since 1.0
 * @see PatternCompiler
 * @see MalformedPatternException
 * @see AwkPattern
 * @see AwkMatcher
 */                        
public final class AwkCompiler implements PatternCompiler {

  /**
   * The default mask for the {@link #compile compile} methods.
   * It is equal to 0 and indicates no special options are active.
   */
  public static final int DEFAULT_MASK          = 0;

  /**
   * A mask passed as an option to the {@link #compile compile} methods
   * to indicate a compiled regular expression should be case insensitive.
   */
  public static final int CASE_INSENSITIVE_MASK = 0x0001;

  /**
   * A mask passed as an option to the  {@link #compile compile} methods
   * to indicate a compiled regular expression should treat input as having
   * multiple lines.  This option affects the interpretation of
   * the <b> . </b> metacharacters.  When this mask is used,
   * the <b> . </b> metacharacter will not match newlines.  The default
   * behavior is for <b> . </b> to match newlines.
   */
  public static final int MULTILINE_MASK = 0x0002;

  static final char _END_OF_INPUT = '\uFFFF';
  
  // All of these are initialized by the compile() and _parse() methods
  // so there is no need or use in initializing them in the constructor
  // although this may change in the future.
  private boolean __inCharacterClass, __caseSensitive, __multiline;
  private boolean __beginAnchor, __endAnchor;
  private char __lookahead;
  private int __position, __bytesRead, __expressionLength;
  private char[] __regularExpression;
  private int __openParen, __closeParen;

  // We do not currently need to initialize any state, but keep this
  // commented out as a reminder that we may have to at some point.
  // public AwkCompiler() {}

  private static boolean __isMetachar(char token) {
    return (token == '*' || token == '?' || token == '+' ||
	    token == '[' || token == ']' || token == '(' ||
	    token == ')' || token == '|' || /* token == '^' ||
	    token == '$' || */ token == '.');
  }

  static boolean _isWordCharacter(char token) {
    return ((token >= 'a' && token <= 'z') || 
	    (token >= 'A' && token <= 'Z') || 
	    (token >= '0' && token <= '9') || 
	    (token == '_'));
  }

  static boolean _isLowerCase(char token){
    return (token >= 'a' && token <= 'z');
  }

  static boolean _isUpperCase(char token){
    return (token >= 'A' && token <= 'Z');
  }

  static char _toggleCase(char token){
    if(_isUpperCase(token))
      return (char)(token + 32);
    else if(_isLowerCase(token))
      return (char)(token - 32);

    return token;
  }

  private void __match(char token) throws MalformedPatternException {
    if(token == __lookahead){
      if(__bytesRead < __expressionLength)
	__lookahead = __regularExpression[__bytesRead++];
      else
	__lookahead = _END_OF_INPUT;
    }
    else
      throw new MalformedPatternException("token: " + token + 
				    " does not match lookahead: " +
				    __lookahead + " at position: " +
					     __bytesRead);
  }

  private void __putback() {
    if(__lookahead != _END_OF_INPUT)
      --__bytesRead;
    __lookahead = __regularExpression[__bytesRead - 1];
  }

  private SyntaxNode __regex() throws MalformedPatternException {
    SyntaxNode left;

    left = __branch();

    if(__lookahead == '|') {
      __match('|');
      return (new OrNode(left, __regex()));
    } 

    return left;
  }

  private SyntaxNode __branch() throws MalformedPatternException {
    CatNode current;
    SyntaxNode left, root;

    left = __piece();

    if(__lookahead == ')'){
      if(__openParen > __closeParen)
	return left;
      else
	  throw
	    new MalformedPatternException("Parse error: close parenthesis"
	     + " without matching open parenthesis at position " + __bytesRead);
    } else if(__lookahead == '|' || __lookahead == _END_OF_INPUT)
      return left;

    root = current = new CatNode();
    current._left = left;

    while(true) {
      left = __piece();

      if(__lookahead == ')'){
	if(__openParen > __closeParen){
	  current._right = left;
	  break;
	}
	else
	  throw
	    new MalformedPatternException("Parse error: close parenthesis"
	     + " without matching open parenthesis at position " + __bytesRead);
      } else  if(__lookahead == '|' || __lookahead == _END_OF_INPUT){
	current._right = left;
	break;
      }

      current._right = new CatNode();
      current = (CatNode)current._right;
      current._left   = left;
    }

    return root;
  }

  private SyntaxNode __piece() throws MalformedPatternException {
    SyntaxNode left;

    left = __atom();

    switch(__lookahead){
    case '+' : __match('+'); return (new PlusNode(left));
    case '?' : __match('?'); return (new QuestionNode(left));
    case '*' : __match('*'); return (new StarNode(left));
    case '{' : return __repetition(left);
    }

    return left;
  }

  // if numChars is 0, this means match as many as you want
  private int __parseUnsignedInteger(int radix, int minDigits, int maxDigits)
    throws MalformedPatternException {
    int num, digits = 0;
    StringBuffer buf;

    // We don't expect huge numbers, so an initial buffer of 4 is fine.
    buf = new StringBuffer(4);

    while(Character.digit(__lookahead, radix) != -1 && digits < maxDigits){
      buf.append((char)__lookahead);
      __match(__lookahead);
      ++digits;
    }

    if(digits < minDigits || digits > maxDigits)
      throw
	new MalformedPatternException(
        "Parse error: unexpected number of digits at position " + __bytesRead);

    try {
      num = Integer.parseInt(buf.toString(), radix);
    } catch(NumberFormatException e) {
      throw
	new MalformedPatternException("Parse error: numeric value at " +
				"position " + __bytesRead + " is invalid");
    }

    return num;
  }

  private SyntaxNode __repetition(SyntaxNode atom)
    throws MalformedPatternException {
    int min, max, startPosition[];
    SyntaxNode root = null;
    CatNode catNode;

    __match('{');

    min = __parseUnsignedInteger(10, 1, Integer.MAX_VALUE);
    startPosition = new int[1];
    startPosition[0] = __position;

    if(__lookahead == '}'){
      // Match exactly min times.  Concatenate the atom min times.
      __match('}');

      if(min == 0)
	throw
	  new MalformedPatternException(
              "Parse error: Superfluous interval specified at position " +
              __bytesRead + ".  Number of occurrences was set to zero.");

      if(min == 1)
	return atom;

      root = catNode = new CatNode();
      catNode._left = atom;

      while(--min > 1) {
	atom = atom._clone(startPosition);

	catNode._right = new CatNode();
	catNode       = (CatNode)catNode._right;
	catNode._left  = atom;
      }

      catNode._right = atom._clone(startPosition);
    } else if(__lookahead == ','){
      __match(',');

      if(__lookahead == '}') {
	// match at least min times
	__match('}');

	if(min == 0)
	  return new StarNode(atom);

	if(min == 1)
	  return new PlusNode(atom);

	root = catNode = new CatNode();
	catNode._left = atom;

	while(--min > 0) {
	  atom = atom._clone(startPosition);

	  catNode._right = new CatNode();
	  catNode       = (CatNode)catNode._right;
	  catNode._left  = atom;
	}

	catNode._right = new StarNode(atom._clone(startPosition));
      } else {
	// match at least min times and at most max times
	max = __parseUnsignedInteger(10, 1, Integer.MAX_VALUE);
	__match('}');

	if(max < min)
	  throw
	    new MalformedPatternException("Parse error: invalid interval; "
	     +  max + " is less than " + min + " at position " + __bytesRead);
	if(max == 0)
	  throw
	    new MalformedPatternException(
	    "Parse error: Superfluous interval specified at position " +
	    __bytesRead + ".  Number of occurrences was set to zero.");

	if(min == 0) {
	  if(max == 1)
	    return new QuestionNode(atom);

	  root = catNode = new CatNode();
	  atom = new QuestionNode(atom);
	  catNode._left = atom;

	  while(--max > 1) {
	    atom =  atom._clone(startPosition);

	    catNode._right = new CatNode();
	    catNode       = (CatNode)catNode._right;
	    catNode._left  = atom;
	  }

	  catNode._right = atom._clone(startPosition);
	} else if(min == max) {
	  if(min == 1)
	    return atom;

	  root = catNode = new CatNode();
	  catNode._left = atom;

	  while(--min > 1) {
	    atom = atom._clone(startPosition);

	    catNode._right = new CatNode();
	    catNode       = (CatNode)catNode._right;
	    catNode._left  = atom;
	  }

	  catNode._right = atom._clone(startPosition);
	} else {
	  int count;

	  root = catNode = new CatNode();
	  catNode._left = atom;

	  for(count=1; count < min; count++) {
	    atom = atom._clone(startPosition);

	    catNode._right = new CatNode();
	    catNode       = (CatNode)catNode._right;
	    catNode._left  = atom;
	  }

	  atom = new QuestionNode(atom._clone(startPosition));

	  count = max-min;

	  if(count == 1)
	    catNode._right = atom;
	  else {
	    catNode._right = new CatNode();
	    catNode = (CatNode)catNode._right;
	    catNode._left = atom;

	    while(--count > 1) {
	      atom = atom._clone(startPosition);

	      catNode._right = new CatNode();
	      catNode       = (CatNode)catNode._right;
	      catNode._left  = atom;
	    }

	    catNode._right = atom._clone(startPosition);
	  }
	}
      }
    } else
      throw
	new MalformedPatternException("Parse error: unexpected character " +
		__lookahead + " in interval at position "	+ __bytesRead);
    __position = startPosition[0];
    return root;
  }

  private SyntaxNode __backslashToken() throws MalformedPatternException {
    SyntaxNode current;
    char token;
    int number;

    __match('\\');

    if(__lookahead == 'x'){
      __match('x');
      // Parse a hexadecimal number
      current = _newTokenNode((char)__parseUnsignedInteger(16, 2, 2),
			     __position++);
    } else if(__lookahead == 'c') {
      __match('c');
      // Create a control character
      token = Character.toUpperCase(__lookahead);
      token = (char)(token > 63 ? token - 64 : token + 64);
      current = new TokenNode(token, __position++);
      __match(__lookahead);
    } else if(__lookahead >= '0' && __lookahead <= '9') {
      __match(__lookahead);

      if(__lookahead >= '0' && __lookahead <= '9'){
	// We have an octal character or a multi-digit backreference.
	// Assume octal character for now.
	__putback();
	number = __parseUnsignedInteger(10, 2, 3);
	number = Integer.parseInt(Integer.toString(number), 8);
	current =  _newTokenNode((char)number, __position++);
      } else {
	// We have either \0, an escaped digit, or a backreference.
	__putback();
	if(__lookahead == '0'){
	  // \0 matches the null character
	  __match('0');
	  current = new TokenNode('\0', __position++);
	} else {
	  // Either an escaped digit or backreference.
	  number = Character.digit(__lookahead, 10);
	  current =  _newTokenNode(__lookahead, __position++);
	  __match(__lookahead);
	}
      }
    } else if(__lookahead == 'b') {
      // Inside of a character class the \b means backspace, otherwise
      // it means a word boundary
      // if(__inCharacterClass)
      // \b always means backspace
      current = new TokenNode('\b', __position++);
      /*
      else 
	current = new TokenNode((char)LeafNode._WORD_BOUNDARY_MARKER_TOKEN,
				position++);
				*/
      __match('b');
    } /*else if(__lookahead == 'B' && !__inCharacterClass){
      current = new TokenNode((char)LeafNode._NONWORD_BOUNDARY_MARKER_TOKEN,
			      position++);
      __match('B');
    } */ else {
      CharacterClassNode characterSet;
      token = __lookahead;

      switch(__lookahead){
      case 'n' : token = '\n'; break;
      case 'r' : token = '\r'; break;
      case 't' : token = '\t'; break;
      case 'f' : token = '\f'; break;
      }

      switch(token) {
      case 'd' :
	characterSet = new CharacterClassNode(__position++);
	characterSet._addTokenRange('0', '9');
	current = characterSet;
	break;
      case 'D' :
	characterSet = new NegativeCharacterClassNode(__position++);
	characterSet._addTokenRange('0', '9');
	current = characterSet;
	break;
      case 'w' :
	characterSet = new CharacterClassNode(__position++);
	characterSet._addTokenRange('0', '9');
	characterSet._addTokenRange('a', 'z');
	characterSet._addTokenRange('A', 'Z');
	characterSet._addToken('_');
	current = characterSet;
	break;
      case 'W' :
	characterSet = new NegativeCharacterClassNode(__position++);
	characterSet._addTokenRange('0', '9');
	characterSet._addTokenRange('a', 'z');
	characterSet._addTokenRange('A', 'Z');
	characterSet._addToken('_');
	current = characterSet;
	break;
      case 's' :
	characterSet = new CharacterClassNode(__position++);
	characterSet._addToken(' ');
	characterSet._addToken('\f');
	characterSet._addToken('\n');
	characterSet._addToken('\r');
	characterSet._addToken('\t');
	current = characterSet;
	break;
      case 'S' :
	characterSet = new NegativeCharacterClassNode(__position++);
	characterSet._addToken(' ');
	characterSet._addToken('\f');
	characterSet._addToken('\n');
	characterSet._addToken('\r');
	characterSet._addToken('\t');
	current = characterSet;
	break;
	default  : current = _newTokenNode(token, __position++); break;
      }

      __match(__lookahead);
    }

    return current;
  }

  private SyntaxNode __atom() throws MalformedPatternException {
    SyntaxNode current;

    if(__lookahead == '(') {
      __match('(');
      ++__openParen;
      current = __regex();
      __match(')');
      ++__closeParen;
    } else if(__lookahead == '[')
      current = __characterClass();
    else if(__lookahead == '.') {
      CharacterClassNode characterSet;

      __match('.');
      characterSet = new NegativeCharacterClassNode(__position++);
      if(__multiline)
	characterSet._addToken('\n');
      current = characterSet;
    } else if(__lookahead == '\\') {
      current = __backslashToken();
    } /*else if(__lookahead == '^') {
      current =
	new TokenNode((char)LeafNode._BEGIN_LINE_MARKER_TOKEN, __position++);
      __match('^');
    } else if(__lookahead == '$') {
      current =
	new TokenNode((char)LeafNode._END_LINE_MARKER_TOKEN, __position++);
      __match('$');
    } */ else if(!__isMetachar(__lookahead)) {
      current = _newTokenNode(__lookahead, __position++);
      __match(__lookahead);
    } else
      throw
	new MalformedPatternException("Parse error: unexpected character " +
				__lookahead + " at position " + __bytesRead);

    return current;
  }

  private SyntaxNode __characterClass() throws MalformedPatternException {
    char lastToken, token;
    SyntaxNode node;
    CharacterClassNode current;

    __match('[');
    __inCharacterClass = true;

    if(__lookahead == '^'){
      __match('^');
      current = new NegativeCharacterClassNode(__position++);
    } else
      current = new CharacterClassNode(__position++);

    while(__lookahead != ']' && __lookahead != _END_OF_INPUT) {

      if(__lookahead == '\\'){
	node = __backslashToken();
	--__position;

	// __backslashToken() (actually newTokenNode()) does not take care of
        // case insensitivity when __inCharacterClass is true.
	if(node instanceof TokenNode){
	  lastToken = ((TokenNode)node)._token;
	  current._addToken(lastToken);
	  if(!__caseSensitive)
	    current._addToken(_toggleCase(lastToken));
	} else {
	  CharacterClassNode slash;
	  slash = (CharacterClassNode)node;
	  // This could be made more efficient by manipulating the
	  // characterSet elements of the CharacterClassNodes but
	  // for the moment, this is more clear.
	  for(token=0; token < LeafNode._NUM_TOKENS; token++){
	    if(slash._matches(token))
	      current._addToken(token);
	  }

	  // A byproduct of this act is that when a '-' occurs after
	  // a \d, \w, etc. it is not interpreted as a range and no
	  // parse exception is thrown.
	  // This is considered a feature and not a bug for now.
	  continue;
	}
      } else {
	lastToken = __lookahead;
	current._addToken(__lookahead);
	if(!__caseSensitive)
	  current._addToken(_toggleCase(__lookahead));
	__match(__lookahead);
      }

      // In Perl, a - is a token if it occurs at the beginning
      // or end of the character class.  Anywhere else, it indicates
      // a range.
      // A byproduct of this implementation is that if a '-' occurs
      // after the end of a range, it is interpreted as a '-' and no
      // exception is thrown. e.g., the second dash in [a-z-x]
      // This is considered a feature and not a bug for now.
      if(__lookahead == '-'){
	__match('-');
	if(__lookahead == ']'){
	  current._addToken('-');
	  break;
	} else if(__lookahead == '\\') {
	  node = __backslashToken();
	  --__position;
	  if(node instanceof TokenNode)
	    token = ((TokenNode)node)._token;
	  else
	    throw new MalformedPatternException(
	   "Parse error: invalid range specified at position " + __bytesRead);
	} else {
	  token = __lookahead;
	  __match(__lookahead);
	}

	if(token < lastToken)
	  throw new MalformedPatternException(
	 "Parse error: invalid range specified at position " + __bytesRead);
	current._addTokenRange(lastToken + 1, token);
	if(!__caseSensitive)
	  current._addTokenRange(_toggleCase((char)(lastToken + 1)),
				_toggleCase(token));
      }
    }

    __match(']');
    __inCharacterClass = false;
    return current;
  }

  SyntaxNode _newTokenNode(char token, int position){
    if(!__inCharacterClass && !__caseSensitive &&
       (_isUpperCase(token) || _isLowerCase(token))){
      CharacterClassNode node = new CharacterClassNode(position);
      node._addToken(token);
      node._addToken(_toggleCase(token));
      return node;
    }

    return new TokenNode(token, position);
  }

  SyntaxTree _parse(char[] expression) throws MalformedPatternException {
    SyntaxTree tree;

    __openParen = __closeParen = 0;
    __regularExpression = expression;
    __bytesRead = 0;
    __expressionLength = expression.length;
    __inCharacterClass = false;

    __position = 0;
    __match(__lookahead); // Call match to read first input.

    if(__lookahead == '^') {
      __beginAnchor = true;
      __match(__lookahead);
    }

    if(__expressionLength > 0 && expression[__expressionLength - 1] == '$') {
      --__expressionLength;
      __endAnchor = true;
    }

    if(__expressionLength > 1 || (__expressionLength == 1 && !__beginAnchor)) {
      CatNode root;
      root = new CatNode();
      root._left  = __regex();
      // end marker
      root._right =
	new TokenNode((char)LeafNode._END_MARKER_TOKEN, __position++);
      tree = new SyntaxTree(root, __position);
    } else 
      tree = new
	SyntaxTree(new TokenNode((char)LeafNode._END_MARKER_TOKEN, 0), 1);

    tree._computeFollowPositions();

    return tree;
  }

  /**
   * Compiles an Awk regular expression into an AwkPattern instance that
   * can be used by an AwkMatcher object to perform pattern matching.
   * <p>
   * @param pattern  An Awk regular expression to compile.
   * @param options  A set of flags giving the compiler instructions on
   *                 how to treat the regular expression.  Currently the
   *                 only meaningful flag is AwkCompiler.CASE_INSENSITIVE_MASK.
   * @return A Pattern instance constituting the compiled regular expression.
   *         This instance will always be an AwkPattern and can be reliably
   *         be casted to an AwkPattern.
   * @exception MalformedPatternException  If the compiled expression
   *  is not a valid Awk regular expression.
   */
  public Pattern compile(char[] pattern, int options)
       throws MalformedPatternException
  {
    SyntaxTree tree;
    AwkPattern regexp;

    __beginAnchor   = __endAnchor = false;
    __caseSensitive = ((options & CASE_INSENSITIVE_MASK) == 0);
    __multiline     = ((options & MULTILINE_MASK) != 0);
    tree   = _parse(pattern);
    regexp = new AwkPattern(new String(pattern), tree);
    regexp._options = options;
    regexp._hasBeginAnchor = __beginAnchor;
    regexp._hasEndAnchor   = __endAnchor;

    return regexp;
  }

  /**
   * Compiles an Awk regular expression into an AwkPattern instance that
   * can be used by an AwkMatcher object to perform pattern matching.
   * <p>
   * @param pattern  An Awk regular expression to compile.
   * @param options  A set of flags giving the compiler instructions on
   *                 how to treat the regular expression.  Currently the
   *                 only meaningful flag is AwkCompiler.CASE_INSENSITIVE_MASK.
   * @return A Pattern instance constituting the compiled regular expression.
   *         This instance will always be an AwkPattern and can be reliably
   *         be casted to an AwkPattern.
   * @exception MalformedPatternException  If the compiled expression
   *  is not a valid Awk regular expression.
   */
  public Pattern compile(String pattern, int options)
       throws MalformedPatternException
  {
    SyntaxTree tree;
    AwkPattern regexp;

    __beginAnchor = __endAnchor = false;
    __caseSensitive = ((options & CASE_INSENSITIVE_MASK) == 0);
    __multiline     = ((options & MULTILINE_MASK) != 0);
    tree   = _parse(pattern.toCharArray());
    regexp = new AwkPattern(pattern, tree);
    regexp._options = options;
    regexp._hasBeginAnchor = __beginAnchor;
    regexp._hasEndAnchor   = __endAnchor;

    return regexp;
  }

  /**
   * Same as calling <b>compile(pattern, AwkCompiler.DEFAULT_MASK);</b>
   * <p>
   * @param pattern  A regular expression to compile.
   * @return A Pattern instance constituting the compiled regular expression.
   *         This instance will always be an AwkPattern and can be reliably
   *         be casted to an AwkPattern.
   * @exception MalformedPatternException  If the compiled expression
   *  is not a valid Awk regular expression.
   */
  public Pattern compile(char[] pattern) throws MalformedPatternException {
    return compile(pattern, DEFAULT_MASK);
  }

  /**
   * Same as calling <b>compile(pattern, AwkCompiler.DEFAULT_MASK);</b>
   * <p>
   * @param pattern  A regular expression to compile.
   * @return A Pattern instance constituting the compiled regular expression.
   *         This instance will always be an AwkPattern and can be reliably
   *         be casted to an AwkPattern.
   * @exception MalformedPatternException  If the compiled expression
   *  is not a valid Awk regular expression.
   */
  public Pattern compile(String pattern) throws MalformedPatternException {
    return compile(pattern, DEFAULT_MASK);
  }

}
