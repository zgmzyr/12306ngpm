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


import java.util.*;

/**
 * Provides convenient static methods for creating a {@code Query}
 * @author Bin Zhang
 *
 */
public final class Queries {



	private Queries()
	{

	}
	
	/**
	 * Recursive searches specified object's ancestor by specified parent selector
	 * @param child the object to search
	 * @param selector selector for getting specified child's parent
	 * @return An {@code Query<T>} contains specified object and it's ancestors
	 */
	public static <T> Query<T> flatternAncestors(T child, Selector<T, T> selector)
	{
		return new Query<T>(new AncestorIterable<T>(child, selector ));
	}
	
	
	private static class AncestorIterable<T> implements Iterable<T>
	{
		public AncestorIterable(T child, Selector<T, T> selector)
		{
			this._child = child;
			this._selector = selector;
		}

		@Override
		public Iterator<T> iterator() {
			return new AncestorIterator<T>(this._child, this._selector);
		}
		
		private T _child;
		private Selector<T, T> _selector;
		
	}
	
	
	private static class AncestorIterator<T> implements Iterator<T>
	{
		public AncestorIterator(T child, Selector<T, T> selector)
		{
			this._next = child;
			this._selector = selector;
		}

		private T _next;
	
		private Selector<T, T> _selector;
		@Override
		public boolean hasNext() {
			return _next != null;
		}
		@Override
		public T next() {
			T rs = _next;
			_next = _selector.select(_next);
			return rs;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}
		
	}

	
	/**
	 * Recursive searches specified object's children by specified child selector
	 * @param ancestor the object to search
	 * @param selector selector for getting specified object's children
	 * @return An {@code Query<T>} contains specified object and all children
	 */
	
	public static <T>  Query<T> flatternChildren(T ancestor, Selector<T, Iterable<T>> selector)
	{
		Iterable<T> rs = new FlatternerIterable<T>(ancestor, selector);

		return new Query<T>(rs);
	}


	private static class FlatternerIterable<T> implements Iterable<T>
	{
		public FlatternerIterable(final T ancestor, final Selector<T, Iterable<T>> selector)
		{
			this._ancestor = ancestor;
			this._selector = selector;
		}

		private T _ancestor;
		private Selector<T, Iterable<T>> _selector;

		@Override
		public Iterator<T> iterator() {
			return new FlatternerIterator<T>(_ancestor, _selector);
		}
	}
	
	private static class FlatternerIterator<T> implements Iterator<T>
	{



		public FlatternerIterator(final T ancestor, final Selector<T, Iterable<T>> selector)
		{
			this._ancestor = ancestor;
			this._selector = selector;
		}

		private T _ancestor;
		private Selector<T, Iterable<T>> _selector;

		private Iterator<T> _children = null;;

		private FlatternerIterator<T> _childIterator = null;

		//0: init; //1: visit children 2://end;
		private int _state = 0;

		@Override
		public boolean hasNext() {
			return _state < 2;
		}


		private void loadChidren()
		{
			Iterable<T> iterable = _selector.select(_ancestor);
			if(iterable != null)
			{
				_children = iterable.iterator();

				if(_children.hasNext())
				{
					_childIterator = new FlatternerIterator<T>(_children.next(), this._selector);
					_state = 1;
				}
				else
				{
					_state = 2;
				}

			}
			else
			{
				_state = 2;
			}
		}

		@Override
		public T next() {
			if(_state == 0)
			{
				loadChidren();
				return this._ancestor;
			}
			else if(_state == 1)
			{
				T rs = _childIterator.next();

				if(! _childIterator.hasNext())
				{
					if(_children.hasNext())
					{
						_childIterator = new FlatternerIterator<T>(_children.next(), this._selector);
					}
					else
					{
						_state = 2;
					}
				}
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
	
	public static <T> Query<T> query(Iterable<T> source)
	{
		return new Query<T>(source);
	}
	
	public static <T> Query<T> query(T[] source)
	{
		return new Query<T>(source);
	}
	
	public static Query<Boolean> query(boolean[] source)
	{
		ArrayList<Boolean> rs = new ArrayList<Boolean>(source.length);
		for(Boolean b : source)
		{
			rs.add(b);
		}
		
		return new Query<Boolean>(rs);
	}
	
	public static Query<Short> query(short[] source)
	{
		ArrayList<Short> rs = new ArrayList<Short>(source.length);
		for(Short b : source)
		{
			rs.add(b);
		}
		
		return new Query<Short>(rs);
	}
	
	public static Query<Integer> query(int[] source)
	{
		ArrayList<Integer> rs = new ArrayList<Integer>(source.length);
		for(Integer b : source)
		{
			rs.add(b);
		}
		
		return new Query<Integer>(rs);
	}
	
	public static Query<Long> query(long[] source)
	{
		ArrayList<Long> rs = new ArrayList<Long>(source.length);
		for(Long b : source)
		{
			rs.add(b);
		}
		
		return new Query<Long>(rs);
	}
	
	public static Query<Float> query(float[] source)
	{
		ArrayList<Float> rs = new ArrayList<Float>(source.length);
		for(Float b : source)
		{
			rs.add(b);
		}
		
		return new Query<Float>(rs);
	}
	
	public static Query<Double> query(double[] source)
	{
		ArrayList<Double> rs = new ArrayList<Double>(source.length);
		for(Double b : source)
		{
			rs.add(b);
		}
		
		return new Query<Double>(rs);
	}
	
	public static Query<Character> query(char[] source)
	{
		ArrayList<Character> rs = new ArrayList<Character>(source.length);
		for(Character b : source)
		{
			rs.add(b);
		}
		
		return new Query<Character>(rs);
	}

}

 

