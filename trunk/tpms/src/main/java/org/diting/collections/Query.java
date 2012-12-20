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
 * Provides capabilities of query on {@code java.util.Iterable<T>} or an array with chainable methods
 * @author Bin Zhang
 *
 * @param <T> The element type of collection
 */
public  class Query<T> implements Iterable<T> {


	private Iterable<T> _source;

	/**
	 * Create a new Query by specified <Iterable<T>>
	 * @param source The source collection
	 */
	Query(Iterable<T> source)
	{
		if(source == null){
			throw new IllegalArgumentException("source");
		}
		this._source = source;
	}


	/**
	 * Create a new Query by specified array
	 * @param source The source array
	 */
	Query(T[] source)
	{
		if(source == null){
			throw new IllegalArgumentException("source");
		}

		this._source = new ArrayIterable<T>(source);
	}

	
	

	private <K,V> Map<K,V> createMap(Comparator<K> comparator)
	{
		
		
		
		Map<K,V> rs;
		if(comparator != null)
		{
			rs = new TreeMap<K,V>(comparator);
		}
		
		else
		{
			rs = new HashMap<K,V>();
		}
		
		
		//Map<K,V> rs = comparator != null ? new TreeMap<K,V>(comparator) : new HashMap<K,V>();
		return rs;
	}

	private interface IRandomAccessor2<T> 
	{
		int getSize();

		T get(int index);

	}

	private class ListRandomAccessor implements IRandomAccessor2<T>
	{
		private List<T> _source;
		public ListRandomAccessor(List<T> source)
		{
			this._source = source;
		}

		@Override
		public int getSize() {
			return this._source.size();
		}

		@Override
		public T get(int index) {
			return this._source.get(index);
		}

	}

	private class ArrayRandomAccessor implements IRandomAccessor2<T>
	{
		private T[] _source;
		public ArrayRandomAccessor(T[] source)
		{
			this._source = source;
		}

		@Override
		public int getSize() {
			return this._source.length;
		}

		@Override
		public T get(int index) {
			return this._source[index];
		}

	}


	private boolean isRandomAccessable(Object obj)
	{
		return obj instanceof List<?> || obj.getClass().isArray() || obj instanceof ArrayIterable<?>;
	}

	@SuppressWarnings("unchecked")
	private IRandomAccessor2<T> createRandomAccessor(Object source)
	{
		if(source instanceof List<?>)
		{
			return new ListRandomAccessor((List<T>)source);
		}
		else if (source.getClass().isArray())
		{
			return new ArrayRandomAccessor((T[])source);
		}
		else if(source instanceof ArrayIterable<?>)
		{
			return new ArrayRandomAccessor(((ArrayIterable<T>)source).getSource());
		}

		throw new UnsupportedOperationException();
	}





	/**
	 * Projects each element of a sequence into a new form
	 * @param selector A transform {@code Selector<T, TResult>} to apply to each element.
	 * @return A {@code Query<T>} whose elements are the result of invoking the transform function on each element of source.
	 */
	public <TResult> Query<TResult> select(Selector<T, TResult> selector)
	{
		Iterable<TResult> rs = new SelectIterable<TResult>(this._source, selector);
		return new Query<TResult>(rs);

	}


	private class SelectIterable<TResult> implements Iterable<TResult>
	{
		public SelectIterable(Iterable<T> source, Selector<T, TResult> selector)
		{
			this._source = source;
			this._selector = selector;
		}

		private Iterable<T> _source;
		private Selector<T, TResult> _selector;
		@Override
		public Iterator<TResult> iterator() {
			return new SelectIterator<TResult>(this._source, this._selector);
		}
	}

	private class SelectIterator<TResult> implements Iterator<TResult>
	{
		public SelectIterator(Iterable<T> source, Selector<T, TResult> selector)
		{
			this._source = source.iterator();
			this._selector = selector;
		}

		private Iterator<T> _source;
		private Selector<T, TResult> _selector;
		@Override
		public boolean hasNext() {
			return _source.hasNext();
		}
		@Override
		public TResult next() {
			T item = this._source.next();
			TResult rs = this._selector.select(item);
			return rs;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}

	}



	/**
	 * Returns distinct elements from a sequence by using hash code to compare values.
	 * @return A {@code Query<T>} that contains distinct elements from the source sequence.
	 */
	public Query<T> distinct()
	{
		HashSet<T> rs = new HashSet<T>();
		for(T item : this._source)
		{
			rs.add(item);
		}
		return new Query<T>(rs);
	}

	/**
	 * Returns distinct elements from a sequence by using a specified {@code Comparator<T>} to compare values. 
	 * @param comparator A {@code Comparator<T>} to compare values 
	 * @return A {@code Query<T>}that contains distinct elements from the sequence. 
	 */
	public Query<T> distinct(Comparator<T> comparator)
	{
		TreeSet<T> rs = new TreeSet<T>(comparator);
		for(T item : this._source)
		{
			rs.add(item);
		}
		return new Query<T>(rs);

	}


	/**
	 * Filters a sequence of values based on a predicate.
	 * @param predicate A {@code Predicate<T>} to test each element for a condition.
	 * @return An {@code Query<T>} that contains elements from the input sequence that satisfy the condition. 

	 * @throws Exception
	 */

	public Query<T> where(Predicate<T> predicate) throws Exception
	{

		WhereIterable rs = new WhereIterable(this._source, predicate);

		return new Query<T>(rs);
	}


	private class WhereIterable implements Iterable<T>
	{

		public WhereIterable(Iterable<T> source, Predicate<T> predicate)
		{
			this._predicate = predicate;
			this._source = source;
		}

		private Iterable<T> _source;
		private Predicate<T> _predicate;


		@Override
		public Iterator<T> iterator() {
			return new WhereIterator(this._source, this._predicate);
		}

	}

	private class WhereIterator implements Iterator<T>
	{

		public WhereIterator(Iterable<T> source, Predicate<T> predicate)
		{
			this._predicate = predicate;
			this._source = source.iterator();
		}

		private Iterator<T> _source;
		private Predicate<T> _predicate;
		private T _current;


		//0: need To Find Next, 1: return current, 2 : end 
		private int _state = 0;


		@Override
		public boolean hasNext() {
			if(_state == 0)
			{
				this.findNext();
			}

			return _state != 2;
		}


		private void findNext() throws UnsupportedOperationException {
			while(this._state == 0)
			{
				if(this._source.hasNext())
				{
					T item = this._source.next();
					try {
						if(this._predicate.evaluate(item))
						{
							this._current = item;
							this._state = 1;
						}
					} catch (Exception e) {
						throw new UnsupportedOperationException(e);
					}

				}
				else	
				{
					this._state = 2;
				}
			}

		}


