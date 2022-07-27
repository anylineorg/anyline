/*
 * $Id: MatchActionProcessor.java,v 1.10 2003/11/07 20:16:24 dfs Exp $
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
 
import java.io.*;
import java.util.*;

import org.apache.oro.text.regex.*;

/**
 * The MatchActionProcessor class provides AWK-like line by line filtering
 * of a text stream, pattern action pair association, and field splitting
 * based on a registered separator.  However, the class can be used with
 * any compatible PatternMatcher/PatternCompiler implementations and
 * need not use the AWK matching classes in org.apache.oro.text.awk.  In fact,
 * the default matcher and compiler used by the class are Perl5Matcher and
 * Perl5Compiler from org.apache.oro.text.regex.
 * <p>
 * To completely understand how to use MatchActionProcessor, you should first
 * look at {@link MatchAction} and {@link MatchActionInfo}.
 * A MatchActionProcessor is first initialized with
 * the desired PatternCompiler and PatternMatcher instances to use to compile
 * patterns and perform matches.  Then, optionally, a field separator may
 * be registered with {@link #setFieldSeparator setFieldSeparator()}
 * Finally, as many pattern action pairs as desired are registerd with
 * {@link #addAction addAction()} before processing the input
 * with {@link #processMatches processMatches()}.  Pattern action
 * pairs are processed in the order they were registered.
 * <p>
 * The look of added actions can closely mirror that of AWK when anonymous
 * classes are used.  Here's an example of how you might use
 * MatchActionProcessor to extract only the second column of a semicolon
 * delimited file:
 * <p>
 * <pre>
 * import java.io.*;
 *
 * import org.apache.oro.text.*;
 * import org.apache.oro.text.regex.*;
 *
 * public final class semicolon {
 *
 *  public static final void main(String[] args) {
 *    MatchActionProcessor processor = new MatchActionProcessor();
 *
 *    try {
 *      processor.setFieldSeparator(";");
 *      // Using a null pattern means to perform the action for every line.
 *      processor.addAction(null, new MatchAction() {
 *        public void processMatch(MatchActionInfo info) {
 *	    // We assume the second column exists
 *          info.output.println(info.fields.elementAt(1));
 *        }
 *     });
 *   } catch(MalformedPatternException e) {
 *     e.printStackTrace();
 *     System.exit(1);
 *   }
 *
 *   try {
 *      processor.processMatches(System.in, System.out);
 *   } catch(IOException e) {
 *     e.printStackTrace();
 *     System.exit(1);
 *   }
 *  }
 *}
 * </pre>
 * You can redirect the following sample input to stdin to test the code:
 * <pre>
 * 1;Trenton;New Jersey
 * 2;Annapolis;Maryland
 * 3;Austin;Texas
 * 4;Richmond;Virginia
 * 5;Harrisburg;Pennsylvania
 * 6;Honolulu;Hawaii
 * 7;Santa Fe;New Mexico
 * </pre>
 *
 * @version @version@
 * @since 1.0
 * @see MatchAction
 * @see MatchActionInfo
 */
public final class MatchActionProcessor {
  private Pattern __fieldSeparator = null;
  private PatternCompiler __compiler;
  private PatternMatcher  __matcher;
  // If a pattern is null, it means to do it for every line.
  private Vector __patterns   = new Vector();
  private Vector __actions    = new Vector();

  private MatchAction __defaultAction = new DefaultMatchAction();

  /**
   * Creates a new MatchActionProcessor instance initialized with the specified
   * pattern compiler and matcher.  The field separator is set to null by
   * default, which means that matched lines will not be split into separate
   * fields unless the field separator is set with
   * {@link #setFieldSeparator setFieldSeparator()}.
   * <p>
   * @param compiler  The PatternCompiler to use to compile registered
   *                  patterns.
   * @param matcher   The PatternMatcher to use when searching for matches.
   */
  public MatchActionProcessor(PatternCompiler compiler, PatternMatcher matcher)
  {
    __compiler = compiler;
    __matcher  = matcher;
  }

  /**
   * Default constructor for MatchActionProcessor.  Same as calling
   * <blockquote><code>
   * MatchActionProcessor(new Perl5Compiler(), new Perl5Matcher());
   * </code></blockquote>
   */
  public MatchActionProcessor() {
    this(new Perl5Compiler(), new Perl5Matcher());
  }


