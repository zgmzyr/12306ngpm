/*
 * Copyright (c) 2012, Bin Zhang
 All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
Neither the name of the Bin Zhang nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */


package org.diting.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;



class ArrayIterable<T> implements Iterable<T>, ICountable
{
	public ArrayIterable(T[] source)
	{
		this._source = source;
	}

	@Override
	public Iterator<T> iterator() {

		return new ArrayIterator(this._source);
	}

	private T[] _source;

	public T[] getSource()
	{
		return _source;
	}

	@Override
	public int count() {

		return _source.length;
	}
	
	private class ArrayIterator implements Iterator<T>
	{
		public ArrayIterator(T[] source)
		{
			this._source = source;
		}

		private int _index = 0;

		private T[] _source;

		@Override
		public boolean hasNext() {
			return _index < this._source.length;
		}

		@Override
		public T next() {
			if(_index < this._source.length)
			{
				T rs = this._source[this._index];
				this._index ++;
				return rs;
			}
			else
			{
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}

	}

}