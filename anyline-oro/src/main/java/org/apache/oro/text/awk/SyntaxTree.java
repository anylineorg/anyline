/*
 * $Id: SyntaxTree.java,v 1.8 2003/11/07 20:16:24 dfs Exp $
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

import java.util.*;

/*
 * IMPORTANT!!!!!!!!!!!!!
 * Don't forget to optimize this module.  The calculation of follow can
 * be accelerated by calculating first and last only once for each node and
 * saving instead of doing dynamic calculation every time.
 */

/**
 * @version @version@
 * @since 1.0
 */
final class SyntaxTree {
  int _positions;
  SyntaxNode _root;
  LeafNode[] _nodes;
  BitSet[] _followSet;
  
  SyntaxTree(SyntaxNode root, int positions) {
    _root      = root;
    _positions = positions;
  }

  void _computeFollowPositions() {
    int index;

    _followSet = new BitSet[_positions];
    _nodes     = new LeafNode[_positions];
    index =    _positions;

    while(0 < index--)
      _followSet[index] = new BitSet(_positions);

    _root._followPosition(_followSet, _nodes);
  }

  private void __addToFastMap(BitSet pos, boolean[] fastMap, boolean[] done){
    int token, node;

    for(node = 0; node < _positions; node++){
      if(pos.get(node) && !done[node]){
	done[node] = true;

	for(token=0; token < LeafNode._NUM_TOKENS; token++){
	  if(!fastMap[token])
	    fastMap[token] = _nodes[node]._matches((char)token);
	}
      }
    }
  }

  boolean[] createFastMap(){
    boolean[] fastMap, done;

    fastMap  = new boolean[LeafNode._NUM_TOKENS]; 
    done     = new boolean[_positions];
    __addToFastMap(_root._firstPosition(), fastMap, done);

    return fastMap;
  }
}
