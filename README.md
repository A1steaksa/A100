# A100 Reference Manual

## User Interface

A100's interface is split into 3 sections:
> 1. The code window
> 2. The debug information
> 3. The console

![](https://i.imgur.com/rjEqW4t.png)

The code window is where editing is done and code execution can be followed.

The debug information portion is where you can see the current state of all registers, main memory, and the string buffer.

The console is where error information is displayed and where print statements are shown.

## Terms & Misc.

### A Note on this Documentation

When reading this documentation, keep in mind that unless otherwise specified,
arguments can be literals or registers. If an argument must be specifically a register, a label name, or a literal, that will be specified in the explanation.

For example:
```
“Adds A and B together and stores the result in register C”
```
>   Indicates that A and B can be either registers or literals, but C must be a
>   register because C was specified to be a register.

### Arguments and delimiters

In A1, the correct syntax for a command is the opcode followed by space
delimited arguments as appropriate for the opcode used.

For example:
```
<OPCODE> <ARGUMENT1> <ARGUMENT2>
```
>   Is the syntax for a 2-argument call.
```

<OPCODE> <ARGUMENT1>
```
>   Is the syntax for a single argument call.

### Correct Case

Correct A1 style is all uppercase.

### Registers

Normal registers are 0 indexed and referenced via the prefix R followed by the
suffix of their number.

For example:
```
R5
```
>   References register 5, or the 6th register.

```
R22
```
>   References the theoretical double-digit register 22 or the 23rd register.

Special registers, such as the program counter, are referenced by their
shorthand name.

For example:
```
PC
```
>   References the program counter register.

```
MH
```
>   References the main memory read/write head position.

### Comments

A comment is any line beginning with “\#”.

For example:
```
0  #This is a comment that will be ignored.
1  # Comments could also have a space after the first symbol
```
### Number Space

A1 uses a simplified short space in the range [-32,768, 32,767]. Storing any value
above that will result in a halting error.

### Labels

Labels in A1 can be any string without spaces that is ended by a “:”.

For example:
```
0  START:
```
>   Is a label called “START”.

```
0  MULTIPLE_WORD_LABEL:
```
>   Is an example of a multi-word label.

### Main Memory

Main memory in A1 is a set of 10,000 memory addresses in the range [0, 9999].
Main memory is accessed using the LOAD and STORE opcodes which load or store
values based on the current value of the main memory read/write head position
register MH.

## Opcodes

### MOV \<A\> \<B\>

Copies A to register B.

For example:
```
0  MOV 1 R2
```
>   Moves the literal number 1 into register 2.

```
0  MOV R1 R4
```
>   Moves the value from register 1 into register 4.

### ADD \<A\> \<B\> \<C\>

Adds A and B together and stores the result in register C.

For example:
```
0  ADD 1 R1 R1
```
>   Increments register 1 by 1.

```
0  ADD R1 R2 R3
```
>   Adds register 1 and register 2 together and stores the result in register 3.

### SUB \<A\> \<B\> \<C\>

Subtracts B from A and stores the result in register C.

For example:
```
0  SUB 1 2 R0
```
>   Subtracts 2 from 1 and stores the resulting -1 in register 0.

```
0  SUB R1 R2 R3
```
>   Subtracts register 2 from register 1 and stores the result in register 3.

### BNE \<A\> \<B\> \<C\>

Checks if A is not equal to B and branches to label or line number literal C if that is the case.

For example:
```
0  BNE 0 R1 START
```
>   Compares the literal 0 and register 1 and branches to the label START if
>   they’re not equal.

### BEQ \<A\> \<B\> \<C\>

Checks if A is equal to B and branches to label or line number literal C if that is the case.

For example:
```
0  BEQ R0 8 5
```
>   Compares register 0 and the literal 8 and branches to the literal line number 5 if
>   they’re equal.

### BGT \<A\> \<B\> \<C\>

Checks if A is greater than B and branches to label or line number literal C if that is the case.

For example:
```
0  BGT R0 R1 NEXT_JUMP
```
>   Branches to the label NEXT_JUMP if register 0 is greater than register 1.

### BLT \<A\> \<B\> \<C\>

Checks if A is less than B and branches to label or line number literal C if that is the case.

For example:
```
0  BGT 500 R3 CHILDNODE
```
>   Branches to the label CHILDNODE if the literal 500 is greater than register.
>   3.

### BR \<A\>

Branches without conditional checking to label or line number literal A.

For example:
```
0  BR START
```
>   Jumps to a label called START.

### LOAD \<A\>

Loads the value of the memory address currently in register MH and stores it.
into register A.

For example:
```
0  MOV 5 MH
1  LOAD R1
```
>   Moves the main memory read/write head to position 5 or the 6th memory.
>   address and loads its value into register 1.

### STORE \<A\>

Stores A into the memory address currently in register MH.

For example:
```
0  MOV 100 MH
1  STORE R5
```
>   Moves the main memory read/write head to position 100 or the 101st memory.
>   address and stores the value of register 5 into it.

### APND \<A\>

Stores A as an ASCII char into the string buffer.

For example:
```
0  APND 65
```
>   Stores the character "A" into the string buffer.

### PRNT

Flushes the contents of the string buffer into console.

For example:
```
0  APND 104
1  APND 101
2  APND 108
3  APND 108
4  APND 111
5  PRNT
```
>   Stores "hello" into the string buffer and prints it to the console.

### DUMP

Flushes the contents of the string buffer to clear it.

For example:
```
0  DUMP
```
>   Clears anything that is currently in the string buffer.

### CLR

Flushes the contents of the console to clear it.

For example:
```
1  CLR
```
>   Clears anything that is currently in the string buffer.

### ASL

Performs an arithmetic shift left on A by B bits and stores the value in register C

For example:
```
1  ASL 2 1 R3
```
>   Shifts the literal 2 left by 1 bit and stores the resulting 4 in register 3

### ASR

Performs an arithmetic shift right on A by B bits and stores the value in register C

For example:
```
1  ASR R5 R2 R3
```
>   Shifts the value of register 5 right by the value of register 2 bits and stores the result in register 3
