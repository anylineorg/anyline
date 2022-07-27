/*
 * $Id: CacheLRU.java,v 1.10 2003/11/07 20:16:25 dfs Exp $
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


package org.apache.oro.util;

import java.util.*;

/**
 * This class is a GenericCache subclass implementing an LRU
 * (Least Recently Used) cache replacement policy.  In other words,
 * values are added to the cache until it becomes full.  Once the
 * cache is full, when a new value is added to the cache, it replaces
 * the least recently used value currently in the cache.  This is probably
 * the best general purpose cache replacement policy.
 * 
 * @version @version@
 * @since 1.0
 * @see GenericCache
 */
public final class CacheLRU extends GenericCache {
  private int __head = 0, __tail = 0;
  private int[] __next, __prev;

  /**
   * Creates a CacheLRU instance with a given cache capacity.
   * <p>
   * @param capacity  The capacity of the cache.
   */
  public CacheLRU(int capacity) { 
    super(capacity);

    int i;

    __next = new int[_cache.length];
    __prev = new int[_cache.length];

    for(i=0; i < __next.length; i++)
      __next[i] = __prev[i] = -1;
  }


  /**
   * Same as:
   * <blockquote><pre>
   * CacheLRU(GenericCache.DEFAULT_CAPACITY);
   * </pre></blockquote>
   */
  public CacheLRU(){
    this(GenericCache.DEFAULT_CAPACITY);
  }


  private void __moveToFront(int index) {
    int next, prev;

    if(__head != index) {
      next = __next[index];
      prev = __prev[index];

      // Only the head has a prev entry that is an invalid index so
      // we don't check.
      __next[prev] = next;

      // Make sure index is valid.  If it isn't, we're at the tail
      // and don't set __prev[next].
      if(next >= 0)
	__prev[next] = prev;
      else
	__tail = prev;

      __prev[index] = -1;
      __next[index] = __head;
      __prev[__head] = index;
      __head        = index;
    }
  }


  public synchronized Object getElement(Object key) { 
    Object obj;

    obj = _table.get(key);

    if(obj != null) {
      GenericCacheEntry entry;

      entry = (GenericCacheEntry)obj;
      // Maintain LRU property
      __moveToFront(entry._index);

      return entry._value;
    }

    return null;
  }


  /**
   * Adds a value to the cache.  If the cache is full, when a new value
   * is added to the cache, it replaces the least recently used value
   * in the cache (i.e., LRU).
   * <p>
   * @param key   The key referencing the value added to the cache.
   * @param value The value to add to the cache.
   */
  public final synchronized void addElement(Object key, Object value) {
    Object obj;

    obj = _table.get(key);

    if(obj != null) {
      GenericCacheEntry entry;

      // Just replace the value, but move it to the front.
      entry = (GenericCacheEntry)obj;
      entry._value = value;
      entry._key   = key;

      __moveToFront(entry._index);

      return;
    }

    // If we haven't filled the cache yet, place in next available spot
    // and move to front.
    if(!isFull()) {
      if(_numEntries > 0) {
	__prev[_numEntries] = __tail;
	__next[_numEntries] = -1;
	__moveToFront(_numEntries);
      }
      ++_numEntries;
    } else {
      // We replace the tail of the list.
      _table.remove(_cache[__tail]._key);
      __moveToFront(__tail);
    }

    _cache[__head]._value = value;
    _cache[__head]._key   = key;
    _table.put(key, _cache[__head]);
  }
}
