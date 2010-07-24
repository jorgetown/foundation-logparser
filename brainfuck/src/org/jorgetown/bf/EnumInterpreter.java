package org.jorgetown.bf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.jcip.annotations.Immutable;

/**
 * Implementation of an interpreter for the brainfuck programming language, using {@link Enum}s as a kind of delegates.
 * 
 * @author jcastro
 * @see <a href="http://en.wikipedia.org/wiki/Brainfuck">Brainfuck, Wikipedia< /a>
 * 
 */
@Immutable
public class EnumInterpreter {
	private static final int DEFAULT_MEMORY_SIZE = 30000;
	private final int memorySize;
	private final byte[] memory;
	private int ptr = 0;
	private final byte[] input;
	private List<Byte> output = new ArrayList<Byte>();
	private int pc = 0;

	public EnumInterpreter(final byte[] program) {
		this(DEFAULT_MEMORY_SIZE, program);
	}

	public EnumInterpreter(final int memorySize, final byte[] program) {
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

	// increment the pointer (to point to the next cell to the right)
	private void incrementPtr() {
		if (++ptr >= memorySize) {
			throw new IllegalStateException("Address '" + ptr
					+ "' outside of memory region");
		}
	}

	// decrement the pointer (to point to the next cell to the left)
	private void decrementPtr() {
		if (--ptr < 0) {
			throw new IllegalStateException("Address '" + ptr
					+ "' outside of memory region");
		}
	}

	private byte derefence() {
		return memory[ptr];
	}

	private void store(final byte value) {
		memory[ptr] = value;
	}

	public String memoryDump() {
		return Arrays.toString(memory);
	}

	// increment the program counter; idempotent at EOF
	private void incrementPC() {
		if (!isEOF()) {
			++pc;
		}
	}

	// decrement the program counter; idempotent at 0
	private void decrementPC() {
		if (pc > 0) {
			--pc;
		}
	}

	private byte readByte() {
		return input[pc];
	}

	private void writeByte(final byte b) {
		output.add(b);
	}

	public String outputDump() {
		return output.toString();
	}

	// jump forward to the command after the corresponding ]
	private void jumpForward() {
		Stack<Character> s = new Stack<Character>();
		s.push('[');
		do {
			incrementPC();
			char c = (char) readByte();
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
		decrementPC();
		while (!s.isEmpty()) {
			char c = (char) readByte();
			if (c == '[') {
				s.pop();
			}
			if (c == ']') {
				s.push(']');
			}
			decrementPC();
		}
		incrementPC();
	}

	public boolean isEOF() {
		return pc >= input.length;
	}

	/**
	 * Using {@link Enum}s and constant-specific method implementations as a kind of function pointers.
	 * 
	 * @author jcastro
	 * 
	 */
	public enum Command {
		// increment the pointer (to point to the next cell to the right)
		INC_PTR('>') {
			public void exec(EnumInterpreter interpreter) {
				interpreter.incrementPtr();
			}
		},

		// decrement the pointer (to point to the next cell to the left)
		DEC_PTR('<') {
			public void exec(EnumInterpreter interpreter) {
				interpreter.decrementPtr();
			}
		},

		// increment (increase by one) the byte at the pointer
		INC('+') {
			public void exec(EnumInterpreter interpreter) {
				byte b = interpreter.derefence();
				b++;
				interpreter.store(b);
			}
		},

		// decrement (decrease by one) the byte at the pointer
		DEC('-') {
			public void exec(EnumInterpreter interpreter) {
				byte b = interpreter.derefence();
				interpreter.store(b > 0 ? --b : 0);
			}
		},

		// output the value of the byte at the pointer
		OUT('.') {
			public void exec(EnumInterpreter interpreter) {
				final byte b = interpreter.derefence();
				interpreter.writeByte(b);
			}
		},

		// accept one byte of input, storing its value in the byte at the pointer
		IN(',') {
			public void exec(EnumInterpreter interpreter) {
				final byte b = interpreter.readByte();
				interpreter.store(b);
			}
		},

		// jump forward to the command after the corresponding ] if the byte at the pointer is zero
		FORWARD('[') {
			public void exec(EnumInterpreter interpreter) {
				byte b = interpreter.derefence();
				if (0 == b) {
					interpreter.jumpForward();
				}
			}
		},

		// jump back to the command after the corresponding [ if the byte at the pointer is nonzero
		BACK(']') {
			public void exec(EnumInterpreter interpreter) {
				byte b = interpreter.derefence();
				if (0 != b) {
					interpreter.jumpBack();
				}
			}
		};

		public abstract void exec(EnumInterpreter interpret);

		private static final Map<Character, Command> lookup = new HashMap<Character, Command>();

		static {
			for (Command c : EnumSet.allOf(Command.class))
				lookup.put(c.getCommand(), c);
		}

		private final char command;

		private Command(char cmd) {
			this.command = cmd;
		}

		private char getCommand() {
			return this.command;
		}

		public static Command lookup(char cmd) {
			return lookup.get(cmd);
		}
	}

	public void executeProgram() {
		byte b;
		Command command;
		while (!isEOF()) {
			b = readByte();
			command = Command.lookup((char) b);
			if (command != null) {
				command.exec(this);
			}
			incrementPC();
		}

		System.out.print("\n>>  ");
		for (byte out : output) {
			System.out.print((char) out);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String program = "++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.";
		// String program =
		// "++++++++++        initialises cell zero to 10 [ >+++++++>++++++++++>+++>+<<<<- ]                 this loop sets the next four cells to 70/100/30/10";
		EnumInterpreter bf = new EnumInterpreter(program.getBytes());
		bf.executeProgram();
	}
}