  /**
   * Registers a pattern action pair, providing options to be used to
   * compile the pattern.  If a pattern is null, the action
   * is performed for every line of input.
   * <p>
   * @param pattern  The pattern to bind to an action.
   * @param options  The compilation options to use for the pattern.
   * @param action   The action to associate with the pattern.
   * @exception MalformedPatternException If the pattern cannot be compiled.
   */
  public void addAction(String pattern, int options, MatchAction action)
       throws MalformedPatternException
  {
    if(pattern != null)
      __patterns.addElement(__compiler.compile(pattern, options));
    else
      __patterns.addElement(null);
    __actions.addElement(action);
  }

  /**
   * Binds a patten to the default action, providing options to be
   * used to compile the pattern.  The default action is to simply print
   * the matched line to the output.  If a pattern is null, the action
   * is performed for every line of input.
   * <p>
   * @param pattern  The pattern to bind to an action.
   * @param options  The compilation options to use for the pattern.
   * @exception MalformedPatternException If the pattern cannot be compiled.
   */
  public void addAction(String pattern, int options)
       throws MalformedPatternException
  {
    addAction(pattern, options, __defaultAction);
  }

  /**
   * Binds a patten to the default action.  The default action is to simply
   * print the matched line to the output.  If a pattern is null, the action
   * is performed for every line of input.
   * <p>
   * @param pattern  The pattern to bind to an action.
   * @exception MalformedPatternException If the pattern cannot be compiled.
   */
  public void addAction(String pattern) throws MalformedPatternException {
    addAction(pattern, 0);
  }


  /**
   * Registers a pattern action pair.  If a pattern is null, the action
   * is performed for every line of input.
   * <p>
   * @param pattern  The pattern to bind to an action.
   * @param action   The action to associate with the pattern.
   * @exception MalformedPatternException If the pattern cannot be compiled.
   */
  public void addAction(String pattern, MatchAction action)
       throws MalformedPatternException
  {
    addAction(pattern, 0, action);
  }

  /**
   * Sets the field separator to use when splitting a line into fields.
   * If the field separator is never set, or set to null, matched input
   * lines are not split into fields.
   * <p>
   * @param separator  A regular expression defining the field separator.
   * @param options    The options to use when compiling the separator.
   * @exception MalformedPatternException If the separator cannot be compiled.
   */
  public void setFieldSeparator(String separator, int options)
       throws MalformedPatternException
  { 
    if(separator == null) {
      __fieldSeparator = null;
      return;
    }
    __fieldSeparator = __compiler.compile(separator, options);
  }


  /**
   * Sets the field separator to use when splitting a line into fields.
   * If the field separator is never set, or set to null, matched input
   * lines are not split into fields.
   * <p>
   * @param separator  A regular expression defining the field separator.
   * @exception MalformedPatternException If the separator cannot be compiled.
   */
  public void setFieldSeparator(String separator)
       throws MalformedPatternException
  {
    setFieldSeparator(separator, 0);
  }


  /**
   * This method reads the provided input one line at a time and for
   * every registered pattern that is contained in the line it executes
   * the associated MatchAction's processMatch() method.  If a field
   * separator has been defined with
   * {@link #setFieldSeparator setFieldSeparator()}, the
   * fields member of the MatchActionInfo instance passed to the
   * processMatch() method is set to a Vector of Strings containing
   * the split fields of the line.  Otherwise the fields member is set
   * to null.  If no match was performed to invoke the action (i.e.,
   * a null pattern was registered), then the match member is set
   * to null.  Otherwise, the match member will contain the result of
   * the match.
   * <p>
   * The input stream, having been exhausted, is closed right before the
   * method terminates and the output stream is flushed.
   * <p>
   * @see MatchActionInfo
   * @param input  The input stream from which to read lines.
   * @param output Where to send output.
   * @param encoding The character encoding of the InputStream source.
   *           If you also want to define an output character encoding,
   *           you should use {@link #processMatches(Reader, Writer)}
   *           and specify the encodings when creating the Reader and
   *           Writer sources and sinks.
   * @exception IOException  If an error occurs while reading input
   *            or writing output.
   */
  public void processMatches(InputStream input, OutputStream output,
			     String encoding)
    throws IOException
  {
    processMatches(new InputStreamReader(input, encoding),
		   new OutputStreamWriter(output));
  }


