/*
 * $Id: GenericCache.java,v 1.8 2003/11/07 20:16:25 dfs Exp $
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
 * This is the base class for all cache implementations provided in the
 * org.apache.oro.util package.  To derive a subclass from GenericCache
 * only the ... methods
 * need be overridden.
 * Although 4 subclasses of GenericCache are provided with this
 * package, users may not derive subclasses from this class.
 * Rather, users should create their own implmentations of the
 * {@link Cache} interface.
 *
 * @version @version@
 * @since 1.0
 * @see Cache
 * @see CacheLRU
 * @see CacheFIFO
 * @see CacheFIFO2
 * @see CacheRandom
 */
public abstract class GenericCache implements Cache, java.io.Serializable {
  /**
   * The default capacity to be used by the GenericCache subclasses
   * provided with this package.  Its value is 20.
   */
  public static final int DEFAULT_CAPACITY = 20;

  int _numEntries;
  GenericCacheEntry[] _cache;
  HashMap _table;

  /**
   * The primary constructor for GenericCache.  It has default
   * access so it will only be used within the package.  It initializes
   * _table to a Hashtable of capacity equal to the capacity argument,
   * _cache to an array of size equal to the capacity argument, and
   * _numEntries to 0.
   * <p>
   * @param capacity The maximum capacity of the cache.
   */
  GenericCache(int capacity) {
    _numEntries = 0;
    _table    = new HashMap(capacity);
    _cache    = new GenericCacheEntry[capacity];

    while(--capacity >= 0)
      _cache[capacity] = new GenericCacheEntry(capacity);
  }

  public abstract void addElement(Object key, Object value);

  public synchronized Object getElement(Object key) { 
    Object obj;

    obj = _table.get(key);

    if(obj != null)
      return ((GenericCacheEntry)obj)._value;

    return null;
  }

  public final Iterator keys() {
    return _table.keySet().iterator();
  }

  /**
   * Returns the number of elements in the cache, not to be confused with
   * the {@link #capacity()} which returns the number
   * of elements that can be held in the cache at one time.
   * <p>
   * @return  The current size of the cache (i.e., the number of elements
   *          currently cached).
   */
  public final int size() { return _numEntries; }

  /**
   * Returns the maximum number of elements that can be cached at one time.
   * <p>
   * @return The maximum number of elements that can be cached at one time.
   */
  public final int capacity() { return _cache.length; }

  public final boolean isFull() { return (_numEntries >= _cache.length); }
}
