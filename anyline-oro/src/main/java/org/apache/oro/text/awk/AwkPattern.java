/*
 * $Id: AwkPattern.java,v 1.7 2003/11/07 20:16:24 dfs Exp $
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

import java.io.Serializable;
import java.util.*;

import org.apache.oro.text.regex.*;


final class DFAState {
  int _stateNumber;
  BitSet _state;

  DFAState(BitSet s, int num){
    _state = s;
    _stateNumber = num;
  }
}

/**
 * An implementation of the Pattern interface for Awk regular expressions.
 * This class is compatible with the AwkCompiler and AwkMatcher
 * classes.  When an AwkCompiler instance compiles a regular expression
 * pattern, it produces an AwkPattern instance containing internal
 * data structures used by AwkMatcher to perform pattern matches.
 * This class cannot be subclassed and cannot be directly instantiated
 * by the programmer as it would not make sense.  It is however serializable
 * so that pre-compiled patterns may be saved to disk and re-read at a later
 * time.  AwkPattern instances should only be created through calls to an
 * AwkCompiler instance's compile() methods
 * 
 * @version @version@
 * @since 1.0
 * @see AwkCompiler
 * @see AwkMatcher
 */
public final class AwkPattern implements Pattern, Serializable {
  final static int _INVALID_STATE = -1, _START_STATE = 1; 

  int _numStates, _endPosition, _options;
  String _expression;
  Vector _Dtrans, _nodeList[], _stateList;
  BitSet _U, _emptySet, _followSet[], _endStates;
  Hashtable _stateMap;
  boolean _matchesNullString, _fastMap[];
  boolean _hasBeginAnchor = false, _hasEndAnchor = false;

  AwkPattern(String expression, SyntaxTree tree){
    int token, node, tstateArray[];
    DFAState dfaState;

    _expression = expression;

    // Assume endPosition always occurs at end of parse.
    _endPosition = tree._positions - 1;
    _followSet   = tree._followSet;

    _Dtrans    = new Vector();
    _stateList = new Vector();
    _endStates = new BitSet();

    _U        = new BitSet(tree._positions);
    _U.or(tree._root._firstPosition());

    tstateArray = new int[LeafNode._NUM_TOKENS];
    _Dtrans.addElement(tstateArray); // this is a dummy entry because we
                                     // number our states starting from 1
    _Dtrans.addElement(tstateArray);

    _numStates = _START_STATE;
    if(_U.get(_endPosition))
      _endStates.set(_numStates);
    dfaState = new DFAState((BitSet)_U.clone(), _numStates);
    _stateMap = new Hashtable();
    _stateMap.put(dfaState._state, dfaState);
    _stateList.addElement(dfaState); // this is a dummy entry because we
                                     // number our states starting from 1
    _stateList.addElement(dfaState);
    _numStates++;

    _U.xor(_U);  // clear bits
    _emptySet = new BitSet(tree._positions);

    _nodeList = new Vector[LeafNode._NUM_TOKENS];
    for(token = 0; token < LeafNode._NUM_TOKENS; token++){
      _nodeList[token] = new Vector();
      for(node=0; node < tree._positions; node++)
	if(tree._nodes[node]._matches((char)token))
	  _nodeList[token].addElement(tree._nodes[node]);
    }

    _fastMap = tree.createFastMap();
    _matchesNullString = _endStates.get(_START_STATE);
  }

  // tstateArray is assumed to have been set before calling this method
  void _createNewState(int current, int token, int[] tstateArray) {
    int node, pos;
    DFAState T, dfaState;

    T    = (DFAState)_stateList.elementAt(current);
    node = _nodeList[token].size();
    _U.xor(_U);  // clear bits
    while(node-- > 0){
      pos = ((LeafNode)_nodeList[token].elementAt(node))._position;
      if(T._state.get(pos))
	_U.or(_followSet[pos]);
    }

    if(!_stateMap.containsKey(_U)){
      dfaState = new DFAState((BitSet)_U.clone(), _numStates++);
      _stateList.addElement(dfaState);
      _stateMap.put(dfaState._state, dfaState);
      _Dtrans.addElement(new int[LeafNode._NUM_TOKENS]);

      if(!_U.equals(_emptySet)){
	tstateArray[token] = _numStates - 1;

	if(_U.get(_endPosition))
	  _endStates.set(_numStates - 1);
      } else
	tstateArray[token] = _INVALID_STATE;
    } else {
      if(_U.equals(_emptySet))
	tstateArray[token] = _INVALID_STATE;
      else 
	tstateArray[token] = ((DFAState)_stateMap.get(_U))._stateNumber;
    }
  }

  int[] _getStateArray(int state) { return ((int[])_Dtrans.elementAt(state)); }


  /**
   * This method returns the string representation of the pattern.
   * <p>
   * @return The original string representation of the regular expression
   *         pattern.
   */
  public String getPattern() { return _expression; }


  /**
   * This method returns an integer containing the compilation options used
   * to compile this pattern.
   * <p>
   * @return The compilation options used to compile the pattern.
   */
  public int getOptions()    { return _options; }
}

