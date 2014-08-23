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

import java.util.List;

/**
 * Decorates a List wrapping all add and addAll methods to ensure that the size stays beneath the max capacity
 * by removing elements from the beginning of the list i.e. a basic bounded buffer.
 *  
 * @author jevans
 *
 * @param <E> list element type
 */
class BoundedListDecorator<E> {
	private List<E> delegate
	private int maxCapacity
	
	public BoundedListDecorator(List<E> delegate, int maxCapacity) {
		this.delegate = delegate
		this.maxCapacity = maxCapacity
	}
	
	private final int reserveCapacity(int reserve) {
		int dump = Math.min(reserve + size() - maxCapacity, maxCapacity)
		if(dump > 0)
			(1..dump).each { delegate.remove(0) }
		return dump
	}

	
		
	def invokeMethod(String name, args) {
		if(args) {
			if(name.startsWith('addAll')) {
				def value
				if(args.length == 1 && args[0] instanceof Collection) {
					final int csize = args[0].size()
					reserveCapacity(csize)
					if(csize > maxCapacity) {
						args[0] = args[0][csize - maxCapacity - 1..csize-1] 
					}
				}
				else if(args.length == 2 && args[1] instanceof Collection) {
					def dumped = reserveCapacity(args[1].size())
					args[0] = args[0] - dumped + 1
					final int csize = args[1].size()
					reserveCapacity(csize)
					if(csize > maxCapacity) {
						args[1] = args[1][csize - maxCapacity - 1..csize-1]
					}

				}
			}
			else if(name.startsWith('add') || name == 'leftShift') {
				reserveCapacity(1)
			}
		} 
		
		delegate.invokeMethod(name, args)
	}
	
	public String toString() {
		return delegate.toString()
	}
}
