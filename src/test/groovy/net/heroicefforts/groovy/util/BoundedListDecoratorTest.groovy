/*
 * Copyright 2014 Heroic Efforts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.heroicefforts.groovy.util

import org.junit.Test
import static org.junit.Assert.assertEquals

class BoundedListDecoratorTest {

	@Test
	public void testFillsToCapacity() {
		def list = new BoundedListDecorator<Integer>(new LinkedList<Integer>(), 10)
		(1..10).each { list << it }
		assertEquals((1..10).collect { it }.toString(), list.toString())
	}
	
	@Test
	public void testMaintainsCapacityWithLeftShift() {
		def list = new BoundedListDecorator<Integer>(new LinkedList<Integer>(), 10)
		(1..15).each { list << it }
		assertEquals((6..15).collect { it }.toString(), list.toString())
	}

	@Test
	public void testMaintainsCapacityWithAdd() {
		def list = new BoundedListDecorator<Integer>(new LinkedList<Integer>(), 10)
		(1..15).each { list.add(it) }
		assertEquals((6..15).collect { it }.toString(), list.toString())
	}

	@Test
	public void testMaintainsCapacityWithAddIndex() {
		def list = new BoundedListDecorator<Integer>(new LinkedList<Integer>(), 10)
		(1..10).each { list.add(it) }
		(11..15).each { list.add(list.size() - 1, it) }
		assertEquals((6..15).collect { it }.toString(), list.toString())
	}

	@Test
	public void testMaintainsCapacityWithAddAll() {
		def list = new BoundedListDecorator<Integer>(new LinkedList<Integer>(), 10)
		(1..10).each { list << it }
		list.addAll((11..15).collect { it })
		assertEquals((6..15).collect { it }.toString(), list.toString())
	}

	@Test
	public void test10PoundsIn5PoundBag() {
		def list = new BoundedListDecorator<Integer>(new LinkedList<Integer>(), 5)
		(1..5).each { list << it }
		list.addAll((1..10).collect { it })
		assertEquals((5..10).collect { it }.toString(), list.toString())
	}

	@Test
	public void test10PoundsIn10PoundBag() {
		def list = new BoundedListDecorator<Integer>(new LinkedList<Integer>(), 10)
		list.addAll((1..10).collect { it })
		assertEquals((1..10).collect { it }.toString(), list.toString())
	}

	@Test
	public void testMaintainsCapacityWithAddAllIndex() {
		def list = new BoundedListDecorator<Integer>(new LinkedList<Integer>(), 10)
		(1..10).each { list << it }
		list.addAll(list.size() - 1, (11..15).collect { it })
		assertEquals((6..15).collect { it }.toString(), list.toString())
	}

}
