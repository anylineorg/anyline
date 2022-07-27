/*
 * $Id: CacheFIFO2.java,v 1.7 2003/11/07 20:16:25 dfs Exp $
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
 * This class is a GenericCache subclass implementing a second
 * chance FIFO (First In First Out) cache replacement policy.  In other
 * words, values are added to the cache until the cache becomes full.
 * Once the cache is full, when a new value is added to the cache, it 
 * replaces the first of the current values in the cache to have been
 * added, unless that value has been used recently (generally
 * between the last cache replacement and now).
 * If the value to be replaced has been used, it is given
 * a second chance, and the next value in the cache is tested for
 * replacement in the same manner.  If all the values are given a
 * second chance, then the original pattern selected for replacement is
 * replaced.
 *
 * @version @version@
 * @since 1.0
 * @see GenericCache
 */
public final class CacheFIFO2 extends GenericCache {
  private int __current = 0;
  private boolean[] __tryAgain;

  /**
   * Creates a CacheFIFO2 instance with a given cache capacity.
   * <p>
   * @param capacity  The capacity of the cache.
   */
  public CacheFIFO2(int capacity) { 
    super(capacity);

    __tryAgain = new boolean[_cache.length];
  }


  /**
   * Same as:
   * <blockquote><pre>
   * CacheFIFO2(GenericCache.DEFAULT_CAPACITY);
   * </pre></blockquote>
   */
  public CacheFIFO2(){
    this(GenericCache.DEFAULT_CAPACITY);
  }


  public synchronized Object getElement(Object key) { 
    Object obj;

    obj = _table.get(key);

    if(obj != null) {
      GenericCacheEntry entry;

      entry = (GenericCacheEntry)obj;

      __tryAgain[entry._index] = true;
      return entry._value;
    }

    return null;
  }


  /**
   * Adds a value to the cache.  If the cache is full, when a new value
   * is added to the cache, it replaces the first of the current values
   * in the cache to have been added (i.e., FIFO2).
   * <p>
   * @param key   The key referencing the value added to the cache.
   * @param value The value to add to the cache.
   */
  public final synchronized void addElement(Object key, Object value) {
    int index;
    Object obj;

    obj = _table.get(key);

    if(obj != null) {
      GenericCacheEntry entry;

      // Just replace the value.  Technically this upsets the FIFO2 ordering,
      // but it's expedient.
      entry = (GenericCacheEntry)obj;
      entry._value = value;
      entry._key   = key;

      // Set the try again value to compensate.
      __tryAgain[entry._index] = true;

      return;
    }

    // If we haven't filled the cache yet, put it at the end.
    if(!isFull()) {
      index = _numEntries;
      ++_numEntries;
    } else {
      // Otherwise, find the next slot that doesn't have a second chance.
      index = __current;
      
      while(__tryAgain[index]) {
	__tryAgain[index] = false;
	if(++index >= __tryAgain.length)
	  index = 0;
      }

      __current = index + 1;
      if(__current >= _cache.length)
	__current = 0;

      _table.remove(_cache[index]._key);
    }

    _cache[index]._value = value;
    _cache[index]._key   = key;
    _table.put(key, _cache[index]);
  }

}