  /**
   * This method reads the provided input one line at a time using the
   * platform standart character encoding and for every registered
   * pattern that is contained in the line it executes the associated
   * MatchAction's processMatch() method.  If a field separator has been
   * defined with {@link #setFieldSeparator setFieldSeparator()}, the
   * fields member of the MatchActionInfo instance passed to the
   * processMatch() method is set to a Vector of Strings containing
   * the split fields of the line.  Otherwise the fields member is set
   * to null.  If no match was performed to invoke the action (i.e.,
   * a null pattern was registered), then the match member is set
   * to null.  Otherwise, the match member will contain the result of
   * the match.
   *
   * <p>
   * The input stream, having been exhausted, is closed right before the
   * method terminates and the output stream is flushed.
   * <p>
   *
   * @see MatchActionInfo
   * @param input  The input stream from which to read lines.
   * @param output Where to send output.
   * @exception IOException  If an error occurs while reading input
   *            or writing output.
   */
  public void processMatches(InputStream input, OutputStream output)
    throws IOException
  {
    processMatches(new InputStreamReader(input),
		   new OutputStreamWriter(output));
  }

  /**
   * This method reads the provided input one line at a time and for
   * every registered pattern that is contained in the line it executes
   * the associated MatchAction's processMatch() method.  If a field
   * separator has been defined with
   * {@link #setFieldSeparator setFieldSeparator()}, the
   * fields member of the MatchActionInfo instance passed to the
   * processMatch() method is set to a Vector of Strings containing
   * the split fields of the line.  Otherwise the fields member is set
   * to null.  If no match was performed to invoke the action (i.e.,
   * a null pattern was registered), then the match member is set
   * to null.  Otherwise, the match member will contain the result of
   * the match.
   * <p>
   * The input stream, having been exhausted, is closed right before the
   * method terminates and the output stream is flushed.
   * <p>
   * @see MatchActionInfo
   * @param input  The input stream from which to read lines.
   * @param output Where to send output.
   * @exception IOException  If an error occurs while reading input
   *            or writing output.
   */
  public void processMatches(Reader input, Writer output)
    throws IOException
  {
    int patternCount, current;
    LineNumberReader reader = new LineNumberReader(input);
    PrintWriter writer    = new PrintWriter(output);
    MatchActionInfo info  = new MatchActionInfo();
    Object obj;
    Pattern pattern;
    MatchAction action;
    List fields = new ArrayList();

    // Set those things that will not change.
    info.matcher        = __matcher;
    info.fieldSeparator = __fieldSeparator;
    info.input          = reader;
    info.output         = writer;
    info.fields         = null;
    patternCount        = __patterns.size();

    info.lineNumber     = 0;

    while((info.line = reader.readLine()) != null) {
      info.charLine = info.line.toCharArray();
      for(current=0; current < patternCount; current++) {
	obj     = __patterns.elementAt(current);
	// If a pattern is null, it means to do it for every line.
	if(obj != null) {
	  pattern = (Pattern)__patterns.elementAt(current);
	  if(__matcher.contains(info.charLine, pattern)) {
	    info.match = __matcher.getMatch();
	    info.lineNumber = reader.getLineNumber();
	    info.pattern    = pattern;
	    if(__fieldSeparator != null) {
	      fields.clear();
	      Util.split(fields, __matcher, __fieldSeparator, info.line);
	      info.fields = fields;
	    } else
	      info.fields = null;
	    action = (MatchAction)__actions.elementAt(current);
	    action.processMatch(info);
	  }
	} else {
	  info.match      = null;
	  info.lineNumber = reader.getLineNumber();
	  if(__fieldSeparator != null) {
	    fields.clear();
	    Util.split(fields, __matcher, __fieldSeparator, info.line);
	    info.fields = fields;
	  } else
	    info.fields = null;
	  action = (MatchAction)__actions.elementAt(current);
	  action.processMatch(info);
	}
      }
    }

    // Flush output but don't close, close input since we reached end.
    writer.flush();
    reader.close();
  }


}
