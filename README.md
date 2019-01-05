Terms & Misc.
=============

A Note on this Documentation
----------------------------

When reading this documentation, keep in mind that unless otherwise specified,
arguments can be either literals or registers. If an argument must be one or the
other, that will be specified in the explanation.

For example:

“Adds A and B together and stores the result in register C”

>   Indicates that A and B can be either registers or literals, but C must be a
>   register because C was specified to be a register.

Arguments and delimiters
------------------------

In A1, the correct syntax for a command is the opcode followed by space
delimited arguments as appropriate for the opcode used.

For example:

\<OPCODE\> \<ARGUMENT1\> \<ARGUMENT2\>

Is the syntax for a 2-argument call.

\<OPCODE\> \<ARGUMENT1\>

Is the syntax for a single argument call.

Correct Case
------------

Correct A1 style is all uppercase.

Registers
---------

Normal registers are 0 indexed and referenced via the prefix R followed by the
suffix of their number.

For example:

R5

References register 5, or the 6th register.

R22

References the theoretical double-digit register 22 or the 23rd register.

Special registers, such as the program counter, are referenced by their
shorthand name.

For example:

PC

References the program counter register.

MH

References the main memory read/write head position.

Comments
--------

A comment is any line beginning with “\#”.

For example:

1.  \#This is a comment that will be ignored.

2.  \# Comments could also have a space after the first symbol

Number Space
------------

A1 uses a simplified integer space in the range [-9,999, 9,999]. Storing any value
above that will result in a halting error.

Labels
------

Labels in A1 can be any string without spaces that is ended by a “:”.

For example:

1.  START:

>   Is a label called “START”.

1.  MULTIPLE_WORD_LABEL:

>   Is an example of a multi-word label.

Main Memory
-----------

Main memory in A1 is a set of 10,000 memory addresses in the range [0, 9999].
Main memory is accessed using the LOAD and STORE opcodes which load or store
values based on the current value of the main memory read/write head position
register MH.

Opcodes
=======

MOV \<A\> \<B\>
---------------

Copies A to register B.

For example:

1.  MOV 1 R2

Moves the literal number 1 into register 2.

1.  MOV R1 R4

Moves the value from register 1 into register 4.

ADD \<A\> \<B\> \<C\>
---------------------

Adds A and B together and stores the result in register C.

For example:

1.  ADD 1 R1 R1

Increments register 1 by 1.

1.  ADD R1 R2 R3

Adds register 1 and register 2 together and stores the result in register 3.

SUB \<A\> \<B\> \<C\>
---------------------

Subtracts B from A and stores the result in register C.

For example:

1.  SUB 1 2 R0

Subtracts 2 from 1 and stores the resulting -1 in register 0.

1.  SUB R1 R2 R3

Subtracts register 2 from register 1 and stores the result in register 3.

BNE \<A\> \<B\> \<C\>
---------------------

Checks if A is not equal to B and branches to label C if that is the case.

For example:

1.  BNE 0 R1 START

>   Compares the literal 0 and register 1 and branches to the label START if
>   they’re not equal.

BEQ \<A\> \<B\> \<C\>
---------------------

Checks if A is equal to B and branches to label C if that is the case.

For example:

1.  BEQ R0 8 LOOP

>   Compares register 0 and the literal 8 and branches to the label LOOP if
>   they’re equal.

BGT \<A\> \<B\> \<C\>
---------------------

Checks if A is greater than B and branches to label C if that is the case.

For example:

1.  BGT R0 R1 NEXT_JUMP

>   Branches to the label NEXT_JUMP if register 0 is greater than register 1.

BLT \<A\> \<B\> \<C\>
---------------------

Checks if A is less than B and branches to label C if that is the case.

For example:

1.  BGT 500 R3 CHILDNODE

>   Branches to the label CHILDNODE if the literal 500 is greater than register
>   3.

BR \<A\>
--------

Branches without conditional checking to label A.

For example:

1.  BR START

>   Jumps to a label called START.

LOAD \<A\>
----------

Loads the value of the memory address currently in register MH and stores it
into register A.

For example:

1.  MOV 5 MH

2.  LOAD R1

>   Moves the main memory read/write head to position 5 or the 6th memory
>   address and loads its value into register 1.

STORE \<A\>
-----------

Stores A into the memory address currently in register MH.

For example:

1.  MOV 100 MH

2.  STORE R5

>   Moves the main memory read/write head to position 100 or the 1001st memory
>   address and stores the value of register 5 into it.