		@Override
		public T next() {
			if(_state == 0)
			{
				this.findNext();
			}
			if(_state == 2)
			{
				throw new NoSuchElementException();
			}
			T rs = this._current;
			this._state = 0;
			return rs;
		}


		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}
	}


	/**
	 * Returns the first element of a sequence, or a default value if the sequence contains no elements.
	 * @return The first element of this sequence or null if the collection is empty.
	 * @throws Exception
	 */
	public T firstOrDefault() throws Exception
	{
		return this.firstOrDefault(new TruePredicate<T>());
	}


	/**
	 * Returns the first element of the sequence that satisfies a condition or <code>null</code> value if no such element is found.
	 * @param predicate A {@code Predicate<T>} to test each element for a condition.
	 * @return null if source is empty or if no element passes the test specified by predicate; otherwise, the first element in source that passes the test specified by predicate. 

	 * @throws Exception
	 */
	public  T firstOrDefault(Predicate<T> predicate) throws Exception
	{
		for(T item : this._source)
		{
			if(predicate.evaluate(item))
			{
				return item;
			}

		}
		return null;
	}


	static class TruePredicate<T2> implements  Predicate<T2>
	{

		@Override
		public boolean evaluate(T2 obj) throws Exception {
			return true;
		}



	}





	/**
	 * Returns the last element of the sequence.	
	 * @return The value at the last position in the sequence.
	 * @throws Exception
	 */
	public T last() throws Exception {
		return this.last(new TruePredicate<T>());
	}

	/**
	 * Returns the last element of a sequence that satisfies a specified condition.
	 * @param predicate A {@code Predicate<T>} to test each element for a condition.
	 * @return The last element in the sequence that passes the test in the specified {@code Predicate<T>}.
	 * @throws Exception
	 */
	public T last(Predicate<T> predicate) throws Exception
	{
		T rs = lastOrDefault(predicate);
		if(rs == null)
		{
			throw new IllegalStateException("No element in collection");
		}
		return rs;
	}

	/**
	 * Returns the last element of the sequence, or a <code>null</code> if the sequence contains no elements.
	 * @return <code>null</code> if the sequence is empty; otherwise, the last element in the {@code Query<T>}. 
	 * @throws Exception
	 */
	public T lastOrDefault() throws Exception
	{
		return this.lastOrDefault(new TruePredicate<T>());
	}

	/**
	 * Returns the last element of the sequence that satisfies a condition or <code>null</code> if no such element is found.
	 * @param predicate A {@code Predicate<T>} to test each element for a condition.
	 * @return <code>null</code> if the sequence is empty or if no elements pass the test in the {@code Predicate<T>}; otherwise, the last element that passes the test in the {@code Predicate<T>}. 
	 * @throws Exception
	 */

	public T lastOrDefault(Predicate<T> predicate) throws Exception {
		for(T item : this.reverse())
		{
			if(predicate.evaluate(item))
			{
				return item;
			}
		}

		return null;
	}


	/**
	 * Returns the first element of the sequence.
	 * @return The first element in the sequence.
	 * @throws Exception
	 */
	public T first() throws Exception
	{
		return this.first(new TruePredicate<T>());
	}

	/**
	 * Returns the first element in the sequence that satisfies a specified condition.
	 * @param predicate A {@code Predicate<T>} to test each element for a condition.
	 * @return The first element in the sequence that passes the test in the specified {@code Predicate<T>}.
	 * @throws Exception
	 */
	public T first(Predicate<T> predicate) throws Exception
	{
		T rs = firstOrDefault(predicate);
		if(rs == null)
		{
			throw new IllegalStateException("No element in collection");
		}
		return rs;

	}

	/**
	 * Filters the elements of an Enumerable based on a specified type. 
	 * @param clazz The type to filter the elements of the sequence on. 
	 * @return A {@code Query<T>}that contains elements from the sequence of type TResult. 
	 * @throws Exception
	 */
	public <T2> Query<T2> ofType(final Class<T2> clazz) throws Exception
	{

		return this.where(new Predicate<T>(){

			@Override
			public boolean evaluate(T obj) throws Exception {

				return clazz.isInstance(obj);
			}}).cast(clazz);

	}


	/**
	 * Converts the elements of an IEnumerable to the specified type. 
	 * @param t2 The type to convert the elements of source to
	 * @return A {@code Query<T>} that contains each element of the source sequence converted to the specified type. 
	 */

	
	public <T2> Query<T2> cast(final Class<T2> t2)
	{


		return this.select(new Selector<T, T2>(){

			@Override
			public T2 select(T item) {
				return t2.cast(item);
			}});

	}

	/**
	 * Returns the only element of the sequence, and throws an exception if there is not exactly one element in the sequence.
	 * @return The single element of the input sequence.
	 * @throws Exception
	 */
	public T single() throws Exception
	{
		return this.single(new TruePredicate<T>());
	}

	/**
	 * Returns the only element of the sequence that satisfies a specified condition, and throws an exception if more than one such element exists.
	 * @param predicate  a {@code Predicate<T>} to test an element for a condition.

	 * @return The single element of the  sequence that satisfies a condition.
	 * @throws Exception
	 */
	public T single(Predicate<T> predicate) throws Exception
	{
		T rs = singleOrDefault(predicate);

		if(rs == null)
		{
			throw new IllegalStateException("No elements match the predicate in collection");
		}

		return rs;
	}


	/**
	 * Returns the only element of a sequence, or <code>null</code> if the sequence is empty; this method throws an exception if there is more than one element in the sequence.
	 * @return The single element of the sequence, or <code>null</code> if the sequence contains no elements. 

	 * @throws Exception
	 */
	public T singleOrDefault() throws Exception
	{
		return this.singleOrDefault(new TruePredicate<T>());
	}

	/**
	 * Returns the only element of the sequence that satisfies a specified condition or <code>null</code> if no such element exists; this method throws an exception if more than one element satisfies the condition.
	 * @param predicate A {@code Predicate<T>} to test an element for a condition.
	 * @return The single element of the input sequence that satisfies the condition, or <code>null</code> if no such element is found. 
	 * @throws Exception
	 */
	public  T singleOrDefault(Predicate<T> predicate) throws Exception
	{
		T rs = null;
		for(T item : this._source)
		{
			if(predicate.evaluate(item))
			{
				if(rs == null)
				{
					rs = item;
				}
				else	
				{ 
					throw new IllegalStateException("More than one elements match the predicate in collection");
				}
			}
		}



		return rs;
	}


	/**
	 * Creates an {@code ArrayList <T>} from A {@code Query<T>}. 
	 * @return An {@code ArrayList <T>} that contains elements from the sequence. 
	 */
	public ArrayList<T> toArrayList()
	{
		ArrayList<T> rs = new ArrayList<T>();
		for(T item : this._source)
		{

			rs.add(item);
		}
		return rs;
	}

	/**
	 * Creates an array from A {@code Query<T>}
	 * @param a the array into which the elements of the list are to be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated for this purpose.
	 * @return An array that contains elements from the sequence. 
	 */
	public <T2> T2[] toArray(T2[] a)
	{
		return this.toArrayList().toArray(a);
	}




	/**
	 * Sorts the elements of the sequence in descending order according to a key.
	 * @param keySelector A {@code Selector<T, TResult>} to extract a key from an element.
	 * @return A {@code Query<T>} whose elements are sorted in descending order according to a key. 

	 */
	public <TKey> Query<T> orderByDescending(Selector<T, TKey> keySelector)
	{
		return this.orderByDescending(keySelector, null);
	}

	/**
	 * Sorts the elements of the sequence in descending order by using a specified {@code Comparator<T>}
	 * @param keySelector A {@code Selector<T, TResult>} to extract a key from an element.
	 * @param comparator A {@code Comparator<T>}to compare keys. 
	 * @return A {@code Query<T>} whose elements are sorted in descending order according to a key. 
	 */
	public <TKey> Query<T> orderByDescending(Selector<T, TKey> keySelector, Comparator<TKey> comparator)
	{
		KeyComparator<TKey> kc = new KeyComparator<TKey>();
		kc.keySelector = keySelector;
		kc.innerComparator = comparator != null ? comparator : new NaturalComparator<TKey>(); 

		OrderByIterable rs = new OrderByIterable(this._source, new ReverseComparator<T>(kc));
		return new Query<T>(rs);
	}


	/**
	 * Sorts the elements of the sequence in ascending order according to a key.
	 * @param keySelector A {@code Selector<T, TResult>} to extract a key from an element.
	 * @return A {@code Query<T>}whose elements are sorted according to a key. 

	 */

	public <TKey> Query<T> orderBy(Selector<T, TKey> keySelector)
	{
		return this.orderBy(keySelector, null);
	}

	/**
	 * Sorts the elements of a sequence in ascending order by using a specified {@code Comparator<T>}.
	 * @param keySelector A {@code Selector<T, TResult>} to extract a key from an element.
	 * @param comparator A {@code Comparator<T>}to compare keys.  
	 * @return A {@code Query<T>} whose elements are sorted according to a key. 
	 */
	public <TKey> Query<T> orderBy(Selector<T, TKey> keySelector, Comparator<TKey> comparator)
	{

		KeyComparator<TKey> kc = new KeyComparator<TKey>();
		kc.keySelector = keySelector;
		kc.innerComparator = comparator != null ? comparator : new NaturalComparator<TKey>(); 

		OrderByIterable rs = new OrderByIterable(this._source, kc);

		return new Query<T>(rs);
	}


	private class OrderByIterable implements Iterable<T>
	{

		public OrderByIterable(Iterable<T> source, Comparator<T> comparator)
		{
			this._source = source;
			this._comparer = comparator;
		}

		private Iterable<T> _source;
		private Comparator<T> _comparer;

		@Override
		public Iterator<T> iterator() {
			return new OrderByIterator(this._source, this._comparer);
		}

	}

	private class OrderByIterator implements Iterator<T>
	{

		public OrderByIterator(Iterable<T> source, Comparator<T> comparator)
		{
			_list = new ArrayList<T>();
			for(T item : source)
			{
				_list.add(item);
			}
			this._comparer = comparator;
		}


		private ArrayList<T> _list;

		private int _index = 0;
		private Comparator<T> _comparer;
		@Override
		public boolean hasNext() {

			return _index < _list.size();
		}
		@Override
		public T next() {
			if(_index >= _list.size())
			{
				throw new NoSuchElementException();
			}

			T rs;
			for(int i = this._list.size() - 1; i > this._index ; i --)
			{
				T x = this._list.get(i);
				T y = this._list.get(i - 1);
				if(this._comparer.compare(x,  y) < 0)
				{
					this._list.set(i, y);
					this._list.set(i - 1, x);
				}
			}

			rs = this._list.get(this._index);
			this._index ++;
			return rs;

		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}

	}


	private class KeyComparator<TKey> implements Comparator<T>
	{
		public Selector<T, TKey> keySelector;
		public Comparator<TKey> innerComparator;

		@Override
		public int compare(T o1, T o2) {
			TKey k1 = keySelector.select(o1);
			TKey k2 = keySelector.select(o2);

			return innerComparator.compare(k1, k2);
		}

	}


	/**
	 * Projects each element of the sequence to A {@code Query<T>}and flattens the resulting sequences into one sequence. 
	 * @param selector A transform {@code Selector<T, TResult>} to apply to each element.
	 * @return A {@code Query<T>} whose elements are the result of invoking the one-to-many transform function on each element of the input sequence. 

	 */

	public <T2> Query<T2> selectMany(Selector<T, Iterable<T2>> selector)
	{
		Iterable<T2> rs = new FromIterable<T2>(this._source, selector);

		return new Query<T2>(rs);
	}

	private class FromIterable<T2> implements Iterable<T2>
	{
		public FromIterable(Iterable<T> source, Selector<T, Iterable<T2>> selector)
		{
			this._source = source;
			this._selector = selector;
		}

		private Iterable<T> _source;
		private Selector<T, Iterable<T2>> _selector;

		@Override
		public Iterator<T2> iterator() {
			return new FromIterator<T2>(this._source, this._selector);
		}
	}


	private class FromIterator<T2> implements Iterator<T2>
	{
		public FromIterator(Iterable<T> source, Selector<T, Iterable<T2>> selector)
		{
			this._source = source.iterator();
			this._selector = selector;
		}

		private Iterator<T> _source;
		private Iterator<T2> _currentIterator;
		private Selector<T, Iterable<T2>> _selector;
		//0: need to call find next; 1: has item; 2 : end
		private boolean _hasNext = true;
		private T2 _current;

		@Override
		public boolean hasNext() {
			
			if(this._hasNext)
			{
				this.tryFindNext();
			}
			
			
			return this._hasNext;
		}
		private void tryFindNext() {
			
			
			while(this._hasNext && this._current == null)
			{
				
				
				if(this._currentIterator == null)
				{
					if(this._source.hasNext())
					{
						T s = this._source.next();
						this._currentIterator = this._selector.select(s).iterator();
					}
					else
					{
						this._hasNext = false;
					}
				}
				else if(!this._currentIterator.hasNext())
				{
					this._currentIterator = null;
				}
				else
				{
					this._current = this._currentIterator.next();
				}
			}
			
			

		}
		@Override
		public T2 next() {
			if(this.hasNext())
			{
				T2 rs = this._current;
				this._current = null;
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



	@Override
	public Iterator<T> iterator() {
		return this._source.iterator();
	}


	/**
	 * Groups the elements of a sequence according to a specified key {@code Selector<T, TResult>}.
	 * @param keySelector A {@code Selector<T, TResult>} to extract the key for each element.
	 * @return A {@code Query<T>} in where each {@code IGrouping } object contains a sequence of objects and a key. 

	 */
	public <TKey> Query<IGrouping<TKey, T>> groupBy(Selector<T, TKey> keySelector)
	{
		return groupBy(keySelector, null);
	}

	
	
	/**
	 * Groups the elements of a sequence according to a specified key selector function and compares the keys by using a specified comparer.
	 * @param keySelector A {@code Selector<T, TResult>} to extract the key for each element.
	 * @param comparator An {@code Comparator<T>} to compare keys. 
	 * @return A {@code Query<T>} in where each {@code IGrouping} object contains a sequence of objects and a key. 
	 */
	public <TKey> Query<IGrouping<TKey, T>> groupBy(Selector<T, TKey> keySelector, Comparator<TKey> comparator)
	{
		
		Map<TKey, ListGroup<TKey,T>> rs =  this.createMap(comparator);

		for(T element : this._source)
		{
			TKey key = keySelector.select(element);
			ListGroup<TKey,T> group = rs.get(key);
			if(group == null)
			{
				group = new ListGroup<TKey, T>(key);
				rs.put(key, group);
			}
			group.add(element);


		}

		return new Query<ListGroup<TKey, T>>(rs.values()).select(new Selector<ListGroup<TKey, T>, IGrouping<TKey, T>>(){

			@Override
			public IGrouping<TKey, T> select(ListGroup<TKey, T> item) {
				return item;
			}});
	}

	/**
	 * Returns the number of elements in the sequence.
	 * @return The number of elements in the sequence.
	 */
	public int count()
	{
		if(this._source instanceof Collection<?>)
		{
			return ((Collection<?>)this._source).size();
		}
		else if(this._source instanceof ICountable)
		{
			return ((ICountable)this._source).count();
		}
		else
		{
			int rs = 0;
			Iterator<T> iterator = this.iterator();
			while(iterator.hasNext())
			{
				iterator.next();
				rs ++;
			}

			return rs;
		}
	}

	/**
	 * Returns a number that represents how many elements in the sequence satisfy a condition.
	 * @param predicate A {@code Predicate<T>} to test each element for a condition.
	 * @return A number that represents how many elements in the sequence satisfy the condition in the predicate.
	 * @throws Exception
	 */
	public int count(Predicate<T> predicate) throws Exception
	{
		int rs = 0;
		Iterator<T> iterator = this.iterator();
		while(iterator.hasNext())
		{


			T item = iterator.next();
			if(predicate.evaluate(item))
			{
				rs ++;
			}
		}

		return rs;
	}

	/**
	 * Inverts the order of the elements in a sequence.
	 * @return A sequence whose elements correspond to those of the sequence in reverse order.
	 */
	public Query<T> reverse()
	{
		if(this._source instanceof ArrayIterable<?>)
		{
			ArrayReverseIterable rs = new ArrayReverseIterable(((ArrayIterable<T>)this._source).getSource());
			return new Query<T>(rs);
		}
		else if ((this._source instanceof List<?>) && (this._source instanceof RandomAccess))
		{
			return new Query<T>( new ReverseIterable((List<T>)this._source));
		}
		else
		{
			ArrayList<T> rs = new ArrayList<T>();
			for(T element : this._source)
			{
				rs.add(element);
			}
			return new Query<T>(rs);
		}
	}


	private class ArrayReverseIterable implements Iterable<T>
	{
		public ArrayReverseIterable(T[] source)
		{
			this._source = source;
		}

		private T[] _source;

		@Override
		public Iterator<T> iterator() {
			return new ArrayReverseIterator(this._source);
		}
	}


	private class ArrayReverseIterator implements Iterator<T>
	{
		public ArrayReverseIterator(T[] source)
		{
			this._source = source;
			this._position = this._source.length - 1;
		}

		private T[] _source;

		@Override
		public boolean hasNext() {
			return _position >= 0;
		}

		private int _position;

		@Override
		public T next() {
			T rs = this._source[this._position];
			this._position --;
			return rs;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}
	}

	private class ReverseIterable implements Iterable<T>
	{
		public ReverseIterable(List<T> source)
		{
			this._source = source;
		}

		private List<T> _source;

		@Override
		public Iterator<T> iterator() {
			return new ReverseIterator(this._source);
		}
	}

	private class ReverseIterator implements Iterator<T>
	{
		public ReverseIterator(List<T> source)
		{
			this._source = source;
			this._position = this._source.size()- 1;
		}

		private List<T> _source;

		@Override
		public boolean hasNext() {
			return _position >= 0;
		}

		private int _position;

		@Override
		public T next() {
			T rs = this._source.get(this._position);
			this._position --;
			return rs;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}
	}

	/**
	 * Returns a specified number of contiguous elements from the start of a sequence.
	 * @param count The number of elements to return.
	 * @return A {@code Query<T>} that contains the specified number of elements from the start of the input sequence. 
	 */
	public Query<T> take(int count)
	{
		return new Query<T>(new TakeIterable(this._source, count));
	}

	private class TakeIterable implements Iterable<T>
	{
		public TakeIterable(Iterable<T> source, int take)
		{
			this._source = source;
			this._take = take;
		}

		private Iterable<T> _source;
		int _take;


		@Override
		public Iterator<T> iterator() {
			return new TakeIterator(this._source, this._take);
		}

	}

	private class TakeIterator implements Iterator<T>
	{

		public TakeIterator(Iterable<T> source, int take)
		{
			this._source = source.iterator();
			this._take = take;
		}

		private Iterator<T> _source;
		int _take;
		int _position = 0;

		@Override
		public boolean hasNext() {
			return _position < this._take && _source.hasNext();
		}

		@Override
		public T next() {
			if(this._position < this._take)
			{
				T rs = _source.next();
				this._position ++;
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

	/**
	 * Determines whether a sequence contains any elements.
	 * @return true if source sequence contains any elements; otherwise, false.
	 */
	public boolean any()
	{
		return this._source.iterator().hasNext();
	}

	/**
	 * Determines whether any element of a sequence satisfies a condition.
	 * @param predicate A {@code Predicate<T>} to test each element for a condition.
	 * @return true if any elements in the source sequence pass the test in the specified predicate; otherwise, false. 

	 */
	public boolean any(Predicate<T> predicate)
	{
		return new WhereIterable(this._source, predicate).iterator().hasNext();
	}

	/**
	 * Produces the set union of two sequences by using a specified {@code Comparator<T>}. 
	 * @param other An array whose distinct elements form the second set for union
	 * @param comparator The {@code Comparator<T>} to compare values
	 * @return A {@code Query<T>}that contains the elements from both input sequences, excluding duplicates. 

	 */
	public Query<T> union(T[] other, Comparator<T> comparator)
	{
		return this.union(new ArrayIterable<T>(other), comparator);
	}

	/**
	 * Produces the set union of two sequences by using a specified {@code Comparator<T>}. 
	 * @param other A sequence whose distinct elements form the second set for union
	 * @param comparator The {@code Comparator<T>} to compare values
	 * @return A {@code Query<T>}that contains the elements from both input sequences, excluding duplicates. 
	 */
	public Query<T> union(Iterable<T> other, Comparator<T> comparator)
	{
		final TreeSet<T> set = new TreeSet<T>(comparator);

		Query<T>  rs = null;
		try {
			rs = this.contact(other).where(new Predicate<T>(){

				@Override
				public boolean evaluate(T obj) throws Exception {
					if(set.contains(obj))
					{
						return false;
					}
					else
					{
						set.add(obj);
						return true;
					}
				}});
		} catch (Exception e) {

			e.printStackTrace();
		}

		return rs;
	}

	/** Produces the set union of two sequences. 
	 * @param other A sequence whose distinct elements form the second set for union
	 * @return A {@code Query<T>}that contains the elements from both input sequences, excluding duplicates. 
	 */
	public Query<T> union(Iterable<T> other)
	{
		final HashSet<T> set = new HashSet<T>();

		Query<T>  rs = null;
		try {
			rs = this.contact(other).where(new Predicate<T>(){

				@Override
				public boolean evaluate(T obj) throws Exception {
					if(set.contains(obj))
					{
						return false;
					}
					else
					{
						set.add(obj);
						return true;
					}
				}});
		} catch (Exception e) {

			e.printStackTrace();
		}

		return rs;

	}

	/** Produces the set union of two sequences. 
	 * @param array An array whose distinct elements form the second set for union
	 * @return A {@code Query<T>}that contains the elements from both input sequences, excluding duplicates. 
	 */
	public Query<T> union(T[] array)
	{
		return this.union(new ArrayIterable<T>(array));
	}


	private class ContactIterable implements Iterable<T>
	{
		public ContactIterable(Iterable<T> s1, Iterable<T> s2)
		{
			this._s1 = s1;
			this._s2 = s2;
		}

		private Iterable<T> _s1;
		private Iterable<T> _s2;
		@Override
		public Iterator<T> iterator() {
			return new ContactIterator(this._s1.iterator(), this._s2.iterator());
		}
	}

	private class ContactIterator implements Iterator<T>
	{

		public ContactIterator(Iterator<T> s1, Iterator<T> s2)
		{
			this._s1 = s1;
			this._s2 = s2;
		}

		private Iterator<T> _s1;
		private Iterator<T> _s2;
		private int _state = 0;



		@Override
		public boolean hasNext() {

			if(_state == 0)
			{
				boolean rs = _s1.hasNext();
				if(rs == false)
				{
					_state = 1;
					rs = _s2.hasNext();
				}
				return rs;
			}
			else
			{
				return _s2.hasNext();
			}


		}

		@Override
		public T next() {

			T rs = null;

			if(_state == 0)
			{
				if(_s1.hasNext())
				{
					rs = _s1.next();
				}
				else
				{
					_state =1;
				}
			}
			if(_state == 1)
			{
				rs = _s2.next();
			}

			return rs;

		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}

	}

	/**
	 * Concatenates two sequences.
	 * @param other The sequence to concatenate to current {@code Query<T>}.
	 * @return A {@code Query<T>} that contains the concatenated elements of the two input sequences. 
	 */
	public Query<T> contact(Iterable<T> other)
	{
		return new Query<T>(new ContactIterable(this._source, other) );
	}

	/**
	 * Concatenates two sequences.
	 * @param other The sequence to concatenate to current {@code Query<T>}.
	 * @return A {@code Query<T>} that contains the concatenated elements of the two input sequences. 
	 */
	public Query<T> contact(T[] other)
	{
		return this.contact(new ArrayIterable<T>(other));
	}



	/**
	 * Determines whether the sequence contains a specified element by using the default equality comparer.
	 * @param obj  The value to locate in the sequence
	 * @return true if the sequence contains the specified element; otherwise, false
	 */
	public boolean contains(T obj)
	{
		for(T item : this._source)
		{
			if(Objects.equals(item, obj))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines whether the sequence contains a specified element by using the specified equality comparer.
	 * @param obj The value to locate in the sequence
	 * @param comparator The {@code Comparator<T>} to compare values
	 * @return true if the sequence contains the specified element; otherwise, false
	 */
	public boolean contains(T obj, Comparator<T> comparator)
	{
		for(T item : this._source)
		{
			if(comparator.compare(obj, item) == 0)
			{
				return true;
			}
		}
		return false;
	}


	/**
	 * Returns the element at a specified index in a sequence.
	 * @param index The zero-based index of the element to retrieve.
	 * @return The element at the specified position in the source sequence.
	 * @throws Exception
	 */
	public T elementAt(int index) throws Exception
	{
		if(this._source instanceof List<?>)
		{
			return ((List<T>)this._source).get(index);
		}
		else if(this._source instanceof ArrayIterable<?>)
		{
			return ((ArrayIterable<T>)this._source).getSource()[index];
		}
		else
		{
			int i = 0;
			for(T item : this._source)
			{
				if(i == index)
				{
					return item;
				}
				i ++;
			}

			throw new IndexOutOfBoundsException("out of index");
		}
	}

	/**
	 * Returns the element at a specified index in a sequence or <code>null</code> if the index is out of range.
	 * @param index The zero-based index of the element to retrieve.
	 * @return <code>null</code> if the index is outside the bounds of the source sequence; otherwise, the element at the specified position in the source sequence. 
	 */
	public T elementAtOrDefault(int index)
	{
		if(this._source instanceof List<?>)
		{
			List<T> l = ((List<T>)this._source);
			return (l.size() > index && index >= 0) ? l.get(index) : null;
		}
		else if(this._source instanceof ArrayIterable<?>)
		{
			T[] a = ((ArrayIterable<T>)this._source).getSource();
			return (a.length > index && index >= 0) ? a[index] : null;
		}
		else
		{
			int i = 0;
			for(T item : this._source)
			{
				if(i == index)
				{
					return item;
				}
				i ++;
			}

			return null;
		}
	}

	/**
	 * Produces the set difference of two sequences by using the default equality comparer to compare values.
	 * @param other An {@code java.util.Iterable<T>} whose elements that also occur in the source sequence will cause those elements to be removed from the returned sequence. 
	 * @return A sequence that contains the set difference of the elements of two sequences.
	 * @throws Exception
	 */
	public Query<T> except(Iterable<T> other) throws Exception
	{
		final HashSet<T> excludes = new HashSet<T>();
		for(T ele : other)
		{
			excludes.add(ele);
		}

		return this.where(new Predicate<T>(){

			@Override
			public boolean evaluate(T obj) throws Exception {
				return ! excludes.contains(obj);
			}});

	}

	/**
	 * Produces the set difference of two sequences by using the default equality comparer to compare values.
	 * @param other An array whose elements that also occur in the source sequence will cause those elements to be removed from the returned sequence. 
	 * @return A sequence that contains the set difference of the elements of two sequences.
	 * @throws Exception
	 */
	public Query<T> except(T[] other) throws Exception
	{
		return this.except(new ArrayIterable<T>(other));
	}

	/**
	 * Produces the set difference of two sequences by using the specified comparator to compare values.
	 * @param other An <Iterable> whose elements that also occur in the source sequence will cause those elements to be removed from the returned sequence.
	 * @param comparator The {@code Comparator<T>} to compare values. 
	 * @return A sequence that contains the set difference of the elements of two sequences.
	 * @throws Exception
	 */
	public Query<T> except(Iterable<T> other, Comparator<T> comparator) throws Exception
	{
		final TreeSet<T> excludes = new TreeSet<T>(comparator);
		for(T ele : other)
		{
			excludes.add(ele);
		}

		return this.where(new Predicate<T>(){

			@Override
			public boolean evaluate(T obj) throws Exception {
				return ! excludes.contains(obj);
			}});
	}


	/**
	 * Produces the set difference of two sequences by using the specified comparator to compare values.
	 * @param other An <Array> whose elements that also occur in the source sequence will cause those elements to be removed from the returned sequence.
	 * @param comparator The {@code Comparator<T>} to compare values. 
	 * @return A sequence that contains the set difference of the elements of two sequences.
	 * @throws Exception
	 */
	public Query<T> except(T[] other, Comparator<T> comparator) throws Exception
	{
		return this.except(new ArrayIterable<T>(other), comparator);
	}


	


	/**
	 * Produces the set intersection of two sequences by using the default equality comparer to compare values.
	 * @param other An {@code java.util.Iterable<T>} whose distinct elements that also appear in the first sequence will be returned. 
	 * @return A sequence that contains the elements that form the set intersection of two sequences.

	 * @throws Exception
	 */
	public Query<T> intersect(Iterable<T> other) throws Exception
	{
		final HashSet<T> includes = new HashSet<T>();
		for(T ele : other)
		{
			includes.add(ele);
		}

		return this.where(new Predicate<T>(){

			@Override
			public boolean evaluate(T obj) throws Exception {
				return includes.contains(obj);
			}});
	}

	/**
	 * Produces the set intersection of two sequences by using the default equality comparer to compare values.
	 * @param other An array whose distinct elements that also appear in the first sequence will be returned. 
	 * @return A sequence that contains the elements that form the set intersection of two sequences.
	 * @throws Exception
	 */
	public Query<T> intersect(T[] other) throws Exception
	{
		return this.intersect(new ArrayIterable<T>(other));
	}

	/**
	 * Produces the set intersection of two sequences by using the specified comparator to compare values.
	 * @param other An {@code java.util.Iterable<T>} whose distinct elements that also appear in the first sequence will be returned.
	 * @param comparator The {@code Comparator<T>} to compare values. 
	 * @return A sequence that contains the elements that form the set intersection of two sequences.
	 * @throws Exception
	 */
	public Query<T> intersect(Iterable<T> other, Comparator<T> comparator) throws Exception
	{
		final TreeSet<T> includes = new TreeSet<T>(comparator);
		for(T ele : other)
		{
			includes.add(ele);
		}

		return this.where(new Predicate<T>(){

			@Override
			public boolean evaluate(T obj) throws Exception {
				return includes.contains(obj);
			}});
	}


	/**
	 * Produces the set intersection of two sequences by using the specified comparator to compare values.
	 * @param other An array whose distinct elements that also appear in the first sequence will be returned.
	 * @param comparator The {@code Comparator<T>} to compare values. 
	 * @return A sequence that contains the elements that form the set intersection of two sequences.
	 * @throws Exception
	 */
	public Query<T> intersect(T[] other, Comparator<T> comparator) throws Exception
	{
		return this.intersect(new ArrayIterable<T>(other), comparator);
	}

	/**
	 * Correlates the elements of two sequences based on matching keys by using the default equality comparer.
	 * @param inner The sequence to join to the source sequence. 
	 * @param outerKeySelector  A {@code Selector<T, TResult>} to extract the join key from each element of the source sequence.
	 * @param innerKeySelector A {@code Selector<T, TResult>} to extract the join key from each element of the source sequence.
	 * @param joint A {@code Joint<T1, T2, TResult>} to create result element from tow matching elements
	 * @return A {@code Query<T>} that has elements of type TResult that are obtained by performing an inner join on two sequences. 
	 */
	public <TInner, TKey, TResult> Query<TResult> join(TInner[] inner, Selector<T, TKey> outerKeySelector, Selector<TInner, TKey> innerKeySelector, Joint<T, TInner, TResult> joint)
	{
		return this.join(new ArrayIterable<TInner>(inner), outerKeySelector, innerKeySelector, joint, null);
	}

	/**
	 * Correlates the elements of two sequences based on matching keys by using the default equality comparer.
	 * @param inner The sequence to join to the source sequence. 
	 * @param outerKeySelector  A {@code Selector<T, TResult>} to extract the join key from each element of the source sequence.
	 * @param innerKeySelector A {@code Selector<T, TResult>} to extract the join key from each element of the source sequence.
	 * @param joint A {@code Joint<T1, T2, TResult>} to create result element from tow matching elements
	 * @return A {@code Query<T>} that has elements of type TResult that are obtained by performing an inner join on two sequences. 
	 */
	public <TInner, TKey, TResult> Query<TResult> join(Iterable<TInner> inner, Selector<T, TKey> outerKeySelector, Selector<TInner, TKey> innerKeySelector, Joint<T, TInner, TResult> joint)
	{
		return this.join(inner, outerKeySelector, innerKeySelector, joint, null);
	}


	/**
	 * Correlates the elements of two sequences based on matching keys by using the specified comparer.
	 * @param inner The sequence to join to the source sequence. 
	 * @param outerKeySelector  A {@code Selector<T, TResult>} to extract the join key from each element of the source sequence.
	 * @param innerKeySelector A {@code Selector<T, TResult>} to extract the join key from each element of the source sequence.
	 * @param joint A {@code Joint<T1, T2, TResult>} to create result element from tow matching elements
	 * @param comparator The {@code Comparator<T>} to compare values.
	 * @return A {@code Query<T>} that has elements of type TResult that are obtained by performing an inner join on two sequences. 
	 * 
	 */
	public <TInner, TKey, TResult> Query<TResult> join(TInner[] inner, Selector<T, TKey> outerKeySelector, Selector<TInner, TKey> innerKeySelector, Joint<T, TInner, TResult> joint, Comparator<TKey> comparator)
	{
		return new Query<TResult>(new JoinIterable<TInner, TKey, TResult>(this._source, new ArrayIterable<TInner>(inner), outerKeySelector, innerKeySelector, joint, comparator));
	}

	/**
	 * Correlates the elements of two sequences based on matching keys by using the specified comparer.
	 * @param inner The sequence to join to the source sequence. 
	 * @param outerKeySelector  A {@code Selector<T, TResult>} to extract the join key from each element of the source sequence.
	 * @param innerKeySelector A {@code Selector<T, TResult>} to extract the join key from each element of the source sequence.
	 * @param joint A {@code Joint<T1, T2, TResult>} to create result element from tow matching elements
	 * @param comparator The {@code Comparator<T>} to compare values.
	 * @return A {@code Query<T>} that has elements of type TResult that are obtained by performing an inner join on two sequences. 
	 * 
	 */
	public <TInner, TKey, TResult> Query<TResult> join(Iterable<TInner> inner, Selector<T, TKey> outerKeySelector, Selector<TInner, TKey> innerKeySelector, Joint<T, TInner, TResult> joint, Comparator<TKey> comparator)
	{
		return new Query<TResult>(new JoinIterable<TInner, TKey, TResult>(this._source, inner, outerKeySelector, innerKeySelector, joint, comparator));
	}

	private class JoinIterable<TInner, TKey, TResult> implements Iterable<TResult>
	{
		public JoinIterable(Iterable<T> outer, Iterable<TInner> inner, Selector<T, TKey> outerKeySelector, Selector<TInner, TKey> innerKeySelector, Joint<T, TInner, TResult> joint, Comparator<TKey> comparator)
		{
			this._outer = outer;
			this._inner = inner;
			this._innerKeySelector = innerKeySelector;
			this._outerKeySelector = outerKeySelector;
			this._joint = joint;
			this._comparator = comparator;
		}

		private Iterable<T> _outer;
		private Iterable<TInner> _inner;
		private Selector<T, TKey> _outerKeySelector;
		private Selector<TInner, TKey> _innerKeySelector;
		private Joint<T, TInner, TResult> _joint;
		private Comparator<TKey> _comparator;

		@Override
		public Iterator<TResult> iterator() {
			return new JoinIterator<TInner, TKey, TResult>(this._outer, this._inner, this._outerKeySelector, this._innerKeySelector, this._joint, this._comparator);
		}

	}


	private class JoinIterator<TInner, TKey, TResult> implements Iterator<TResult>
	{
		public JoinIterator(Iterable<T> outer, Iterable<TInner> inner, Selector<T, TKey> outerKeySelector, Selector<TInner, TKey> innerKeySelector, Joint<T, TInner, TResult> joint, Comparator<TKey> comparator)
		{
			this._outer = outer.iterator();
			this._inner = inner;
			this._innerKeySelector = innerKeySelector;
			this._outerKeySelector = outerKeySelector;
			this._joint = joint;
			this._comparator = comparator;
		}

		private Iterator<T> _outer;
		private Iterable<TInner> _inner;
		private Selector<T, TKey> _outerKeySelector;
		private Selector<TInner, TKey> _innerKeySelector;
		private Joint<T, TInner, TResult> _joint;
		private Comparator<TKey> _comparator;


		private Map<TKey, IGrouping<TKey, TInner>> _innerGroups;
		private Iterator<TInner> _currentInner = null;
		private T _currentOut = null;

		private TResult _current;
		private int _state = 0;

		private void tryFindNext()
		{
			if(_state == 0)
			{
				this._innerGroups = createMap(this._comparator);
				for(IGrouping<TKey, TInner> group : new Query<TInner>(this._inner).groupBy(this._innerKeySelector, this._comparator))
				{
					this._innerGroups.put(group.getKey(), group);
				}
				this._state = 1;
			}

			while(_current == null && this._state == 1)
			{
				if(this._currentOut == null )
				{
					if(this._outer.hasNext())
					{
						this._currentOut = this._outer.next();
						this._currentInner = null;
					}
					else
					{
						this._state = 2;
					}

				}
				else if(this._currentInner == null )
				{
					IGrouping<TKey, TInner> g = this._innerGroups.get(this._outerKeySelector.select(this._currentOut));
					if(g != null)
					{
						this._currentInner = g.iterator();
					}
					else
					{
						this._currentOut = null;
						this._currentInner = null;
					}
				}
				else if(!this._currentInner.hasNext())
				{
					this._currentOut = null;
					this._currentInner = null;
				}
				else
				{
					TInner inner = this._currentInner.next();
					try {
						this._current = this._joint.join(this._currentOut, inner);
					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
			}


		}

		@Override
		public boolean hasNext() {
			this.tryFindNext();
			return this._state < 2;
		}
		@Override
		public TResult next() {
			if(this.hasNext())
			{
				TResult rs = this._current;
				this._current = null;

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


	/**
	 * Correlates the elements of two sequences based on equality of keys and groups the results. The specified comparator is used to compare keys.

	 * @param inner The sequence to join to the source sequence.
	 * @param outerKeySelector A {@code Selector<T, TResult>} to extract the join key from each element of the source sequence.
	 * @param innerKeySelector A {@code Selector<T, TResult>} to extract the join key from each element of the second sequence.
	 * @param joint A {@code Joint<T1, T2, TResult>} to create a result element from an element from the first sequence and a collection of matching elements from the second sequence.
	 * @param comparator An {@code Comparator<T>}to hash and compare keys. 
	 * @return A {@code Query<T>}that contains elements of type TResult that are obtained by performing a grouped join on two sequences. 
	 */
	public <TInner, TKey, TResult> Query<IGrouping<TKey, TResult>> groupJoin(Iterable<TInner> inner, Selector<T, TKey> outerKeySelector, Selector<TInner, TKey> innerKeySelector, Joint<T, TInner, TResult> joint, Comparator<TKey> comparator)
	{
	

		return new Query<IGrouping<TKey, TResult>>(new GroupJoinIterable<TInner, TKey, TResult>(this._source, inner, outerKeySelector, innerKeySelector, joint, comparator));
	}
	
	/**
	 * Correlates the elements of two sequences based on equality of keys and groups the results. The specified comparator is used to compare keys.

	 * @param inner The sequence to join to the source sequence.
	 * @param outerKeySelector A {@code Selector<T, TResult>} to extract the join key from each element of the source sequence.
	 * @param innerKeySelector A {@code Selector<T, TResult>} to extract the join key from each element of the second sequence.
	 * @param joint A {@code Joint<T1, T2, TResult>} to create a result element from an element from the first sequence and a collection of matching elements from the second sequence.
	 * @param comparator An {@code Comparator<T>}to hash and compare keys. 
	 * @return A {@code Query<T>}that contains elements of type TResult that are obtained by performing a grouped join on two sequences. 
	 */
	public <TInner, TKey, TResult> Query<IGrouping<TKey, TResult>> groupJoin(TInner[] inner, Selector<T, TKey> outerKeySelector, Selector<TInner, TKey> innerKeySelector, Joint<T, TInner, TResult> joint, Comparator<TKey> comparator)
	{
		return new Query<IGrouping<TKey, TResult>>(new GroupJoinIterable<TInner, TKey, TResult>(this._source, new ArrayIterable<TInner>(inner), outerKeySelector, innerKeySelector, joint, comparator));
	}
	
	/**
	 * Correlates the elements of two sequences based on equality of keys and groups the results. The default comparator is used to compare keys.

	 * @param inner The sequence to join to the source sequence.
	 * @param outerKeySelector A {@code Selector<T, TResult>} to extract the join key from each element of the source sequence.
	 * @param innerKeySelector A {@code Selector<T, TResult>} to extract the join key from each element of the second sequence.
	 * @param joint A {@code Joint<T1, T2, TResult>} to create a result element from an element from the first sequence and a collection of matching elements from the second sequence.
	 * @return A {@code Query<T>}that contains elements of type TResult that are obtained by performing a grouped join on two sequences. 
	 */
	public <TInner, TKey, TResult> Query<IGrouping<TKey, TResult>> groupJoin(Iterable<TInner> inner, Selector<T, TKey> outerKeySelector, Selector<TInner, TKey> innerKeySelector, Joint<T, TInner, TResult> joint)
	{
	

		return new Query<IGrouping<TKey, TResult>>(new GroupJoinIterable<TInner, TKey, TResult>(this._source, inner, outerKeySelector, innerKeySelector, joint, null));
	}
	
	/**
	 * Correlates the elements of two sequences based on equality of keys and groups the results. The default comparator is used to compare keys.

	 * @param inner The sequence to join to the source sequence.
	 * @param outerKeySelector A {@code Selector<T, TResult>} to extract the join key from each element of the source sequence.
	 * @param innerKeySelector A {@code Selector<T, TResult>} to extract the join key from each element of the second sequence.
	 * @param joint A {@code Joint<T1, T2, TResult>} to create a result element from an element from the first sequence and a collection of matching elements from the second sequence.
	 * @return A {@code Query<T>}that contains elements of type TResult that are obtained by performing a grouped join on two sequences. 
	 */
	public <TInner, TKey, TResult> Query<IGrouping<TKey, TResult>> groupJoin(TInner[] inner, Selector<T, TKey> outerKeySelector, Selector<TInner, TKey> innerKeySelector, Joint<T, TInner, TResult> joint)
	{
		return new Query<IGrouping<TKey, TResult>>(new GroupJoinIterable<TInner, TKey, TResult>(this._source, new ArrayIterable<TInner>(inner), outerKeySelector, innerKeySelector, joint, null));
	}

	private class GroupJoinIterable<TInner, TKey, TResult> implements Iterable<IGrouping<TKey, TResult>>
	{
		public GroupJoinIterable(Iterable<T> outer, Iterable<TInner> inner, Selector<T, TKey> outerKeySelector, Selector<TInner, TKey> innerKeySelector, Joint<T, TInner, TResult> joint, Comparator<TKey> comparator)
		{
			this._outer = outer;
			this._inner = inner;
			this._innerKeySelector = innerKeySelector;
			this._outerKeySelector = outerKeySelector;
			this._joint = joint;
			this._comparator = comparator;
		}

		private Iterable<T> _outer;
		private Iterable<TInner> _inner;
		private Selector<T, TKey> _outerKeySelector;
		private Selector<TInner, TKey> _innerKeySelector;
		private Joint<T, TInner, TResult> _joint;
		private Comparator<TKey> _comparator;

		@Override
		public Iterator<IGrouping<TKey, TResult>> iterator() {
			return new GroupJoinIterator<TInner, TKey, TResult>(this._outer, this._inner, this._outerKeySelector, this._innerKeySelector, this._joint, this._comparator);
		}

	}

	private class GroupJoinIterator<TInner, TKey, TResult> implements Iterator<IGrouping<TKey, TResult>>
	{
		public GroupJoinIterator(Iterable<T> outer, Iterable<TInner> inner, Selector<T, TKey> outerKeySelector, Selector<TInner, TKey> innerKeySelector, Joint<T, TInner, TResult> joint, Comparator<TKey> comparator)
		{
			this._outer = outer;
			this._inner = inner;
			this._innerKeySelector = innerKeySelector;
			this._outerKeySelector = outerKeySelector;
			this._joint = joint;
			this._comparator = comparator;
		}

		private Iterable<T> _outer;
		private Iterable<TInner> _inner;
		private Selector<T, TKey> _outerKeySelector;
		private Selector<TInner, TKey> _innerKeySelector;
		private Joint<T, TInner, TResult> _joint;
		private Comparator<TKey> _comparator;

		private Iterator<IGrouping<TKey, T>> _outGroups;
		private Map<TKey, IGrouping<TKey, TInner>> _innerGroups;
		private IGrouping<TKey, TResult> _current = null;

		private int _state = 0;

		private void tryFindNext()
		{
			if(_state == 0)
			{
				this._outGroups = new Query<T>(this._outer).groupBy(this._outerKeySelector, this._comparator).iterator();

				this._innerGroups =createMap(this._comparator);
				for(IGrouping<TKey, TInner> group : new Query<TInner>(this._inner).groupBy(this._innerKeySelector, this._comparator))
				{
					this._innerGroups.put(group.getKey(), group);
				}
				this._state = 1;
			}

			while(this._state == 1 && _current == null)
			{
				if(this._outGroups.hasNext())
				{
					IGrouping<TKey, T> g1 = this._outGroups.next();
					IGrouping<TKey, TInner> g2 = this._innerGroups.get(g1.getKey());
					if(g2 != null)
					{
						this._current = new IterableGroup<TKey, TResult>(g1.getKey(), new CrossJoinIterable<TInner, TResult>(g1, g2, this._joint));
					}
				}
				else
				{
					this._state = 2;
				}
				
			}
		}

		@Override
		public boolean hasNext() {
			this.tryFindNext();
			return this._state < 2;
		}
		@Override
		public IGrouping<TKey, TResult> next() {
			if(this.hasNext())
			{
				IGrouping<TKey, TResult> rs = this._current;
				this._current = null;

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

	
	/**
	 * Cross join the elements of two sequences
	 * @param inner The sequence to join to
	 * @param joint A {@code Joint<T1, T2, TResult>} to create result element from tow matching elements 
	 * @return A {@code Query<T>} that has elements of type TResult that are obtained by performing a cross join on two sequences. 
	 */
	public <TInner, TResult> Query<TResult> crossJoin(TInner[] inner, Joint<T, TInner, TResult> joint)
	{
		return new Query<TResult>(new CrossJoinIterable<TInner, TResult>(this._source, new ArrayIterable<TInner>(inner), joint));
	}
	

	/**
	 * Cross join the elements of two sequences
	 * @param inner The sequence to join to
	 * @param joint A {@code Joint<T1, T2, TResult>} to create result element from tow matching elements 
	 * @return A {@code Query<T>} that has elements of type TResult that are obtained by performing a cross join on two sequences. 
	 */
	public <TInner, TResult> Query<TResult> crossJoin(Iterable<TInner> inner, Joint<T, TInner, TResult> joint)
	{
		return new Query<TResult>(new CrossJoinIterable<TInner, TResult>(this._source, inner, joint));
	}
	
	private class CrossJoinIterable<TInner, TResult> implements Iterable<TResult>
	{
		public CrossJoinIterable(Iterable<T> first, Iterable<TInner> second, Joint<T, TInner, TResult> joint)
		{
			this._first = first;
			this._second = second;
			this._joint = joint;
		}

		private Iterable<T> _first;
		private Iterable<TInner> _second;
		private Joint<T, TInner, TResult> _joint;
		@Override
		public Iterator<TResult> iterator() {
			return new CrossJoinIterator<TInner, TResult>(this._first, this._second, this._joint);
		}
	}

	private class CrossJoinIterator<TInner, TResult> implements Iterator<TResult>
	{
		public CrossJoinIterator(Iterable<T> first, Iterable<TInner> second, Joint<T, TInner, TResult> joint)
		{
			this._first = first.iterator();
			this._second = second;
			this._joint = joint;
		}

		private Iterator<T> _first;
		private Iterable<TInner> _second;
		private Iterator<TInner> _secondIterator;
		private Joint<T, TInner, TResult> _joint;
		private T _currentFirst;
		private TInner _currentSecond;
		int _state = 0;

		private void tryFindNext()
		{
			if(this._state == 0)
			{
				this._secondIterator = this._second.iterator();
				this._state = 1;
			}

			while((this._currentFirst == null || this._currentSecond == null) && _state == 1)
			{
				if(this._currentFirst == null)
				{
					if(this._first.hasNext())
					{
						this._currentFirst = this._first.next();
					}
					else
					{
						this._state = 2;
					}
				}
				else if(this._currentSecond == null)
				{
					if(this._secondIterator.hasNext())
					{
						this._currentSecond = this._secondIterator.next();
					}
					else
					{
						this._currentFirst = null;
						this._secondIterator = this._second.iterator();
					}
				}
			}
		}

		
		@Override
		public boolean hasNext() {
			this.tryFindNext();
			return this._state < 2;
		}
		@Override
		public TResult next() {
			if(this.hasNext())
			{
				TResult rs = null;
				try {
					rs = this._joint.join(this._currentFirst, this._currentSecond);

				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
				this._currentSecond = null;
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




	/**
	 * Bypasses a specified number of elements in a sequence and then returns the remaining elements.
	 * @param count The number of elements to skip before returning the remaining elements.
	 * @return A {@code Query<T>}that contains the elements that occur after the specified index in the input sequence. 
	 */
	public Query<T> skip(int count)
	{
		Iterable<T> rs;
		if(this.isRandomAccessable(this._source))
		{
			rs = new RandomSkipIterable(this.createRandomAccessor(this._source), count);
		}
		else
		{
			rs = new SkipIterable(this._source, count);
		}

		return new Query<T>(rs);
	}


	private class SkipIterable implements Iterable<T>
	{

		public SkipIterable(Iterable<T> accessor, int count)
		{
			this._accessor = accessor;
			this._count = count;
		}

		private Iterable<T> _accessor;
		private int _count;

		@Override
		public Iterator<T> iterator() {
			return new SkipIterator(this._accessor, this._count);
		}

	}

	private class SkipIterator implements Iterator<T>
	{

		public SkipIterator(Iterable<T> source, int count)
		{
			this._iterator = source.iterator();
			this._count = count;
		}

		private boolean _skipped = false;
		private Iterator<T> _iterator;
		private int _count;


		private void doSkip()
		{
			if(!this._skipped)
			{
				this._skipped = true;
				for(int i = 0; i < _count && this._iterator.hasNext(); i ++)
				{
					this._iterator.hasNext();
				}

			}
		}

		@Override
		public boolean hasNext() {
			doSkip();
			return this._iterator.hasNext();

		}

		@Override
		public T next() {
			doSkip();
			return this._iterator.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}

	}


	private class RandomSkipIterable implements Iterable<T>
	{

		public RandomSkipIterable(IRandomAccessor2<T> accessor, int count)
		{
			this._accessor = accessor;
			this._count = count;
		}

		private IRandomAccessor2<T> _accessor;
		private int _count;



		@Override
		public Iterator<T> iterator() {
			return new RandomSkipIterator(this._accessor, this._count);
		}

	}

	private class RandomSkipIterator implements Iterator<T>
	{
		public RandomSkipIterator(IRandomAccessor2<T> accessor, int count)
		{
			this._accessor = accessor;
			this._pos = count;
		}

		private IRandomAccessor2<T> _accessor;

		private int _pos;

		@Override
		public boolean hasNext() {
			return _pos < this._accessor.getSize();
		}

		@Override
		public T next() {

			if(hasNext())
			{
				T rs = this._accessor.get(this._pos);
				this._pos ++;
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

	/**
	 * Bypasses elements in a sequence as long as a specified condition is true and then returns the remaining elements.
	 * @param predicate The {@code Predicate<T>} to test each element for a condition
	 * @return A {@code Query<T>}that contains the elements from the source sequence starting at the first element in the linear series that does not pass the test specified by predicate. 
	 */
	public Query<T> skipWhile(Predicate<T> predicate)
	{
		return new Query<T>(new SkipWhileIterable(this._source, predicate)); 
	}

	private class SkipWhileIterable implements Iterable<T>
	{
		public SkipWhileIterable(Iterable<T> source, Predicate<T> predicate)
		{
			this._source = source;
			this._predicate = predicate;
		}

		private Iterable<T> _source;
		private Predicate<T> _predicate;

		@Override
		public Iterator<T> iterator() {
			return new SkipWhileIterator(this._source, this._predicate);
		}

	}

	private class SkipWhileIterator implements Iterator<T>
	{

		public SkipWhileIterator(Iterable<T> source, Predicate<T> predicate)
		{
			this._source = source.iterator();
			this._predicate = predicate;
		}

		private Iterator<T> _source;
		private Predicate<T> _predicate;


		private int _state = 0; //0 : unskipped; 1 : first : 2 : remains;
		private T _first;
		private void doSkip() throws Exception
		{
			if(_state == 0)
			{
				while(_source.hasNext())
				{
					T item = _source.next();
					if(this._predicate.evaluate(item))
					{
						this._state = 1;
						this._first = item;

						return;
					}
				}
				this._state = 2;
			}
		}
		@Override
		public boolean hasNext() {
			try {
				doSkip();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			return this._state == 1 || this._source.hasNext();
		}

		@Override
		public T next() {
			try {
				doSkip();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}

			if(this._state == 1)
			{
				this._state = 2;
				return this._first;

			}
			else
			{
				return this._source.next();
			}


		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}

	}

	/**
	 * Returns elements from a sequence as long as a specified condition is true.
	 * @param predicate A {@code Predicate<T>} to test each element for a condition.
	 * @return A {@code Query<T>}that contains the elements from the input sequence that occur before the element at which the test no longer passes. 
	 */
	public Query<T> takeWhile(Predicate<T> predicate)
	{
		return new Query<T>(new TakeWhileIterable(this._source, predicate));
	}

	private class TakeWhileIterable implements Iterable<T>
	{

		public TakeWhileIterable(Iterable<T> source, Predicate<T> predicate)
		{
			this._source = source;
			this._predicate = predicate;
		}

		private Iterable<T> _source;
		private Predicate<T> _predicate;

		@Override
		public Iterator<T> iterator() {
			return new TakeWhileIterator(this._source, this._predicate);
		}

	}

	private class TakeWhileIterator implements Iterator<T>
	{
		public TakeWhileIterator(Iterable<T> source, Predicate<T> predicate)
		{
			this._source = source.iterator();
			this._predicate = predicate;
		}

		private Iterator<T> _source;
		private Predicate<T> _predicate;
		private T _current = null;
		private boolean _hasNext = true;

		private void tryGetNext()
		{
			if(_hasNext)
			{

				if(_current == null)
				{
					if(this._source.hasNext())
					{
						T item = this._source.next();
						try {
							if(this._predicate.evaluate(item))
							{
								this._current = item;
							}
							else
							{
								this._hasNext = false;
							}
						} catch (Exception e) {
							throw new IllegalStateException(e);
						}
					}
					else
					{
						this._hasNext = false;
					}

				}
			}

		}

		@Override
		public boolean hasNext() {
			tryGetNext();
			return this._hasNext;
		}


		@Override
		public T next() {
			tryGetNext();
			T rs = this._current;
			this._current = null;
			return rs;

		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}
	}
	/**
	 * Merges two sequences by using the specified {@code Joint<T1, T2, TResult>}.
	 * @param other The second sequence to merge. 
	 * @param joint The {@code Joint<T1, T2, TResult>} that specifies how to merge the elements from the two sequences. 
	 * @return A {@code Query<T>}that contains merged elements of two input sequences. 

	 */
	public <TOther, TResult>  Query<TResult> zip(TOther[] other, Joint<T, TOther, TResult> joint)
	{
		return new Query<TResult>(new ZipIterable<TOther, TResult>(this._source, new ArrayIterable<TOther>(other), joint));
	}
	

	/**
	 * Merges two sequences by using the specified {@code Joint<T1, T2, TResult>}.
	 * @param other The second sequence to merge. 
	 * @param joint The {@code Joint<T1, T2, TResult>} that specifies how to merge the elements from the two sequences. 
	 * @return A {@code Query<T>}that contains merged elements of two input sequences. 

	 */
	public <TOther, TResult>  Query<TResult> zip(Iterable<TOther> other, Joint<T, TOther, TResult> joint)
	{
		return new Query<TResult>(new ZipIterable<TOther, TResult>(this._source, other, joint));
	}

	private class ZipIterable<TOther, TResult> implements Iterable<TResult>
	{
		public ZipIterable(Iterable<T> first, Iterable<TOther> second, Joint<T, TOther, TResult> joint)
		{
			this._first = first;
			this._second = second;
			this._joint = joint;
		}

		private Iterable<T> _first;
		private Iterable<TOther> _second;
		private Joint<T, TOther, TResult> _joint;

		@Override
		public Iterator<TResult> iterator() {
			return new ZipIterator<TOther, TResult>(this._first, this._second, this._joint);
		}

	}


	private class ZipIterator<TOther, TResult> implements Iterator<TResult>
	{
		public ZipIterator(Iterable<T> first, Iterable<TOther> second, Joint<T, TOther, TResult> joint)
		{
			this._first = first.iterator();
			this._second = second.iterator();
			this._joint = joint;
		}

		private Iterator<T> _first;
		private Iterator<TOther> _second;
		private Joint<T, TOther, TResult> _joint;
		@Override
		public boolean hasNext() {
			return _first.hasNext() && this._second.hasNext();
		}
		@Override
		public TResult next() {
			T f = this._first.next();
			TOther s = this._second.next();
			TResult rs;
			try {
				rs = this._joint.join(f, s);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			return rs;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}



	}
	
	/**
	 * Creates a {@code Map<TKey, TValue>}from an {@code Query<T>}according to a specified key {@code Selector} 
	 * @param selector The {@code Selector} to extract key
	 * @return A {@code Map<TKey, TValue>} that contains keys and values. 
	 * @throws Exception
	 */
	public <TKey> Map<TKey, T> toMap(Selector<T, TKey> selector) throws Exception
	{
		HashMap<TKey, T> rs = new HashMap<TKey, T>();
		for(T item : this._source)
		{
			TKey key = selector.select(item);
			if(!rs.containsKey(key))
			{
			    rs.put(key, item);
			}
			else
			{
				throw new DuplicateKeyException();
			}
		}
		
		return rs;
	}
	
	/**
	 * Creates a {@code Map<TKey, TValue>}from an {@code Query<T>}according to a specified key {@code Selector} and key {@code Comparator<T>} 
	 * @param selector The {@code Selector} to extract key.
	 * @param comparator The {@code Comparator<T>} to compare keys.
	 * @return A {@code Map<TKey, TValue>} that contains keys and values. 
	 * @throws Exception
	 */
	public <TKey> Map<TKey, T> toMap(Selector<T, TKey> selector, Comparator<TKey> comparator) throws Exception
	{
		Map<TKey, T> rs = createMap(comparator);
		for(T item : this._source)
		{
			TKey key = selector.select(item);
			if(!rs.containsKey(key))
			{
			    rs.put(key, item);
			}
			else
			{
				throw new DuplicateKeyException();
			}
		}
		
		return rs;
	}



}


