package org.jorgetown.bf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import net.jcip.annotations.Immutable;

/**
 * Implementation of an interpreter for the brainfuck programming language.
 * 
 * @author jcastro
 * @see <a href="http://en.wikipedia.org/wiki/Brainfuck">Brainfuck, Wikipedia< /a>
 * 
 */
@Immutable
public class Interpreter {
	private static final int DEFAULT_MEMORY_SIZE = 30000;
	private final int memorySize;
	private final byte[] memory;
	private int ptr = 0;
	private final byte[] input;
	private List<Byte> output = new ArrayList<Byte>();
	private int pc = 0;

	public Interpreter(final byte[] program) {
		this(DEFAULT_MEMORY_SIZE, program);
	}

	public Interpreter(final int memorySize, final byte[] program) {
		if (0 >= memorySize) {
			throw new IllegalArgumentException("Illegal memory size; '"
					+ memorySize + "' <= 0");
		}
		if (null == program) {
			throw new IllegalArgumentException("Program cannot be null");
		}
		this.memorySize = memorySize;
		this.memory = new byte[memorySize];
		this.input = program;
		System.out.printf("brainfuck interpreter setup: \nmemory size=%d\nprogram size=%dbytes\n\n",
				memorySize,
				input.length);
	}

	public void executeProgram() {
		byte b;
		while (!isEOF()) {
			b = input[pc];
			switch (b) {
			case '>':
				// increment the pointer (to point to the next cell to the right)
				if (++ptr >= memorySize) {
					throw new IllegalStateException("Address '" + ptr
							+ "' outside of memory region");
				}
				break;
			case '<':
				// decrement the pointer (to point to the next cell to the left)
				if (--ptr < 0) {
					throw new IllegalStateException("Address '" + ptr
							+ "' outside of memory region");
				}
				break;
			case '+':
				// increment (increase by one) the byte at the pointer
				b = memory[ptr];
				memory[ptr] = ++b;
				break;
			case '-':
				// decrement (decrease by one) the byte at the pointer
				b = memory[ptr];
				memory[ptr] = (b > 0 ? --b : 0); // negative values not allowed
				break;
			case '.':
				// output the value of the byte at the pointer
				output.add(memory[ptr]);
				break;
			case ',':
				// accept one byte of input, storing its value in the byte at the pointer
				memory[ptr] = input[pc];
				break;
			case '[':
				// jump forward to the command after the corresponding ] if the byte at the pointer is zero
				if (0 == memory[ptr]) {
					jumpForward();
				}
				break;
			case ']':
				// jump back to the command after the corresponding [ if the byte at the pointer is nonzero
				if (0 != memory[ptr]) {
					jumpBack();
				}
				break;
				// no-op
			default:
				break;
			}
			pc++;
		}

		System.out.print("\n>>  ");
		for (byte out : output) {
			System.out.print((char) out);
		}
	}

	// jump forward to the command after the corresponding ]
	private void jumpForward() {
		Stack<Character> s = new Stack<Character>();
		s.push('[');
		do {
			++pc;
			char c = (char) input[pc];
			if (c == '[') {
				s.push('[');
			}
			if (c == ']') {
				s.pop();
			}
		} while (!s.isEmpty());

	}

	// jump back to the command after the corresponding [
	private void jumpBack() {
		Stack<Character> s = new Stack<Character>();
		s.push(']');
		--pc;
		while (!s.isEmpty()) {
			char c = (char) input[pc];
			if (c == '[') {
				s.pop();
			}
			if (c == ']') {
				s.push(']');
			}
			--pc;
		}
		++pc;
	}

	public boolean isEOF() {
		return pc >= input.length;
	}

	public String memoryDump() {
		return Arrays.toString(memory);
	}

	public String outputDump() {
		return output.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// @see http://en.wikipedia.org/wiki/Brainfuck
		String program = "++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.";
		Interpreter bf = new Interpreter(program.getBytes());
		bf.executeProgram(); // program prints '>> Hello World!' to the console
	}
}
