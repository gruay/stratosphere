/***********************************************************************************************************************
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/

package eu.stratosphere.types.parser;

import eu.stratosphere.types.IntValue;

/**
 * Parses a decimal text field into a IntValue.
 * Only characters '1' to '0' and '-' are allowed.
 */
public class DecimalTextIntParser extends FieldParser<IntValue> {
	
	private static final long OVERFLOW_BOUND = 0x7fffffffL;
	private static final long UNDERFLOW_BOUND = 0x80000000L;

	private IntValue result;
	
	@Override
	public int parseField(byte[] bytes, int startPos, int limit, char delimiter, IntValue reusable) {
		long val = 0;
		boolean neg = false;
		
		this.result = reusable;
		
		if (bytes[startPos] == '-') {
			neg = true;
			startPos++;
			
			// check for empty field with only the sign
			if (startPos == limit || bytes[startPos] == delimiter) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_ORPHAN_SIGN);
				return -1;
			}
		}
		
		for (int i = startPos; i < limit; i++) {
			if (bytes[i] == delimiter) {
				reusable.setValue((int) (neg ? -val : val));
				return i+1;
			}
			if (bytes[i] < 48 || bytes[i] > 57) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_ILLEGAL_CHARACTER);
				return -1;
			}
			val *= 10;
			val += bytes[i] - 48;
			
			if (val > OVERFLOW_BOUND && (!neg || val > UNDERFLOW_BOUND)) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_OVERFLOW_UNDERFLOW);
				return -1;
			}
		}
		
		reusable.setValue((int) (neg ? -val : val));
		return limit;
	}
	
	@Override
	public IntValue createValue() {
		return new IntValue();
	}

	@Override
	public IntValue getLastResult() {
		return this.result;
	}
	
	public static final int parseField(byte[] bytes, int startPos, int length, char delim) {
		long val = 0;
		boolean neg = false;
		
		
		if (bytes[startPos] == '-') {
			neg = true;
			startPos++;
			length--;
			// check for empty field with only the sign
			if (length == 0 || bytes[startPos] == delim) {
				throw new NumberFormatException("Orphaned minus sign.");
			}
		}
		
		for (; length > 0; startPos++, length--) {
			if (bytes[startPos] == delim) {
				return (int) (neg ? -val : val);
			}
			if (bytes[startPos] < 48 || bytes[startPos] > 57) {
				throw new NumberFormatException();
			}
			val *= 10;
			val += bytes[startPos] - 48;
			
			if (val > OVERFLOW_BOUND && (!neg || val > UNDERFLOW_BOUND)) {
				throw new NumberFormatException("Number format Overlfow/Underflow");
			}
		}
		return (int) (neg ? -val : val);
	}

	
}
