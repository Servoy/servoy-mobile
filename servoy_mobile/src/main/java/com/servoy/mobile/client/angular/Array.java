package com.servoy.mobile.client.angular;

import java.util.stream.Stream;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.JsArrayLike;

/**
 * An interface to a Javascript array. The implementation may be different on the client
 * and server
 *
 * @author pstockley
 *
 * @param <T> The type of value this array holds
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Array")
public interface Array<T> extends JsArrayLike<T>
{
	/**
	 * Returns if an array is not empty or null
	 *
	 * @param a the array
	 * @return <code>true</code> if not empty
	 */
	@JsOverlay
	static boolean isNotEmpty(Array< ? > a)
	{

		return !isEmpty(a);
	}

	/**
	 * Returns if an array is empty or null
	 *
	 * @param a the array
	 * @return <code>true</code> if empty or null
	 */
	@JsOverlay
	static boolean isEmpty(Array< ? > a)
	{

		return null == a || a.getLength() == 0;
	}

	/**
	 * Return an array that is the concatenation of this array + the supplied array
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/concat">concat spec</a>
	 *
	 * @param a the array, or values to concatenate
	 * @return the new array
	 */
	Array<T> concat(Array<T> a);

	@JsMethod(name = "concat")
	Array<T> concatValue(T a);

	/**
	 * Convert each element of the array to a String and join them with a comma
	 * separator. The value returned from this method may vary between browsers
	 * based on how JavaScript values are converted into strings.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/join">join spec</a>
	 *
	 * @param separator The separator to place between each element
	 * @return The joined string
	 */
	String join(String separator);

	/**
	 * Pushes the given value onto the end of the array.
	 *
	 * @param value The value to add to the end of the array
	 */
	void push(T value);

	/**
	 * Pushes the given value onto the end of the array.
	 *
	 * @param value The value to add to the end of the array
	 */
	void push(T... value);

	/**
	 * Shifts the first value off the array.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/shift">shift spec</a>
	 *
	 * @return the shifted value
	 */
	T shift();

	/**
	 * Shifts a value onto the beginning of the array.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/unshift">unshift spec</a>
	 *
	 * @param value the value to the stored
	 */
	void unshift(T value);

	/**
	 * Return the reversed contents of the array
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/reverse">reverse spec</a>
	 *
	 * @return The reversed array
	 */
	Array<T> reverse();

	/**
	 * Return a slice of this array
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/slice">slice spec</a>
	 *
	 * @param startIndx Zero-based index at which to begin extraction
	 * @param endIndx   Zero-based index at which to end extraction. slice extracts up to but not including endIndx.
	 * @return The array slice
	 */
	Array<T> slice(int startIndx, int endIndx);

	/**
	 * Changes the content of an array by removing existing elements and/or adding new elements.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/splice">splice spec</a>
	 *
	 * @param startIndx   Index at which to start changing the array (with origin 0).
	 *                    If greater than the length of the array, actual starting index will be
	 *                    set to the length of the array. If negative, will begin that many elements
	 *                    from the end
	 * @param deleteCount An integer indicating the number of old array elements to remove.
	 *                    If deleteCount is 0, no elements are removed. In this case, you should
	 *                    specify at least one new element. If deleteCount is greater than the
	 *                    number of elements left in the array starting at start, then all of
	 *                    the elements through the end of the array will be deleted. If deleteCount
	 *                    is omitted, deleteCount will be equal to (arr.length - start).
	 * @return An array containing the deleted elements. If only one element is removed, an array
	 * of one element is returned. If no elements are removed, an empty array is returned.
	 */
	Array<T> splice(int startIndx, int deleteCount);

	/**
	 * Changes the content of an array by removing existing elements and/or adding new elements.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/splice">splice spec</a>
	 *
	 * @param startIndx Index at which to start changing the array (with origin 0).
	 *                  If greater than the length of the array, actual starting index will be
	 *                  set to the length of the array. If negative, will begin that many elements
	 *                  from the end
	 * @return An array containing the deleted elements. If only one element is removed, an array
	 * of one element is returned. If no elements are removed, an empty array is returned.
	 */
	Array<T> splice(int startIndx);

	/**
	 * Changes the content of an array by removing existing elements and/or adding new elements.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/splice">splice spec</a>
	 *
	 * @param startIndx   Index at which to start changing the array (with origin 0).
	 *                    If greater than the length of the array, actual starting index will be
	 *                    set to the length of the array. If negative, will begin that many elements
	 *                    from the end
	 * @param deleteCount An integer indicating the number of old array elements to remove.
	 *                    If deleteCount is 0, no elements are removed. In this case, you should
	 *                    specify at least one new element. If deleteCount is greater than the
	 *                    number of elements left in the array starting at start, then all of
	 *                    the elements through the end of the array will be deleted. If deleteCount
	 *                    is omitted, deleteCount will be equal to (arr.length - start).
	 * @param toAdd       The elements to add to the array, beginning at the start index
	 * @return An array containing the deleted elements. If only one element is removed, an array
	 * of one element is returned. If no elements are removed, an empty array is returned.
	 */
	Array<T> splice(int startIndx, int deleteCount, T... toAdd);

	/**
	 * Returns the first index at which a given element can be found in the array, or -1 if it is not present.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/indexOf">indexof spec</a>
	 *
	 * @param searchElement Element to locate in the array
	 * @param fromIndex     The index to start the search at. If the index is greater than or equal to
	 *                      the array's length, -1 is returned, which means the array will not be
	 *                      searched. If the provided index value is a negative number, it is taken
	 *                      as the offset from the end of the array. Note: if the provided index is
	 *                      negative, the array is still searched from front to back.
	 * @return If the calculated index is less than 0, then the whole array will be searched.
	 * Default: 0 (entire array is searched).
	 */
	int indexOf(T searchElement, int fromIndex);

	/**
	 * Returns the first index at which a given element can be found in the array, or -1 if it is not present.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/indexOf">indexof spec</a>
	 *
	 * @param searchElement Element to locate in the array
	 * @return The first index at which a given element can be found in the array, or -1 if it is not present.
	 */
	int indexOf(T searchElement);

	/**
	 * Returns the last index at which a given element can be found in the array, or -1 if it is
	 * not present. The array is searched backwards, starting at fromIndex.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/lastIndexOf">lastIndexof spec</a>
	 *
	 * @param searchElement Element to locate in the array
	 * @param fromIndex     The index at which to start searching backwards. Defaults to the
	 *                      array's length minus one, i.e. the whole array will be searched. If the
	 *                      index is greater than or equal to the length of the array, the whole array
	 *                      will be searched. If negative, it is taken as the offset from the end of
	 *                      the array. Note that even when the index is negative, the array is still
	 *                      searched from back to front.
	 * @return The last index at which a given element can be found in the array, or -1 if it is
	 * not present
	 */
	int lastIndexOf(T searchElement, int fromIndex);

	/**
	 * Returns the last index at which a given element can be found in the array, or -1 if it is
	 * not present. The array is searched backwards, starting at the end
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/lastIndexOf">lastIndexof spec</a>
	 *
	 * @param searchElement Element to locate in the array
	 * @return The last index at which a given element can be found in the array, or -1 if it is
	 * not present
	 */
	int lastIndexOf(T searchElement);

	/**
	 * Executes a provided function once per array element.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/forEach">forEach spec</a>
	 *
	 * @param fn One of ForEachFn or ForEachFullFn
	 */
	@JsMethod(name = "forEach")
	void forEachElem(ForEachFn<T> fn);

	@JsMethod(name = "forEach")
	void forEachElem(ForEachFullFn<T> fn);

	/**
	 * Creates a new array with all elements that pass the test implemented by the provided function
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/filter">filter spec</a>
	 *
	 * @param fn One of TestFn or TestFullFn
	 * @return The new filtered array
	 */
	Array<T> filter(TestFn<T> fn);

	Array<T> filter(TestFullFn<T> fn);

	/**
	 * Returns the value of the first element in the array that satisfies the provided testing function. Otherwise null is returned.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/find">find spec</a>
	 *
	 * @param fn One of TestFn or TestFullFn
	 * @return A value in the array if an element passes the test; otherwise, null.
	 */
	T find(TestFn<T> fn);

	T find(TestFullFn<T> fn);

	/**
	 * Creates a new array with the results of calling a provided function on every element in this array
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/map">map spec</a>
	 *
	 * @param fn One of MapFn or MapFullFn
	 * @param <T2> The type of output from the map function
	 *
	 * @return The new mapped array
	 */
	<T2 extends Object> Array<T2> map(MapFn<T, T2> fn);

	<T2 extends Object> Array<T2> map(MapFullFn<T, T2> fn);

	/**
	 * Applies a function against an accumulator and each value of the array (from left-to-right)
	 * to reduce it to a single value
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/reduce">reduce spec</a>
	 *
	 * @param fn           One of ReduceFn, ReduceDoubleFn or ReduceFullFn
	 * @param initialValue The initial accumulator value
	 * @param <A> The type of accumulator
	 * @return The reduced accumulator value
	 */
	<A extends Object> A reduce(ReduceFn<A, T> fn, A initialValue);

	<A extends Object> A reduce(ReduceFullFn<A, T> fn, A initialValue);

	/**
	 * Applies a function against an accumulator and each value of the array (from right-to-left)
	 * to reduce it to a single value
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/reduceRight">reduceRight spec</a>
	 *
	 * @param fn           One of ReduceFn, ReduceDoubleFn or ReduceFullFn
	 * @param initialValue The initial accumulator value
	 * @param <A> The type of accumulator
	 * @return The reduced accumulator value
	 */
	<A extends Object> A reduceRight(ReduceFn<A, T> fn, A initialValue);

	<A extends Object> A reduceRight(ReduceFullFn<A, T> fn, A initialValue);

	/**
	 * Tests whether some element in the array passes the run implemented by the provided function.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some">some spec</a>
	 *
	 * @param fn Function to run for each element, taking three arguments
	 * @return true if the run function returns true for one of the elements
	 */
	boolean some(TestFn<T> fn);

	boolean some(TestFullFn<T> fn);

	/**
	 * Tests whether all elements in the array pass the run implemented by the provided function.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/every">every spec</a>
	 *
	 * @param fn Function to run for each element, taking three arguments
	 * @return true if the run function returns true for all of the elements
	 */
	boolean every(TestFn<T> fn);

	boolean every(TestFullFn<T> fn);

	/**
	 * Sorts the elements of an array in place and returns the array. The sort is not necessarily stable.
	 * The default sort order is according to string Unicode code points.
	 * <p>
	 * See <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort">sort spec</a>
	 *
	 * @param fn Specifies a function that defines the sort order. If omitted, the array is
	 *           sorted according to each character's Unicode code point value, according
	 *           to the string conversion of each element.
	 * @return The sorted array
	 */
	Array<T> sort(SortFn<T> fn);

	Array<T> sort();

	/*********************************************************************************************
	 * Java Compatibility
	 *********************************************************************************************/

	/**
	 * Return an Iterable&lt;T&gt; for this Array. This enables Arrays to be the target of
	 * target of the "for-each loop" statement
	 *
	 * @return An Iterable
	 */
	@JsOverlay
	public default Iterable<T> asIterable()
	{
		return asList();
	}


	@JsOverlay
	public default Stream<T> stream()
	{
		return asList().stream();
	}

	/* Functional interfaces */

	@JsFunction
	public static interface TestFn<T>
	{
		boolean test(T currentValue);
	}

	@JsFunction
	public static interface TestFullFn<T>
	{
		boolean test(T currentValue, int index, Array<T> theArray);
	}

	@JsFunction
	public interface ForEachFn<T>
	{
		void exec(T currentValue);
	}

	@JsFunction
	public interface ForEachFullFn<T>
	{
		void exec(T currentValue, int index, Array<T> theArray);
	}

	@JsFunction
	public interface MapFn<T, T2>
	{
		T2 doMap(T currentValue);
	}

	@JsFunction
	public interface MapFullFn<T, T2>
	{
		T2 doMap(T currentValue, int index, Array<T> theArray);
	}

	@JsFunction
	public interface ReduceFn<A, T>
	{
		A doReduce(A PreviousAccumulator, T currentValue);
	}

	@JsFunction
	public interface ReduceDoubleFn<T>
	{
		double doReduce(double PreviousAccumulator, T currentValue);
	}

	@JsFunction
	public interface ReduceFullFn<A, T>
	{
		A doReduce(A PreviousAccumulator, T currentValue, int index, Array<T> theArray);
	}

	@JsFunction
	public interface SortFn<T>
	{
		int compare(T o1, T o2);
	}
}